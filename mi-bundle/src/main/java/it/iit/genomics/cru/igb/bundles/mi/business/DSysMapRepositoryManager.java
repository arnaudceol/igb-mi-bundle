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
package it.iit.genomics.cru.igb.bundles.mi.business;

import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapDownload;
import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapLocalRepository;
import java.util.HashMap;

/**
 *
 * @author arnaudceol
 */
public class DSysMapRepositoryManager {

    private static final DSysMapRepositoryManager instance = new DSysMapRepositoryManager();

    private DSysMapRepositoryManager() {

    }

    public static DSysMapRepositoryManager getInstance() {
        return instance;
    }

    private final HashMap<String, DSysMapLocalRepository> repositories = new HashMap<>();

    public DSysMapLocalRepository getRepository(String path) {

        if (repositories.containsKey(path)) {
            return repositories.get(path);
        }

        DSysMapDownload download = new DSysMapDownload(path);

        if (false == download.isDatDownloaded()) {
            download.downloadDat();
        }

        DSysMapLocalRepository repo = new DSysMapLocalRepository(path);

        repositories.put(path, repo);

        return repo;

    }
}
