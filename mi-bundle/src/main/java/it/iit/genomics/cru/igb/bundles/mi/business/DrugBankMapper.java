package it.iit.genomics.cru.igb.bundles.mi.business;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;

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
		String localPath = getClass().getResource("/drug_links_wt_het.cvs").getFile();
		System.out.println("drugbang file: " + localPath);
		 BufferedReader br = null;

         try {

             String sCurrentLine;

             br = new BufferedReader(new FileReader(localPath));

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
