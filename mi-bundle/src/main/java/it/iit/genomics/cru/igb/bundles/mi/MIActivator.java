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

import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.services.XServiceRegistrar;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;

import it.iit.genomics.cru.igb.bundles.mi.business.DrugBankMapper;

/**
 * @author Arnaud Ceol
 *
 *         Bundle activator.
 *
 */
public class MIActivator extends XServiceRegistrar<IgbService> implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(MIActivator.class);

	private final String version = "2.1";

	public MIActivator() {
		super(IgbService.class);
	}

	@Override
	protected ServiceRegistration<?>[] getServices(BundleContext bundleContext, IgbService igbService)
			throws Exception {

		ServiceManager.getInstance().setService(igbService);

		// assuming last file menu item is Exit, leave it there
		// JRPMenu file_menu = igbService.getMenu("file");
		// final int index = file_menu.getItemCount() - 1;
		// file_menu.insertSeparator(index);
		return new ServiceRegistration[] {
				// bundleContext.registerService(IgbTabPanelI.class,
				// getPage(bundleContext, igbService), props)
				// bundleContext.registerService(IgbTabPanel.class, new
				// TestPanel(igbService, "TEST", "Test Bundle", false), null),
				// bundleContext.registerService(TrackClickListener.class, new
				// VCFListener(igbService), null)
		};
	}

	@Override
	public void start(BundleContext _bundleContext) throws Exception {
		super.start(_bundleContext);
		AnalyticsConfigData config = new AnalyticsConfigData("UA-55459434-2");
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);
		tracker.trackEvent("Greetings", "Start IGB MI Bundle version " + version);

		logger.info("The Molecular Interaction Bundle is ready");

		// Initialize the DrugBank mapper
		DrugBankMapper.getInstance();
	}
}
