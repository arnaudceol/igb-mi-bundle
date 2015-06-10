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

import it.iit.genomics.cru.structures.bridges.eppic.model.EppicAnalysisList;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aceol
 */
public class EppicJaxbClient {

    static final Logger logger = LoggerFactory.getLogger(EppicJaxbClient.class);

    /**
     *
     */
    public final static String eppicUrl = "http://www.eppic-web.org/ewui/ewui/dataDownload?type=xml&id=";

    private final String localPath;

    /**
     *
     * @param localPath
     */
    public EppicJaxbClient(String localPath) {
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

            return getPdbInterfaces(localName);

        } catch (Exception e) {
            logger.error("Cannot download EPPIC analysis for PDB " + pdbId, e);
            return new EppicAnalysisList();
        }

    }

    private EppicAnalysisList getPdbInterfaces(String fileName) {
        try {

            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            XMLStreamReader xmler = xmlif.createXMLStreamReader(new FileReader(fileName));

            JAXBContext jaxbContext = JAXBContext.newInstance(
                    EppicAnalysisList.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            EppicAnalysisList result;
            result = (EppicAnalysisList) jaxbUnmarshaller
                    .unmarshal(xmler);
            return result;
        } catch (JAXBException | IOException | XMLStreamException e) {
            logger.error(null, e);
        }
        return null;
    }

}
