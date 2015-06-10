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
package it.iit.genomics.cru.structures.bridges.eppic.client;

import it.iit.genomics.cru.structures.bridges.eppic.model.EppicAnalysis;
import it.iit.genomics.cru.structures.bridges.eppic.model.EppicAnalysisList;
import it.iit.genomics.cru.structures.bridges.eppic.model.Interface;
import it.iit.genomics.cru.structures.bridges.eppic.model.InterfaceCluster;
import it.iit.genomics.cru.structures.bridges.eppic.model.InterfaceScore;
import it.iit.genomics.cru.structures.bridges.eppic.model.Residue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author aceol
 */
@Deprecated
public class EppicWSClient {

    /**
     *
     */
    public final static Logger logger = LoggerFactory.getLogger(EppicWSClient.class);

    /**
     *
     */
    public final static String eppicUrl = "http://www.eppic-web.org/ewui/ewui/dataDownload?type=xml&id=";

    private final String localPath;

    /**
     *
     * @param localPath
     */
    public EppicWSClient(String localPath) {
        this.localPath = localPath;
    }

    /**
     *
     * @param pdbId
     * @return
     */
    public EppicAnalysisList retrievePDB(String pdbId) {
        String localName = localPath + pdbId + ".xml";
        // is it in local ?

        try {

            File file = new File(localName);
            if (false == file.exists()) {
                // Download it
                URL url = new URL(eppicUrl + pdbId);

                FileUtils.copyURLToFile(url, file);

            }

            return parse(new FileInputStream(localName));

        } catch (Exception e) {
            logger.error("Cannot download EPPIC analysis for PDB " + pdbId, e);
            return new EppicAnalysisList();
        }

    }

    private EppicAnalysisList parse(InputStream is) {

        EppicAnalysisList result = new EppicAnalysisList();

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
            NodeList analysisList = doc.getElementsByTagName("eppicAnalysis");

            for (int i = 0; i < analysisList.getLength(); i++) {

                Element analysisElement = (Element) analysisList.item(i);

                EppicAnalysis analysis = new EppicAnalysis();

                InterfaceCluster cluster = new InterfaceCluster();

                analysis.getInterfaceClusters().add(cluster);

                result.getEppicAnalysis().add(analysis);

                analysis.setPdbCode(analysisElement.getElementsByTagName("pdbCode").item(0).getFirstChild().getNodeValue());

                NodeList interfaceClusterList = analysisElement.getElementsByTagName("interfaceCluster");

                for (int iCluster = 0; iCluster < interfaceClusterList.getLength(); iCluster++) {
                    Element interfaceClusterElement = (Element) interfaceClusterList.item(iCluster);

                    NodeList interfaceList = interfaceClusterElement.getElementsByTagName("interface");

                    for (int iInterface = 0; iInterface < interfaceList.getLength(); iInterface++) {
                        Element interfaceElement = (Element) interfaceList.item(iInterface);
                        Interface eppicInterface = new Interface();

                        cluster.getInterfaces().add(eppicInterface);

                        eppicInterface.setChain1(interfaceElement.getElementsByTagName("chain1").item(0).getFirstChild().getNodeValue());
                        eppicInterface.setChain2(interfaceElement.getElementsByTagName("chain2").item(0).getFirstChild().getNodeValue());

                        NodeList scoreList = interfaceElement.getElementsByTagName("interfaceScore");

                        for (int iScore = 0; iScore < scoreList.getLength(); iScore++) {

                            Element scoreElement = (Element) scoreList.item(iScore);

                            InterfaceScore score = new InterfaceScore();

                            score.setCallName(scoreElement.getElementsByTagName("callName").item(0).getFirstChild().getNodeValue());
                            score.setMethod(scoreElement.getElementsByTagName("method").item(0).getFirstChild().getNodeValue());
                            eppicInterface.getInterfaceScores().add(score);
                        }

                        NodeList residueList = interfaceElement.getElementsByTagName("residue");

                        for (int iResidue = 0; iResidue < residueList.getLength(); iResidue++) {

                            Element residueElement = (Element) residueList.item(iResidue);

                            Residue residue = new Residue();

                            residue.setAsa(Double.parseDouble(residueElement.getElementsByTagName("asa").item(0).getFirstChild().getNodeValue()));
                            residue.setBsa(Double.parseDouble(residueElement.getElementsByTagName("bsa").item(0).getFirstChild().getNodeValue()));
                            residue.setBsaPercentage(Double.parseDouble(residueElement.getElementsByTagName("bsaPercentage").item(0).getFirstChild().getNodeValue()));
                            residue.setEntropyScore(Double.parseDouble(residueElement.getElementsByTagName("entropyScore").item(0).getFirstChild().getNodeValue()));
                            residue.setPdbResidueNumber(Integer.parseInt(residueElement.getElementsByTagName("residueNumber").item(0).getFirstChild().getNodeValue()));
                            residue.setSide(Integer.parseInt(residueElement.getElementsByTagName("side").item(0).getFirstChild().getNodeValue()));
                            residue.setPdbResidueNumber(Integer.parseInt(residueElement.getElementsByTagName("pdbResidueNumber").item(0).getFirstChild().getNodeValue()));

                            eppicInterface.getResidues().add(residue);
                        }

                    }

                }

            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | NumberFormatException e) {
//            logger.error( "Failed to parse mutations from " + dsysmapUrl, e);
        }

        return result;
    }

}
