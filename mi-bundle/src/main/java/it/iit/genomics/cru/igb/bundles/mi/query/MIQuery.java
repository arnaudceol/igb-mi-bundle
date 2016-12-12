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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

import it.iit.genomics.cru.igb.bundles.mi.commons.MIBundleConfiguration;
import it.iit.genomics.cru.igb.bundles.mi.commons.MICommons;

/**
 *
 * @author Arnaud Ceol
 *
 * This class contains all the parameters for the query that should be run.
 *
 */
public class MIQuery extends AbstractMIQuery {

    private final String label;

    private String userStructuresPath = null;
    private String pdbMirrorPath = null;
    private String i3dMirrorPath = null;

    public MIQuery() {
        super();
        label = "test";
    }
    
	// public LocalRepository getUserRepository() {
    // return userRepository;
    // }
    public MIQuery(Collection<SeqSymmetry> selection, QueryType queryType,
            boolean createTracks, boolean searchPDB, boolean searchInteractome3D, boolean searchDsysmap, boolean searchEPPIC, boolean searchPDBLocal, boolean searchUserStructures,
            boolean searchProteinStructures,  boolean searchPPI,boolean searchNucleicAcid, boolean searchLigands, boolean searchModifications,
            String psiquicServer, String taxid, String species,
            List<String> sequenceNames) {
        super();
        this.label = "MI-" + MICommons.getInstance().nextQueryIndex();

        this.selectedSymmetries = new ArrayList<>();
        
        if (selection != null) {
            this.selectedSymmetries.addAll(selection);
        } 
        
        this.queryType = queryType;
        this.createTracks = createTracks;        
        this.searchInteractome3D = searchInteractome3D;
        this.searchDsysmap = searchDsysmap;
        this.searchEPPIC = searchEPPIC;
        this.searchPDB = searchPDB;
        this.searchPDBLocal = searchPDBLocal;
        this.searchUserStructures = searchUserStructures;

        this.searchPPI = searchPPI;
        this.searchNucleicAcid = searchNucleicAcid;
        this.searchLigands = searchLigands;

        this.searchModifications = searchModifications;
        
        this.searchProteinStructures = searchProteinStructures;
        this.psiquicServer = psiquicServer;
        this.taxid = taxid;
        this.species = species;
        this.sequences = sequenceNames;

        // init user repository
        pdbURL = MIBundleConfiguration.getInstance().getPdbURL();
        userStructuresPath = MIBundleConfiguration.getInstance()
                .getUserStructuresDirectory();
        pdbMirrorPath = MIBundleConfiguration.getInstance().getPdbLocalMirror();
        i3dMirrorPath = MIBundleConfiguration.getInstance().getI3DStructuresDirectory();
    }

    public String getUserStructuresPath() {
        return userStructuresPath;
    }

    public String getPdbMirrorPath() {
        return pdbMirrorPath;
    }

    public String getI3dMirrorPathw() {
        return i3dMirrorPath;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "MIQuery [queryType=" + queryType + ", searchInteractome3D="
                + searchInteractome3D+ ", searchDSysMap="
                + searchDsysmap + ", searchPDB=" + searchPDB
                + ", searchLocalStructures="
                + searchUserStructures + ", searchProteinStructures=" + searchProteinStructures
                + ", psiquicServer=" + psiquicServer + ", taxid=" + taxid
                + ", number of symmetries=" + selectedSymmetries.size() + "]";
    }

}
