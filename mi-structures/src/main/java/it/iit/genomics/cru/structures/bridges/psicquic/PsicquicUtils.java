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
package it.iit.genomics.cru.structures.bridges.psicquic;

/**
 * @author Arnaud Ceol
 *
 * Utilities for accessing PSICQUIC data
 *
 */
import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;
import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import psidev.psi.mi.tab.PsimiTabException;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;

/**
 *
 * @author Arnaud Ceol
 */
public class PsicquicUtils {
    
    /**
     *
     */
    public final static String UNSPECIFIED_TERM = "unspecified";
    
    private static final Logger logger = LoggerFactory.getLogger(PsicquicUtils.class);

    /**
     *
     */
    public final static String defaultProvider = "intact";

    /**
     *
     */
    public final static String registryUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=STATUS&format=xml"; //"http://37.34.38.126:8080/psicquic-registry/registry?action=STATUS&format=xml";// "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=STATUS&format=xml";

    private final HashMap<String, String> psicquicUrls = new HashMap<>();

    private static PsicquicUtils instance;

    private PsicquicUtils() {
        super();
    }

    /**
     *
     * @return
     */
    public static PsicquicUtils getInstance() {
        if (instance == null) {
            instance = new PsicquicUtils();
        }

        return instance;
    }

    /**
     *
     * @return
     */
    public String[] getProviderNames() {
        String[] providers = psicquicUrls.keySet().toArray(
                new String[psicquicUrls.keySet().size()]);
        Arrays.sort(providers);

        return providers;
    }

    /**
     *
     * @param name
     * @return
     */
    public String getUrl(String name) {
        return psicquicUrls.get(name);
    }

    /**
     *
     * @param name
     * @param url
     */
    public void addUrl(String name, String url) {
        psicquicUrls.put(name, url);
    }

    /**
     *
     * @param url
     * @return
     */
    public String getServerName(String url) {
        for (String name : psicquicUrls.keySet()) {
            if (psicquicUrls.get(name).equals(url)) {
                return name;
            }
        }
        return null;
    }

    /**
     *
     * @param psicquicUrl
     * @param id
     * @return
     * @throws BridgesRemoteAccessException
     */
    public Collection<Interaction> getInteractors(String psicquicUrl, String id) throws BridgesRemoteAccessException {

        PsicquicClient client = new PsicquicClient(psicquicUrl);
        PsimiTabReader mitabReader = new PsimiTabReader();

        // key = ac1#ac2
        HashMap<String, Interaction> interactions = new HashMap<>();

        try {
            if (client.countByInteractor(id) == 0) {
                return interactions.values();
            }

            InputStream searchResult = client.getByInteractor(id);

            Collection<BinaryInteraction> binaryInteractions = mitabReader
                    .read(searchResult);

            for (BinaryInteraction interaction : binaryInteractions) {

                String xref1 = null;
                String xref2 = null;

                if (null == interaction.getInteractorA()) {
                    continue;
                }

                if (null == interaction.getInteractorB()) {
                    continue;
                }

                for (CrossReference xref : interaction.getInteractorA()
                        .getIdentifiers()) {
                    if (UniprotkbUtils.isUniprotAc(xref.getIdentifier())) {
                        xref1 = xref.getIdentifier();
                        break;
                    }
                }

                for (CrossReference xref : interaction.getInteractorB()
                        .getIdentifiers()) {
                    if (UniprotkbUtils.isUniprotAc(xref.getIdentifier())) {
                        xref2 = xref.getIdentifier();
                        break;
                    }
                }

                if (xref1 == null || xref2 == null) {
                    continue;
                }

                // Remove isoform number
                if (xref1.matches(".*\\-[0-9]+")) {
                    xref1 = xref1.split("-")[0];
                }

                if (xref2.matches(".*\\-[0-9]+")) {
                    xref2 = xref2.split("-")[0];
                }

                String xref;

                if (false == xref1.equals(id)) {
                    xref = xref1;
                } else if (false == xref2.equals(id)) {
                    xref = xref2;
                } else {
                    // homodimer
                    xref = xref2;
                }

//				interactors.add(xref);
                Interaction classifiedInteraction;
                String key = xref1.compareTo(xref2) <= 0 ? xref1 + "#" + xref2 : xref2 + "#" + xref1;

                if (interactions.containsKey(key)) {
                    classifiedInteraction = interactions.get(key);
                } else {
                    classifiedInteraction = new Interaction(xref1, xref2);
                    interactions.put(key, classifiedInteraction);
                }

                if (interaction.getInteractionTypes().isEmpty()) {
                    String interactionType = UNSPECIFIED_TERM;
                    classifiedInteraction.addType(interactionType);
                } else {
                    for (Object interactionTypeObject : interaction
                            .getInteractionTypes()) {

                        String interactionType = getNameFromObject(interactionTypeObject);
                        classifiedInteraction.addType(interactionType);
                    }
                }

                if (interaction.getDetectionMethods().isEmpty()) {
                    String detectionMethod = UNSPECIFIED_TERM;
                    classifiedInteraction.addMethod(detectionMethod);
                } else {
                    for (Object methodObject : interaction
                            .getDetectionMethods()) {
                        String detectionMethod = getNameFromObject(methodObject);
                        classifiedInteraction.addMethod(detectionMethod);
                    }
                }
                
                
                for (Object bibRef : interaction.getPublications()) {
                    String pmid = getPmidsFromObject(bibRef);
                    if (pmid != null) {
                        classifiedInteraction.addBibRef(pmid);
                    }
                }

            }
        } catch (IOException | PsimiTabException e) {
            logger.error("Problem with psicquic server: {0} : {1}", new Object[]{psicquicUrl, id});
            throw new BridgesRemoteAccessException("Problem with psicquic server: " + psicquicUrl + " : " + id);
        }

        return interactions.values();
    }

