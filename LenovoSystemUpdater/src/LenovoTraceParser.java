
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class LenovoTraceParser {
	
	/*
	 * 
	 * Author: Karthick
	 * 
	 * This Class will parse Update_ApplicabilityRulesTrace.txt
	 * Retrieves package id and its current state.
	 * ThinInstaller is invoked with SCAN to generate Update_ApplicabilityRulesTrace.txt
	 *	
	 */
	String package_id;
    String package_state;
    
    public LenovoTraceParser (File tracelog, String package_id, boolean DEBUG)
    {
    	if (DEBUG)	System.out.println(DebugInfo() + "Initializing  Install State as : Missing");
    	setPackageState("Missing");
    	try
		{
	    	if (tracelog.exists()) 
	        {
	    		BufferedReader reader = new BufferedReader(new FileReader(tracelog));
	            String line;
	            while ((line = reader.readLine()) != null) 
	        	{
	            	//if (DEBUG) System.out.println(DebugInfo() + line);
	            	if(line.toLowerCase().contains(package_id))	setId(package_id);
	            	if(line.toLowerCase().contains("detectinstall(true)") && getId()!=null && getId().equals(package_id) )	
	            	{
	            		if (DEBUG) System.out.println(DebugInfo() + getId() + line );
	            		setPackageState("Installed"); 
	            		if (DEBUG) System.out.println(DebugInfo() + "Setting Package State: " + getPackageState());
	            		break;	            		
	            	}
	            	if(line.toLowerCase().contains("detectinstall(false)") && getId()!=null && getId().equals(package_id) )	
	            	{
	            		if (DEBUG) System.out.println(DebugInfo() + getId() + line );
	            		setPackageState("Missing"); 
	            		if (DEBUG) System.out.println(DebugInfo() + "Setting Package State: " + getPackageState());	            			            		
	            	}
	            	if(line.toLowerCase().contains("dependencies(false)") && getId()!=null && getId().equals(package_id) )	
	            	{
	            		if (DEBUG) System.out.println(DebugInfo() + getId() + line );
	            		setPackageState("Not Applicable"); 
	            		if (DEBUG) System.out.println(DebugInfo() + "Resetting Package State: " + getPackageState());
	            		break;	            		
	            	}
	            	else if(line.toLowerCase().contains("dependencies(true)") && getId()!=null && getId().equals(package_id) )	
	            	{
	            		if (DEBUG) System.out.println(DebugInfo() + getId() + line );
	            		setPackageState("Missing"); 
	            		if (DEBUG) System.out.println(DebugInfo() + "Setting Package State: " + getPackageState());
	            		break;	            		
	            	}
	        	}
	            reader.close();
	        }
	    	else {	System.out.println(DebugInfo() + "Update_ApplicabilityRulesTrace.txt does not exist");	}
		}
	    catch (IOException io) {io.printStackTrace();}	    
    }
    
    //Initializing Variables
  	public void setId(String id) {this.package_id = id;}
    public void setPackageState(String state) {	this.package_state = state; }
      
    //Retrieving Variables
  	public String getId() {return package_id;}
  	public String getPackageState() {return package_state;}
  	
  	protected String DebugInfo() 
	{
		String currenttime = new java.text.SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z] ").format(new Date()); 
		String logtimestamp = currenttime	+ "- Client Engineering Info - " + System.getProperty("user.name") + " [TraceParser] ";
		return logtimestamp;
	}
}