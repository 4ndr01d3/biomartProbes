package uk.ac.ebi.enfin.biomart;

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import uk.ac.ebi.enfin.biomart.probes.MicroarrayProbes;
import uk.ac.ebi.enfin.biomart.probes.SolrProbes;
import uk.ac.ebi.enfin.biomart.probes.SpecieMapping;

import java.util.List;
import java.util.Map;

/**
* User: rafael
* Date: 07-Jul-2010
* Time: 13:43:44
*/
public class TestSolrProbes extends TestCase {
    String solrUrl = "http://localhost:8983/solr/uniprot2probes";
//    String solrUrl = "http://tc-test-3.ebi.ac.uk:9700/solr-probe2uniprot";
//    String solrUrl = "http://tc-test-3.ebi.ac.uk:9700/solr-probe2swissprot";

//    public void testSolrEmbedded(){
//        String folder = "C:\\rafael\\IdeaProjects\\solr\\example\\exampledocs\\";
//        String file = "books.csv";
//        SolrProbes sP = new SolrProbes();
//        sP.setEmbeddedSolrConnection();
//        sP.addFilesToSolr(folder, file);
//    }

//    public void testSolrHttp(){
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection(solrUrl);
//        assertNotNull(sP.getSolrServer());
//    }

//    public void testUploadGallusWithSolrHttp(){
//        String folder = "C:\\rafael\\IdeaProjects\\solr\\example\\biomartProbes\\";
//        String file = "ggallus_gene_ensembl.affy_chicken.txt";
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection();
//        sP.addFilesToSolr(folder, file);
//    }



//    public void testSearchSolrEmbeddes(){
//        SolrProbes sP = new SolrProbes();
//        sP.setEmbeddedSolrConnection();
//        sP.getResults();
//    }

//     public void testSearchSolrHttp(){
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection();
//        sP.getResults();
//    }

//    public void testSearchQuerySolrHttp(){
//        String query = "probe:*693*";
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection();
//        QueryResponse result = sP.getResults(query);
//
//        query = "probe:*69*";
//        result = sP.getResults(query);
//        assertNotNull(result);
//    }

//      public void testUploadAllWithSolrHttp(){
////        String folder = "C:\\rafael\\IdeaProjects\\solr\\example\\biomartProbes";
////        String folder = "C:/rafael/IdeaProjects/solr/example/biomartProbes";
//        String folder = "C:/rafael/IdeaProjects/solr/example/test";
//        String file = "*.*";
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection(solrUrl);
//        sP.deleteEverything();
//        sP.addFilesToSolr(folder, file);
//    }

//    public void testDeleteAllWithSolrHttp(){
//        String file = "*.*";
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection(solrUrl);
//        sP.deleteEverything();
//    }

//    public void testQueries(){
//        try {
//            SolrProbes sP = new SolrProbes();
//            sP.setHttpSolrConnection();
//            SolrServer solrServer = sP.getSolrServer();
//
//            String query = "*:*";
//            SolrQuery solrQuery = new SolrQuery();
//            solrQuery.setQuery(query);
//            solrQuery.addSortField( "specie", SolrQuery.ORDER.asc );
//            solrQuery.addSortField( "probe", SolrQuery.ORDER.asc );
//            QueryResponse response = solrServer.query(solrQuery);
//            long count = response.getResults().getNumFound();
//            System.out.println("This query \""+query+"\" return " + response.getResults().getNumFound() + " results");
//        } catch (SolrServerException e) {
//            System.out.println(e.getMessage());
//        }
//    }

//    public void testSearch2QuerySolrHttp(){
//        String query01 = "probe:*693*";
//        String query02 = "probe:*59625_at*";
//        SolrProbes sP = new SolrProbes();
//        sP.setHttpSolrConnection(solrUrl);
//        QueryResponse result01 = sP.getResults(query01);
//        QueryResponse result02 = sP.getResults(query02);
//        assertNotNull(result01);
//        assertNotNull(result02);
//    }


//    public void testComplexQuery(){
////        String query = "(probe:59625_at q.op=AND probe:211397_x_at)";
//        String query = "(uniprotAcc:P53017)";
//        SolrProbes sP = new SolrProbes();
////        sP.setHttpSolrConnection("http://localhost:8983/solr");
//        sP.setHttpSolrConnection(solrUrl);
//        QueryResponse result = sP.getResults(query);
//        assertNotNull(result);
//    }

    public void testGetSpecies (){
        SolrProbes sP = new SolrProbes();
        sP.setHttpSolrConnection(solrUrl);
        Map<String,Long> result = sP.getSpecies();
        assertNotNull(result);
    }

    public void testGetUniprotAcc (){
        SolrProbes sP = new SolrProbes();
        sP.setHttpSolrConnection(solrUrl);
        Map<String,Long> result = sP.getUniprotAcc(3,2);
        assertNotNull(result);
    }

    public void testGetProbeAcc (){
        SolrProbes sP = new SolrProbes();
        sP.setHttpSolrConnection(solrUrl);
        int result = sP.getFieldSizeByLuke("probe");
        assertNotNull(result);
    }

    public void testGetPlatformPerSpecie(){
        SolrProbes sP = new SolrProbes();
        sP.setHttpSolrConnection(solrUrl);
        List<SpecieMapping> result = sP.getPlatformPerSpecie();
        assertNotNull(result);
    }

}
