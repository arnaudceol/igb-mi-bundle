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
import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicUtils;

import java.util.Collection;
import java.util.List;

import java.util.ArrayList;

/**
 *
 * @author Arnaud Ceol
 *
 * This class allow to store the parameters for a query. Once the user starts
 * the query, this class create an instance of a query that can be stored with
 * the result of the framework, while the current configuration can be modified
 * to run a new query.
 *
 */
public class MIQueryManager extends AbstractMIQuery {

    private static MIQueryManager instance;

    private final IGBLogger igbLogger;
    
    private MIQueryManager() {
        this.selectedSymmetries = new ArrayList<>();
        igbLogger = IGBLogger.getMainInstance();
    }

    public static MIQueryManager getInstance() {
        if (instance == null) {
            instance = new MIQueryManager();
        }
        return instance;
    }

    public MIQuery getMIQuery() {
        return new MIQuery(selectedSymmetries, queryType, createTracks, searchPDB, searchInteractome3D, searchDsysmap, searchEPPIC, searchPDBLocal, searchUserStructures,
                searchProteinStructures, searchNucleicAcid, searchLigands, searchModifications,
                psiquicServer, taxid, species, sequences);
    }

    public void setCreateTracks(boolean createNewTracks) {
        this.createTracks = createNewTracks;
    }

    public void setPsiquicServer(String psiquicServerName) {
        this.psiquicServer = PsicquicUtils.getInstance().getUrl(psiquicServerName);
    }

    public void setPdbURL(String pdbURL) {
        this.pdbURL = pdbURL;
    }

    public void setQueryType(QueryType queryType) {
        igbLogger.info("Set query type: " + queryType);
        this.queryType = queryType;
    }

    public void setTaxid(String taxid) {
        this.taxid = taxid;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setSequences(List<String> sequences) {
        this.sequences = sequences;
    }

    public void setSearchInteractome3D(boolean searchInteractome3D) {
        igbLogger.info("Search strutures in Interactome3D: " + searchInteractome3D);
        this.searchInteractome3D = searchInteractome3D;
    }

    public void setSearchDSysMap(boolean searchDSysMap) {
        igbLogger.info("Search strutures and contacts in DSysMap: " + searchDSysMap);
        this.searchDsysmap = searchDSysMap;
    }
    
    public void setSearchEPPIC(boolean searchEPPIC) {
        igbLogger.info("Search strutures and contacts in EPPIC: " + searchEPPIC);
        this.searchEPPIC = searchEPPIC;
    }
    
    public void setSearchPDB(boolean searchPDB) {
        igbLogger.info("Search strutures in PDB: " + searchPDB);
        this.searchPDB = searchPDB;
    }

    public void setSearchPDBLocal(boolean searchPDBLocal) {
        igbLogger.info("Search strutures in PDB local mirror: " + searchPDBLocal);
        this.searchPDBLocal = searchPDBLocal;
    }

    public void setSearchUserStructures(boolean searchUserStructures) {
        igbLogger.info("Search local strutures: " + searchUserStructures);
        this.searchUserStructures = searchUserStructures;
    }

    public void setSearchProteinStructures(boolean searchProteinStructures) {
        igbLogger.info("Search protein structures: " + searchProteinStructures);
        this.searchProteinStructures = searchProteinStructures;
    }

    public void setSelectedSymmetries(Collection<SeqSymmetry> selectedSymmetries) {
        this.selectedSymmetries.clear();
        this.selectedSymmetries.addAll(selectedSymmetries);
    }

    public void setSearchNucleicAcid(boolean selected) {
        igbLogger.info("Search RNA/DNA: " + selected);
        this.searchNucleicAcid = selected;
    }

    public void setSearchLigands(boolean selected) {
        igbLogger.info("Search ligands: " + selected);
        this.searchLigands = selected;
    }

    public void setSearchModifications(boolean searchModifications) {
        igbLogger.info("Search modifications: " + searchModifications);
        this.searchModifications = searchModifications;
    }
    
}
