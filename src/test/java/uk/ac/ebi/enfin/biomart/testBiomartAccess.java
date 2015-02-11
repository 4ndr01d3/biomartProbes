package uk.ac.ebi.enfin.biomart;

import junit.framework.TestCase;
import uk.ac.ebi.enfin.biomart.probes.MicroarrayProbes;

import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rafael
 * Date: 02-Jul-2010
 * Time: 13:58:57
 */
public class testBiomartAccess extends TestCase {
	//    public void testDownloadFilesFromInternet(){
	//        BiomartAccess bA = new BiomartAccess();
	////        String url = "http://www.biomart.org/biomart/martservice?query=%3C%3Fxml+version%3D%271.0%27+encoding%3D%27UTF-8%27%3F%3E%3C!DOCTYPE+Query%3E%3CQuery++virtualSchemaName+%3D+%27default%27+formatter+%3D+%27TSV%27+header+%3D+%270%27+uniqueRows+%3D+%271%27+count+%3D+%270%27+datasetConfigVersion+%3D+%270.6%27+%3E%3CDataset+name+%3D+%27celegans_gene_ensembl%27+interface+%3D+%27default%27+%3E%3CFilter+name+%3D+%27with_affy_c_elegans%27+excluded+%3D+%270%27%2F%3E%3CAttribute+name+%3D+%27affy_c_elegans%27+%2F%3E%3CAttribute+name+%3D+%27uniprot_swissprot_accession%27+%2F%3E%3CAttribute+name+%3D+%27uniprot_swissprot%27+%2F%3E%3CAttribute+name+%3D+%27uniprot_sptrembl%27+%2F%3E%3C%2FDataset%3E%3C%2FQuery%3E";
	//        String url = "http://www.google.co.uk/";
	//        bA.downloadFileFromInternet(url,"results/test.txt");
	//    }

	//    public void testDatasets(){
	//        BiomartAccess bA = new BiomartAccess();
	//        String[][] datasets = bA.getDatasets("ensembl");
	//        assertTrue(datasets.length > 1);
	//    }

	//    public void testFilteredDatasets(){
	//        BiomartAccess bA = new BiomartAccess();
	//        String[][] datasets = bA.getDatasets("ensembl", "Homo sapiens");
	//        assertTrue(datasets.length > 0);
	//    }

	public void testAttributes(){
//		System.setProperty("http.proxyHost", "proxynet.uct.ac.za");
//		System.setProperty("http.proxyPort", "8080");
//		System.setProperty("http.nonProxyHosts", "localhost,127.0.0.1,137.158.205.152,uct.ac.za");
//		Authenticator.setDefault(new Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return (new PasswordAuthentication("SLZGUS001", "Str33tSp1r1t".toCharArray()));
//			}
//		});

		BiomartAccess bA = new BiomartAccess();
		bA.setServiceUrl(BiomartAccess.ensemblServiceUrl);
		String[][] attributes = bA.getAttributes("hsapiens_gene_ensembl");
		assertTrue(attributes.length > 1);
	}


	public void testSplitLines(){
		BiomartAccess bA = new BiomartAccess();
		List<String> lines = new ArrayList<String>();
		lines.add("178341_at\t\t\tO18032");
		lines.add("178341_at\t\t\t\t");
		lines.add("192402_at\tQ9U2Q9\tGSK3_CAEEL\t");
		lines.add("172990_at\tQ9U2Q9\tGSK3_CAEEL\t");
		lines.add("192814_at\t\t\tQ9XUM9");
		String[][] result = bA.splitResult(lines);
		assertTrue(result.length > 1);
	}
}
