package uk.ac.ebi.enfin.biomart.probes;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;
import uk.ac.ebi.enfin.biomart.BiomartAccess;
import uk.ac.ebi.enfin.biomart.probes.SpecieMapping;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * User: rafael
 * Date: 02-Jul-2010
 * Time: 12:15:31
 *
 * Class which queries ensembl biomart and creates files with mapping
 * information between microarray prob and uniprot
 */


public class MicroarrayProbes {
	static Logger logger = Logger.getLogger(MicroarrayProbes.class);
	private String probeAttributeType = "display_label_11056";
	private String folder = "";
	public static final int FETCH_FOR_UNIPROT=0;
	public static final int FETCH_FOR_ENSEMBL=1;

	public MicroarrayProbes() {
		System.out.println(" mhh ");
	}

	/**
	 * Create mapping files for all the ensembl species
	 */
	public void createFiles(int type){
		String specie = "all";
		String microarrayPlatform = "all";
		createFiles(specie, microarrayPlatform, folder, type);
	}

	/**
	 * Create mapping files defining the specie to query
	 * @param specie
	 */
	public void createFiles(String specie,int type){
		String microarrayPlatform = "all";
		createFiles(specie, microarrayPlatform, folder, type);
	}

	/**
	 * Create mapping files defining the specie to query
	 * @param specie
	 * @param folder
	 */
	public void createFiles(String specie, String folder,int type){
		String microarrayPlatform = "all";
		createFiles(specie, microarrayPlatform, folder, type);
	}

