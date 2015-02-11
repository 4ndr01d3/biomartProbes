package uk.ac.ebi.enfin.biomart.probes;

import java.util.List;
import java.util.Map;

/**
 * User: rafael
 * Date: 13-Sep-2010
 * Time: 17:16:54
 */ 
public class SpecieMapping {
    private String specie;
    private String dataset;
    private Map<String,Long> platforms;
    public SpecieMapping(String specieName, String dataset, Map<String,Long> platforms) {
        this.specie = specieName;
        this.dataset = dataset;
        this.platforms = platforms;
    }

    public String getSpecie() {
        return specie;
    }

    public String getDataset() {
        return dataset;
    }

    public Map<String,Long> getPlatforms() {
        return platforms;
    }
}
