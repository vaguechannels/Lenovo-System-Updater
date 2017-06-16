
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import marimba.io.FastOutputStream;

import com.marimba.castanet.checksum.Checksum;
import com.marimba.castanet.checksum.ChecksumAlgorithm;
import com.marimba.castanet.checksum.ChecksumFactory;
import com.marimba.desktop.FileSystem;
import com.marimba.tools.util.OSConstants;

public class syncSourceContents {
	
	protected boolean DEBUG;
	
	// Constructor
	public syncSourceContents() {
		this.DEBUG = LenovoSystemUpdate.DEBUG;		
	}

	protected boolean syncFile(URL baseURL, String sourceFileName, File destFile, boolean overWrite) 
	{
		if (DEBUG) System.out.println(DebugInfo() + "baseURL: " + baseURL);
		if (DEBUG) System.out.println(DebugInfo() + "sourceFileName: " + sourceFileName);
		if (DEBUG) System.out.println(DebugInfo() + "destFile: " + destFile);
		if (DEBUG) System.out.println(DebugInfo() + "overWrite: " + overWrite);
		
		if (destFile.exists())	{if (overWrite) return syncFile(baseURL, sourceFileName, destFile);} 
		else { return syncFile(baseURL, sourceFileName, destFile);	}
		return false;
	}

	protected boolean syncFile(URL baseURL, String sourceFileName, File destFile) 
    {
		if (DEBUG) System.out.println(DebugInfo() + "baseURL: " + baseURL);
		if (DEBUG) System.out.println(DebugInfo() + "sourceFileName: " + sourceFileName);
		if (DEBUG) System.out.println(DebugInfo() + "destFile: " + destFile);
		
        if (DEBUG) System.out.println(DebugInfo() + "Syncing File");
        URL srcURL;
        try {srcURL = new URL(baseURL, sourceFileName);} 
        catch (MalformedURLException e) {e.printStackTrace();return false;}
        
        if (destFile.exists()) 
        {
            if (!destFile.isFile()) 
            {   //If file exists but is a directory, there's nothing we can do
            	if (DEBUG) System.out.println(DebugInfo() + "Warning: destination file '" + destFile.toString() + "' exists but is not a file");
                return false;
            }
            if (compare(srcURL, destFile)) {
                // checksums match, nothing more to do
            	if (DEBUG) System.out.println(DebugInfo() + "checksums match, don't need to copy file '" + destFile.getName() + "'");
                return true;
            }
        }
        // file does not exist or checksums don't match, so update the file
        return copy(srcURL, destFile);
    }	
    
    /**
     * Compare two files by comparing their checksums.
     * Return true if the checksums match, false otherwise.
     */
    protected boolean compare(URL srcURL, File destFile) 
    {
    	if (DEBUG) System.out.println(DebugInfo() + "srcURL: " + srcURL);
    	if (DEBUG) System.out.println(DebugInfo() + "destFile: " + destFile);
        try 
        {
            ChecksumAlgorithm alg = ChecksumFactory.getAlgorithm("MD5");
            // calculate checksum for source (storage)
            Checksum c1 = null;
            InputStream src = null;
            try {	src = srcURL.openStream();	c1 = alg.checksum(src);	} 
            catch (FileNotFoundException e)	{if (DEBUG) System.out.println(DebugInfo() + "Warning: could not retrieve checksum for source file '" + srcURL.getFile() + "'"); return false;} 
            finally { if (src != null) {src.close(); } }
            
            if (DEBUG) System.out.println(DebugInfo() + "Source Checksum: C1 is " + ((null == c1) ? "null" : c1.toString()));
            
            if (c1 == null) {if (DEBUG) System.out.println(DebugInfo() + "Warning: could not retrieve checksum for source file '" + srcURL.getFile() + "'"); return false; }
            
            // calculate checksum for destination (filesystem)
            Checksum c2;
            try {	c2 = alg.checksum(destFile); } 
            catch (FileNotFoundException e) 
            {
                // We have already determined that the file exists according to Java, but if the checksum code disagrees, return false and sync it anyway.
                // This is for defect 34266.
            	if (DEBUG) System.out.println(DebugInfo() + "Warning: could not retrieve checksum for destination file '" + destFile.getName() + "'");
                return false;
            }
            if (DEBUG) System.out.println(DebugInfo() + "Destination Checksum: C2 is " + ((null == c2) ? "null" : c2.toString()));
            
            // compare the checksums. return false if they differ
            if (c2 == null || !c1.equals(c2)) return false;
        } 
        catch (IOException e) {	e.printStackTrace();	return false;	}
        return true;
    }
    
    /**
     * Copy a file from a source to a destination, and make the file executable afterwards by setting its mode bits.
     */
    protected boolean copy(URL srcURL, File destFile) 
    {
    	if (DEBUG) System.out.println(DebugInfo() + "srcURL: " + srcURL);
    	if (DEBUG) System.out.println(DebugInfo() + "destFile: " + destFile);
        try 
        {
            // Local file doesn't exist or checksums don't match, so we'll copy the file from storage onto the filesystem.
        	if (DEBUG) System.out.println(DebugInfo() + "Checksums do not match, copying file '" + destFile.getName() + "'");
			// This is to ensure that the file has the following access permissions before the channel tries to update it:
			// Owner -  Allow Read, Allow Write, Allow Execute
			// Group -  Allow Read, Allow Write, Allow Execute
			// Others - Allow Read, Allow Write, Allow Execute
            if (destFile.exists())	{	if (OSConstants.UNIX)	{	FileSystem.setModeBits(destFile, 0x007);	}	}
            InputStream src = srcURL.openStream();
            try 
            {
                FastOutputStream dst = new FastOutputStream(destFile.getAbsolutePath());
                try { copy(src, dst); if (DEBUG) System.out.println(DebugInfo() + "Copy/Stage Succeeded"); } 
                finally { dst.close(); }
            } 
            finally { src.close(); }
        } 
        catch (IOException e) { if (DEBUG)	e.printStackTrace(); return false; }
		// Only execute permission allowed for all the users
		// Owner -  Deny Read, Deny Write, Allow Execute
		// Group -  Deny Read, Deny Write, Allow Execute
		// Others - Deny Read, Deny Write, Allow Execute
		// REMIND === Defect #42037 has been filed denoting this error in the class	"com.marimba.desktop.FileSystem"
        if (destFile.exists())	{	if (OSConstants.UNIX)	{	FileSystem.setModeBits(destFile, 0x004); }	}
        if (DEBUG) {
            // verify that we correctly set the mode bits
            int b = FileSystem.getModeBits(destFile);
            if (DEBUG) System.out.println(DebugInfo() + "mode bits set to " + b + " on file " + destFile.getAbsolutePath());
        }
        return true;
    }
    /**
     * Copies data from one stream to another.
     */
    protected void copy(InputStream in, OutputStream out)
    {
        byte[] buf = new byte[4096];
        try 
        {
            while (true) 
            {
                int n = in.read(buf, 0, buf.length);
                if (n <= 0) {	return;	}
                out.write(buf, 0, n);
            }
        } 
        catch (IOException e) {	}
    }
    
    protected static String DebugInfo() 
	{
		String currenttime = new java.text.SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z] ").format(new Date()); 
		String logtimestamp = currenttime	+ "- Client Engineering Info - " + System.getProperty("user.name") + " ";
		return logtimestamp;
	}

}
