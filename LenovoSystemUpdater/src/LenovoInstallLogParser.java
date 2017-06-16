import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class LenovoInstallLogParser {
	
	/*
	 * 
	 * Author: Karthick
	 * 
	 * This Class will parse Installation Log & Retrieve Package Install Date/Status
	 * 
	 */
	
	String package_installdate;
    String package_status;
    String[] package_values;
    
    public LenovoInstallLogParser (File installlog , String package_id, boolean DEBUG)
    {
    	if (DEBUG)	System.out.println(DebugInfo() + "Initializing  Install Status as : UnDefined");
    	setPackageInstallStatus("UnDefined");
    	
    	if (DEBUG)	System.out.println(DebugInfo() + "Initializing  Install Date as : 00/00/0000");
    	setPackageInstallDate("00/00/0000");
    	
    	try
		{
    		if (DEBUG)	System.out.println(DebugInfo() + "Verify Existance of Installation Log...........");
	    	if (installlog.exists()) 
	        {
	    		BufferedReader reader = new BufferedReader(new FileReader(installlog));
	            String line = null;
	            if (DEBUG)	System.out.println(DebugInfo() + "Parsing Installation Log for attributes of package Id: " + package_id);
	            while ((line = reader.readLine()) != null) 
	        	{
	            	if(line.toLowerCase().startsWith(package_id))
	            	{
	            		package_values = line.split(":");
	            		if (DEBUG)	System.out.println(DebugInfo() + Arrays.toString(package_values));

	            		//Reset Package Install Date
	            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Install Date as : " + package_values[2].toString().trim());
	            		
	            		setPackageInstallDate(package_values[2].toString().trim()); 
	                      
	            		//Reset Package Install Status
	                    if (DEBUG)	System.out.println(DebugInfo() + "Resetting Install Status as : " + package_values[3].toString().trim());
	                    setPackageInstallStatus(package_values[3].toString().trim());
	            	}
	        	}
	            reader.close();
	        }
	    	else {	System.out.println(DebugInfo() + "Installation log does not exist");	}
		}
	    catch (IOException io) {io.printStackTrace();}
    }
    
    //Initializing Variables
  	public void setPackageInstallDate(String version) {this.package_installdate = version;}
    public void setPackageInstallStatus(String version) {this.package_status = version;}
      
    //Retrieving Variables
  	public String getPackageInstallDate() {return package_installdate;}
  	public String getPackageInstallStatus() {return package_status;}
  	
  	protected String DebugInfo() 
	{
		String currenttime = new java.text.SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z] ").format(new Date()); 
		String logtimestamp = currenttime	+ "- Client Engineering Info - " + System.getProperty("user.name") + " [InstallLogParser] ";
		return logtimestamp;
	}
}