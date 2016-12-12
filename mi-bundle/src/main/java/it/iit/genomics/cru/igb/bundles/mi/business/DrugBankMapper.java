package it.iit.genomics.cru.igb.bundles.mi.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Map HET from PDB to drugbank
 * @author aceol 
 */
public class DrugBankMapper {

	private static DrugBankMapper mapper;
	
	private DrugBankMapper() {
		loadMapping();
	}
	
	
	public static DrugBankMapper getInstance() {
		if (mapper == null) {
			mapper = new DrugBankMapper();
		}
		
		return mapper;		
	}
	
	private HashMap<String, String> drugNames = new HashMap<>();
	private HashMap<String, String> drugIds = new HashMap<>();
	

	public boolean isDrug(String het) {
		return drugIds.containsKey(het);
	}
	
	
	public String getDrugName(String het) {
		return drugNames.get(het);
	}

	public String getDrugBankId(String het) {
		return drugIds.get(het);
	}
	
	public String getDrugBankLink(String het) {
		return "http://www.drugbank.ca/drugs/" + getDrugBankId(het);
	}
	
	public int getNumberOfDrugs() {
		return drugIds.keySet().size();
	}
	
	private void loadMapping() {
//        networkButton.setIcon(new ImageIcon(getClass().getResource("/network.jpg")));
		String localPath = getClass().getResource("/drug_links_wt_het.cvs").getFile();
		System.out.println("drugbang file: " + localPath);
		 BufferedReader br = null;
		 
         try {

             String sCurrentLine;
//             InputStream response =  getClass().getResource("/drug_links_wt_het.cvs").openStream();
//             response.
//             br = new BufferedReader(new FileReader(localPath));

             br = new BufferedReader(
            		    new InputStreamReader(getClass().getResource("/drug_links_wt_het.cvs").openStream()));

//            		    String inputLine;
//            		    while ((inputLine = in.readLine()) != null)
//            		        System.out.println(inputLine);
//            		    in.close();
             
             while ((sCurrentLine = br.readLine()) != null) {
            	 String[] columns = sCurrentLine.split(",") ;
            	 String id = columns[0];
            	 String name = columns[1];
            	 String het = columns[2];
            	 
            	 drugNames.put(het, name);
            	 drugIds.put(het, id);
             }
         } catch (IOException ex) {
             IGBLogger.getMainInstance().severe("Cannot read file " + localPath);
         }
	}
	
	public static void main(String[] args) {
		System.out.println(DrugBankMapper.getInstance().getNumberOfDrugs());		
	}
	
}