    /**
     *
     * @return
     * @throws BridgesRemoteAccessException
     */
    public Collection<PsicquicService> getActiveServerFromRegistry() throws BridgesRemoteAccessException {

        ArrayList<PsicquicService> services = new ArrayList<>();

        try {
            URL url = new URL(registryUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                throw new BridgesRemoteAccessException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(conn.getInputStream());

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // interaction structure
            NodeList serviceList = doc.getElementsByTagName("service");

            for (int i = 0; i < serviceList.getLength(); i++) {

                Element serviceElement = (Element) serviceList.item(i);

                String name = serviceElement.getElementsByTagName("name")
                        .item(0).getFirstChild().getNodeValue();
                String restUrl = serviceElement.getElementsByTagName("restUrl")
                        .item(0).getFirstChild().getNodeValue();
                boolean active = "true".equals(serviceElement
                        .getElementsByTagName("active").item(0).getFirstChild()
                        .getNodeValue());
                int count = Integer.parseInt(serviceElement
                        .getElementsByTagName("count").item(0).getFirstChild()
                        .getNodeValue());

                PsicquicService service = new PsicquicService(name, restUrl,
                        active, count);
                services.add(service);
            }

        } catch (IOException | ParserConfigurationException | SAXException | DOMException | NumberFormatException e) {
            logger.error("Fail to retrieve active servers from the registry");
            throw new BridgesRemoteAccessException("Cannot access PSICQUIC registry. Either their the registry is down or there is a network problem.");
        }

        return services;
    }

    private static String getNameFromObject(Object object) {
        if (CrossReference.class.isInstance(object)) {
            CrossReference xref = (CrossReference) object;
            if (xref.getText() != null) {
                return xref.getText();
            }
            return xref.getIdentifier();
        }

        return object.toString() + " (" + object.getClass().getCanonicalName()
                + ")";
    }

    private static String getPmidsFromObject(Object object) {
        HashSet<String> pmids = new HashSet<>();

        if (CrossReference.class.isInstance(object)) {
            CrossReference xref = (CrossReference) object;
            if (xref.getDatabase().equals("pubmed")) {
                return xref.getIdentifier();
            }
        }

        return null;
    }

}
