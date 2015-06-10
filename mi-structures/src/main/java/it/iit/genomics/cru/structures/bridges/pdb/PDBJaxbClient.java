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

import it.iit.genomics.cru.structures.bridges.pdb.model.MoleculeDescription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 */
public class PDBJaxbClient {

    private static final Logger logger = LoggerFactory.getLogger(PDBJaxbClient.class);
    
    /**
     *
     */
    public final static String pdbUrl = "http://www.rcsb.org/pdb/rest/";

    /**
     *
     * @param pdbId
     * @return
     */
    public MoleculeDescription getDescription(String pdbId) {
        try {
            URL url = new URL(pdbUrl + "/describeMol?structureId=" + pdbId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            br.readLine();

            String apiOutput;
            String xml = "";
            while ((apiOutput = br.readLine()) != null) {
                xml = xml + apiOutput;
            }

            conn.disconnect();

            JAXBContext jaxbContext = JAXBContext
                    .newInstance(it.iit.genomics.cru.structures.bridges.pdb.model.MoleculeDescription.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.unmarshal(new StringReader(xml));
            MoleculeDescription result = (MoleculeDescription) jaxbUnmarshaller
                    .unmarshal(new StringReader(xml));
            return result;
        } catch (IOException | JAXBException e) {
            logger.error( null, e);
        }

        return null;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        String[] pdbId = {"1CO1"};

        PDBJaxbClient client = new PDBJaxbClient();

        for (String pdb : pdbId) {
            MoleculeDescription mol = client.getDescription(pdb);

//			System.out.println(mol.getStructureId().getId());
//
//			for (Polymer polymer : mol.getStructureId().getPolymers()) {
//
//				System.out.println("  " + polymer.getType());
//			}
        }

    }

}
