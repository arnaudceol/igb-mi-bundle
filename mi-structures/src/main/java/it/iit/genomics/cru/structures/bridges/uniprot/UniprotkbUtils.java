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
package it.iit.genomics.cru.structures.bridges.uniprot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.collect.HashMultimap;

import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.ModifiedResidue;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.model.position.UniprotPosition;


/**
 * @author Arnaud Ceol
 *
 * Utilities to retrieve Uniprot AC and gene names from the Uniprotkb database
 */
public class UniprotkbUtils {

    private static final Logger logger = LoggerFactory.getLogger(UniprotkbUtils.class);

    private final String taxid;

    private static final String UNIPROT_SERVER = "http://www.uniprot.org/";

    private static final String DBFETCH_SERVER = "http://www.ebi.ac.uk/Tools/dbfetch/dbfetch";

    private static final String UNIPROT_TOOL = "uniprot";

    private static final String TAXONOMY_TOOL = "taxonomy";

    /**
     * Number of time we will wait and retry in case of failure to communicate
     * with Uniprot
     */
    private static int allowedUniprotFailures = 10;

    /**
     * Maximum number of query (gene names, uniprot acc.) sent in a single
     * request
     */
    private final static int maxQueries = 10;

    private final HashMultimap<String, MoleculeEntry> cache = HashMultimap.create();

    private static final HashMap<String, UniprotkbUtils> instances = new HashMap<>();

    private UniprotkbUtils(String taxid) {
        this.taxid = taxid;
    }

    /**
     *
     * @param taxid
     * @return
     */
    public static UniprotkbUtils getInstance(String taxid) {
        if (instances.get(taxid) == null) {
            instances.put(taxid, new UniprotkbUtils(taxid));
        }
        return instances.get(taxid);
    }

    private final String USER_AGENT = "Mozilla/5.0";

    private Collection<MoleculeEntry> getUniprotEntriesXML(String location) throws BridgesRemoteAccessException {
        return getUniprotEntriesXML(location, true);
    }

