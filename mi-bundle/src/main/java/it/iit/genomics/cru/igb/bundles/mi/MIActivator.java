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
package it.iit.genomics.cru.igb.bundles.mi;

import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.igb.bundles.mi.business.PsicquicInitWorker;
import it.iit.genomics.cru.igb.bundles.mi.view.MIPanel;

import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.affymetrix.igb.service.api.IGBService;
import com.affymetrix.igb.service.api.IGBTabPanel;
import com.affymetrix.igb.window.service.WindowActivator;

/**
 * @author Arnaud Ceol
 *
 * Bundle activator.
 *
 */
public class MIActivator extends WindowActivator implements BundleActivator {

    private final String version = "2.1";

    private final IGBLogger igbLogger;

    public MIActivator() {
        super();
        this.igbLogger = IGBLogger.getMainInstance();
    }

    @Override
    public void start(BundleContext _bundleContext) throws Exception {
        super.start(_bundleContext);


        AnalyticsConfigData config = new AnalyticsConfigData("UA-55459434-2");
        JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);
        tracker.trackEvent("Greetings", "Start IGB MI Bundle version " + version);

        // Load psicquic
        PsicquicInitWorker worker = new PsicquicInitWorker();

        worker.execute();

        igbLogger.info(
                "The Molecular Interaction Bundle is ready");

        igbLogger.debug(
                "The Molecular Interaction Bundle is ready (DEBUG)");

    }

    @Override
    protected IGBTabPanel getPage(BundleContext context, IGBService igbService) {
        return new MIPanel(igbService, "MI", "Molecular Interactions", false);
    }

}
