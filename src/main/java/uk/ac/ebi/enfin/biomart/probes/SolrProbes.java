package uk.ac.ebi.enfin.biomart.probes;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldTypeInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User: rafael
 * Date: 07-Jul-2010
 * Time: 15:08:01
 *
 * Class to connect to a Solr server, update probe files and make queries
 */
public class SolrProbes {
    static Logger logger = Logger.getLogger(SolrProbes.class);
    protected SolrServer solrServer;

    public SolrProbes() {

    }

    public void setHttpSolrConnection(String solrUrl){
        CommonsHttpSolrServer server = null;
        try {
            server = new CommonsHttpSolrServer(solrUrl);
            server.setSoTimeout(100000);  // socket read timeout
            server.setConnectionTimeout(10000);
            server.setDefaultMaxConnectionsPerHost(10000);
            server.setMaxTotalConnections(1000);
            server.setFollowRedirects(false);  // defaults to false
            // allowCompression defaults to false.
            // Server side must support gzip or deflate for this to have any effect.
            server.setAllowCompression(true);
            server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        solrServer = server;
    }


    public void setEmbeddedSolrConnection(String solrHome){
        EmbeddedSolrServer server = null;
        if(System.getProperty("solr.solr.home") == null){
            System.setProperty("solr.solr.home", solrHome);
        }
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = null;
        try {
            coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer(coreContainer, "");
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage());
        } catch (SAXException e) {
            logger.error(e.getMessage());
        }
        solrServer = server;
    }


    public void addFilesToSolr(String folder){
        String file = "*.*";
        addFilesToSolr(folder, file);
    }