    private Collection<MoleculeEntry> getUniprotEntriesXML(String location,
            boolean waitAndRetryOnFailure) throws BridgesRemoteAccessException {

        String url = location + "&format=xml";

        ArrayList<MoleculeEntry> uniprotEntries = new ArrayList<>();
        try {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(
                    ClientPNames.ALLOW_CIRCULAR_REDIRECTS, Boolean.TRUE);
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("User-Agent", USER_AGENT);

            HttpResponse response = client.execute(request);

            if (response.getEntity().getContentLength() == 0) {
                // No result
                return uniprotEntries;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(response.getEntity()
                    .getContent()));

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // interaction structure
            NodeList entryList = doc.getElementsByTagName("entry");

            for (int i = 0; i < entryList.getLength(); i++) {

                Element entryElement = (Element) entryList.item(i);

                
                String dataset = entryElement.getAttribute("dataset");
                
                String ac = entryElement.getElementsByTagName("accession")
                        .item(0).getFirstChild().getNodeValue();

                MoleculeEntry uniprotEntry = new MoleculeEntry(ac);
                
                uniprotEntry.setDataset(dataset);

                // Taxid
                Element organism = (Element) entryElement.getElementsByTagName(
                        "organism").item(0);

                String organismCommonName = null;
                String organismScientificName = null;
                String organismOtherName = null;

                NodeList organismNames = organism
                        .getElementsByTagName("name");

                for (int j = 0; j < organismNames.getLength(); j++) {

                    Element reference = (Element) organismNames.item(j);
                    switch (reference.getAttribute("type")) {
                        case "scientific":
                            organismScientificName = reference.getTextContent();
                            break;
                        case "common":
                            organismCommonName = reference.getTextContent();
                            break;
                        default:
                            organismOtherName = reference.getTextContent();
                            break;
                    }
                }

                if (null != organismCommonName) {
                    uniprotEntry.setOrganism(organismCommonName);
                } else if (null != organismScientificName) {
                    uniprotEntry.setOrganism(organismScientificName);
                } else if (null != organismOtherName) {
                    uniprotEntry.setOrganism(organismOtherName);
                }

                NodeList organismReferences = organism
                        .getElementsByTagName("dbReference");

                for (int j = 0; j < organismReferences.getLength(); j++) {
                    Element reference = (Element) organismReferences.item(j);
                    if (reference.hasAttribute("type")
                            && "NCBI Taxonomy".equals(reference
                                    .getAttribute("type"))) {
                        String proteinTaxid = reference.getAttribute("id");
                        uniprotEntry.setTaxid(proteinTaxid);
                    }
                }

                // GENE
                NodeList geneNames = entryElement.getElementsByTagName("gene");

                for (int j = 0; j < geneNames.getLength(); j++) {
                    Element gene = (Element) geneNames.item(j);

                    NodeList nameList = gene.getElementsByTagName("name");

                    for (int k = 0; k < nameList.getLength(); k++) {
                        Element name = (Element) nameList.item(k);
                        uniprotEntry.addGeneName(name.getFirstChild()
                                .getNodeValue());
                    }
                }

                // modified residues
                HashMap<String, ModifiedResidue> modifiedResidues = new HashMap<>();

                NodeList features = entryElement
                        .getElementsByTagName("feature");
                for (int j = 0; j < features.getLength(); j++) {
                    Element feature = (Element) features.item(j);

                    if (false == entryElement.equals(feature
                            .getParentNode())) {
                        continue;
                    }

                    // ensembl
                    if (feature.hasAttribute("type")
                            && "modified residue".equals(feature
                                    .getAttribute("type"))) {

                        String description = feature
                                .getAttribute("description").split(";")[0];

                        if (false == modifiedResidues.containsKey(description)) {
                            modifiedResidues.put(description, new ModifiedResidue(description));
                        }

                        NodeList locations = feature
                                .getElementsByTagName("location");
                        for (int k = 0; k < locations.getLength(); k++) {
                            Element loc = (Element) locations.item(k);
                            NodeList positions = loc
                                    .getElementsByTagName("position");
                            for (int l = 0; l < positions.getLength(); l++) {
                                Element position = (Element) positions.item(l);
                                modifiedResidues.get(description).addPosition(new UniprotPosition(Integer.parseInt(position.getAttribute("position"))));
                            }

                        }
                    }
                }

                uniprotEntry.getModifications().addAll(modifiedResidues.values());

                // Xrefs:
                NodeList dbReferences = entryElement
                        .getElementsByTagName("dbReference");
                for (int j = 0; j < dbReferences.getLength(); j++) {
                    Element dbReference = (Element) dbReferences.item(j);

                    if (false == entryElement.equals(dbReference
                            .getParentNode())) {
                        continue;
                    }

                    NodeList molecules = dbReference
                            .getElementsByTagName("molecule");

                    // ensembl
                    if (dbReference.hasAttribute("type")
                            && "Ensembl".equals(dbReference
                                    .getAttribute("type"))) {

                        // transcript ID
                        String id = dbReference
                                .getAttribute("id");

                        for (int iMolecule = 0; iMolecule < molecules.getLength(); iMolecule++) {
                            Element molecule = (Element) molecules.item(iMolecule);
                            uniprotEntry.addXrefToVarSplice(id, molecule.getAttribute("id"));
                        }

                        uniprotEntry.addEnsemblGene(id);

                        NodeList properties = dbReference
                                .getElementsByTagName("property");

                        for (int k = 0; k < properties.getLength(); k++) {
                            Element property = (Element) properties.item(k);

                            if (property.hasAttribute("type")
                                    && "gene ID".equals(property
                                            .getAttribute("type"))) {
                                uniprotEntry.addEnsemblGene(property
                                        .getAttribute("value"));
                            }
                        }
                    }

                    // refseq
                    if (dbReference.hasAttribute("type")
                            && "RefSeq"
                            .equals(dbReference.getAttribute("type"))) {
                        NodeList properties = dbReference
                                .getElementsByTagName("property");
                        for (int k = 0; k < properties.getLength(); k++) {
                            Element property = (Element) properties.item(k);
                            if (property.hasAttribute("type")
                                    && "nucleotide sequence ID".equals(property
                                            .getAttribute("type"))) {

                                String id = property
                                        .getAttribute("value");
                                if (molecules.getLength() > 0) {
                                    for (int iMolecule = 0; iMolecule < molecules.getLength(); iMolecule++) {
                                        Element molecule = (Element) molecules.item(iMolecule);

                                        // If refseq, add also without the version                                       
                                        uniprotEntry.addXrefToVarSplice(id, molecule.getAttribute("id"));
                                        uniprotEntry.addXrefToVarSplice(id.split("\\.")[0], molecule.getAttribute("id"));

                                    }
                                } else {
                                    // If refseq, add also without the version                                       
                                    uniprotEntry.addXrefToVarSplice(id, ac);
                                    uniprotEntry.addXrefToVarSplice(id.split("\\.")[0], ac);
                                }

                                uniprotEntry.addRefseq(id);

                            }
                        }
                    }

                    /* PDB chains will be imported from the webservice */
                    // PDB
                    if (dbReference.hasAttribute("type")
                            && "PDB".equals(dbReference.getAttribute("type"))) {
                        NodeList properties = dbReference
                                .getElementsByTagName("property");
                        String method = null;
                        String chains = null;

                        for (int k = 0; k < properties.getLength(); k++) {
                            Element property = (Element) properties.item(k);
                            if (property.hasAttribute("type")
                                    && "method".equals(property
                                            .getAttribute("type"))) {
                                method = property.getAttribute("value");
                            } else if (property.hasAttribute("type")
                                    && "chains".equals(property
                                            .getAttribute("type"))) {
                                chains = property.getAttribute("value");
                            }
                        }

                        if (method != null && "Model".equals(method)) {
                            continue;
                        }

                        if (chains == null) {
                            continue;
                        }

                        String pdb = dbReference.getAttribute("id");

                        uniprotEntry.addPDB(
                                pdb,
                                method);

                        for (String chainElement : chains.split(",")) {
                            try {
                                String chainNames = chainElement.split("=")[0];
                                int start = Integer.parseInt(chainElement
                                        .split("=")[1].trim().split("-")[0]);
                                int end = Integer.parseInt(chainElement
                                        .split("=")[1].trim().split("-")[1]
                                        .replace(".", ""));
                                for (String chainName : chainNames.split("/")) {
                                    uniprotEntry.addChain(
                                            pdb,
                                            new ChainMapping(pdb, chainName
                                                    .trim(), start, end),
                                            method);
                                }
                            } catch (ArrayIndexOutOfBoundsException aiobe) {
                                // IGBLogger.getInstance().warning(
                                // "Cannot parse chain: " + chainElement
                                // + ", skip");
                            }
                        }
                    }

                }

                // Sequence
                NodeList sequenceElements = entryElement
                        .getElementsByTagName("sequence");

                for (int j = 0; j < sequenceElements.getLength(); j++) {
                    Element sequenceElement = (Element) sequenceElements
                            .item(j);

                    if (false == sequenceElement.getParentNode().equals(
                            entryElement)) {
                        continue;
                    }
                    String sequence = sequenceElement.getFirstChild()
                            .getNodeValue().replaceAll("\n", "");
                    uniprotEntry.setSequence(sequence);
                }

                // Diseases
                NodeList diseases = entryElement
                        .getElementsByTagName("disease");

                for (int j = 0; j < diseases.getLength(); j++) {
                    Element disease = (Element) diseases.item(j);

                    NodeList nameList = disease.getElementsByTagName("name");

                    for (int k = 0; k < nameList.getLength(); k++) {
                        Element name = (Element) nameList.item(k);
                        uniprotEntry.addDisease(name.getFirstChild()
                                .getNodeValue());
                    }
                }

                // Get fasta for all varsplice
                String fastaQuery = "http://www.uniprot.org/uniprot/" + uniprotEntry.getUniprotAc() + ".fasta?include=yes";

                try {
                    //HttpClient fastaClient = new DefaultHttpClient();

                    client.getParams().setParameter(
                            ClientPNames.ALLOW_CIRCULAR_REDIRECTS, Boolean.TRUE);
                    HttpGet fastaRequest = new HttpGet(fastaQuery);

                    // add request header
                    request.addHeader("User-Agent", USER_AGENT);

                    HttpResponse fastaResponse = client.execute(fastaRequest);

                    if (fastaResponse.getEntity().getContentLength() == 0) {
                        continue;
                    }

                    InputStream is = fastaResponse.getEntity().getContent();

                    try {
                        LinkedHashMap<String, ProteinSequence> fasta = FastaReaderHelper.readFastaProteinSequence(is);

                        boolean mainSequence = true;
                        
                        for (ProteinSequence seq : fasta.values()) {
//                            logger.info("Add sequence: " + seq.getAccession().getID() + " : " + seq.getSequenceAsString());
                            uniprotEntry.addSequence(seq.getAccession().getID(), seq.getSequenceAsString());
                            if (mainSequence) {
                                uniprotEntry.setMainIsoform(seq.getAccession().getID());
                                mainSequence = false;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Cannot retrieve fasta for : " +  uniprotEntry.getUniprotAc());
                    }
                } catch (IOException | IllegalStateException ex) {
                    logger.error(null, ex);
                }

                uniprotEntries.add(uniprotEntry);

            }

        } catch (SAXParseException se) {
            // Nothing was return
            // IGBLogger.getInstance()
            // .error("Uniprot returns empty result: " + url);
        } catch (IOException | ParserConfigurationException | IllegalStateException | SAXException | DOMException | NumberFormatException e) {
            if (waitAndRetryOnFailure && allowedUniprotFailures > 0) {
                try {
                    allowedUniprotFailures--;
                    Thread.sleep(5000);
                    return getUniprotEntriesXML(location, false);
                } catch (InterruptedException e1) {
                    logger.error("Fail to retrieve data from " + location);
                    throw new BridgesRemoteAccessException("Fail to retrieve data from Uniprot " + location);
                }
            } else {
                logger.error("Problem with Uniprot: " +  url);
                throw new BridgesRemoteAccessException("Fail to retrieve data from Uniprot " + location);
            }
        }

        for (MoleculeEntry entry : uniprotEntries) {
            addToCache(entry);
        }

        return uniprotEntries;
    }

    private void addToCache(MoleculeEntry protein) {

        // add to chache by uniprotAc, gene name, refseq ..
        // Only use the first one. Using synomyms may cause ambiguity.
        String geneName = protein.getGeneName();

        if (geneName != null) {
            cache.put(geneName.toUpperCase(), protein);
        }

        for (String xref : protein.getRefseqs()) {
            // remove version
            cache.put(xref.toUpperCase().split("\\.")[0], protein);
        }

        cache.put(protein.getUniprotAc(), protein);

    }

    /**
     *
     * @param genes
     * @return
     * @throws BridgesRemoteAccessException
     */
    public HashMultimap<String, MoleculeEntry> getUniprotEntriesFromGenes(
            Collection<String> genes) throws BridgesRemoteAccessException {
        String tool = UNIPROT_TOOL;

        HashMultimap<String, MoleculeEntry> gene2uniprots = HashMultimap.create();

        HashSet<String> genes2get = new HashSet<>();

        try {
        	
        	int numGenesProcessed = 0;
        	
            for (String gene : genes) {
            	numGenesProcessed++;
                if (cache.containsKey(gene.toUpperCase())) {
                    gene2uniprots.putAll(gene, cache.get(gene.toUpperCase()));
                } else {
                    genes2get.add(gene);
                    // if size == limit, do query
                    if ( genes2get.size() == maxQueries || numGenesProcessed == genes.size() ) {
                        String location = UNIPROT_SERVER
                                + tool
                                + "/?"
                                + "query=keyword:181+AND+organism:"
                                + URLEncoder.encode("\"" + taxid + "\"",
                                        "UTF-8")
                                + "+AND+(gene:"
                                + URLEncoder.encode(StringUtils.join(genes2get,
                                                " OR gene:"), "UTF-8") + ")";
                                                
                        Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);
                         
                        for (MoleculeEntry entry : uniprotEntries) {
                            String geneName = entry.getGeneName();

                            // Only use the first one. Using synomyms may cause
                            // ambiguity.
                            if (geneName != null
                                    && genes.contains(geneName)) {
                                gene2uniprots.put(geneName, entry);
                            }
                        }

                        genes2get.clear();
                    }

                }
            }

            if (genes2get.isEmpty()) {
                return gene2uniprots;
            }

            String location = UNIPROT_SERVER
                    + tool
                    + "/?"
                    + "query=keyword:181+AND+organism:"
                    + URLEncoder.encode("\"" + taxid + "\"", "UTF-8")
                    + "+AND+(gene:"
                    + URLEncoder.encode(
                            StringUtils.join(genes2get, " OR gene:"), "UTF-8")
                    + ")";

            Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

            for (MoleculeEntry entry : uniprotEntries) {
                String geneName = entry.getGeneName();

                // Only use the first one. Using synomyms may cause ambiguity.
                if (geneName != null && gene2uniprots.containsKey(geneName)) {
                    gene2uniprots.put(geneName, entry);
                }
            }

        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
            logger.error("cannot get proteins for " + StringUtils.join(genes, ", "), e);
        }

        return gene2uniprots;
    }

    /**
     *
     * @param refSeqs
     * @return
     * @throws BridgesRemoteAccessException
     */
    public HashMultimap<String, MoleculeEntry> getUniprotEntriesFromRefSeqs(
            Collection<String> refSeqs) throws BridgesRemoteAccessException {
        String tool = UNIPROT_TOOL;

        HashMultimap<String, MoleculeEntry> refseq2uniprots = HashMultimap.create();

        if (refSeqs.isEmpty()) {
            return refseq2uniprots;
        }

        HashSet<String> refs2get = new HashSet<>();

        try {
            for (String refseq : refSeqs) {
                if (cache.containsKey(refseq.toUpperCase().split("\\.")[0])) {
                    refseq2uniprots.putAll(refseq,
                            cache.get(refseq.toUpperCase().split("\\.")[0]));
                } else {
                    refs2get.add(refseq);

                    // if size == limit, do query
                    if (refs2get.size() == maxQueries) {

                        String location = UNIPROT_SERVER
                                + tool
                                + "/?"
                                + "query=keyword:181+AND+organism:"
                                + URLEncoder.encode("\"" + taxid + "\"",
                                        "UTF-8")
                                + "+AND+(database%3A(type%3Arefseq+"
                                + URLEncoder
                                .encode(""
                                        + StringUtils
                                        .join(refs2get,
                                                ") OR database:(type:refseq ")
                                        + "", "UTF-8") + "))";

                        Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

                        for (MoleculeEntry entry : uniprotEntries) {
                            for (String xref : entry.getRefseqs()) {
                                if (xref.endsWith(".")) {
                                    xref = xref.substring(0, xref.length() - 1);
                                }
                                if (refseq2uniprots.containsKey(xref.trim())) {
                                    refseq2uniprots.put(xref, entry);
                                } else if (refseq2uniprots.containsKey(xref
                                        .split("[.]")[0])) {
                                    refseq2uniprots.put(xref.split("[.]")[0],
                                            entry);
                                }
                            }
                        }
                        refs2get.clear();
                    }

                }
            }

            if (refs2get.isEmpty()) {
                return refseq2uniprots;
            }

            String location = UNIPROT_SERVER
                    + tool
                    + "/?"
                    + "query=keyword:181+AND+organism:"
                    + URLEncoder.encode("\"" + taxid + "\"", "UTF-8")
                    + "+AND+(database%3A(type%3Arefseq+"
                    + URLEncoder
                    .encode(""
                            + StringUtils.join(refs2get,
                                    ") OR database:(type:refseq ") + "",
                            "UTF-8") + "))";

            Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

            for (MoleculeEntry entry : uniprotEntries) {
                for (String xref : entry.getRefseqs()) {
                    if (xref.endsWith(".")) {
                        xref = xref.substring(0, xref.length() - 1);
                    }
                    if (refseq2uniprots.containsKey(xref.trim())) {
                        refseq2uniprots.put(xref, entry);
                    } else if (refseq2uniprots
                            .containsKey(xref.split("[.]")[0])) {
                        refseq2uniprots.put(xref.split("[.]")[0], entry);
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("cannot get proteins for " + StringUtils.join(refSeqs, ", "), e);
        }

        return refseq2uniprots;
    }

    /**
     *
     * @param ensemblGeneIDs
     * @return
     * @throws BridgesRemoteAccessException
     */
    public HashMultimap<String, MoleculeEntry> getUniprotEntriesFromEnsembl(
            Collection<String> ensemblGeneIDs) throws BridgesRemoteAccessException {
        String tool = UNIPROT_TOOL;

        HashMultimap<String, MoleculeEntry> ensembl2uniprots = HashMultimap.create();

        if (ensemblGeneIDs.isEmpty()) {
            return ensembl2uniprots;
        }

        HashSet<String> refs2get = new HashSet<>();

        try {

            for (String ensemblGeneID : ensemblGeneIDs) {
                if (cache
                        .containsKey(ensemblGeneID.toUpperCase().split("\\.")[0])) {
                    ensembl2uniprots.putAll(ensemblGeneID, cache
                            .get(ensemblGeneID.toUpperCase().split("\\.")[0]));
                } else {
                    refs2get.add(ensemblGeneID);

                    // if size == limit, do query
                    if (refs2get.size() == maxQueries) {
                        String location = UNIPROT_SERVER
                                + tool
                                + "/?"
                                + "query=keyword:181+AND+organism:"
                                + URLEncoder.encode("\"" + taxid + "\"",
                                        "UTF-8")
                                + "+AND+(database%3A(type%3Aensembl+"
                                + URLEncoder
                                .encode(""
                                        + StringUtils
                                        .join(refs2get,
                                                ") OR database:(type:ensembl ")
                                        + "", "UTF-8") + "))";

                        Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

                        for (MoleculeEntry entry : uniprotEntries) {
                            for (String xref : entry.getEnsemblGenes()) {
                                if (xref.endsWith(".")) {
                                    xref = xref.substring(0, xref.length() - 1);
                                }
                                if (ensembl2uniprots.containsKey(xref.trim())) {
                                    ensembl2uniprots.put(xref, entry);
                                } else if (ensembl2uniprots.containsKey(xref
                                        .split("[.]")[0])) {
                                    ensembl2uniprots.put(xref.split("[.]")[0],
                                            entry);
                                }
                            }
                        }
                        refs2get.clear();
                    }
                }
            }

            if (refs2get.isEmpty()) {
                return ensembl2uniprots;
            }

            String location = UNIPROT_SERVER
                    + tool
                    + "/?"
                    + "query=keyword:181+AND+organism:"
                    + URLEncoder.encode("\"" + taxid + "\"", "UTF-8")
                    + "+AND+(database%3A(type%3Aensembl+"
                    + URLEncoder.encode(
                            ""
                            + StringUtils.join(refs2get,
                                    ") OR database:(type:ensembl ")
                            + "", "UTF-8") + "))";

            Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

            for (MoleculeEntry entry : uniprotEntries) {
                for (String xref : entry.getEnsemblGenes()) {
                    if (xref.endsWith(".")) {
                        xref = xref.substring(0, xref.length() - 1);
                    }
                    if (ensembl2uniprots.containsKey(xref.trim())) {
                        ensembl2uniprots.put(xref, entry);
                    } else if (ensembl2uniprots
                            .containsKey(xref.split("[.]")[0])) {
                        ensembl2uniprots.put(xref.split("[.]")[0], entry);
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("cannot get proteins for " + StringUtils.join(ensemblGeneIDs, ", "), e);
        }

        return ensembl2uniprots;
    }

    /**
     *
     * @param xrefs
     * @return
     * @throws BridgesRemoteAccessException
     */
    public HashMap<String, MoleculeEntry> getUniprotEntriesFromUniprotAccessions(
            Collection<String> xrefs) throws BridgesRemoteAccessException {
        return getUniprotEntriesFromUniprotAccessions(
                xrefs, true);
    }

    /**
     *
     * @param uniprotAc
     * @param filterTaxid
     * @return
     * @throws BridgesRemoteAccessException
     */
    public MoleculeEntry getUniprotEntriesFromUniprotAccession(
            String uniprotAc, boolean filterTaxid) throws BridgesRemoteAccessException {

        Collection<String> acs = new ArrayList<>();
        acs.add(uniprotAc);

        return getUniprotEntriesFromUniprotAccessions(
                acs, filterTaxid).get(uniprotAc);
    }

    /**
     *
     * @param xrefs
     * @param filterTaxid
     * @return
     * @throws BridgesRemoteAccessException
     */
    public HashMap<String, MoleculeEntry> getUniprotEntriesFromUniprotAccessions(
            Collection<String> xrefs, boolean filterTaxid) throws BridgesRemoteAccessException {
        String tool = UNIPROT_TOOL;

        // remove xrefs that are not uniprotAcs
        Collection<String> uniprotAcs = getUniprotAcs(xrefs);

        HashMap<String, MoleculeEntry> results = new HashMap<>();

        HashSet<String> ref2get = new HashSet<>();

        try {
            for (String ref : uniprotAcs) {
                if (cache.containsKey(ref.toUpperCase())) {
                    results.put(ref, cache.get(ref.toUpperCase()).iterator()
                            .next());
                } else {
                    ref2get.add(ref);

                    // if size == limit, do query
                    if (ref2get.size() == maxQueries) {
                        String location = UNIPROT_SERVER
                                + tool
                                + "/?"
                                + "query=(accession:"
                                + URLEncoder
                                .encode(StringUtils.join(ref2get,
                                                " OR accession:") + "", "UTF-8")
                                + ")";
                        if (filterTaxid) {
                            location += "+AND+keyword:181+AND+organism:"
                                    + URLEncoder.encode("\"" + taxid + "\"",
                                            "UTF-8");
                        }

                        Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

                        for (MoleculeEntry entry : uniprotEntries) {
                            results.put(entry.getUniprotAc(), entry);
                        }
                        ref2get.clear();
                    }
                }
            }

            if (ref2get.isEmpty()) {
                return results;
            }

            String location = UNIPROT_SERVER
                    + tool
                    + "/?"
                    + "query=(accession:"
                    + URLEncoder.encode(
                            StringUtils.join(ref2get, " OR accession:") + "",
                            "UTF-8") + ")";
            if (filterTaxid) {
                location += "+AND+keyword:181+AND+organism:"
                        + URLEncoder.encode("\"" + taxid + "\"",
                                "UTF-8");
            }
            Collection<MoleculeEntry> uniprotEntries = getUniprotEntriesXML(location);

            for (MoleculeEntry entry : uniprotEntries) {
                results.put(entry.getUniprotAc(), entry);
            }

        } catch (UnsupportedEncodingException e) {
            logger.error("cannot get proteins for " + StringUtils.join(xrefs, ", "), e);
        }

        return results;
    }

    private static final String[][] DEFAULT_SPECIES = {{"Homo sapiens", "9606"}, {"Mus musculus", "10090"}};

    /**
     *
     * @param name
     * @return
     * @throws BridgesRemoteAccessException
     */
    public static ArrayList<String[]> getSpeciesFromName(String name) throws BridgesRemoteAccessException {

        ArrayList<String[]> results = new ArrayList<>();

        // Search first defaults taxid to avoid a remote connection to uniprot:
        for (String[] species : DEFAULT_SPECIES) {
            if (species[0].equals(name)) {
                results.add(species);
                return results;
            }
        }

        String tool = TAXONOMY_TOOL;

        try {
            String url = UNIPROT_SERVER + tool + "/?"
                    + "query=complete:yes+AND+("
                    + URLEncoder.encode(name, "UTF-8") + ")&format=tab";

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String line;

            while ((line = rd.readLine()) != null) {

                if (false == line.startsWith("Taxon")) {
                    String[] fields = line.split("\t");
                    if (fields.length >= 3) {
                        String[] specie = {fields[2], fields[0]};
                        results.add(specie);
                    }
                }
            }

        } catch (IOException | IllegalStateException e) {
            logger.error("cannot get species for " + name, e);
            throw new BridgesRemoteAccessException("Fail to retrieve species " + name + " from Uniprot ");
        }

        return results;
    }

    /**
     * Check syntax of a cross reference and verify it is a Uniprot Ac (without
     * isoform)
     *
     * @param xref
     * @return
     */
    public static boolean isUniprotAc(String xref) {
        return xref.matches("[A-Za-z0-9]{6}(\\-[0-9]+)?");
    }

    /**
     *
     * @param xref
     * @return
     */
    public static boolean isChebiAc(String xref) {
        return xref.matches("CHEBI:[0-9]+");
    }

    private static Collection<String> getUniprotAcs(Collection<String> xrefs) {
        HashSet<String> uniprotAcs = new HashSet<>();

        for (String xref : xrefs) {
            if (xref.matches(".*\\-[0-9]+")) {
                xref = xref.split("-")[0];
            }

            if (isUniprotAc(xref)) {
                uniprotAcs.add(xref);
            }
        }
        return uniprotAcs;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        ArrayList<String> acs = new ArrayList<>();

        acs.add("P84022");

        HashMap<String, MoleculeEntry> prots = UniprotkbUtils
                .getInstance("9606")
                .getUniprotEntriesFromUniprotAccessions(acs);

        for (MoleculeEntry entry : prots.values()) {
            System.out.println(entry);
            for (String pdb : entry.getPdbs()) {
                System.out.println("# " + pdb);
                for (ChainMapping chain : entry.getChains(pdb)) {
                    System.out.println("- " + pdb + ": " + chain.getChain());
                }
            }
            System.out.println("Diseases: " + StringUtils.join(entry.getDiseases(), ", "));
        }
    }

    /**
     *
     * @param pdo
     * @return
     */
    public Collection<String> getProteinsInStructures(String pdo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