	/**
	 * Create mapping files defining the folder where to store the
	 * files and the specie to query
	 * @param specie
	 * @param folder
	 */
	public void createFiles(String specie, String microarrayPlatform, String folder,int type){
		this.folder = folder;
		List<String> sources = new ArrayList<String>();
		switch (type){
			case FETCH_FOR_UNIPROT:
				sources.add("swissprot");
				sources.add("trembl");
				break;
			case FETCH_FOR_ENSEMBL:
				sources.add("ensembl");
				break;
		}

		BiomartAccess bA = new BiomartAccess();
		List<SpecieMapping> specieMappings = getSpeciePlatformMapping(specie);
		for(SpecieMapping sM:specieMappings){
			String dataset = sM.getDataset();
			String specieName = sM.getSpecie();
			Map<String,Long> platforms = sM.getPlatforms();
			for(String platform:platforms.keySet()){
				for(String source:sources){
					String XMLquery = createXmlQuery(dataset,platform,source);
					if (!createFolderIfdoesNotExist(source))
						break;
					try {
						String XMLqueryEncoded = URLEncoder.encode(XMLquery, "UTF-8");
						/* Find results per attr  */
						logger.debug("Sending queries to biomart ...");
						logger.debug(XMLquery);
						if(microarrayPlatform == null || microarrayPlatform == "" || microarrayPlatform == "all"){
							downloadFileFromBiomart(XMLqueryEncoded, platform, dataset, specieName, source);
						} else if(platform.equalsIgnoreCase(microarrayPlatform)){
							downloadFileFromBiomart(XMLqueryEncoded, platform, dataset, specieName, source);
						}
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}
		}
	}

	private boolean createFolderIfdoesNotExist(String uniprotSource) {
		boolean exists = (new File(folder + uniprotSource)).exists();
		if (!exists) {
			exists = (new File(folder+ uniprotSource)).mkdir();
			if (!exists) 
				System.out.println("Error creating the \"" + folder + uniprotSource + "\" directory to save your results");
		}
		return exists;
	}

	protected String createXmlQuery(String dataset, String platform, String source){
		String XMLquery = "<?xml version='1.0' encoding='UTF-8'?>";
		XMLquery += "<!DOCTYPE Query>";
		XMLquery += "<Query  virtualSchemaName = 'default' formatter = 'TSV' header = '0' uniqueRows = '1' count = '0' datasetConfigVersion = '0.6' >";
		XMLquery += "<Dataset name = '"+dataset+"' interface = 'default' >";
		XMLquery += "<Filter name = 'with_"+platform+"' excluded = '0'/>";
		XMLquery += "<Attribute name = '"+platform+"' />";
		//XMLquery += "<Attribute name = 'ensembl_transcript_id' />";
		if(source.equalsIgnoreCase("trembl")){
			XMLquery += "<Attribute name = 'uniprot_sptrembl' />";
		} else if(source.equalsIgnoreCase("swissprot")){
			XMLquery += "<Attribute name = 'uniprot_swissprot_accession' />";
		} else if(source.equalsIgnoreCase("ensembl")){
			XMLquery += "<Attribute name = 'ensembl_gene_id' />";
		}
		XMLquery += "</Dataset>";
		XMLquery += "</Query>";
		return XMLquery;
	}


	public List<SpecieMapping> getSpeciePlatformMapping(String specie){
		List<SpecieMapping> specieMapping = new ArrayList<SpecieMapping>();
		BiomartAccess bA = new BiomartAccess();
		bA.setServiceUrl(bA.ensemblServiceUrl);
		/* Find datasets */
		logger.debug("Looking for ensembl datasets");
		String[][] datasets = bA.getDatasets("ensembl", specie);
		for (int i = 0; i < datasets.length; i++) {
			String[] datasetRow = datasets[i];
			String dataset = datasetRow[1];
			String specieGeneBuild = datasetRow[2];
			String[] specieGeneBuildSplit =  specieGeneBuild.split(" genes \\(");
			String specieName = specieGeneBuildSplit[0];
			//String geneBuild = specieGeneBuildSplit[1].substring(0, specieGeneBuildSplit[1].length()-1);
			/* Find attr per dataset */
			logger.debug("Looking for available attributes in ensembl datasets");
			String[][] attributes = bA.getAttributes(dataset);
			logger.debug("Filtering attributes by \"" + probeAttributeType + "\"");
			if(attributes.length == 0){
				logger.info("No microarray probe information found for " + specie);
			}
			Map<String,Long> platforms = new HashMap<String,Long>();
			for (int j = 0; j < attributes.length; j++) {
				String[] attributeRow = attributes[j];
				logger.debug(attributeRow[0]);
				if(attributeRow.length >= 6){
					String attributeName = attributeRow[0];
					String attributeType = attributeRow[6];
					if(attributeType.equalsIgnoreCase(probeAttributeType)){
						platforms.put(attributeName,null);
					}
				}
			}
			SpecieMapping sM = new SpecieMapping(specieName, dataset, platforms);
			specieMapping.add(sM);
		}
		return specieMapping;
	}

	/* Method to retrieve the path of the folder for the results */
	public String getResultsPath(){
		File dir = new File(folder);
		return dir.getAbsolutePath();
	}

	/* Get folder name */
	public String getFolder() {
		return folder;
	}

	/* set a folder name */
	public void setFolder(String folder) {
		this.folder = folder;
	}


	private String[] concatArrays(String[] A, String[] B) {
		String[] C= new String[A.length+B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return replaceArrayNull(C);
	}

	private String[] replaceArrayNull(String[] C){
		for(int i=0; i<C.length;i++){
			if(C[i]==null){
				C[i]="";
			}
		}
		return C;
	}

	/**
	 * Method used by createFiles() to save results in a file.
	 * @param XMLquery
	 * @param microarrayPlatform
	 * @param dataset
	 * @param specie
	 */
	private void downloadFileFromBiomart(String XMLquery, String microarrayPlatform, String dataset, String specie, String source) {
		String[] extraFields = new String []{"specie", "platform"};
		String[] solrFields = new String[3];
		if(source.equalsIgnoreCase("trembl")){
			solrFields = new String []{"probe", "tremblAcc"};
		} else if(source.equalsIgnoreCase("swissprot")){
			solrFields = new String []{"probe", "swissprotAcc"};
		} else if(source.equalsIgnoreCase("ensembl")){
			solrFields = new String []{"probe", "ensemblAcc"};
		} else {
			logger.error("No source defined!");
		}
		String[] fields =  concatArrays(extraFields, solrFields);
		int maxNumOfLines = 150000;

		String tab = "\t";
		String comma = ",";
		String newline = System.getProperty("line.separator");


		try {
			/* Fetch file from Biomart */
			String url = BiomartAccess.ensemblServiceUrl + "?query=" + XMLquery;
			System.out.println("Downloading from: "+url);
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			String line;
			BufferedWriter out = null;
			int countLines = 0;
			int countFiles = 0;
			readLines:
				while ((line = in.readLine()) != null) {
					if(line.length() > 1 && line.toLowerCase().indexOf("problem") == -1 && line.toLowerCase().indexOf("ERROR") == -1){
						/* Create more than one file if the input got more than a limit of lines */
						if (countLines % maxNumOfLines == 0) {
							if(countLines != 0){
								out.close();
							}
							/* Open a new file and write column names */
							URI uri = null;
							uri = new URI(folder + source + "/" + dataset + "." + microarrayPlatform + "." + countFiles + ".txt");
							System.out.println("Writing in file: "+uri.getPath());
							FileWriter fstream = new FileWriter(uri.getPath());
							out = new BufferedWriter(fstream);
							for (int i = 0; i < fields.length; i++){
								out.write(fields[i]);
								if(i < fields.length-1){
									out.write(comma);
								}
							}
							countFiles++;
						}
						countLines++;
						String[] results = line.split("\t");
						String[] emptyArray = new String[solrFields.length - results.length];
						String[] row = concatArrays(results, emptyArray);
						/* Save just results with Uniprot Accs */
						String microarrayProbe = row[0];
						String uniprotAcc = row[1];
						if(uniprotAcc.length() > 0){
							/* Write file */
							out.write(newline); //todp: out.newline()
							out.write(specie);
							out.write(comma);
							out.write(microarrayPlatform);
							out.write(comma);
							out.write(microarrayProbe);
							out.write(comma);
							out.write(uniprotAcc);
						}
					} else {
						logger.debug("Not valid content for " + url);
						break readLines;
					}
				}
			out.close();
			in.close();
		} catch (URISyntaxException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}



	}
}
