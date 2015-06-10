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
package it.iit.genomics.cru.structures.bridges.pdb;

import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.pdb.model.Chain;
import it.iit.genomics.cru.structures.bridges.pdb.model.Ligand;
import it.iit.genomics.cru.structures.bridges.pdb.model.MacroMolecule;
import it.iit.genomics.cru.structures.bridges.pdb.model.MoleculeDescription;
import it.iit.genomics.cru.structures.bridges.pdb.model.Polymer;
import it.iit.genomics.cru.structures.bridges.pdb.model.PolymerDescription;
import it.iit.genomics.cru.structures.bridges.pdb.model.StructureID;
import it.iit.genomics.cru.structures.bridges.pdb.model.Taxonomy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Arnaud Ceol
 */
public class PDBWSClient {

    /**
     *
     */
    public final static String pdbUrl = "http://www.rcsb.org/pdb/rest/";

    static final Logger logger = LoggerFactory.getLogger(PDBWSClient.class);

    /**
     *
     * @param pdbIds
     * @return
     * @throws BridgesRemoteAccessException
     */
    public MoleculeDescription getDescription(Collection<String> pdbIds) throws BridgesRemoteAccessException {
        try {
            URL url = new URL(pdbUrl + "/describeMol?structureId="
                    + StringUtils.join(pdbIds, ","));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                throw new BridgesRemoteAccessException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            MoleculeDescription result = parseMoleculeDescription(conn
                    .getInputStream());

            conn.disconnect();

            return result;
        } catch (IOException e) {
            logger.error( "Fail to get PDB files for {0}", StringUtils.join(pdbIds, ", there may be a network problem."));
            throw new BridgesRemoteAccessException("Failed access to PDB REST service");
        }
    }

    /**
     *
     * @param pdbIds
     * @return
     * @throws BridgesRemoteAccessException
     */
    public Collection<Ligand> getLigands(Collection<String> pdbIds) throws BridgesRemoteAccessException{
        try {

            // get ligands
            URL urlLigand = new URL(pdbUrl + "ligandInfo?structureId="
                    + StringUtils.join(pdbIds, ","));

            HttpURLConnection connLigand = (HttpURLConnection) urlLigand
                    .openConnection();
            connLigand.setRequestMethod("GET");
            connLigand.setRequestProperty("Accept", "application/xml");

            if (connLigand.getResponseCode() != 200) {
                throw new BridgesRemoteAccessException("Failed : HTTP error code : "
                        + connLigand.getResponseCode() + " " + urlLigand);
            }

            Collection<Ligand> ligands = parseLigands(connLigand
                    .getInputStream());

            connLigand.disconnect();
            return ligands;
        } catch (IOException e) {
            logger.error( "Fail to get ligands for " + StringUtils.join(pdbIds, ", "), e);
            throw new BridgesRemoteAccessException("Failed access to PDB REST service");
        }
    }

    /**
     *
     * @param is
     * @return
     */
    public MoleculeDescription parseMoleculeDescription(InputStream is) {

        MoleculeDescription result = new MoleculeDescription();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // interaction structure
            NodeList structureIds = doc.getElementsByTagName("structureId");

            for (int iStruct = 0; iStruct < structureIds.getLength(); iStruct++) {
                Element structureIdNode = (Element) structureIds.item(iStruct);

                StructureID structureId = new StructureID();

                structureId.setId(structureIdNode.getAttribute("id"));

                ArrayList<Polymer> polymers = new ArrayList<>();

                NodeList polymerList = structureIdNode.getElementsByTagName("polymer");

                for (int iPolymer = 0; iPolymer < polymerList.getLength(); iPolymer++) {

                    Element polymerElement = (Element) polymerList
                            .item(iPolymer);

                    Polymer polymer = new Polymer();

                    polymer.setEntityNr(Integer.parseInt(polymerElement
                            .getAttribute("entityNr")));
                    polymer.setLength(Integer.parseInt(polymerElement
                            .getAttribute("length")));
                    polymer.setType(polymerElement.getAttribute("type"));
                    polymer.setWeight(Double.parseDouble(polymerElement
                            .getAttribute("weight")));

                    PolymerDescription description = new PolymerDescription();
                    description
                            .setDescription(((Element) polymerElement
                                    .getElementsByTagName("polymerDescription")
                                    .item(0)).getAttribute("description"));

                    polymer.setPolymerDescription(description);

                    Element taxonomyElement = (Element) polymerElement
                            .getElementsByTagName("Taxonomy").item(0);

                    if (null != taxonomyElement) {
                        Taxonomy taxonomy = new Taxonomy();

                        taxonomy.setId(taxonomyElement.getAttribute("id"));
                        taxonomy.setName(taxonomyElement.getAttribute("name"));
                        polymer.setTaxonomy(taxonomy);
                    }

                    NodeList chainsList = polymerElement
                            .getElementsByTagName("chain");
                    for (int iChain = 0; iChain < chainsList.getLength(); iChain++) {

                        Element chainElement = (Element) chainsList
                                .item(iChain);
                        Chain chain = new Chain();
                        chain.setId(chainElement.getAttribute("id"));
                        polymer.getChains().add(chain);
                    }

                    MacroMolecule macroMolecule = new MacroMolecule();

                    NodeList macroMoleculeList = polymerElement
                            .getElementsByTagName("macroMolecule");

                    if (macroMoleculeList.getLength() > 0) {
                        Element mmElement = (Element) macroMoleculeList.item(0);
                        macroMolecule.setName(pdbUrl);

                        NodeList accList = mmElement
                                .getElementsByTagName("accession");
                        for (int iAcc = 0; iAcc < accList.getLength(); iAcc++) {

                            Element accElement = (Element) accList
                                    .item(iAcc);
                            macroMolecule.getAccession().add(accElement.getAttribute("id"));
                        }

                        polymer.setMacromolecule(macroMolecule);

                    }

                    polymers.add(polymer);
                }

                structureId.setPolymers(polymers);
                result.getStructureId().add(structureId);
            }
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            logger.error( "Fail to parse Molecule", e);
        }

