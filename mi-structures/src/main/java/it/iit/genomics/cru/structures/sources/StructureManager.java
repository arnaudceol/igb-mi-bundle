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

import java.util.HashMap;

/**
 * @author Arnaud Ceol
 *
 * Manage information about structures (both PDB and Interactome3D) in order not
 * to download the same structure twice.
 *
 */
public class StructureManager {

    /**
     *
     */
    public enum StructureSourceType {

        /**
         *
         */
        PDB,

        /**
         *
         */
        INTERACTOME3D,

        /**
         *
         */
        USER
    }

    private static StructureManager instance;

    /**
     * Associate a StructureSource to a SourceType and a local directory path
     */
    private final HashMap<StructureSourceType, HashMap<String, StructureSource>> sources = new HashMap<>();

    private StructureManager() {
        // init maps
        for (StructureSourceType source : StructureSourceType.values()) {
            sources.put(source, new HashMap<String, StructureSource>());
        }
    }

    /**
     *
     * @return
     */
    public static StructureManager getInstance() {
        if (instance == null) {
            instance = new StructureManager();
        }

        return instance;
    }

    /**
     *
     * @param type
     * @param path
     * @param cacheDir
     * @return
     */
    public StructureSource getStructureSource(StructureSourceType type,
            String path, String cacheDir) {

        if (false == sources.get(type).containsKey(path)) {
            StructureSource source;
            switch (type) {
                case PDB:
                    source = new PDBStructureSource(path);
                    source.setCacheDir(cacheDir);
                    sources.get(type).put(path, source);
                    break;
                case INTERACTOME3D:
                    source = new I3DStructureSource(path);
                    source.setCacheDir(cacheDir);
                    sources.get(type).put(path, source);
                    break;
                case USER:
                    source = new USERStructureSource(path);
                    sources.get(type).put(path, source);
                    break;
                default:

            }

        }
        return sources.get(type).get(path);
    }

}
