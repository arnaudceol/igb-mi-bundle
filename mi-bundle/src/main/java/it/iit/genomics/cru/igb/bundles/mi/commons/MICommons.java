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

import java.util.HashSet;

/**
 *
 * @author Arnaud Ceol
 *
 * Properties and variables commons by other classes
 *
 */
public class MICommons {

    /**
     * If true, some features will be deactivated
     */
    public static boolean testVersion = false;

    private static MICommons instance;

    public static MICommons getInstance() {
        if (instance == null) {
            instance = new MICommons();
        }

        return instance;
    }
    
    /**
     * Proteins to ignore, e.g. with different taxid
     */
    private final HashSet<String> proteinBlackList = new HashSet<>();

    public void addProteinToBlackList(String proteinAc) {
        this.proteinBlackList.add(proteinAc);
    }

    public boolean isBlackListed(String proteinAc) {
        return this.proteinBlackList.contains(proteinAc);
    }

    /**
     * Number of the current query, used to name the tracks and the tabs.
     */
    private int queryIndex = 0;

    public int nextQueryIndex() {
        return ++queryIndex;
    }

}
