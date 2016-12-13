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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;
import it.iit.genomics.cru.igb.bundles.mi.view.ProgressPanel;
import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicService;
import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicUtils;

/**
 * @author Arnaud Ceol
 *
 * Initialize PSICQUIC information: 1. get the list of available databases from
 * the registry 2. do a query to get the ones with Uniprot references.
 *
 */
public class PsicquicInitWorker extends SwingWorker<List<Integer>, String> {

    private static JProgressBar progressBar;

    public final static String nullServer = "== none ==";

	private static final Logger logger = LoggerFactory.getLogger(PsicquicInitWorker.class);

    
    @Override
    protected List<Integer> doInBackground() throws Exception {

        int progress = 0;
        // Initialize progress property.
        setProgress(0);
        publish("Load Psicquic services");

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);

        ProgressPanel.getInstance().addBar(progressBar);

        PsicquicUtils psiquic = PsicquicUtils.getInstance();
        
        Collection<PsicquicService> activeServices;
                 
        try {
        activeServices = psiquic
                .getActiveServerFromRegistry();
        } catch (BridgesRemoteAccessException e) {
        	logger.error("Cannot reach the PSICQUIC registry, either the registry is down or there is a network problem.");
            publish("Cannot reach the PSICQUIC registry!");
            setProgress(100);

            return null;
        }
        
        int progressSteps = 100 / (activeServices.size() + 1);

        progress += progressSteps;

        setProgress(progress);

        psiquic.addUrl(nullServer, nullServer);

        for (PsicquicService service : activeServices) {

            publish(service.getName());

            if (service.isActive()) {
                try {
                    if (service.getClient().countByQuery(
                            "idA:uniprotkb AND idB:uniprotkb") > 0) {
                        psiquic.addUrl(service.getName(), service.getRestUrl());
                    } else {
                        logger.warn(service.getName() + " has no Uniprot xrefs, skip.");
                        publish(service.getName() + " has no Uniprot xrefs, skip");
                    }
                } catch (IOException ioe) {                        
                	logger.warn("Cannot access psicquic service "
                            + service.getName());
                    publish("Cannot access psicquic service "
                            + service.getName());
                }
            } else { logger.warn("PSICQUIC service " + service.getName() + " is not active, skip");
                publish(service.getName() + " is not active, skip");
            }

            progress += progressSteps;
            setProgress(progress);

        }

        publish("done");
        setProgress(100);

        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String message : chunks) {
            progressBar.setValue(getProgress());
            progressBar.setString("Load Psicquic services: " + message);
        }
    }

    @Override
    protected void done() {
        MIView.getInstance()
                .getMiSearch()
                .setPsicquicProviders(
                        PsicquicUtils.getInstance().getProviderNames());

    }
}
