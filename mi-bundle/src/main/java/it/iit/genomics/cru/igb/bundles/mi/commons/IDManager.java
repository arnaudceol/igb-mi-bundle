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

/**
 * @author Arnaud Ceol
 *
 * Create unique identifiers.
 *
 */
public class IDManager {

    private int nextId = 0;

    public final static String ID_PREFIX = "MI-";

    private IDManager() {

    }

    private static IDManager instance = new IDManager();

    public static IDManager getInstance() {
        if (instance == null) {
            instance = new IDManager();
        }
        return instance;
    }

    private int getNextId() {
        return ++nextId;
    }

    public String getMiID() {
        return ID_PREFIX + getNextId();
    }

}
