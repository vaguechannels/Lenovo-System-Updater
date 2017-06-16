
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class LenovoPackageXMLParser {
	
	/*
	 * Author: Karthick
	 * Using StAX Parser
	 * 
	 * This Class parses database.xml from staged content
	 * Retrieves Package ID/Name/Title/Version/RebootType/SeverityType/ReleaseDate
	 */
	
	String tagContent;
	String package_id;
    String package_name;
    String package_title;
    String package_version;
    String package_reboottype;
    String package_severitytype;
    String package_releasedate;
    
    public LenovoPackageXMLParser(File packagexml, boolean DEBUG)
    {
    	FileInputStream xmlFile;
		try 
		{
			xmlFile = new FileInputStream(packagexml);
			XMLInputFactory factory = XMLInputFactory.newInstance();
		    XMLStreamReader reader = factory.createXMLStreamReader(xmlFile);
		    if (DEBUG)	System.out.println(DebugInfo() + "Parsing Lenovo Package XML");
		    while(reader.hasNext())
		    {
		    	int event = reader.next();
		    	switch(event)
		    	{
		    		case XMLStreamConstants.START_ELEMENT: 
		        	if(reader.getAttributeCount() != 0)
		        	{
		        		if(reader.getAttributeCount() == 0)	System.out.println(reader.getLocalName() + ":");
		        		for(int i=0;i<reader.getAttributeCount();i++)
		            	{
		            		if(reader.getLocalName().toString().toLowerCase().equals("package") && reader.getAttributeName(i).toString().equals("name"))
		            		{
		            			if (DEBUG)	System.out.println(DebugInfo() + "Retrieved " + reader.getLocalName() + "_" + reader.getAttributeName(i) + ":" + reader.getAttributeValue(i));
		            			
		            			//Reset Package Name
			            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package Name as: " + reader.getAttributeValue(i));
			            		setPackageName(reader.getAttributeValue(i));
		            		}
		            		if(reader.getLocalName().toString().toLowerCase().equals("package") && reader.getAttributeName(i).toString().equals("id"))
		            		{
		            			if (DEBUG)	System.out.println(DebugInfo() + "Retrieved " + reader.getLocalName() + "_" + reader.getAttributeName(i) + ":" + reader.getAttributeValue(i));
		            			
		            			//Reset Package ID
			            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package ID as: " + reader.getAttributeValue(i));
			            		setId(reader.getAttributeValue(i));
		            		}
		            		if(reader.getLocalName().toString().toLowerCase().equals("package") && reader.getAttributeName(i).toString().equals("version"))
		            		{
		            			if (DEBUG)	System.out.println(DebugInfo() + "Retrieved " + reader.getLocalName() + "_" + reader.getAttributeName(i) + ":" + reader.getAttributeValue(i));
		            			
		            			//Reset Package Version
			            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package Version as: " + reader.getAttributeValue(i));
			            		setPackageVersion(reader.getAttributeValue(i));
		            		}
		            		
		            		if(reader.getLocalName().toString().toLowerCase().equals("severity") && reader.getAttributeName(i).toString().equals("type"))
		            		{
		            			if (DEBUG)	System.out.println(DebugInfo() + "Retrieved " + reader.getLocalName() + "_" + reader.getAttributeName(i) + ":" + reader.getAttributeValue(i));
		            			
		            			//Reset Package SeverityType
			            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package SeverityType as: " + reader.getAttributeValue(i));
			            		setPackageSeverityType(reader.getAttributeValue(i));
		            		}
		            		if(reader.getLocalName().toString().toLowerCase().equals("reboot") && reader.getAttributeName(i).toString().equals("type"))
		            		{
		            			if (DEBUG)	System.out.println(DebugInfo() + "Retrieved " + reader.getLocalName() + "_" + reader.getAttributeName(i) + ":" + reader.getAttributeValue(i));
		            			
		            			//Reset Package RebootType
			            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package RebootType as: " + reader.getAttributeValue(i));
			            		setPackageRebootType(reader.getAttributeValue(i));
		            		}
		            	}
		        	}
		        	break;
		         
		        case XMLStreamConstants.CHARACTERS:
		        	tagContent = reader.getText().trim();
		        	break;
	
		        case XMLStreamConstants.END_ELEMENT:
		        	if(reader.getLocalName().toString().toLowerCase().equals("desc"))	
		        	{
		        		//Reset Package Title
	            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package Title as: " + tagContent);
		        		setPackageTitle(tagContent);
		        	}
		        	if(reader.getLocalName().toString().toLowerCase().equals("releasedate"))	
		        	{
		        		//Reset Package ReleaseDate
	            		if (DEBUG)	System.out.println(DebugInfo() + "Resetting Package ReleaseDate as: " + tagContent);
		        		setPackageReleaseDate(tagContent);		        	
		        	}
		        	break;
	
		        case XMLStreamConstants.START_DOCUMENT:
		        	break;
		    	}	    
		    }	    
		} 
		catch (FileNotFoundException | XMLStreamException e){	e.printStackTrace();	}
    }

	//Initializing Variables
	public void setId(String id) {this.package_id = id;}
    public void setPackageName(String name) {this.package_name = name;}
    public void setPackageTitle(String title) {this.package_title = title;}
    public void setPackageVersion(String version) {this.package_version = version;}
    public void setPackageRebootType(String version) {this.package_reboottype = version;}
    public void setPackageSeverityType(String version) {this.package_severitytype = version;}
    public void setPackageReleaseDate(String version) {this.package_releasedate = version;}
    
    //Retrieving Variables
	public String getId() {return package_id;}
	public String getPackageName() {return package_name;}
	public String getPackageTitle() {return package_title;}
	public String getPackageVersion() {return package_version;}
	public String getPackageRebootType() {return package_reboottype;}
	public String getPackageSeverityType() {return package_severitytype;}
	public String getPackageReleaseDate() {return package_releasedate;}   
	
	protected String DebugInfo() 
	{
		String currenttime = new java.text.SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z] ").format(new Date()); 
		String logtimestamp = currenttime	+ "- Client Engineering Info - " + System.getProperty("user.name") + " [XMLParser] ";
		return logtimestamp;
	}
}
