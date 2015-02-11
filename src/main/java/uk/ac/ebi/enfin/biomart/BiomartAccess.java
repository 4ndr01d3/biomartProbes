package uk.ac.ebi.enfin.biomart;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rafael
 * Date: 02-Jul-2010
 * Time: 12:15:05
 *
 * Class to access and query biomart sources
 */
public class BiomartAccess {
    static Logger logger = Logger.getLogger(BiomartAccess.class);
    private String serviceUrl;
    public static String biomartServiceUrl = "http://www.biomart.org/biomart/martservice";
    public static String ensemblServiceUrl = "http://www.ensembl.org/biomart/martservice";

    public BiomartAccess(){
        // Use the ensemblServiceUrl mart by default.
        setServiceUrl(ensemblServiceUrl);
    }

    /**
     * Send a query to biomart and get results
     * @param XMLquery
     * @return
     */
    public String[][] getResult(String XMLquery){
        String url = serviceUrl + "?query=" + XMLquery;
        List<String> result = wget(url);
        String[][] resultSplit = splitResult(result);
        if(resultSplit.length > 0){
            return resultSplit;
        } else {
            String[][] emptyArray = new String[0][];
            return emptyArray;
        }
    }

    /**
     * Get available datasets for one specific mart
     * @param mart
     * @return
     */
    public String[][] getDatasets(String mart){
        return getDatasets(mart, "all");
    }

    /**
     * Get the dataset name for one specific mart and one specific specie
     * @param mart
     * @return
     */
    public String[][] getDatasets(String mart, String specie) {
    	if (mart.equals("ensembl")) mart="ENSEMBL_MART_ENSEMBL";
        String url = serviceUrl + "?type=datasets&mart=" + mart;
        List<String> result = wget(url);
        String[][] resultSplit = splitResult(result);
        /* Return all datasets */
        if(specie.equalsIgnoreCase("all")){
            if(resultSplit.length > 0){
                return resultSplit;
            } else {
                String[][] emptyArray = new String[0][];
                return emptyArray;
            }
        /* Return a dataset for one specific specie */            
        } else {
            String[][] filteredResult = new String[1][];
            for (int i = 0; i < resultSplit.length; i++) {
                String[] datasetRow = resultSplit[i];
                String specieGeneBuild = datasetRow[2];
                if(specieGeneBuild.indexOf(specie) != -1){
                    filteredResult[0] = datasetRow;
                    break;
                }
            }
            if(filteredResult.length > 0){
                return filteredResult;
            } else {
                String[][] emptyArray = new String[0][];
                return emptyArray;
            }
        }
    }


    /**
     * Get available filters for one specific dataset
     * @param dataset
     * @return
     */
    public String[][] getFilters(String dataset) {
        String url = serviceUrl + "?type=filters&dataset=" + dataset;
        List<String> result = wget(url);
        String[][] resultSplit = splitResult(result);
        if(resultSplit.length > 0){
            return resultSplit;
        } else {
            String[][] emptyArray = new String[0][];
            return emptyArray;
        }
    }

    /**
     * Get available attributes for one specific dataset
     * @param dataset
     * @return
     */
    public String[][] getAttributes(String dataset) {
        String url = getServiceUrl() + "?type=attributes&dataset=" + dataset;
        List<String> result = wget(url);
        if(result.size() == 0 && !getServiceUrl().equalsIgnoreCase(biomartServiceUrl)){
            url = biomartServiceUrl + "?type=attributes&dataset=" + dataset;
            result = wget(url);
        }
        String[][] resultSplit = splitResult(result);
        if(resultSplit.length > 0){
            return resultSplit;
        } else {
            String[][] emptyArray = new String[0][];
            return emptyArray;
        }
    }

    /**
     * Get the URL that is going to be use to query biomart
     * @return
     */
    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * Set the URL that is going to be use to query biomart
     * @param serviceUrl
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void downloadFileFromInternet(String url, String destinationFile){
        URL google = null;
        try {
            google = new URL(url);
            ReadableByteChannel rbc = null;
            try {
                rbc = Channels.newChannel(google.openStream());
                FileOutputStream fos = new FileOutputStream(destinationFile, true);
                fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }



    private List wget(String url) {
        List result = new ArrayList<String>();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if(line.length() > 1){
                    result.add(line);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return result;
    }



    /**
     * Split results from biomart into an array (rows) of arrays (fields)
     * @param lines
     * @return
     */
    protected String[][] splitResult(List<String> lines) {
        String[][] emptyArray = new String[0][];
        if(lines.size() > 0){
            /* Ignore problem response in the biomart request */
            if(lines.get(0).toLowerCase().indexOf("problem") != -1){
                logger.error(lines.get(0));
                return emptyArray;
            } else {
                logger.debug("splitting biomart result into String[][]");
                String[][] array = new String[lines.size()][];
                int c = 0;
                for (String line : lines) {
                    String[] values = line.split("\t");
                    array[c] = values;
                    c++;
                }
                return array;
            }
        } else {
            logger.error("Empty input");
            return emptyArray;
        }
    }





}
