package uk.ac.ebi.enfin.biomart.commandline;

import java.io.File;

import uk.ac.ebi.enfin.biomart.probes.MicroarrayProbes;
import uk.ac.ebi.enfin.biomart.probes.SolrProbes;

public class Main {
	public static final String FETCH_PROBE_DATA	= "FETCH";
	public static final String DELETE_PROBE_DATA= "DELETE";
	public static final String UPLOAD_PROBE_DATA= "UPLOAD";
	public static String command_jar = "java -jar biomartProbes.jar OPTION [arguments]";

	public static void main(String[] args){
		if (args.length<1){
			System.out.println("ERROR: The execution command is malformed. No enough arguments. The command should be like:\n"+command_jar+"\nOPTIONS: FETCH | DELETE | UPLOAD");
		}else{
			if (args[0].equals(FETCH_PROBE_DATA)){
				/* Create results directory if it does not exist */
				String folder = "results";
				if (args.length<2){
					System.out.println("ERROR: The execution command is malformed. The option FETCH requires a type (UNIPROT|ENSEMBL) and optionally you can indicate the specie(s) to fetch the data. If no species provided, all the available data will be downloaded.");
					return;
				}
				int type=-1;
				if (args[1].equalsIgnoreCase("UNIPROT")){
					folder+="UNIPROT";
					type=MicroarrayProbes.FETCH_FOR_UNIPROT;
				}else if (args[1].equalsIgnoreCase("ENSEMBL")){
					folder+="ENSEMBL";
					type=MicroarrayProbes.FETCH_FOR_ENSEMBL;
				}else{
					System.out.println("ERROR: The execution command is malformed. The option FETCH requires a type (UNIPROT|ENSEMBL) and optionally you can indicate the specie(s) to fetch the data. If no species provided, all the available data will be downloaded.");
					return;
				}

				boolean exists = (new File(folder)).exists();
				if (!exists) {
					boolean success = (new File(folder)).mkdir();
					if (!success) {
						System.out.println("Error creating the \"" + folder + "\" directory to save your results");
						return;
					}
				}

				/* Save result files in folder */
				folder = folder + "/";
				MicroarrayProbes mP = new MicroarrayProbes();
				if(args.length > 2){
					//to skip the first argument, which is the command FETCH
					for(int i=2;i<args.length;i++ ){
						String arg=args[i];
						mP.createFiles(arg, folder,type);
					}
				} else {
					mP.createFiles("all", folder,type);
				}
				System.out.println("Check results in ... " + mP.getResultsPath());

			} else if (args[0].equals(DELETE_PROBE_DATA)){
				if(args.length != 2){
					System.out.println("ERROR: The execution command is malformed. The DELETE option requires 1 argument providing the Solr instance URL. The command should be like:\n"+command_jar+"\nOPTIONS: FETCH | DELETE | UPLOAD");
					return;
				}
				String solrUrl = args[1];
				SolrProbes sP = new SolrProbes();
				sP.setHttpSolrConnection(solrUrl);
				sP.deleteEverything();

				System.out.println("Solr has been updated! Please check your input in ..." + sP.getSolrServer());
			} else if (args[0].equals(UPLOAD_PROBE_DATA)){
				if(args.length != 3){
					System.out.println("ERROR: The execution command is malformed. The UPLOAD option requires 2 arguments. First the folder with the biomart results, and second the Solr instance URL. The command should be like:\n"+command_jar+"\nOPTIONS: FETCH | DELETE | UPLOAD");
					return;
				}
				String solrUrl = args[2];
				String folder = args[1];
				String file = "*.*";

				SolrProbes sP = new SolrProbes();
				sP.setHttpSolrConnection(solrUrl);
				sP.addFilesToSolr(folder, file);

				System.out.println("Solr has been updated! Please check your input in ..." + sP.getSolrServer());
			} else {
				System.out.println("ERROR: The execution command malformed. No valid Option. The command should be like:\n"+command_jar+"\nOPTIONS: FETCH | DELETE | UPLOAD");	
			}
		}
	}
}
