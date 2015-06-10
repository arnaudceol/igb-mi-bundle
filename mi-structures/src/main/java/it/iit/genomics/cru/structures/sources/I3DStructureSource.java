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

import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DJaxbClient;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DJaxbClient.QueryType;
import it.iit.genomics.cru.structures.model.StructureException;
import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
import org.biojava.nbio.structure.Structure;
import org.slf4j.LoggerFactory;


/**
 * @author Arnaud Ceol
 *
 * Manage information about structures (both PDB and Interactome3D) in order not
 * to download the same structure twice.
 *
 */
public class I3DStructureSource extends StructureSource {
    
    /**
     * Local copy of the repository
     */
    protected String localPath = null;

    /**
     *
     * @param i3dURL
     */
    public I3DStructureSource(String i3dURL) {
        super(StructureSourceType.INTERACTOME3D);
    }

    /**
     *
     * @param i3dURL
     * @param localPath
     */
    public I3DStructureSource(String i3dURL, String localPath) {
        super(StructureSourceType.INTERACTOME3D);
        this.localPath = localPath;
    }

    /**
     *
     * @param structureID
     * @return
     * @throws StructureException
     */
    @Override
    public Structure getStructure(String structureID)
          throws StructureException   {

        QueryType queryType;
        if (structureID.charAt(10) == '-') {
            queryType = QueryType.protein;
        } else {
            queryType = QueryType.interaction;
        }

        Interactome3DJaxbClient client = new Interactome3DJaxbClient();

        String outputFileName;

        if (localPath == null) {
            outputFileName = getCacheFilename(structureID);

            if (false == downloadedStructures.contains(structureID)) {
                try {
                    File outputfile = new File(outputFileName);
                    
                    // try {
                    String pdbContent = client
                            .getPdbFiles(structureID + ".pdb", queryType).iterator()
                            .next().getContents();
                    InputStream is = new ByteArrayInputStream(pdbContent.getBytes());
                    
                    try (FileOutputStream outPut = new FileOutputStream(outputfile)) {
                        GZIPOutputStream gzOutPut = new GZIPOutputStream(outPut);
                        try (PrintWriter pw = new PrintWriter(gzOutPut)) {
                            BufferedReader fileBuffer = new BufferedReader(
                                    new InputStreamReader(is));
                            
                            String line;
                            while ((line = fileBuffer.readLine()) != null) {
                                pw.println(line);
                            }
                            pw.flush();
                        }
                        
                        outPut.flush();
                    }
                    
                    downloadedStructures.add(structureID);
                } catch (IOException ex) {
                    logger.error( null, ex);
                    throw new StructureException(ex);
                }
            }
        } else {
            outputFileName = getLocalFilename(structureID);
        }

        Structure pdbStructure;
        try {
            pdbStructure = pdbFileReader.getStructure(outputFileName);
        } catch (IOException ex) {
            LoggerFactory.getLogger(I3DStructureSource.class.getName()).error( null, ex);
            throw new StructureException(ex);
        }

        return pdbStructure;

    }

    private String getCacheFilename(String structureID) {
        return cacheDir + structureID + ".ent.gz";
    }

    private String getLocalFilename(String structureID) {
        return localPath + structureID + ".ent.gz";
    }

    /**
     *
     * @param structureID
     * @return
     */
    @Override
    protected String getFileName(String structureID) {
        if (null == this.localPath) {
            return getCacheFilename(structureID);
        } else {
            return getLocalFilename(structureID);
        }
    }

}
