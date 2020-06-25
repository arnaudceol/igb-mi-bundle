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
package it.iit.genomics.cru.structures.sources;

import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.biojava.nbio.structure.Structure;

/**
 * @author Arnaud Ceol
 *
 * Manage information about structures (both PDB and Interactome3D) in order not
 * to download the same structure twice.
 *
 */
public class PDBStructureSource extends StructureSource {
    
    private String pdbURL = null;

    /**
     * Local copy of the repository
     */
    protected String localPath = null;

    /**
     *
     * @param path either a PDB URL or a local directory
     */
    public PDBStructureSource(String path) {
        super(StructureSourceType.PDB);

        if (path.startsWith("http") || path.startsWith("ftp")) {
            this.pdbURL = path;
        } else {
            this.localPath = path;
        }
    }

    /**
     *
     * @param structureID
     * @return
     */
    @Override
    public Structure getStructure(String structureID) {

        if (isBlackListed(structureID)) {
            System.out.println("Black listed: " + structureID);
            return null;
        }

        if (pdbURL != null) {
            return getRemoteStructure(structureID);
        } else {
            return getLocalStructure(structureID);
        }

    }

    /**
     *
     * @param structureID
     * @return
     */
    public Structure getRemoteStructure(String structureID) {

        String pdbId = structureID.toLowerCase();
        String middle = pdbId.substring(1, 3);

        String outputFileName = getCacheFilename(structureID);
        File outputfile = new File(outputFileName);

        if (false == downloadedStructures.contains(structureID)) {

            InputStream uStream;
            String path = String.format("%s%s%s/pdb%s.ent.gz", pdbURL,
                    "/data/structures/divided/pdb/", middle, pdbId);

            try {
                URL url = new URL(path);
                uStream = url.openStream();
            } catch (IOException e) {
				// JOptionPane.showMessageDialog(null, "Cannot get PDB file "
                // + path);
                logger.error( "Cannot get PDB file {0}", path);
                addToBlackList(structureID);
                return null;
            }

            try {
                try (InputStream conn = new GZIPInputStream(uStream)) {
                    FileOutputStream outPut = new FileOutputStream(outputfile);
                    GZIPOutputStream gzOutPut = new GZIPOutputStream(outPut);
                    PrintWriter pw = new PrintWriter(gzOutPut);

                    BufferedReader fileBuffer = new BufferedReader(
                            new InputStreamReader(conn));
                    String line;
                    while ((line = fileBuffer.readLine()) != null) {
                        pw.println(line);
                    }
                    pw.flush();
                    pw.close();

                    outPut.flush();
                    outPut.close();
                }
                uStream.close();

            } catch (MalformedURLException e) {
				// JOptionPane.showMessageDialog(null, "Cannot get PDB file " +
                // path);
                // logger.error("Cannot get PDB file " + path);
                return null;
            } catch (IOException e) {
				// JOptionPane.showMessageDialog(null, "Cannot get PDB file " +
                // path);
                // logger.error("Cannot get PDB file " + path);
                return null;
            }
        }

        try {
            // getInterfaces(outputFileName, structureID);
            Structure pdbStructure = pdbFileReader.getStructure(outputfile);
            downloadedStructures.add(structureID);
            return pdbStructure;
        } catch (IOException | ExceptionInInitializerError e) {
            logger.error( "Cannot get PDB file {0}", structureID);
            addToBlackList(structureID);
            // logger.error("Cannot get PDB for " + structureID);
            return null;
        }

    }

    /**
     *
     * @param structureID
     * @return
     */
    public Structure getLocalStructure(String structureID) {

        try {
            // getInterfaces(outputFileName, structureID);
            Structure pdbStructure = pdbFileReader.getStructure(getFileName(structureID));
            downloadedStructures.add(structureID);
            return pdbStructure;
        } catch (IOException e) {
            logger.error( "Cannot get PDB file {0}", structureID);
            addToBlackList(structureID);
            // logger.error("Cannot get PDB for " + structureID);
            return null;
        }

    }

    private String getCacheFilename(String structureID) {
        return cacheDir + structureID + ".ent.gz";
    }

    /**
     *
     * @param structureID
     * @return
     */
    @Override
    protected String getFileName(String structureID) {

        if (localPath != null) {
            String pdbId = structureID.toLowerCase();
            String middle = pdbId.substring(1, 3);

            return String.format("%s/%s/pdb%s.ent.gz", localPath, middle, pdbId);
        }
        return getCacheFilename(structureID);

    }

    /**
     *
     * @return
     */
    public String getPdbURL() {
        return pdbURL;
    }

    /**
     *
     * @param pdbURL
     */
    public void setPdbURL(String pdbURL) {
        this.pdbURL = pdbURL;
    }

}