        return result;
    }

    /**
     *
     * @param is
     * @return
     */
    public Collection<Ligand> parseLigands(InputStream is) {

        ArrayList<Ligand> result = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList ligandList = doc.getElementsByTagName("ligand");

            for (int iLigand = 0; iLigand < ligandList.getLength(); iLigand++) {

                Element ligandElement = (Element) ligandList.item(iLigand);

                Ligand ligand = new Ligand();

                ligand.setStructureId(ligandElement.getAttribute("structureId"));
                ligand.setChemicalId(ligandElement.getAttribute("chemicalID"));
                ligand.setType(ligandElement.getAttribute("type"));
                ligand.setMolecularWeight(Double.parseDouble(ligandElement
                        .getAttribute("molecularWeight")));

                NodeList nodeList = ligandElement.getElementsByTagName("chemicalName");

                if (null != nodeList && nodeList.getLength() > 0) {
                    ligand.setChemicalName(((Element) nodeList.item(0))
                            .getTextContent());
                }

                nodeList = ligandElement.getElementsByTagName("formula");
                if (null != nodeList && nodeList.getLength() > 0) {
                    ligand.setFormula(((Element) nodeList.item(0))
                            .getTextContent());
                }

                nodeList = ligandElement.getElementsByTagName("InChI");
                if (null != nodeList && nodeList.getLength() > 0) {

                    ligand.setInChI(((Element) nodeList.item(0))
                            .getTextContent());
                }

                nodeList = ligandElement.getElementsByTagName("InChIKey");
                if (null != nodeList && nodeList.getLength() > 0) {

                    ligand.setInChIKey(((Element) nodeList.item(0))
                            .getTextContent());
                }

                nodeList = ligandElement.getElementsByTagName("smiles");
                if (null != nodeList && nodeList.getLength() > 0) {

                    ligand.setSmiles(((Element) nodeList.item(0))
                            .getTextContent());
                }
                result.add(ligand);
            }
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException | DOMException e) {
            logger.error( "Fail to parse ligand", e);
        }

        return result;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        String[] pdbId = {"1NKP", "4HHB", "1K1K"};

        ArrayList<String> pdbIds = new ArrayList<>();

        PDBWSClient client = new PDBWSClient();

        pdbIds.addAll(Arrays.asList(pdbId));

        for (StructureID structureId : client.getDescription(pdbIds)
                .getStructureId()) {

            System.out.println(structureId.getId());

            for (Polymer polymer : structureId.getPolymers()) {

                System.out.println(structureId.getId() + "  Polymer  "
                        + polymer.getType());

                for (Chain chain : polymer.getChains()) {
                    System.out.println("   chain  " + chain.getId());
                }
            }
        }
        for (Ligand ligand : client.getLigands(pdbIds)) {

            System.out.println(ligand.getStructureId() + "   Ligand  "
                    + ligand.getType() + ", " + ligand.getChemicalId());
        }

    }

}
