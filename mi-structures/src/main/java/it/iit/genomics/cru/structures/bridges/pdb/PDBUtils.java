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

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Arnaud Ceol
 *
 * Utilities to access PDB
 *
 */
public class PDBUtils {

    private static final HashMap<String, String> pdbUrls = new HashMap<>();

    /**
     *
     * @return
     */
    public static Collection<String> getAvailablePDBProviders() {
        return pdbUrls.keySet();
    }

    static {
        String[][] pdbProviders = {
            {"PDBe (UK)", "ftp://ftp.ebi.ac.uk/pub/databases/rcsb/pdb/"},
            {"RCSB PDB (USA)", "ftp://ftp.wwpdb.org/pub/pdb/"},
            {"PDBj (Japan)", "ftp://ftp.pdbj.org/pub/pdb/"}
        };

        for (String[] provider : pdbProviders) {
            pdbUrls.put(provider[0], provider[1]);
        }

    }

    /**
     *
     * @return
     */
    public static String[] getProviderNames() {
        return pdbUrls.keySet().toArray(new String[pdbUrls.keySet().size()]);
    }

    /**
     *
     * @param name
     * @return
     */
    public static String getUrl(String name) {
        return pdbUrls.get(name);
    }

}
