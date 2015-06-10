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
package it.iit.genomics.cru.igb.bundles.mi.query;

import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 *
 * @author Arnaud Ceol
 *
 * Abstract class to manage a Query.
 *
 */
public class AbstractMIQuery {

    /* Search Type */
    public enum QueryType {
        INTRA, EXTRA
    };

    /* Queries */
    protected QueryType queryType = QueryType.EXTRA;

    /* Tracks */
    protected boolean createTracks = false;

    /* Structure */
    protected boolean searchDsysmap = false;
    protected boolean searchEPPIC = false;
    protected boolean searchInteractome3D = true;
    protected boolean searchPDB = false;
    protected boolean searchPDBLocal = false;
    protected boolean searchUserStructures = false;
    protected boolean searchModifications = false;

    protected boolean searchLigands;

    protected boolean searchNucleicAcid;
    
    protected boolean searchProteinStructures = false;

    protected String pdbURL;

    public String getPdbURL() {
        return pdbURL;
    }


    /* Psicquic */
    protected String psiquicServer = PsicquicUtils.getInstance().getUrl(PsicquicUtils.defaultProvider);

    /* Taxid */
    protected String taxid;

    protected String species;

    /* Genome */
    List<String> sequences = new ArrayList<>();

    /* Selected Symmetries */
    protected ArrayList<SeqSymmetry> selectedSymmetries;

    /* 
	 * Show tracks: better not doing it if two many 
	 * This one is not defined by the user by default but change according to the number of results.
	 * The user has the possibility to show them later. 
     */
    private boolean showTracks = true;

    public boolean showTracks() {
        return showTracks;
    }

    public boolean createTracks() {
        return createTracks;
    }

    public void setShowTracks(boolean showTracks) {
        this.showTracks = showTracks;
    }

    public Collection<SeqSymmetry> getSelectedSymmetries() {
        return selectedSymmetries;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public boolean searchInteractome3D() {
        return searchInteractome3D;
    }

    public boolean searchDSysMap() {
        return searchDsysmap;
    }
    
    public boolean searchEPPIC() {
        return searchEPPIC;
    }
    
    
    public boolean searchPDB() {
        return searchPDB;
    }

    public boolean searchPDBLocal() {
        return searchPDBLocal;
    }

    public boolean searchUserStructures() {
        return searchUserStructures;
    }

    public boolean searchProteinStructures() {
        return searchProteinStructures;
    }

    public boolean searchLigands() {
        return searchLigands;
    }

    public boolean searchNucleicAcid() {
        return searchNucleicAcid;
    }
 
    public boolean searchModifications() {
        return searchModifications;
    }

    public String getPsiquicServer() {
        return psiquicServer;
    }

    public String getTaxid() {
        return taxid;
    }

    public String getSpecies() {
        return species;
    }

    public List<String> getSequences() {
        return sequences;
    }

}
