package uk.ac.ebi.enfin.biomart;

import junit.framework.TestCase;
import uk.ac.ebi.enfin.biomart.probes.MicroarrayProbes;
import uk.ac.ebi.enfin.biomart.probes.SpecieMapping;

import java.util.List;

/**
 * User: rafael
 * Date: 05-Jul-2010
 * Time: 16:45:05
 */
public class TestMicroarrayProbes extends TestCase {
//    public void testMicroarrayProbes(){
//    	try{
//    		new MicroarrayProbes().createFiles("Gallus gallus", "results/",MicroarrayProbes.FETCH_FOR_UNIPROT);
//    	}catch(Exception e){
//        	fail("exception: "+e.getMessage());
//        }
////        new MicroarrayProbes().createFiles("Homo sapiens", "results/");
////        new MicroarrayProbes().createFiles("Caenorhabditis elegans", "results/");
//    }

    public void testMicroarrayProbesFolder(){
        MicroarrayProbes mP = new MicroarrayProbes();
        mP.setFolder("test");
        String resultPath = mP.getResultsPath();
        assertTrue(resultPath.length() > 0);
    }

    public void testMicroarrayProbesFolder2(){
        MicroarrayProbes mP = new MicroarrayProbes();
        String resultPath = mP.getResultsPath();
        assertTrue(resultPath.length() > 0);
    }

    public void tezstGetSpeciePlatformMapping(){
        MicroarrayProbes mP = new MicroarrayProbes();
        List<SpecieMapping> results = mP.getSpeciePlatformMapping("Homo sapiens");
        assertTrue(results.size() > 0);
    }
}
