/**
 * *****************************************************************************
 * Copyright 2014 Fondazione Istituto Italiano di Tecnologia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *****************************************************************************
 */
package it.iit.genomics.cru.structures.bridges.bridges;

import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;
import it.iit.genomics.cru.utils.maps.MapOfMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.lang.StringUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Arnaud Ceol
 * @date 01/mar/2013
 * @time 15:56:24
 */
public class UniprotkbUtilsTest {

    @Test
    public void testGetUniprotAcFromGene() throws BridgesRemoteAccessException {

        String[] genes = {"kefC", "yliI"};

        ArrayList<String> geneCollection = new ArrayList<>();
        geneCollection.addAll(Arrays.asList(genes));

        MapOfMap<String, MoleculeEntry> entries = UniprotkbUtils.getInstance("83333").getUniprotEntriesFromGenes(geneCollection);

        Assert.assertEquals(genes.length, entries.keySet().size());

        for (String gene : genes) {            
            System.out.println("organism: " + entries.get(gene).iterator().next().getOrganism());
            Assert.assertEquals(1, entries.get(gene).size());
        }
    }

    
    public void testGetGenesFromUniprotAcs() throws BridgesRemoteAccessException {

        String[] uniprotAcs = {"Q9NUR3", "P52803", "P02765", "Q9BX66", "P29074", "Q9ULD4"};

        ArrayList<String> uniprotAcsCollection = new ArrayList<>();
        uniprotAcsCollection.addAll(Arrays.asList(uniprotAcs));

        HashMap<String, MoleculeEntry> entries = UniprotkbUtils.getInstance("9606").getUniprotEntriesFromUniprotAccessions(uniprotAcsCollection);

        Assert.assertEquals(uniprotAcs.length, entries.keySet().size());
    }

    
    public void testGetUniprotEntriesFromGenes() throws BridgesRemoteAccessException {

        ArrayList<String> genes = new ArrayList<>();
        genes.add("leuS");
        MapOfMap<String, MoleculeEntry> entries = UniprotkbUtils.getInstance("83333").getUniprotEntriesFromGenes(genes);

        Assert.assertEquals(1, entries.keySet().size());
    }

//    
//    public void testGetUniprotEntriesFromPdb() {
//
//        Collection<String> acs = UniprotkbUtils.getInstance("9606").getProteinsInStructures("3PDO");
//
//        Assert.assertEquals(3, acs.size());
//    }

    
    public void testIsUniprotAcs() {
        Assert.assertTrue(UniprotkbUtils.isUniprotAc("P12345") && UniprotkbUtils.isUniprotAc("P123X5-12") && false == UniprotkbUtils.isUniprotAc("CHEBI:1223") && false == UniprotkbUtils.isUniprotAc("P123X5-12-3"));
    }

    
    public void testGetSpeciesFromName() throws BridgesRemoteAccessException {
        Assert.assertTrue(UniprotkbUtils.getSpeciesFromName("Homo sapiens").size() > 0);
    }

    
    public void testVarSplice() throws BridgesRemoteAccessException {
        String uniprotAc = "O14746";
        
        String[] uniprotAcs = {uniprotAc};

        ArrayList<String> uniprotAcsCollection = new ArrayList<>();
        uniprotAcsCollection.addAll(Arrays.asList(uniprotAcs));

        HashMap<String, MoleculeEntry> entries = UniprotkbUtils.getInstance("9606").getUniprotEntriesFromUniprotAccessions(uniprotAcsCollection);
System.out.println(entries.get(uniprotAc).getVarSpliceAC("NM_198253"));
//System.out.println(entries.get(uniprotAc).getVarSpliceAC("NM_001145339.2")); 

        HashSet<String> geneIds = new HashSet<>();
               geneIds.addAll(entries.get(uniprotAc).getGeneNames());
                geneIds.addAll(entries.get(uniprotAc).getRefseqs());
                geneIds.addAll(entries.get(uniprotAc).getEnsemblGenes());
        System.out.println("GeneIds: " + StringUtils.join(geneIds, ", "));
        
System.out.println(entries.get(uniprotAc).getVarSpliceAC("NM_198253"));        
System.out.println(entries.get(uniprotAc).getSequence("NM_198253"));


        Assert.assertEquals(uniprotAc, entries.get(uniprotAc).getVarSpliceAC("NM_198253"));
        Assert.assertEquals(uniprotAc, entries.get(uniprotAc).getSequence("NM_198253").getSequence());
        
        
    }
    
    
}
