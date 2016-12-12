/* 
 * Copyright 2015 Fondazione Istituto Italiano di Tecnologia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.igb.bundles.mi.model;

import java.awt.Color;
import java.util.HashMap;

import it.iit.genomics.cru.structures.model.MoleculeEntry;

/**
 * Use to assign the same color to nodes according to their species 
 * in all the bundle.
 * @author Arnaud Ceol
 */
public class TaxonColorer {
    
    private static final HashMap<String, TaxonColorer> colorers = new HashMap<>();
    
    private final static Color MAIN_SPECIES = Color.green;
    private final static Color OTHER_SPECIES = Color.ORANGE;
    private final static Color NUCLEIC_ACID = Color.MAGENTA;
    private final static Color LIGAND = new Color(218,165,32);
    private final static Color MODIFICATION = new Color(255,158,0);
    
    
    /**
     * The species associated to a query. Other species will be associated to a
     * different color.
     */
    private final String mainSpecies;
    
    private TaxonColorer(String mainTaxon) {
        this.mainSpecies = mainTaxon;
    }
    
    public static TaxonColorer getColorer(String species) {
        if (colorers.containsKey(species)) {
            return colorers.get(species);
        }
        
        TaxonColorer colorer = new TaxonColorer(species);
        colorers.put(species, colorer);
        return colorer;
        
    }
    
    public Color getColor(String species) {
        
        if (species == null){
            return OTHER_SPECIES;
        }
        
        if (species.equals(mainSpecies)) {
                return MAIN_SPECIES;
        }
        
        switch (species) {
            case MoleculeEntry.TAXID_DNA:
                // same as RNA
            case MoleculeEntry.TAXID_RNA:
                 return NUCLEIC_ACID;
            case MoleculeEntry.TAXID_LIGAND:
                return LIGAND;
            case MoleculeEntry.TAXID_MODIFICATION:
                return MODIFICATION;
            default:
                return OTHER_SPECIES;
        }
    }
    
}
