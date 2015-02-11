package uk.ac.ebi.enfin.biomart.util;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.biomart.BiomartAccess;
import uk.ac.ebi.enfin.biomart.probes.MicroarrayProbes;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: rafael
 * Date: 10-Aug-2010
 * Time: 14:52:54
 */
public class TransformProbeFiles {
    static Logger logger = Logger.getLogger(TransformProbeFiles.class);
    MicroarrayProbes mP = new MicroarrayProbes();

    public void fromUniprotFolder(){
        File dir = new File("results/uniprot/");
        String[] files = dir.list(filter);
        for (int i=0; i<files.length; i++) {
            start(files[i]);
        }
    }

    public void start(String fileName) {
        String[] solrFields = new String []{"specie","platform","probe","swissprotAcc","uniprotId","tremblAcc"};
        String tab = "\t";
        String comma = ",";
        String newline = System.getProperty("line.separator");


        try {
            /* Fetch file from Biomart */
            File file = new File("results/uniprot/" + fileName);
            BufferedReader in = new BufferedReader(new FileReader(file));

            /* Trembl */
            FileWriter tremblFstream = new FileWriter("results/trembl/"+file.getName());
            BufferedWriter tremblOut = null;
            tremblOut = new BufferedWriter(tremblFstream);

            /* Swissprot */
            FileWriter swissprotFstream = new FileWriter("results/swissprot/"+file.getName());
            BufferedWriter swissprotOut = null;
            swissprotOut = new BufferedWriter(swissprotFstream);

            Map<String,ArrayList<String>> tProb2Prot = new HashMap<String,ArrayList<String>>();
            Map<String,ArrayList<String>> tProt2Prob = new HashMap<String,ArrayList<String>>();
            Map<String,ArrayList<String>> sProb2Prot = new HashMap<String,ArrayList<String>>();
            Map<String,ArrayList<String>> sProt2Prob = new HashMap<String,ArrayList<String>>();

            String line;
            int countLines = 0;
            readLines:
            while ((line = in.readLine()) != null) {
                if(countLines == 0){
                    tremblOut.write("specie,platform,probe,tremblAcc");
                    swissprotOut.write("specie,platform,probe,swissprotAcc");
                } else {
                    String[] results = line.split(comma);
                    String[] emptyArray = new String[solrFields.length - results.length];
                    String[] row = concatArrays(results, emptyArray);
                    String specie = row[0];
                    String platform = row[1];
                    String probe = row[2];
                    String swissprotAcc = row[3];
                    String uniprotId = row[4];
                    String tremblAcc = row[5];


                    /* TREMBL */
                    boolean tRedundant = false;
                    if(tremblAcc.length() > 1){
                        /* t2b */
                        if(tProt2Prob.containsKey(tremblAcc)){
                            if(tProt2Prob.get(tremblAcc).contains(probe)){
                                //redundant
                                tRedundant = true;
                            } else {
                                tProt2Prob.get(tremblAcc).add(probe);
                            }
                        } else {
                            ArrayList<String> newProbe = new ArrayList<String>();
                            newProbe.add(probe);
                            tProt2Prob.put(tremblAcc, newProbe);
                        }
                        /* b2t */
                        if(tProb2Prot.containsKey(probe)){
                            if(tProb2Prot.get(probe).contains(tremblAcc)){
                                //redundant
                                tRedundant = true;
                            } else {
                                tProb2Prot.get(probe).add(tremblAcc);
                            }
                        } else {
                            ArrayList<String> newProteinAcc = new ArrayList<String>();
                            newProteinAcc.add(tremblAcc);
                            tProb2Prot.put(probe, newProteinAcc);
                        }
                    }  else {
                        tRedundant = true;
                    }



                    /* SWISSPROT */
                    boolean sRedundant = false;
                    if(swissprotAcc.length() > 1){
                        /* t2b */
                        if(sProt2Prob.containsKey(swissprotAcc)){
                            if(sProt2Prob.get(swissprotAcc).contains(probe)){
                                //redundant
                                sRedundant = true;
                            } else {
                                sProt2Prob.get(swissprotAcc).add(probe);
                            }
                        } else {
                            ArrayList<String> newProbe = new ArrayList<String>();
                            newProbe.add(probe);
                            sProt2Prob.put(swissprotAcc, newProbe);
                        }
                        /* b2t */
                        if(sProb2Prot.containsKey(probe)){
                            if(sProb2Prot.get(probe).contains(swissprotAcc)){
                                //redundant
                                sRedundant = true;
                            } else {
                                sProb2Prot.get(probe).add(swissprotAcc);
                            }
                        } else {
                            ArrayList<String> newProteinAcc = new ArrayList<String>();
                            newProteinAcc.add(swissprotAcc);
                            sProb2Prot.put(probe, newProteinAcc);
                        }
                    } else {
                        sRedundant = true;
                    }


                    if(!tRedundant){
                    /* Write trembl file */
                        tremblOut.write(newline);
                        tremblOut.write(specie);
                        tremblOut.write(comma);
                        tremblOut.write(platform);
                        tremblOut.write(comma);
                        tremblOut.write(probe);
                        tremblOut.write(comma);
                        tremblOut.write(tremblAcc);
                    }

                    if(!sRedundant){
                        /* Write swissprot file */
                        swissprotOut.write(newline);
                        swissprotOut.write(specie);
                        swissprotOut.write(comma);
                        swissprotOut.write(platform);
                        swissprotOut.write(comma);
                        swissprotOut.write(probe);
                        swissprotOut.write(comma);
                        swissprotOut.write(swissprotAcc);
                    }
                }

                countLines++;
            }
            swissprotOut.close();
            tremblOut.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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

        /* Used to just read files */
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) { return !name.startsWith("."); }
    };

}
