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
package it.iit.genomics.cru.structures.business;

import it.iit.genomics.cru.structures.sources.PDBStructureSource;
import it.iit.genomics.cru.structures.sources.StructureSource;
import it.iit.genomics.cru.utils.maps.MapOfMap;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.asa.AsaCalculator;
import org.biojava.nbio.structure.asa.GroupAsa;

/**
 *
 * @author Arnaud Ceol
 */
public class Accessibility {

    private final static double DEFAULT_PROBE_SIZE = AsaCalculator.DEFAULT_PROBE_SIZE;
    private final static int DEFAULT_N_SPHERE_POINTS = 92; //AsaCalculator.DEFAULT_N_SPHERE_POINTS /4;
    private final static int DEFAULT_NTHREADS = AsaCalculator.DEFAULT_NTHREADS;

    /**
     *
     * @param structure
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public static MapOfMap<String, String> getContactChains(Structure structure)
            throws NumberFormatException, IOException {

        MapOfMap<String, String> contactChains = new MapOfMap<>();

        // 1. get all residues
        AsaCalculator asa = new AsaCalculator(structure,
                DEFAULT_PROBE_SIZE,
                DEFAULT_N_SPHERE_POINTS,
                DEFAULT_NTHREADS, false);

        HashMap<String, Double> complexAccessibility = new HashMap<>();

        GroupAsa[] asas = asa.getGroupAsas();

        for (GroupAsa a : asas) {
            Group g = a.getGroup();
            String key = g.getResidueNumber() + ":" + g.getChainId();
            System.out.println(key);
            complexAccessibility.put(key, a.getAsaU());
        }

        for (Chain c : structure.getChains()) {
            // remove c2

            for (Chain c2 : structure.getChains()) {
                if (c.equals(c2)) {
                    continue;
                }
                Structure s2 = new StructureImpl();

                for (Chain cc : structure.getChains()) {
                    if (false == cc.equals(c2)) {
                        s2.addChain(c2);
                    }
                }

                AsaCalculator asa2 = new AsaCalculator(s2,
                        DEFAULT_PROBE_SIZE,
                        DEFAULT_N_SPHERE_POINTS,
                        DEFAULT_NTHREADS, false);
                GroupAsa[] asas2 = asa2.getGroupAsas();

                for (GroupAsa a : asas2) {
                    Group g = a.getGroup();

                    if (c2.getChainID().equals(g.getChainId())) {
                        continue;
                    } else {
                        System.out.println("glop: " + c2.getChainID() + " / "
                                + g.getChainId());
                    }

                    String key = g.getResidueNumber() + ":" + g.getChainId();

                    double complex = complexAccessibility.get(key);
                    System.out.println(key + ": " + a.getAsaU() + " - "
                            + complex);
                    if (a.getAsaU() - complex > 1) {
                        contactChains.add(key, c2.getChainID());
                        System.out.println(key + ">" + c.getChainID());
                    } else {
                        System.out.println(key + "<" + c.getChainID());
                    }

                }
            }

        }
        return contactChains;
    }

    /**
     *
     * @param structure
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public static MapOfMap<String, String> getContactHetams(Structure structure)
            throws NumberFormatException, IOException {

        MapOfMap<String, String> contactChains = new MapOfMap<>();

        // 1. get all residues
        AsaCalculator asa = new AsaCalculator(structure,
                DEFAULT_PROBE_SIZE,
                DEFAULT_N_SPHERE_POINTS,
                DEFAULT_NTHREADS, false);

        HashMap<String, Double> complexAccessibility = new HashMap<>();

        GroupAsa[] asas = asa.getGroupAsas();

        for (GroupAsa a : asas) {
            Group g = a.getGroup();
            String key = g.getResidueNumber() + ":" + g.getChainId();
            complexAccessibility.put(key, a.getAsaU());
        }

        for (Chain c : structure.getChains()) {
            // remove c2

            for (Chain c2 : structure.getChains()) {
                if (c.equals(c2)) {
                    continue;
                }
                Structure s2 = new StructureImpl();

                for (Chain cc : structure.getChains()) {
                    if (false == cc.equals(c2)) {
                        s2.addChain(c2);
                    }
                }

                AsaCalculator asa2 = new AsaCalculator(s2,
                        DEFAULT_PROBE_SIZE,
                        DEFAULT_N_SPHERE_POINTS,
                        DEFAULT_NTHREADS, false);
                GroupAsa[] asas2 = asa2.getGroupAsas();

                for (GroupAsa a : asas2) {
                    Group g = a.getGroup();

                    if (c2.getChainID().equals(g.getChainId())) {
                        continue;
                    } else {
                        System.out.println("glop: " + c2.getChainID() + " / "
                                + g.getChainId());
                    }

                    String key = g.getResidueNumber() + ":" + g.getChainId();

                    double complex = complexAccessibility.get(key);
                    System.out.println(key + ": " + a.getAsaU() + " - "
                            + complex);
                    if (a.getAsaU() - complex > 1) {
                        contactChains.add(key, c2.getChainID());
                        System.out.println(key + ">" + c.getChainID());
                    } else {
                        System.out.println(key + "<" + c.getChainID());
                    }

                }
            }

        }
        return contactChains;
    }

    /**
     *
     * @param structure
     * @param chainNamePairs
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public static MapOfMap<String, String> getContactChains(
            Structure structure, List<String[]> chainNamePairs)
            throws NumberFormatException, IOException {

        MapOfMap<String, String> contactChains = new MapOfMap<>();

        HashMap<String, Chain> name2chain = new HashMap<>();

        for (Chain chain : structure.getChains()) {
            name2chain.put(chain.getChainID(), chain);
        }

        ArrayList<Chain[]> chainPairs = new ArrayList<>();

        for (String[] pair : chainNamePairs) {
            Chain[] chainPair = {name2chain.get(pair[0]),
                name2chain.get(pair[1])};
            chainPairs.add(chainPair);
        }

        // 1. get all residues
        AsaCalculator asa = new AsaCalculator(structure,
                DEFAULT_PROBE_SIZE,
                DEFAULT_N_SPHERE_POINTS,
                DEFAULT_NTHREADS, false);

        HashMap<String, Double> complexAccessibility = new HashMap<>();

        GroupAsa[] asas = asa.getGroupAsas();

        for (GroupAsa a : asas) {
            Group g = a.getGroup();

            String key = g.getResidueNumber() + ":" + g.getChainId();

            complexAccessibility.put(key, a.getAsaU());
        }

        for (Chain[] chainPair : chainPairs) {
            Chain c = chainPair[0];
            Chain c2 = chainPair[1];

            Structure s2 = new StructureImpl();

            for (Chain cc : structure.getChains()) {
                if (false == cc.getChainID().equals(c2.getChainID())) {
                    s2.addChain(cc);
                }
            }

            AsaCalculator asa2 = new AsaCalculator(s2,
                    DEFAULT_PROBE_SIZE,
                    DEFAULT_N_SPHERE_POINTS,
                    DEFAULT_NTHREADS, false);
            GroupAsa[] asas2 = asa2.getGroupAsas();

            for (GroupAsa a : asas2) {
                Group g = a.getGroup();

                if (false == c.getChainID().equals(g.getChainId())) {
                    continue;
                }

                String key = g.getResidueNumber() + ":" + g.getChainId();

                Double complex = complexAccessibility.get(key);
                if (a.getAsaU() - complex >= 1) {
                    contactChains.add(key, c2.getChainID());
                }

            }
        }
        return contactChains;
    }

    /**
     *
     * @param structure
     * @param chainNamePairs
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public static MapOfMap<String, String> getFasterContactChains(
            Structure structure, List<String[]> chainNamePairs)
            throws NumberFormatException, IOException {

        MapOfMap<String, String> contactChains = new MapOfMap<>();

        HashSet<Chain> chains = new HashSet<>();

        HashMap<String, Chain> name2chain = new HashMap<>();

        for (Chain chain : structure.getChains()) {
            name2chain.put(chain.getChainID(), chain);
        }

        ArrayList<Chain[]> chainPairs = new ArrayList<>();

        for (String[] pair : chainNamePairs) {
            Chain[] chainPair = {name2chain.get(pair[0]),
                name2chain.get(pair[1])};
            chainPairs.add(chainPair);

            Chain[] chainPair2 = {name2chain.get(pair[1]),
                name2chain.get(pair[0])};
            chainPairs.add(chainPair2);

            chains.add(name2chain.get(pair[0]));
            chains.add(name2chain.get(pair[1]));
        }

        // 1. get single chain accessibility
        HashMap<String, Double> singleAccessibility = new HashMap<>();

        for (Chain chain : chains) {
            Structure s2 = new StructureImpl();

            s2.addChain(chain);

            AsaCalculator asa2 = new AsaCalculator(s2,
                    DEFAULT_PROBE_SIZE,
                    DEFAULT_N_SPHERE_POINTS,
                    DEFAULT_NTHREADS, false);
            GroupAsa[] asas2 = asa2.getGroupAsas();

            for (GroupAsa a : asas2) {
                Group g = a.getGroup();
                String key = g.getResidueNumber() + ":" + g.getChainId();

                singleAccessibility.put(key, a.getAsaU());

            }
        }

        for (Chain[] chainPair : chainPairs) {
            Chain c1 = chainPair[0];
            Chain c2 = chainPair[1];

            Structure s2 = new StructureImpl();

            s2.addChain(c1);
            s2.addChain(c2);

            AsaCalculator asa2 = new AsaCalculator(s2,
                    DEFAULT_PROBE_SIZE,
                    DEFAULT_N_SPHERE_POINTS,
                    DEFAULT_NTHREADS, false);
            GroupAsa[] asas2 = asa2.getGroupAsas();

            for (GroupAsa a : asas2) {
                Group g = a.getGroup();

                if (c2.getChainID().equals(g.getChainId())) {
                    continue;
                }

                String key = g.getResidueNumber() + ":" + g.getChainId();

                Double single = singleAccessibility.get(key);

                if (single - a.getAsaU() >= 1) {
                    if (false == g.getChainId().equals(c2.getChainID())) {
                        contactChains.add(key, c2.getChainID());
                    } else if (false == g.getChainId().equals(c1.getChainID())) {
                        contactChains.add(key, c1.getChainID());

                    }
                }
            }
        }
        return contactChains;
    }

    /**
     *
     * @param structure
     * @param chainNamePairs
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public static MapOfMap<String, String> getFasterContactHETATMS(
            Structure structure, List<String[]> chainNamePairs)
            throws NumberFormatException, IOException {

        MapOfMap<String, String> contactChains = new MapOfMap<>();

        HashSet<Chain> chains = new HashSet<>();

        HashMap<String, Chain> name2chain = new HashMap<>();

        for (Chain chain : structure.getChains()) {
            name2chain.put(chain.getChainID(), chain);
        }

        ArrayList<Chain[]> chainPairs = new ArrayList<>();

        for (String[] pair : chainNamePairs) {
            Chain[] chainPair = {name2chain.get(pair[0]),
                name2chain.get(pair[1])};
            chainPairs.add(chainPair);

            chains.add(name2chain.get(pair[0]));
        }

        // 1. get single chain accessibility
        HashMap<String, Double> singleAccessibility = new HashMap<>();

        for (Chain chain : chains) {
            Structure s2 = new StructureImpl();

            s2.addChain(chain);

            AsaCalculator asa2 = new AsaCalculator(s2,
                    DEFAULT_PROBE_SIZE,
                    DEFAULT_N_SPHERE_POINTS,
                    DEFAULT_NTHREADS, true);
            GroupAsa[] asas2 = asa2.getGroupAsas();

            for (GroupAsa a : asas2) {
                Group g = a.getGroup();
                String key = g.getResidueNumber() + ":" + g.getChainId();

                singleAccessibility.put(key, a.getAsaU());

            }
        }

        for (String[] chainPair : chainNamePairs) {
            Chain c1 = name2chain.get(chainPair[0]);

            List<Group> hets = structure.getHetGroups();

            for (Group het : hets) {
                if (chainPair[1].equals(het.getChemComp().getId())) {
                    c1.getAtomGroups().remove(het);
                }
            }

            Structure s2 = new StructureImpl();

            s2.addChain(c1);

            AsaCalculator asa2 = new AsaCalculator(s2,
                    DEFAULT_PROBE_SIZE,
                    DEFAULT_N_SPHERE_POINTS,
                    DEFAULT_NTHREADS, true);
            GroupAsa[] asas2 = asa2.getGroupAsas();

            for (GroupAsa a : asas2) {
                Group g = a.getGroup();

                String key = g.getResidueNumber() + ":" + g.getChainId();

                Double single = singleAccessibility.get(key);

                if (single - a.getAsaU() < -1) {
                    contactChains.add(key, chainPair[1]);
                }
            }
        }
        return contactChains;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String pdb = "4HHB"; // "1G3N"; // A0A585-K7N5N2-EXP-4mnq.pdb1-E-0-D-0";

        StructureSource ss = new PDBStructureSource(
                "ftp://ftp.ebi.ac.uk/pub/databases/rcsb/pdb/"); // new
        // I3DStructureSource(null);
        String[] chains = {"A", "HEM"};

        ArrayList<String[]> pairs = new ArrayList<>();
        pairs.add(chains);
        java.util.Date date = new java.util.Date();
        System.out.println(new Timestamp(date.getTime()));

        Structure s = ss.getStructure(pdb);
        date = new java.util.Date();
        System.out.println(new Timestamp(date.getTime()));
        System.out.println("get asa");
        // System.out.println("name: " +s);
        MapOfMap<String, String> acc2 = Accessibility.getFasterContactHETATMS(
                s, pairs);
        System.out.println("contacts: " + acc2.keySet().size());
        date = new java.util.Date();
        System.out.println(new Timestamp(date.getTime()));

    }

}