    public void addFilesToSolr(String folderName, String fileName){
        String filePath;
        String[] files;

        if(folderName.length() > 0){
            folderName = folderName.replaceAll("\\\\", "/");
            if(!folderName.substring(folderName.length()-1).equals("/")){
                folderName = folderName + "/";
            }
        }

        if(fileName.equalsIgnoreCase("*.*") || fileName.equalsIgnoreCase("*") || fileName.equalsIgnoreCase("all")){
            File dir = new File(folderName);
            files = dir.list(filter);
        } else {
            files = new String[0];
            files[0] = new String(fileName);
        }


        for (int i=0; i<files.length; i++) {
            try {
                filePath = folderName + files[i];
                if ((new File(filePath)).isDirectory()){
                	this.addFilesToSolr(filePath, fileName);
                	break;
                }
                ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/csv");
                req.addFile(new File(filePath));
//                req.setParam("header", "false");
//                req.setParam("fieldnames", "specie,platform,probe,swissprotAcc,uniprotId,tremblAcc");
                req.setParam("literal.id", filePath); // Not sure if necessary. It provides the necessary unique id for the document being indexed
                req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
                NamedList<Object> result = null;
                try {
                    result = solrServer.request(req);
//                    solrServer.commit();
                    logger.info("Result: " + result);
                } catch (SolrServerException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }




    public QueryResponse getResults(String query){
        QueryResponse response = null;
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            solrQuery.setStart(0);
            solrQuery.setRows(100000);
            solrQuery.addSortField( "specie", SolrQuery.ORDER.asc );
            solrQuery.addSortField( "probe", SolrQuery.ORDER.asc );  
            response = solrServer.query(solrQuery);
            long numberOfResults = response.getResults().getNumFound();
            //todo:iterate taking into account numberOfresults
            logger.info("This query \""+query+"\" return " + numberOfResults + " results");
        } catch (SolrServerException e) {
            logger.error(e.getMessage());
        }
        return response;
    }


    public List<SpecieMapping> getPlatformPerSpecie(){
        List<SpecieMapping> platformPerSpecie = new ArrayList<SpecieMapping>();
        String facetField = "platform";
        Map<String,Long> species = getSpecies();
        for(String specie:species.keySet()){
            String query = "specie_exact:"+specie;
            Map<String,Long> platfroms = filterByFacetField(query, facetField);
            SpecieMapping sM = new SpecieMapping(specie,null,platfroms);
            platformPerSpecie.add(sM);
        }
        return platformPerSpecie;
    }

    public Map<String,Long> getSpecies(){
        String facetField = "specie_exact";
        String query = "*:*";
        Map<String,Long> species = filterByFacetField(query,facetField);
        return species;
    }


    public Map<String,Long> getUniprotAcc(String facetField,Integer facetOffset, Integer facetLimit){
        String query = "*:*";
        Map<String,Long> species = filterByFacetField(query,facetField, facetOffset, facetLimit);
        return species;
    }
    /**
     * Returns the number of times uniprot acc appear in the solr
     * @param facetOffset This parameter is used to paginate results from a query. When specified, it indicates the offset in the complete result set for the queries where the set of returned documents should begin. (i.e. the first record appear in the result set is the offset).
     * @param facetLimit This parameter is used to paginate results from a query. When specified, it indicates the maximum number of documents from the complete result set to return to the client for every request. (You can consider it as the maximum number of result appear in the page).
     * @return
     */
    public Map<String,Long> getUniprotAcc(Integer facetOffset, Integer facetLimit){
        String facetField = "uniprotAcc";
        return this.getUniprotAcc(facetField, facetOffset, facetLimit);
    }

    public Map<String,Long> getUniprotAcc(){
        Integer facetOffset = 0;
        Integer facetLimit = -1;
        String facetField = "uniprotAcc";
        return this.getUniprotAcc(facetField, facetOffset, facetLimit);
    }


    public Map<String,Long> getEnsemblAcc(String facetField,Integer facetOffset, Integer facetLimit){
        String query = "*:*";
        Map<String,Long> species = filterByFacetField(query,facetField, facetOffset, facetLimit);
        return species;
    }
    /**
     * Returns the number of times uniprot acc appear in the solr
     * @param facetOffset This parameter is used to paginate results from a query. When specified, it indicates the offset in the complete result set for the queries where the set of returned documents should begin. (i.e. the first record appear in the result set is the offset).
     * @param facetLimit This parameter is used to paginate results from a query. When specified, it indicates the maximum number of documents from the complete result set to return to the client for every request. (You can consider it as the maximum number of result appear in the page).
     * @return
     */
    public Map<String,Long> getEnsemblAcc(Integer facetOffset, Integer facetLimit){
        String facetField = "ensemblAcc";
        return this.getUniprotAcc(facetField, facetOffset, facetLimit);
    }

    public Map<String,Long> getEnsemblAcc(){
        Integer facetOffset = 0;
        Integer facetLimit = -1;
        String facetField = "ensemblAcc";
        return this.getUniprotAcc(facetField, facetOffset, facetLimit);
    }
    public Map<String,Long> getProbesAcc(String facetField,Integer facetOffset, Integer facetLimit){
        String query = "*:*";
        Map<String,Long> species = filterByFacetField(query,facetField, facetOffset, facetLimit);
        return species;
    }
    public Map<String,Long> getProbesAcc(Integer facetOffset, Integer facetLimit){
        String facetField = "probe";
        return this.getUniprotAcc(facetField, facetOffset, facetLimit);
    }

    public Map<String,Long> getProbesAcc(){
        Integer facetOffset = 0;
        Integer facetLimit = -1;
        String facetField = "probe";
        return this.getProbesAcc(facetField, facetOffset, facetLimit);
    }

    private Map<String,Long> filterByFacetField(String query, String facetField){
        Integer facetOffset = 0;
        Integer facetLimit = -1;
        Map<String,Long> filteringResults;
        filteringResults = filterByFacetField(query, facetField, facetOffset, facetLimit);
        return filteringResults;
    }

    /**
     * Returns a Map with the name of the field and the number of times it happens.
     * @param query
     * @param facetField Field to filter
     * @param facetOffset This parameter is used to paginate results from a query. When specified, it indicates the offset in the complete result set for the queries where the set of returned documents should begin. (i.e. the first record appear in the result set is the offset).
     * @param facetLimit This parameter is used to paginate results from a query. When specified, it indicates the maximum number of documents from the complete result set to return to the client for every request. (You can consider it as the maximum number of result appear in the page).
     * @return
     */
    private Map<String,Long> filterByFacetField(String query, String facetField, Integer facetOffset, Integer facetLimit){
        Map<String,Long> filteringResults = new HashMap<String,Long>();
        QueryResponse solrResponse = null;
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            solrQuery.set("f."+facetField+".facet.offset",facetOffset);
            solrQuery.setRows(0);
            solrQuery.setFacetSort(FacetParams.FACET_SORT_COUNT);
            solrQuery.setFacet(true);
            solrQuery.setStart(facetOffset);
            solrQuery.setFacetLimit(facetLimit);
            solrQuery.setFacetMinCount(1); // It will take results with one or more than one results
            solrQuery.addFacetField(facetField);
            solrResponse = solrServer.query(solrQuery);
        } catch (SolrServerException e) {
            logger.error(e.getMessage());
        }

        if(solrResponse != null){
            List<FacetField> facetFields = solrResponse.getFacetFields();
            if(facetFields.size() > 0){
                facetFieldLoop:
                for(FacetField fField:facetFields){
                    if(fField.getName().equalsIgnoreCase(facetField)){
                        List<Count> results = fField.getValues();
                        for(Count result:results){
                            String resultName = result.getName();
                            long resultNumber = result.getCount();
                            filteringResults.put(resultName,resultNumber);
                        }
                        break facetFieldLoop;
                    }
                }
            }
        }
        return filteringResults;
    }
    
    public int getFieldSizeByLuke(String field){
    	LukeRequest luke = new LukeRequest();
    	luke.setShowSchema( false );
    	try {
			LukeResponse rsp = luke.process( solrServer );
			return rsp.getFieldInfo(field).getDistinct();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return 0;
    }

    public void deleteEverything(){
        try {
            logger.info("Deleting everything");
            solrServer.deleteByQuery( "*:*" );
            solrServer.commit();
        } catch (SolrServerException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    /* Used to just read files */
    FilenameFilter filter = new FilenameFilter() { 
        public boolean accept(File dir, String name) { return !name.startsWith("."); }
    };

    public SolrServer getSolrServer() {
        return solrServer;
    }
}
