package it.iit.genomics.cru.igb.bundles.mi;

import org.lorainelab.igb.services.IgbService;



public class ServiceManager {

	private IgbService service;
	
	private static ServiceManager manager;
	
	public static ServiceManager getInstance() {
		if (manager == null) {
			manager = new ServiceManager();
		}
		return manager;
	}
	

	public void setService(IgbService service) {
		this.service = service;
	}
	public IgbService getService() {
		return service;
	}
	
	
	
}
