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
package it.iit.genomics.cru.igb.bundles.mi.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.affymetrix.common.CommonUtils;

import it.iit.genomics.cru.igb.bundles.mi.business.genes.IGBQuickLoadGeneManager;
import it.iit.genomics.cru.structures.bridges.pdb.PDBUtils;

/**
 *
 * @author Arnaud Ceol
 *
 * Manage user configurations. The configuration is saved in the file
 * mibundle/mibundle.properties inside the IGB configuration repository (in
 * Linux: ~/.igb)
 *
 */
public final class MIBundleConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(IGBQuickLoadGeneManager.class);
    
    private static MIBundleConfiguration instance = null;

    /**
     * By default the property file is saved each time a property is modified.
     * Set to false for instance when loading the property file to avoid
     * read/write access to the file.
     */
    private boolean disableSave = false;

    private MIBundleConfiguration() {
        
        // set the path to cache files
        String lineSplit = System.getProperty("file.separator");

        igbBundlePath = CommonUtils.getInstance().getAppDataDirectory()
                + lineSplit + "mibundle" + lineSplit;
        setCachePath(igbBundlePath + lineSplit + "cache" + lineSplit);

    }

    public static MIBundleConfiguration getInstance() {
        if (instance == null) {
            instance = new MIBundleConfiguration();
        }

        return instance;
    }

    /* System paths */
    private final String igbBundlePath;
    private String cachePath;

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String tempPath) {
        this.cachePath = tempPath;
        createTempDirectoryIfNeeded();
    }

    /**
     * Returns the space occupied by the temporary folder in bytes
     *
     * @return
     */
    public long getCacheSpace() {
        File directory = new File(cachePath);
        return folderSize(directory);
    }

    /**
     * Suggested by Tendayi Mawushe at
     * http://stackoverflow.com/questions/2149785/size-of-folder-or-file
     *
     * @param directory
     * @return
     */
    private static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }

    public void clearCache() {
        File directory = new File(cachePath);
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    /**
     * Check the directory that contains the local cache of PDB and chemical
     * component definiton files exists or create it.
     */
    private void createTempDirectoryIfNeeded() {

        // First the root directory
        File tempDir = new File(igbBundlePath);

        // if the directory does not exist, create it
        if (!tempDir.exists()) {
            logger.info(
                    "creating directory for PDB files: " + cachePath);
            tempDir.mkdir();
        }

        tempDir = new File(cachePath);

        // if the directory does not exist, create it
        if (!tempDir.exists()) {
            logger.info(
                    "creating directory for PDB files: " + cachePath);
            tempDir.mkdir();
        }
    }

    /**
     * ****************************************************************************************
     * property file
     */
    private static final String propertiesFilename = "mibundle.properties";

    private static final String pdbURLProperty = "PDB mirror URL";

    private static final String i3dStructuresDirProperty = "I3D local directory";    
    private static final String userStructuresDirProperty = "User structures directory";
    private static final String pdbLocalProperty = "PDB local directory";

    private static final String exportFolderProperty = "export directory";

    private Properties properties;

    public void loadProperties() {
        // create and load default properties
        properties = new Properties();

        /**
         * Do not overwrite property file during the loading.
         */
        disableSave = true;

        try {
            File f = new File(igbBundlePath + propertiesFilename);
            if (!f.exists()) {
                f.createNewFile();
            }

            FileInputStream in = new FileInputStream(f);

            logger.info(
                    "load properties");

            properties.load(in);

            if (properties.getProperty(pdbURLProperty) != null) {
                setPdbUrl(properties.getProperty(pdbURLProperty));
            }

            if (properties.getProperty(pdbLocalProperty) != null) {
                setPdbLocalMirror(properties.getProperty(pdbLocalProperty));
            }

            if (properties.getProperty(userStructuresDirProperty) != null) {
                setUserStructuresDirectory(properties.getProperty(userStructuresDirProperty));
            }

            if (properties.getProperty(exportFolderProperty) != null) {
                setExportFolder(properties.getProperty(exportFolderProperty));
            }

            if (properties.getProperty(i3dStructuresDirProperty) != null) {
                setI3DStructuresDirectory(properties.getProperty(i3dStructuresDirProperty));
            }

            in.close();

        } catch (FileNotFoundException e) {
            // create file

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        disableSave = false;
    }

    public void saveProperties() {
        if (disableSave) {
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream(igbBundlePath + propertiesFilename);
            properties.store(out, "Saving MI Bundle properties");
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Interactome3D Disable access to Interactome3D in case of access failure
	 *
     */
    private boolean disableInteractome3D = false;

    /* Psicquic */
    private HashSet<String> disabledPsicquicServers = new HashSet<String>();

    public boolean isDisabledInteractome3D() {
        return disableInteractome3D;
    }

    public void disableInteractome3D() {
        this.disableInteractome3D = true;
    }

    public void disablePsicquicServer(String psicquicServer) {
        this.disabledPsicquicServers.add(psicquicServer);
    }

    public void enablePsicquicServer(String psicquicServer) {
        if (this.disabledPsicquicServers.contains(psicquicServer)) {
            this.disabledPsicquicServers.remove(psicquicServer);
        }
    }

    /* PDB */
    protected String pdbUrl = PDBUtils.getUrl("PDBe (UK)");

    protected String pdbLocalMirror = null;

    protected String i3dStructuresDir = null;

    protected String userStructuresDir = null;

    protected String exportFolder = null;

    public void setPdbUrl(String pdbUrl) {
        this.pdbUrl = pdbUrl;
        properties.setProperty(pdbURLProperty, pdbUrl);
        saveProperties();
    }

    public String getPdbURL() {
        return this.pdbUrl;
    }

    public String getPdbLocalMirror() {
        return pdbLocalMirror;
    }

    public void setPdbLocalMirror(String pdbLocalMirror) {
        this.pdbLocalMirror = pdbLocalMirror;
        if (pdbLocalMirror == null) {
            properties.remove(pdbLocalProperty);
        } else {
            properties.setProperty(pdbLocalProperty, pdbLocalMirror);
        }
        saveProperties();
    }

    public String getI3DStructuresDirectory() {
        return i3dStructuresDir;
    }

    public String getUserStructuresDirectory() {
        return userStructuresDir;
    }

    public void setUserStructuresDirectory(String userStructuresDir) {
        this.userStructuresDir = userStructuresDir;
        if (userStructuresDir == null) {
            properties.remove(userStructuresDirProperty);
        } else {
            properties.setProperty(userStructuresDirProperty, userStructuresDir);
        }
        saveProperties();
    }

    public void setI3DStructuresDirectory(String i3dStructuresDir) {
        this.i3dStructuresDir = i3dStructuresDir;
        if (i3dStructuresDir == null) {
            properties.remove(i3dStructuresDirProperty);
        } else {
            properties.setProperty(i3dStructuresDirProperty, i3dStructuresDir);
        }
        saveProperties();
    }

    public String getExportFolder() {
        return exportFolder;
    }

    public void setExportFolder(String exportFolder) {
        this.exportFolder = exportFolder;
        properties.setProperty(exportFolderProperty, exportFolder);
        saveProperties();
    }

}
