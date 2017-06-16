
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


//Importing Swing Components
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;


//Importing Marimba Components
import com.marimba.intf.application.IApplication;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.ILauncher;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.util.IConfig;

//Importing JNA Components
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;

public class LenovoSystemUpdate extends JFrame implements ActionListener, ItemListener, IApplication {

	//Global Variables
	protected static String ForceInstallOn, APPLICATION_NAME, APPLICATION_VERSION, FRAME_TITLE;
	protected static long CURRENT_EPOCH = System.currentTimeMillis()/1000;
	protected static boolean DEBUG, is64bit, userSelectionDone = false;
	protected static File CHANNEL_DIRECTORY;
	protected static int seconds = 0;
		
	//Marimba Objects
	protected static IApplicationContext context;
	protected static IWorkspace workspace;
	protected static ILauncher launcher;
	protected static IConfig tunerConfig;	//To Access Tuner Properties
	protected static IConfig config;		//To Access Channel Parameters
	protected static IConfig chConfig;		//To Access Channel Properties
	
	//Prompt Variables
	protected static String PROMPTMESSAGE_TIMETAKEN, PROMPTMESSAGE_RESTART;
	protected static String background = "background.jpg";
	
	protected static JButton BUTTON_PROCEED, BUTTON_POSTPONE, BUTTON_SNOOZE;
	protected static JLabel PROMPTMESSAGE_BACKGROUNDLABEL;
	protected static JComboBox SNOOZE_LIST = new JComboBox();
	protected static int SNOOZE_INDEX;
		
	//Completed Variables
	protected static String COMPLETEDMESSAGE_HEADER, COMPLETEDMESSAGE_BAR;
	protected static boolean TIMER_EXPIRED = false;
	protected static int TIMER_COMPLETION = 1;	//Value In Minutes
	protected static Timer TIMER;
	protected static JProgressBar progressBar;
	protected static JTextField jtext01,jtext02;
	protected static JLabel labelleft, labelright;
	protected static String logo = "logo.png";
	
	//Progress Variables
	protected static String PROGRESSMESSAGE_HEADER, PROGRESSMESSAGE_BAR, PROGRESSMESSAGE_FOOTER;
	
	//Reboot Variables
	protected static String REBOOTMESSAGE_COMPLETE, REBOOTMESSAGE_RESTARTREQUIRED, REBOOTMESSAGE_SAVEWORK;
	protected static int TIMER_REBOOT = 5;	//Value In Minutes
	
	//SystemUpdate Variables
	protected static String COMPLIANCE = "Compliant";
	protected static File STAGE_DIR = null , INSTALL_LOG = null, TRACE_LOG = null;
	
	//ProcessKill Variables 
	protected static final String TASKLIST = "tasklist";
	protected static final String KILL = "taskkill /F /T /IM ";
	protected static ArrayList<String> LIST_PROCESS = null;
	
	syncSourceContents syncSRC = new syncSourceContents();	
	
	@Override
	public void notify(Object target, int msg, Object arg) {
	 switch (msg) {
		
		case APP_INIT:
			context = (IApplicationContext) arg;
			config = context.getConfiguration();
			launcher = (ILauncher) context.getFeature("launcher");
			workspace = (IWorkspace) context.getFeature("workspace");
			tunerConfig = (IConfig) context.getFeature("config");
			chConfig = workspace.getChannelCreate(this.context.getChannelURL().toString());
			//chConfig.setProperty("update.schedule","every 1 days update at 4:00am");
			//chConfig.setProperty("start.schedule","every 1 days update every 360 minutes");
			//chConfig.setProperty("start.schedule.blackoutexempt","true");
			CHANNEL_DIRECTORY = new File(context.getDataDirectory().substring(0, context.getDataDirectory().length() - 5));
			copySourceFiles();
			try {Thread.sleep(2000);} catch (InterruptedException e1) {	e1.printStackTrace();}//Pause for two seconds
			break;
			
		case APP_START:
			System.out.println(DebugInfo() + "Notification received APP_START....");
			
			String debugflag = tunerConfig.getProperty("dsg.marimba.lenovosystemupdate.debug.enabled");
			if (debugflag!= null && debugflag.equals("true")) {	 DEBUG = true;	}	
			if (DEBUG) System.out.println(DebugInfo() + "DebugInfo# " + "Debug flag enabled, So we will print all debug messages");
			
			try
			{	
				//Initialize Variables
				initializeVariables();
				
				/*************************************Entering Rabbit Hole*************************************/
				
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				
				if(!isLenovoUpdatesCompliant())
				{
					//Verify if user is logged on
					if(!System.getProperty("user.name").endsWith("$"))
					{
						if((Long.valueOf(getvalue("dsg.lenovosystemupdate.forceinstall.active")) - CURRENT_EPOCH) > 0 || !getvalue("dsg.lenovosystemupdate.forceinstall.enabled").equals("true"))
						{
							if((CURRENT_EPOCH - Long.valueOf(getvalue("dsg.lenovosystemupdate.prompt.next")))>0)
							{
								if (DEBUG) System.out.println(DebugInfo() + "Value of userSelectionDone before starting Proceed/Postpone Frame: " + userSelectionDone);
								if (DEBUG) System.out.println(DebugInfo() + "Starting Proceed/Postpone Frame");
								
								System.out.println(DebugInfo() + "FRAME_TITLE" + FRAME_TITLE);
								
								LenovoSystemUpdate POPFrame = new LenovoSystemUpdate(FRAME_TITLE, APPLICATION_NAME, APPLICATION_VERSION, Boolean.valueOf(getvalue("dsg.lenovosystemupdate.forceinstall.enabled")),Boolean.valueOf(getvalue("dsg.lenovosystemupdate.proceed.enabled")),Boolean.valueOf(getvalue("dsg.lenovosystemupdate.postpone.enabled")));
								POPFrame.setVisible(true);								
								
								setvalue("dsg.lenovosystemupdate.prompt.last" , Long.toString(CURRENT_EPOCH));
								if (DEBUG) System.out.println(DebugInfo() + "Waiting On User's Choice.");
								
								setvalue("dsg.lenovosystemupdate.prompt.count" , Integer.toString(Integer.parseInt(getvalue("dsg.lenovosystemupdate.prompt.count"))+1));
								while(!userSelectionDone)	Thread.sleep(3000);	
								
								if(getvalue("dsg.lenovosystemupdate.exitcode").equals("0"))	startLenovoUpdates();								
							}
							else
							{
								System.out.println(DebugInfo() + "Next prompt time not reached. We will prompt in " +  (Long.valueOf(getvalue("dsg.lenovosystemupdate.prompt.next")) - CURRENT_EPOCH)/60  + " Minutes");
								setvalue("dsg.lenovosystemupdate.exitcode", "1");
								if (DEBUG) System.out.println(DebugInfo() + "Failing channel since we have not reached next prompt time");
								context.stop();
							}
						}
						else
						{
							setvalue("dsg.lenovosystemupdate.exitcode", "0");
							System.out.println(DebugInfo() + "Mandatory installation time reached. Installation will begin........");									
							if(getvalue("dsg.lenovosystemupdate.exitcode").equals("0"))	startLenovoUpdates();							
						}
					}
					else 
					{
						System.out.println(DebugInfo() + "No LoggedOn User: Installation will begin........");
						setvalue("dsg.lenovosystemupdate.exitcode", "0");
						if(thinInstaller("INSTALL") == 0)	thinInstaller("SCAN");
						context.stop();						
					}
				}
				else
				{
					System.out.println(DebugInfo() + "All Lenovo update packages are compliant. Stopping channnel....");
				}
			} 
			catch (Exception e) {e.printStackTrace();}
	 	    context.stop();
			break;
     
		case APP_ARGV:
            context.stop();
            break;
		
		case APP_STOP:
			System.out.println(DebugInfo() + "Notification received APP_STOP...");
			if(getvalue("dsg.lenovosystemupdate.terminateprocess.enabled").equals("true"))	killProcess();	//Process Kills
			disposeOrphanFrames();
			break;
	 	}
	}
	
	protected void startLenovoUpdates()
	{
		try
		{
			thinInstaller("SCAN");
			if(thinInstaller("INSTALL") == 0)
			{
				thinInstaller("SCAN");
				System.out.println(DebugInfo() + "Retrieving the Progress Message Dialog, so we can close it");			        	
				disposeOrphanFrames();
				
	        	//At this point the Progress Message Dialog should have been disposed
	        	if(getvalue("dsg.lenovosystemupdate.reboot.required").equals("true"))
	        	{
		        	if(config.getProperty("dsg.lenovosystemupdate.reboot.timer") != null) 
		    		{	
		    			if (DEBUG) System.out.println(DebugInfo() + "Retrieved Reboot Timer is " + config.getProperty("dsg.lenovosystemupdate.reboot.timer") + " Minutes");	
		    			TIMER_REBOOT = Integer.parseInt(config.getProperty("dsg.lenovosystemupdate.reboot.timer"));				
		    		}
		    		shutdowntool(TIMER_REBOOT);
	    		}
	        	else
	        	{
	        		System.out.println(DebugInfo() + "Launching Completion Dialog");
	        		LenovoSystemUpdate CD = new LenovoSystemUpdate(FRAME_TITLE, APPLICATION_NAME, APPLICATION_VERSION, TIMER_COMPLETION);
					CD.setVisible(true);
					TIMER = new Timer(1000, CD);
					TIMER.start();
					if (DEBUG) System.out.println(DebugInfo() + "Value of TIMER_EXPIRED before Launching Completion Dialog: " + TIMER_EXPIRED);
					while(!TIMER_EXPIRED)Thread.sleep(3000);
					System.out.println(DebugInfo() + "Retrieving the Completion Dialog Frame, so we can close it");			        	
					disposeOrphanFrames();
	        	}
			}
		}
		catch (Exception e) {e.printStackTrace();}		
	}
	
	protected void initializeVariables()
	{
		if (DEBUG) System.out.println(DebugInfo() + "Initializing Variables..............");
		
		//Create a calendar
		Calendar cal = Calendar.getInstance();
		  
		APPLICATION_NAME = "Lenovo System Updates";
		APPLICATION_VERSION = new SimpleDateFormat("MMM").format(cal.getTime()) + " " + cal.get(Calendar.YEAR);
				
		if(config.getProperty("dsg.lenovosystemupdate.application.name") != null){
			APPLICATION_NAME = config.getProperty("dsg.lenovosystemupdate.application.name");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized APPLICATION_NAME : " + APPLICATION_NAME);}
		
		if(config.getProperty("dsg.lenovosystemupdate.application.version") != null){
			APPLICATION_VERSION = config.getProperty("dsg.lenovosystemupdate.application.version");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized APPLICATION_VERSION : " + APPLICATION_VERSION);}
		
		FRAME_TITLE = new StringBuilder().append("Dicks Sporting Goods - ").append(APPLICATION_NAME).append(" ").append(APPLICATION_VERSION).toString();
		
		/*************************************Resetting Prompt Message Variables*************************************/
		
		PROMPTMESSAGE_TIMETAKEN = "This update will take approximately 30-45 minutes to complete. All " + APPLICATION_NAME 
				+ " related applications will be shutdown during the update and a reboot will occur once completed. We strongly recommend to save all your work before proceeding with the updates.";
		
		PROMPTMESSAGE_RESTART = "NOTE: Once the update has started, please do not open any " + APPLICATION_NAME 
				+ " based applications or shutdown/restart your computer. The system will let you know once the update is complete and ready for a reboot.";
		
		if(config.getProperty("dsg.lenovosystemupdate.promptmessage.timetaken") != null){
			PROMPTMESSAGE_TIMETAKEN = config.getProperty("dsg.lenovosystemupdate.promptmessage.timetaken");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROMPTMESSAGE_TIMETAKEN : " + PROMPTMESSAGE_TIMETAKEN);}
		
		if(config.getProperty("dsg.lenovosystemupdate.promptmessage.restart") != null){
			PROMPTMESSAGE_RESTART = config.getProperty("dsg.lenovosystemupdate.promptmessage.restart");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROMPTMESSAGE_RESTART : " + PROMPTMESSAGE_RESTART);}
		
		/*************************************Resetting Progress Message Variables*************************************/
		
		PROGRESSMESSAGE_HEADER = APPLICATION_NAME + " " + APPLICATION_VERSION + ": Update is in progress.";
    	PROGRESSMESSAGE_BAR = "Please do not access " + APPLICATION_NAME + " or shutdown/restart your computer";
    	PROGRESSMESSAGE_FOOTER = "The system will prompt you to reboot once the update is complete."; 
		
		if(config.getProperty("dsg.lenovosystemupdate.progressmessage.header") != null){
			PROGRESSMESSAGE_HEADER = config.getProperty("dsg.lenovosystemupdate.progressmessage.header");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_HEADER : " + PROGRESSMESSAGE_HEADER);}
		
		if(config.getProperty("dsg.lenovosystemupdate.progressmessage.bar") != null){
			PROGRESSMESSAGE_BAR = config.getProperty("dsg.lenovosystemupdate.progressmessage.bar");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_BAR : " + PROGRESSMESSAGE_BAR);}
		
		if(config.getProperty("dsg.lenovosystemupdate.progressmessage.footer") != null){
			PROGRESSMESSAGE_FOOTER = config.getProperty("dsg.lenovosystemupdate.progressmessage.footer");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_FOOTER : " + PROGRESSMESSAGE_FOOTER);}
		
		/*************************************Resetting Completion Dialog Message Variables*************************************/
		
		COMPLETEDMESSAGE_HEADER = APPLICATION_NAME + " " + APPLICATION_VERSION + ": Update is complete.";
		COMPLETEDMESSAGE_BAR = "Please reboot your machine if prompted before accessing " + APPLICATION_NAME;					
		
		if(config.getProperty("dsg.lenovosystemupdate.completedmessage.header") != null){
			COMPLETEDMESSAGE_HEADER = config.getProperty("dsg.lenovosystemupdate.completedmessage.header");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_HEADER : " + COMPLETEDMESSAGE_HEADER);}
		
		if(config.getProperty("dsg.lenovosystemupdate.completedmessage.bar") != null){
			COMPLETEDMESSAGE_BAR = config.getProperty("dsg.lenovosystemupdate.completedmessage.bar");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_BAR : " + COMPLETEDMESSAGE_BAR);}
		
		if(config.getProperty("dsg.lenovosystemupdate.completion.timer") != null){
			TIMER_COMPLETION = Integer.parseInt(config.getProperty("dsg.lenovosystemupdate.completion.timer"));
			if (DEBUG) System.out.println(DebugInfo() + "Initialized TIMER_COMPLETION : " + TIMER_COMPLETION);}	
		
		
		/*************************************Resetting Reboot Message Variables*************************************/
		
		REBOOTMESSAGE_COMPLETE = APPLICATION_NAME + " " + APPLICATION_VERSION + " Update is complete. A reboot is required to complete the installation.";
		REBOOTMESSAGE_RESTARTREQUIRED = "Please do not attempt to access " + APPLICATION_NAME + " before restarting the computer.";
		REBOOTMESSAGE_SAVEWORK = "\n\n***Please save all your work. This computer will restart in 5 minutes***";
		
		if(config.getProperty("dsg.lenovosystemupdate.rebootmessage.updatecomplete") != null){
			REBOOTMESSAGE_COMPLETE = config.getProperty("dsg.lenovosystemupdate.rebootmessage.updatecomplete");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized REBOOTMESSAGE_COMPLETE : " + REBOOTMESSAGE_COMPLETE);}
		
		if(config.getProperty("dsg.lenovosystemupdate.rebootmessage.restarrequired") != null){
			REBOOTMESSAGE_RESTARTREQUIRED = config.getProperty("dsg.lenovosystemupdate.rebootmessage.restarrequired");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized REBOOTMESSAGE_RESTARTREQUIRED : " + REBOOTMESSAGE_RESTARTREQUIRED);}
		
		if(config.getProperty("dsg.lenovosystemupdate.rebootmessage.savework") != null){
			REBOOTMESSAGE_SAVEWORK = config.getProperty("dsg.lenovosystemupdate.rebootmessage.savework");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized REBOOTMESSAGE_SAVEWORK : " + REBOOTMESSAGE_SAVEWORK);}	
		
		if(config.getProperty("dsg.lenovosystemupdate.reboot.timer") != null){
			TIMER_REBOOT = Integer.parseInt(config.getProperty("dsg.lenovosystemupdate.reboot.timer"));
			if (DEBUG) System.out.println(DebugInfo() + "Initialized TIMER_REBOOT : " + TIMER_REBOOT);}		
		
		
		/*************************************Initializing/Resetting InteractiveConsole Variables*************************************/
		
		//Initializing dsg.lenovosystemupdate.proceed.enabled
		if(getvalue("dsg.lenovosystemupdate.proceed.enabled") == null)
		{		
			setvalue("dsg.lenovosystemupdate.proceed.enabled", "true");	
			//Resetting dsg.lenovosystemupdate.proceed.enabled
			if(config.getProperty("dsg.lenovosystemupdate.proceed.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.proceed.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.proceed.enabled", config.getProperty("dsg.lenovosystemupdate.proceed.enabled"));
			}												
		}
		
		//Initializing dsg.lenovosystemupdate.postpone.enabled
		if(getvalue("dsg.lenovosystemupdate.postpone.enabled") == null)
		{	
			setvalue("dsg.lenovosystemupdate.postpone.enabled", "true");
			//Resetting dsg.lenovosystemupdate.postpone.enabled
			if(config.getProperty("dsg.lenovosystemupdate.postpone.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.postpone.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.postpone.enabled", config.getProperty("dsg.lenovosystemupdate.postpone.enabled"));
			}
		}
		
		//Initializing dsg.lenovosystemupdate.snooze.enabled
		if(getvalue("dsg.lenovosystemupdate.snooze.enabled") == null)
		{			
			setvalue("dsg.lenovosystemupdate.snooze.enabled", "true");
			//Resetting dsg.lenovosystemupdate.snooze.enabled
			if(config.getProperty("dsg.lenovosystemupdate.snooze.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.snooze.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.snooze.enabled", config.getProperty("dsg.lenovosystemupdate.snooze.enabled"));
			}
		}
		
		//Initializing dsg.lenovosystemupdate.progress.enabled
		if(getvalue("dsg.lenovosystemupdate.progress.enabled") == null)
		{			
			setvalue("dsg.lenovosystemupdate.progress.enabled", "true");
			//Resetting dsg.lenovosystemupdate.progress.enabled
			if(config.getProperty("dsg.lenovosystemupdate.progress.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.progress.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.progress.enabled", config.getProperty("dsg.lenovosystemupdate.progress.enabled"));
			}						
		}
		
		//Initializing dsg.lenovosystemupdate.terminateprocess.enabled
		if(getvalue("dsg.lenovosystemupdate.terminateprocess.enabled") == null)
		{			
			setvalue("dsg.lenovosystemupdate.terminateprocess.enabled", "false");
			//Resetting dsg.lenovosystemupdate.terminateprocess.enabled
			if(config.getProperty("dsg.lenovosystemupdate.terminateprocess.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.terminateprocess.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.terminateprocess.enabled", config.getProperty("dsg.lenovosystemupdate.terminateprocess.enabled"));
				//Initializing dsg.lenovosystemupdate.terminateprocess.list
				if(getvalue("dsg.lenovosystemupdate.terminateprocess.list") == null)
				{
					if(config.getProperty("dsg.lenovosystemupdate.terminateprocess.list") != null)
					{
						setvalue("dsg.lenovosystemupdate.terminateprocess.list", config.getProperty("dsg.lenovosystemupdate.terminateprocess.list"));
						if (DEBUG) System.out.println(DebugInfo() + "Initialized Process Termination List : " + getvalue("dsg.lenovosystemupdate.terminateprocess.list"));
					}
				}
			}
		}
		
		//Initializing dsg.lenovosystemupdate.postpone.maxDays
		if(getvalue("dsg.lenovosystemupdate.postpone.maxDays")==null)
		{		
			setvalue("dsg.lenovosystemupdate.postpone.maxDays", "30");
			//Resetting dsg.lenovosystemupdate.postpone.maxDays
			if(config.getProperty("dsg.lenovosystemupdate.postpone.maxDays") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.postpone.maxDays' with Package configured value");
				setvalue("dsg.lenovosystemupdate.postpone.maxDays", config.getProperty("dsg.lenovosystemupdate.postpone.maxDays"));
			}
		}
		
		//Initializing dsg.lenovosystemupdate.postpone.maxAttempts
		if(getvalue("dsg.lenovosystemupdate.postpone.maxAttempts")==null)
		{	
			setvalue("dsg.lenovosystemupdate.postpone.maxAttempts", "10");
			//Resetting dsg.lenovosystemupdate.postpone.maxAttempts
			if(config.getProperty("dsg.lenovosystemupdate.postpone.maxAttempts") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.postpone.maxAttempts' with Package configured value");
				setvalue("dsg.lenovosystemupdate.postpone.maxAttempts", config.getProperty("dsg.lenovosystemupdate.postpone.maxAttempts"));
			}
		}
		
		//Initializing dsg.lenovosystemupdate.forceinstall.enabled
		if(getvalue("dsg.lenovosystemupdate.forceinstall.enabled")==null)
		{	
			setvalue("dsg.lenovosystemupdate.forceinstall.enabled", "false");
			//Resetting dsg.lenovosystemupdate.forceinstall.enabled
			if(config.getProperty("dsg.lenovosystemupdate.forceinstall.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.forceinstall.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.forceinstall.enabled", config.getProperty("dsg.lenovosystemupdate.forceinstall.enabled"));
			}
		}

		//Initializing dsg.lenovosystemupdate.reboot.abort.enabled
		if(getvalue("dsg.lenovosystemupdate.reboot.abort.enabled")==null)
		{	
			setvalue("dsg.lenovosystemupdate.reboot.abort.enabled", "false");
			//Resetting dsg.lenovosystemupdate.reboot.abort.enabled
			if(config.getProperty("dsg.lenovosystemupdate.reboot.abort.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.reboot.abort.enabled' with Package configured value");
				setvalue("dsg.lenovosystemupdate.reboot.abort.enabled", config.getProperty("dsg.lenovosystemupdate.reboot.abort.enabled"));
			}
		}
		
		/*************************************Initializing default InteractiveConsole Variables*************************************/
		
		//ExitCode
		if(getvalue("dsg.lenovosystemupdate.exitcode")==null)	setvalue("dsg.lenovosystemupdate.exitcode", "1");

		//Select Snooze Number
		if(getvalue("dsg.lenovosystemupdate.snooze.index")==null)	setvalue("dsg.lenovosystemupdate.snooze.index", "1");
		
		//Select Snooze Hours
		if(getvalue("dsg.lenovosystemupdate.snooze.choosen")==null)	setvalue("dsg.lenovosystemupdate.snooze.choosen", "0");
		
		//Last Prompted
		if(getvalue("dsg.lenovosystemupdate.prompt.last")==null)	setvalue("dsg.lenovosystemupdate.prompt.last", "0");
		
		//Next Scheduled Prompt(If Postponed)
		if(getvalue("dsg.lenovosystemupdate.prompt.next")==null)	setvalue("dsg.lenovosystemupdate.prompt.next", "0");
		
		//Compliance Level
		if(getvalue("dsg.lenovosystemupdate.compliance.level")==null)	setvalue("dsg.lenovosystemupdate.compliance.level", "Compliant");
		
		//Total Prompt Times
		if(getvalue("dsg.lenovosystemupdate.prompt.count")==null)	setvalue("dsg.lenovosystemupdate.prompt.count", "0");
		
		//Which Button Clicked(Proceed/Postpone/Snooze)
		if(getvalue("dsg.lenovosystemupdate.button.clicked")==null)	setvalue("dsg.lenovosystemupdate.button.clicked", "none");						
		
		//Is Reboot Really Required?
		if(getvalue("dsg.lenovosystemupdate.reboot.required")==null)	setvalue("dsg.lenovosystemupdate.reboot.required", "false");
		
		//Mandatory Install Date(Days)
		if(getvalue("dsg.lenovosystemupdate.forceinstall.active")==null)	setvalue("dsg.lenovosystemupdate.forceinstall.active", "0");
						
		if(getvalue("dsg.lenovosystemupdate.forceinstall.enabled").equals("true"))	
		{
			System.out.println(DebugInfo() + "Reconfiguring 'dsg.marimba.interactiveuserconsole.forceinstall.active' with Package configured value");
			setvalue("dsg.lenovosystemupdate.forceinstall.active", Long.toString(CURRENT_EPOCH + (86400*(Integer.parseInt(getvalue("dsg.lenovosystemupdate.postpone.maxDays"))))));
			ForceInstallOn = new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z ").format(new Date (Long.valueOf(getvalue("dsg.lenovosystemupdate.forceinstall.active"))*1000));
		}
		
		if (DEBUG) System.out.println(DebugInfo() + "Current TimeStamp: " + CURRENT_EPOCH);
		System.out.println(DebugInfo() + "Current Display Date: " + new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z ").format(new Date (CURRENT_EPOCH*1000)));				
		
		//Display next prompt related information only if they are already set
		if(!(Long.valueOf(getvalue("dsg.lenovosystemupdate.prompt.next")) == 0))
		{
			if (DEBUG) System.out.println(DebugInfo() + "Next Prompt TimeStamp: " + Long.valueOf(getvalue("dsg.lenovosystemupdate.prompt.next")));
			System.out.println(DebugInfo() + "Next Prompt Display Date: " + new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z ").format(new Date (Long.valueOf(getvalue("dsg.lenovosystemupdate.prompt.next"))*1000)));
			if (DEBUG) System.out.println(DebugInfo() + "Difference: " + (CURRENT_EPOCH - Long.valueOf(getvalue("dsg.lenovosystemupdate.prompt.next")))/60 + " Minutes");
		}
	}
	
	protected void disposeOrphanFrames()
	{
		final Window[] allOpenProgressMessageWindows = Window.getWindows();
    	if (allOpenProgressMessageWindows != null)
    	{
    	    for (final Window window : allOpenProgressMessageWindows)
    	    {
    	        if(window.toString().contains(FRAME_TITLE))	
    	        {
    	        	System.out.println(DebugInfo() + "Found Frame:" + window);
        	        window.dispose();
        	        System.out.println(DebugInfo() + "Frame Disposed");
    	        }			        	        
    	    }
    	}
	}

	
	public LenovoSystemUpdate() {}
	
	//PROMPT
	public LenovoSystemUpdate(String frameTitle, String AppName, String AppVersion, boolean Mandatory,boolean proceed,boolean postpone) 
	{		
		getContentPane().setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(null);
		setTitle(frameTitle);
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setBounds(100, 100, 800, 558);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JLabel lblApplicationName = new JLabel("Mandatory Update Required - " + AppName + " " + AppVersion);
		lblApplicationName.setForeground(Color.RED);
		lblApplicationName.setBounds(0, 150, 800, 50);
		lblApplicationName.setHorizontalAlignment(SwingConstants.CENTER);
		lblApplicationName.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 21));
		getContentPane().add(lblApplicationName);
		
		JLabel lblTimetaken = new JLabel(convertToMultiline(PROMPTMESSAGE_TIMETAKEN));
		lblTimetaken.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTimetaken.setHorizontalAlignment(SwingConstants.CENTER);
		lblTimetaken.setBounds(50, 350, 700, 50);
		lblTimetaken.setForeground(Color.BLACK);
		getContentPane().add(lblTimetaken);
		
		JLabel lblRestart = new JLabel(convertToMultiline(PROMPTMESSAGE_RESTART));
		lblRestart.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRestart.setHorizontalAlignment(SwingConstants.CENTER);
		lblRestart.setBounds(50, 400, 700, 50);
		lblRestart.setForeground(Color.BLACK);
		getContentPane().add(lblRestart);
		
		JLabel labelforceStart = new JLabel("AFTER " + ForceInstallOn + "YOU WILL NOT LONGER BE PROMPTED & INSTALLATION WILL BEGIN"); 
		labelforceStart.setHorizontalAlignment(SwingConstants.CENTER);
		labelforceStart.setForeground(Color.RED);
		labelforceStart.setFont(new Font("Tahoma", Font.BOLD, 13));
		labelforceStart.setBounds(50, 430, 700, 50);
		//Show only if force install is enabled
		if(Mandatory)getContentPane().add(labelforceStart);
		
		BUTTON_PROCEED = new JButton("PROCEED");
		BUTTON_PROCEED.setBackground(new Color(143, 188, 143));
		BUTTON_PROCEED.setFont(new Font("Tahoma", Font.BOLD, 11));
		BUTTON_PROCEED.setBounds(200, 480, 100, 50);
		BUTTON_PROCEED.addActionListener(this);
		//To Enable/Disable Proceed option
		BUTTON_PROCEED.setEnabled(proceed);
		getContentPane().add(BUTTON_PROCEED);
		
		BUTTON_POSTPONE = new JButton("POSTPONE");
		BUTTON_POSTPONE.setBackground(new Color(143, 188, 143));
		BUTTON_POSTPONE.setFont(new Font("Tahoma", Font.BOLD, 11));
		BUTTON_POSTPONE.setBounds(500, 480, 100, 50);
		BUTTON_POSTPONE.addActionListener(this);
		//To Enable/Disable Postpone option
		BUTTON_POSTPONE.setEnabled(postpone);
		getContentPane().add(BUTTON_POSTPONE);
		
		PROMPTMESSAGE_BACKGROUNDLABEL = new JLabel("");
		PROMPTMESSAGE_BACKGROUNDLABEL.setEnabled(true);
		PROMPTMESSAGE_BACKGROUNDLABEL.setBounds(0, 0, 800, 500);
		//PROMPTMESSAGE_BACKGROUNDLABEL.setIcon(new ImageIcon(this.getClass().getResource(background))); //Works for IScript
		PROMPTMESSAGE_BACKGROUNDLABEL.setIcon(new ImageIcon(context.getDataDirectory() + background));	//Works for IApplication
		getContentPane().add(PROMPTMESSAGE_BACKGROUNDLABEL);		
	}
	
	//SNOOZE
	public LenovoSystemUpdate(String frameTitle, String AppName, String AppVersion, boolean snooze, int snoozeLeft) 
	{
		getContentPane().setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(null);
		setTitle(frameTitle);
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setBounds(100, 100, 400, 150);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JLabel lblMandatoryUpdateRequired = new JLabel("Mandatory Update Required - " + AppName + " " + AppVersion);
		lblMandatoryUpdateRequired.setHorizontalAlignment(SwingConstants.CENTER);
		lblMandatoryUpdateRequired.setForeground(Color.BLACK);
		lblMandatoryUpdateRequired.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblMandatoryUpdateRequired.setBounds(0, 0, 394, 50);
		getContentPane().add(lblMandatoryUpdateRequired);
		
		SNOOZE_LIST.setModel(new DefaultComboBoxModel(new String[] {"1 Hour", "2 Hours", "4 Hours", "6 Hours", "8 Hours"}));
		SNOOZE_LIST.setFont(new Font("Tahoma", Font.BOLD, 11));
		SNOOZE_LIST.setBackground(Color.LIGHT_GRAY);
		SNOOZE_LIST.setMaximumRowCount(5);
		SNOOZE_LIST.setBounds(214, 90, 75, 25);
		SNOOZE_LIST.addItemListener(this);
		SNOOZE_LIST.setSelectedItem(-1);
		getContentPane().add(SNOOZE_LIST);
		
		BUTTON_SNOOZE = new JButton("SNOOZE");
		BUTTON_SNOOZE.setFont(new Font("Tahoma", Font.BOLD, 11));
		BUTTON_SNOOZE.setBackground(Color.LIGHT_GRAY);
		BUTTON_SNOOZE.setBounds(299, 90, 85, 25);
		BUTTON_SNOOZE.addActionListener(this);
		BUTTON_SNOOZE.setEnabled(snooze);	//To Enable/Disable Postpone option
		getContentPane().add(BUTTON_SNOOZE);
		
		JLabel lblSnoozeIn = new JLabel("Click Snooze to be reminded in....");
		lblSnoozeIn.setHorizontalAlignment(SwingConstants.CENTER);
		lblSnoozeIn.setForeground(Color.BLACK);
		lblSnoozeIn.setBounds(0, 87, 215, 30);
		getContentPane().add(lblSnoozeIn);
		
		JLabel lblSnoozeLeft = new JLabel("You may snooze this upto " + snoozeLeft + " times");
		lblSnoozeLeft.setHorizontalAlignment(SwingConstants.CENTER);
		lblSnoozeLeft.setForeground(Color.BLACK);
		lblSnoozeLeft.setBounds(44, 50, 302, 25);
		getContentPane().add(lblSnoozeLeft);	
	}
	
	//PROGRESS
	public LenovoSystemUpdate(String frameTitle, String headermessage, String barmessage, String footermessage)
	{
		getContentPane().setBackground(new Color(255, 255, 224));
		getContentPane().setLayout(null);
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setVisible(false);
		validate();
		setBounds(100, 100, 845, 85);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle(frameTitle);		
		
		jtext01 = new JTextField();
		jtext01.setBounds(140, 0, 560, 20);
		jtext01.setForeground(Color.DARK_GRAY);
		jtext01.setBackground(Color.WHITE);
		jtext01.setHorizontalAlignment(SwingConstants.CENTER);
		jtext01.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
		jtext01.setEditable(false);
		jtext01.setText(headermessage);
		getContentPane().add(jtext01);		
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(140, 20, 560, 20);
		progressBar.setIndeterminate(true);
		progressBar.setForeground(new Color(0, 128, 128));
		progressBar.setBackground(Color.WHITE);
		progressBar.setFont(new Font("Tahoma", Font.BOLD, 11));
		progressBar.setString(barmessage);
		getContentPane().add(progressBar);
				
		jtext02 = new JTextField();
		jtext02.setBounds(140, 40, 560, 20);
		jtext02.setForeground(Color.DARK_GRAY);
		jtext02.setBackground(Color.WHITE);
		jtext02.setFont(new Font("Tahoma", Font.BOLD, 11));
		jtext02.setHorizontalAlignment(SwingConstants.CENTER);
		jtext02.setEditable(false);
		jtext02.setText(footermessage);
		getContentPane().add(jtext02);
		
		labelleft = new JLabel("");
		labelleft.setBounds(0, 0, 145, 65);
		//labelleft.setIcon(new ImageIcon(this.getClass().getResource(logo)));	//Works with IScript
		labelleft.setIcon(new ImageIcon(context.getDataDirectory() + logo));	//Works with IApplication
		getContentPane().add(labelleft);
		
		labelright = new JLabel("");
		labelright.setBounds(700, 0, 145, 65);
		//labelright.setIcon(new ImageIcon(this.getClass().getResource(logo)));	//Works with IScript
		labelright.setIcon(new ImageIcon(context.getDataDirectory() + logo)); //Works with IApplication
		getContentPane().add(labelright);
	}
	
	//COMPLETED
	public LenovoSystemUpdate(String frameTitle, String AppName, String AppVersion, int min) 
	{
		setType(Type.UTILITY);
		setResizable(false);
		getContentPane().setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
		getContentPane().setForeground(Color.WHITE);
		getContentPane().setLayout(null);
		setAlwaysOnTop(true);
		setBounds(100, 100, 845, 85);
		setLocationRelativeTo(null);
		setTitle(frameTitle);		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);			
		
		jtext01 = new JTextField();
		jtext01.setBounds(140, 0, 560, 20);
		jtext01.setForeground(Color.BLACK);
		jtext01.setBackground(Color.WHITE);
		jtext01.setHorizontalAlignment(SwingConstants.CENTER);
		jtext01.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 12));
		jtext01.setEditable(false);
		jtext01.setText(COMPLETEDMESSAGE_HEADER);
		getContentPane().add(jtext01);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(140, 20, 560, 20);
		progressBar.setForeground(new Color(0, 128, 128));
		progressBar.setBackground(Color.WHITE);
		progressBar.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		progressBar.setString(COMPLETEDMESSAGE_BAR);
		getContentPane().add(progressBar);
		
		jtext02 = new JTextField();
		jtext02.setBounds(140, 40, 560, 20);
		jtext02.setForeground(Color.BLACK);
		jtext02.setBackground(Color.WHITE);
		jtext02.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 12));
		jtext02.setHorizontalAlignment(SwingConstants.CENTER);
		jtext02.setEditable(false);
		jtext02.addActionListener(this);
		getContentPane().add(jtext02);		
		
		labelleft = new JLabel("");
		labelleft.setBounds(0, 0, 145, 65);
		//labelleft.setIcon(new ImageIcon(this.getClass().getResource(logo)));	//Works with IScript
		labelleft.setIcon(new ImageIcon(context.getDataDirectory() + logo));	//Works with IApplication
		getContentPane().add(labelleft);
		
		labelright = new JLabel("");
		labelright.setBounds(700, 0, 145, 65);
		//labelright.setIcon(new ImageIcon(this.getClass().getResource(logo)));	//Works with IScript
		labelright.setIcon(new ImageIcon(context.getDataDirectory() + logo)); //Works with IApplication
		getContentPane().add(labelright);				
	}
	
	public void itemStateChanged(ItemEvent p) 
	{
		if(p.getStateChange() == ItemEvent.SELECTED) 
		{	
			SNOOZE_INDEX = SNOOZE_LIST.getSelectedIndex();	
			if (DEBUG) System.out.println(DebugInfo() + "Index Selected per Item Listener: " + SNOOZE_INDEX);
		}
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == BUTTON_PROCEED)
		{
			if (DEBUG) System.out.println(DebugInfo() + "Proceed Button was clicked. Updating Boolean userSelectionDone with " + userSelectionDone);
			System.out.println(DebugInfo() + "User Choose to Proceed, So We Will Proceed");
			setvalue("dsg.lenovosystemupdate.exitcode", "0");
			setvalue("dsg.lenovosystemupdate.button.clicked", "proceed");
			setvalue("dsg.lenovosystemupdate.reboot.required", "true");
			System.out.println(DebugInfo() + "Disposing Proceed/Postpone Frame");	closeframe();			
			if(getvalue("dsg.lenovosystemupdate.progress.enabled").equals("true"))	showProgressMsg();
			userSelectionDone = true;			
		}
		
		else if(e.getSource() == BUTTON_POSTPONE)
		{
			System.out.println(DebugInfo() + "User Choose to Postpone, So We Will Postpone");
			setvalue("dsg.lenovosystemupdate.exitcode", "1");
			setvalue("dsg.lenovosystemupdate.reboot.required", "false");			
			System.out.println(DebugInfo() + "Disposing Proceed/Postpone Frame");	closeframe();
			setvalue("dsg.lenovosystemupdate.button.clicked", "postpone");	ifpostponed();
			setvalue("dsg.lenovosystemupdate.postpone.maxAttempts", Integer.toString(Integer.parseInt(getvalue("dsg.lenovosystemupdate.postpone.maxAttempts"))-1));
			if(!(Integer.parseInt(getvalue("dsg.lenovosystemupdate.postpone.maxAttempts")) > 0))	setvalue("dsg.lenovosystemupdate.postpone.enabled", "false");			
			System.out.println(DebugInfo() + "Waiting On User's Choice.");
		} 
		
		else if(e.getSource() == BUTTON_SNOOZE)	
		{	
			if (DEBUG) System.out.println(DebugInfo() + "Snooze Button was clicked. Updating Boolean userSelectionDone with " + userSelectionDone);
			if(!(Integer.parseInt(getvalue("dsg.lenovosystemupdate.postpone.maxAttempts")) > 0))	setvalue("dsg.lenovosystemupdate.postpone.enabled", "false");
			System.out.println(DebugInfo() + "Index Selected per Action Listener: " + SNOOZE_INDEX);
			setvalue("dsg.lenovosystemupdate.snooze.index", Integer.toString(SNOOZE_INDEX));
			System.out.println(DebugInfo() + "User Choose to Postpone: " + SNOOZE_LIST.getSelectedItem());
			setvalue("dsg.lenovosystemupdate.snooze.choosen", SNOOZE_LIST.getSelectedItem().toString());
			setvalue("dsg.lenovosystemupdate.prompt.next", Long.toString(CURRENT_EPOCH + (3600*(Integer.parseInt(SNOOZE_LIST.getSelectedItem().toString().substring(0 ,SNOOZE_LIST.getSelectedItem().toString().indexOf(" ")))))));
			setvalue("dsg.lenovosystemupdate.exitcode", "1");
			setvalue("dsg.lenovosystemupdate.button.clicked", "snooze");
			setvalue("dsg.lenovosystemupdate.reboot.required", "false");
			if (DEBUG) System.out.println(DebugInfo() + "Disposing Snooze Frame");	closeframe();
			userSelectionDone = true;
		}		
		else if(e.getSource() == TIMER)
		{
			jtext02.setText("This Notification Message Will Close in: " + TIMER_COMPLETION + " Minutes "+ String.format("%02d", seconds)+ " Seconds");
			seconds--;	//Decrement Seconds
			//If Countdown Expired
			if(seconds==0 && TIMER_COMPLETION==0)
			{
				TIMER.stop();
				System.out.println(DebugInfo() + "Countdown Expired. Disposing Completion Dialog");
				TIMER_EXPIRED = true;
				closeframe();				
			}
			//Reset Seconds to decrement from 59.
			if(seconds==0 || seconds<0)
			{
				seconds=59; 
				TIMER_COMPLETION--;
				System.out.println(DebugInfo() + "Seconds Reset & Minute Decrement Completed");				
			}			
		}				
	}
	
	protected void closeframe()
	{
		this.setVisible(false);	//Notify JFrame to Close	
		this.dispose();			//Kills JFrame	
	}
	
	protected void startFrame()
	{
		this.setVisible(true);	//Start JFrame 
	}	
	
	protected void ifpostponed()
	{
		try 
		{
			userSelectionDone = false;
			if (DEBUG) System.out.println(DebugInfo() + "userSelectionDone value before starting Snooze Frame" + userSelectionDone);
			if (DEBUG) System.out.println(DebugInfo() + "Starting Snooze Frame");
			if (DEBUG) System.out.println(DebugInfo() + "FrameTitle : " + FRAME_TITLE);
			if (DEBUG) System.out.println(DebugInfo() + "Application Name : " + APPLICATION_NAME);
			
			LenovoSystemUpdate snoozeframe = new LenovoSystemUpdate(FRAME_TITLE, APPLICATION_NAME, APPLICATION_VERSION, Boolean.valueOf(getvalue("dsg.lenovosystemupdate.snooze.enabled")), Integer.parseInt(getvalue("dsg.lenovosystemupdate.postpone.maxAttempts")));
			snoozeframe.startFrame();
		} 
		catch (Exception e) {e.printStackTrace();}
	}
	
	protected void showProgressMsg()
	{
		try 
		{
			System.out.println(DebugInfo() + "Launching Progress Dialog");
			if (DEBUG) System.out.println(DebugInfo() + "FrameTitle : " + FRAME_TITLE);
			if (DEBUG) System.out.println(DebugInfo() + "PROGRESSMESSAGE_HEADER : " + PROGRESSMESSAGE_HEADER);
			if (DEBUG) System.out.println(DebugInfo() + "PROGRESSMESSAGE_BAR : " + PROGRESSMESSAGE_BAR);
			if (DEBUG) System.out.println(DebugInfo() + "PROGRESSMESSAGE_FOOTER : " + PROGRESSMESSAGE_FOOTER);
			
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			
			LenovoSystemUpdate progressframe = new LenovoSystemUpdate(FRAME_TITLE, PROGRESSMESSAGE_HEADER, PROGRESSMESSAGE_BAR, PROGRESSMESSAGE_FOOTER);
			progressframe.startFrame();			
		} 
		catch (Exception e) {e.printStackTrace();}
	}
	
	protected int shutdowntool(int timer) 
	{
		if (DEBUG) System.out.println(DebugInfo() + "REBOOTMESSAGE_COMPLETE : " + REBOOTMESSAGE_COMPLETE);
		if (DEBUG) System.out.println(DebugInfo() + "REBOOTMESSAGE_RESTARTREQUIRED : " + REBOOTMESSAGE_RESTARTREQUIRED);
		if (DEBUG) System.out.println(DebugInfo() + "REBOOTMESSAGE_SAVEWORK : " + REBOOTMESSAGE_SAVEWORK);
		
		String rebootmessage =  new StringBuilder().append(REBOOTMESSAGE_COMPLETE).append(REBOOTMESSAGE_RESTARTREQUIRED).append(REBOOTMESSAGE_SAVEWORK).toString();
		
		String[] command;	
		
		if (getvalue("dsg.lenovosystemupdate.reboot.abort.enabled").equals("false")) 
		{
			command = new String[] {CHANNEL_DIRECTORY + "\\data\\" + "ShutdownTool.exe" , "/r", "/t:"+ timer*60, "/m:0", "/d:" + rebootmessage, "/c", "/n"}; //No Option To Abort
		}
		else
		{
			command = new String[] {CHANNEL_DIRECTORY + "\\data\\" + "ShutdownTool.exe" , "/r", "/t:"+ timer*60, "/m:0", "/d:" + rebootmessage, "/n"};	//Displays Option To Abort
		}		
				
		if(DEBUG) 
		{	
			System.out.print(DebugInfo() + "Formulated Command: ");	
			for (String element : command)	{System.out.print(element + " ");}	System.out.println();	
		}
		try 
		{
			ProcessBuilder pb = new ProcessBuilder(command);
			Process process = pb.start();
			if(DEBUG) System.out.println(DebugInfo() + "Initiating Process: " + pb.command());		
			return 0;						
		} 
		catch (IOException e) {	e.printStackTrace(); return 1;	}
	}
	
	protected boolean isLenovoUpdatesCompliant()
	{
		boolean isCompliant = true;
		COMPLIANCE = "Compliant"; 
		try 
		{
			STAGE_DIR = new File("C:/Windows/ImgMgr/Stage/LenovoUpdates");
			setvalue("dsg.lenovosystemupdate.stage.directory", STAGE_DIR.toString());
			
			INSTALL_LOG = new File("C:/ProgramData/Lenovo/ThinInstaller", new StringBuilder().append(InetAddress.getLocalHost().getHostName()).append("Installation.log").toString());
			setvalue("dsg.lenovosystemupdate.installation.log", INSTALL_LOG.toString());
			
			TRACE_LOG = new File(new StringBuilder().append(ProgramFilesMacro()).append("\\ThinInstaller\\logs").toString() , new StringBuilder().append("Update_ApplicabilityRulesTrace.txt").toString());
			setvalue("dsg.lenovosystemupdate.trace.log", TRACE_LOG.toString());
			
			if(STAGE_DIR.exists())
			{
				File[] packageDir = STAGE_DIR.listFiles();
				for (File folder : packageDir) 
				{
					if (folder.isDirectory()) 
					{
						if(DEBUG)	System.out.println(DebugInfo() + "Package Directory: " + folder);
						File [] packageContent = folder.listFiles();
						
						for (File files : packageContent)
						{
							if(files.toString().toLowerCase().contains(".xml") && files.getName().toLowerCase().contains(folder.getName()))
							{
								if(DEBUG)	System.out.println(DebugInfo() + "Package XML: " + files);
								
								LenovoPackageXMLParser packagexmlparser = new LenovoPackageXMLParser(files, DEBUG);
								LenovoTraceParser traceparcer = new LenovoTraceParser(TRACE_LOG, packagexmlparser.getId(), DEBUG);
								LenovoInstallLogParser installlogparser = new LenovoInstallLogParser(INSTALL_LOG , packagexmlparser.getId(), DEBUG);
								
								//LenovoPackageXMLParser Mandatory Variables
								System.out.println(DebugInfo() + "Package ID: " + packagexmlparser.getId());
								System.out.println(DebugInfo() + "Package Name: " + packagexmlparser.getPackageName());
								System.out.println(DebugInfo() + "Package Title: " + packagexmlparser.getPackageTitle());
								System.out.println(DebugInfo() + "Package Version: " + packagexmlparser.getPackageVersion());

								//LenovoPackageXMLParser Optional Variables
								if(DEBUG)	System.out.println(DebugInfo() + "Package RebootType: " + packagexmlparser.getPackageRebootType());
								if(DEBUG)	System.out.println(DebugInfo() + "Package SeverityType: " + packagexmlparser.getPackageSeverityType());
								if(DEBUG)	System.out.println(DebugInfo() + "Package ReleaseDate: " + packagexmlparser.getPackageReleaseDate());
								
								//LenovoTraceParser Variables
								System.out.println(DebugInfo() + "Package State: " + traceparcer.getPackageState());
								
								//Upload Captured Information To Registry
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package Name", packagexmlparser.getPackageName());
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package Title", packagexmlparser.getPackageTitle());
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package Version", packagexmlparser.getPackageVersion());
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package RebootType", packagexmlparser.getPackageRebootType());
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package SeverityType", packagexmlparser.getPackageSeverityType());
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package ReleaseDate", packagexmlparser.getPackageReleaseDate());
								setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package InstallState", traceparcer.getPackageState());
								
								//LenovoInstallLogParser Variables
								if(traceparcer.getPackageState().equals("Installed") && installlogparser.getPackageInstallStatus().equals("Missing"))
								{
									System.out.println(DebugInfo() + "Package InstallStatus: " + "Effectively Installed");
									setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package InstallStatus", "Effectively Installed");
								}
								else if(traceparcer.getPackageState().equals("Not Applicable") && installlogparser.getPackageInstallStatus().equals("Missing"))
								{
									System.out.println(DebugInfo() + "Package InstallStatus: " + "Not Applicable");
								}
								else
								{	
									System.out.println(DebugInfo() + "Package InstallStatus: " + installlogparser.getPackageInstallStatus());
									setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package InstallStatus", installlogparser.getPackageInstallStatus());
									
									System.out.println(DebugInfo() + "Package InstallDate: " + installlogparser.getPackageInstallDate());
									setRegKey(RegistryMacro() + "DSG\\LenovoUpdates\\" + packagexmlparser.getId(), "Package InstallDate", installlogparser.getPackageInstallDate());								
								}
								
								//Before initial run, all status will be Missing 
								if(!traceparcer.getPackageState().equals("Installed") && !installlogparser.getPackageInstallStatus().equals("Success") && !traceparcer.getPackageState().equals("Not Applicable"))
								{
									isCompliant = false;	COMPLIANCE = "Non-Compliant";	System.out.println(DebugInfo() + "Package Compliance: " + COMPLIANCE);
								}
								
								if(!traceparcer.getPackageState().equals("Installed") && installlogparser.getPackageInstallStatus().equals("Success")){	System.out.println(DebugInfo() + "Package Compliance: Compliant");	}
								
								if(traceparcer.getPackageState().equals("Installed") || traceparcer.getPackageState().equals("Not Applicable")){	System.out.println(DebugInfo() + "Package Compliance: Compliant");	}
								
								System.out.println();
							}
						}				
					}
				}
			}
			else {	System.out.println(DebugInfo() + "Package Stage Directory is empty or does not exist");	}
		} 
		catch (IOException e) {e.printStackTrace();}		
		setvalue("dsg.lenovosystemupdate.compliance.level", COMPLIANCE);
		System.out.println(DebugInfo() + "Overall Compliance: " + COMPLIANCE);	
		return isCompliant;
	}
	
	protected int thinInstaller(String action)
	{
		int thinInstallerExitCode = 0;
		
		System.setProperty("jna.debug_load", config.getProperty("jna.debug_load"));
		if(DEBUG) System.out.println(DebugInfo() + "jna.debug_load=" + System.getProperty("jna.debug_load"));
		
		System.setProperty("jna.debug_load.jna", config.getProperty("jna.debug_load.jna"));
		if(DEBUG) System.out.println(DebugInfo() + "jna.debug_load.jna=" + System.getProperty("jna.debug_load.jna"));
		
		System.setProperty("jna.nosys", config.getProperty("jna.nosys"));
		if(DEBUG) System.out.println(DebugInfo() + "jna.nosys=" + System.getProperty("jna.nosys"));
		
		WString PAYANARPEYAR = new WString("srv-LenovoAdmin");
		WString DOMAIN = new WString("CORPORATE");
		WString KADAVUSOLL = new WString("LPuBB+qex6zHVs4T");
		
		String CMD = "C:\\Windows\\System32\\CMD.exe /C start /min";
	    if(DEBUG) System.out.println(DebugInfo() + "CMD: " + CMD);
	    
	    String localRepository = "C:\\Windows\\ImgMgr\\Stage\\LenovoUpdates";
	    if(DEBUG) System.out.println(DebugInfo() + "Local Repository: " + localRepository);
	    
	    String currentDir = new StringBuilder().append(ProgramFilesMacro()).append("\\ThinInstaller\\").toString();
	    if(DEBUG) System.out.println(DebugInfo() + "Current Directory: " + currentDir);
	    
	    String ThinInstallerExe = "ThinInstaller.exe";
	    if(DEBUG) System.out.println(DebugInfo() + "ThinInstaller Exe: " + ThinInstallerExe);
	    
	    String CommandLine = new StringBuilder().toString();

	    //Action Install
	  	//ThinInstaller.exe /CM -search R -action INSTALL –Repository C:\Windows\ImgMgr\Stage\LenovoUpdates -showprogress -noicon -includerebootpackages 1,3,4 -noreboot
	    if(action.toUpperCase().equals("INSTALL"))
	    {
	    	CommandLine = new StringBuilder().append(CMD).append(" ").append(ThinInstallerExe).append(" ").append("/CM -search R -action INSTALL").append(" ").
	    			append("-Repository").append(" ").append(localRepository).append(" -showprogress").append(" -noicon").append(" -includerebootpackages").append(" 1,3,4").append(" -noreboot").toString();
		    if(DEBUG) System.out.println(DebugInfo() + "CommandLine " + CommandLine);		    
	    }
	    	
	  	//Action Scan
	    //ThinInstaller.exe /CM -search R -action SCAN –Repository C:\Windows\ImgMgr\Stage\LenovoUpdates
	    if(action.toUpperCase().equals("SCAN"))
	    {
	    	CommandLine = new StringBuilder().append(CMD).append(" ").append(ThinInstallerExe).append(" ").append("/CM -search R -action SCAN").append(" ").append("-Repository").append(" ").append(localRepository).toString();
	    	if(DEBUG) System.out.println(DebugInfo() + "CommandLine " + CommandLine);
	    }
	    
	  	String jnalibpath = new StringBuilder().append(CHANNEL_DIRECTORY.toString()).append("\\data").toString();
	    if(DEBUG) System.out.println(DebugInfo() + "JNA Library Path " + jnalibpath);
	    
	    System.setProperty("jna.library.path", jnalibpath);
		System.setProperty("jna.boot.library.path", jnalibpath);
	   	System.setProperty("jna.platform.library.path", jnalibpath);
		
	   	if(DEBUG) System.out.println(DebugInfo() + "jna.boot.library.name & jnidispatch " + System.getProperty("jna.boot.library.name", "jnidispatch"));
	   	if(DEBUG) System.out.println(DebugInfo() + "jna.platform.library.path " + System.getProperty("jna.platform.library.path"));
	   	if(DEBUG) System.out.println(DebugInfo() + "jna.boot.library.path " + System.getProperty("jna.boot.library.path"));
	   	if(DEBUG) System.out.println(DebugInfo() + "jna.library.path " + System.getProperty("jna.library.path"));
	   	
	   	if(DEBUG) System.out.println(DebugInfo() + "java.library.path " + System.getProperty("java.library.path"));
	   	if(DEBUG) System.out.println(DebugInfo() + "java.class.path " + System.getProperty("java.class.path"));
    
    	boolean result = MoreAdvApi32.INSTANCE.CreateProcessWithLogonW
	       (PAYANARPEYAR,									// lpUsername
	    	DOMAIN,                   						// lpDomain , null if local
	    	KADAVUSOLL,										// lpPassword
	        MoreAdvApi32.LOGON_NETCREDENTIALS_ONLY,			// dwLogonFlags
	        null,											// lpApplicationName
	        new WString(CommandLine),						// lpCommandLine
	        MoreAdvApi32.CREATE_NEW_CONSOLE,				// dwCreationFlags
	        null,                                           // lpEnvironment
	        new WString(currentDir),        				// lpCurrentDirectory
	        new STARTUPINFO(),								// lpStartupInfo
	        new PROCESS_INFORMATION());						// lpProcessInfo
	        
    	if (!result) 
    	{
    		int error = Kernel32.INSTANCE.GetLastError();
    		System.out.println(DebugInfo() + "OS error #" + error);
    		if (error == 0 ) thinInstallerExitCode = 0;
    		System.out.println(DebugInfo() + Kernel32Util.formatMessageFromLastErrorCode(error));
	    }
    	
    	System.out.println(DebugInfo() + "ThinInstaller Action " + action + " Exit Code:#" + Kernel32.INSTANCE.GetLastError() + "." + Kernel32Util.formatMessageFromLastErrorCode(Kernel32.INSTANCE.GetLastError()));
    	
    	//Sleeping for 5 seconds.
    	try {	while (isProcessRunning(ThinInstallerExe))Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
    	
    	//SCAN Validation
    	if(action.toUpperCase().equals("SCAN"))
    	{
	    	if(isLenovoUpdatesCompliant())	{	System.out.println(DebugInfo() + "All Lenovo Updates are Compliant");	}
	    	else {	System.out.println(DebugInfo() + "One of More Lenovo Updates are Non-Compliant. Please review logs for more information");	}
    	}
		
		return thinInstallerExitCode;
	}
	
	protected void copySourceFiles() 
	{
		if (DEBUG) System.out.println(DebugInfo() + "Initializing source files");
		URL url = context.getBaseURL();			
		 
        File stageDir = new File(context.getDataDirectory());
        if (!stageDir.exists()) 
        {
        	if (DEBUG) System.out.println(DebugInfo() + "Setting up directory: " + stageDir);
        	stageDir.mkdir();
        }
        syncSRC.syncFile(url,"lib/jna-4.2.2.jar",new File(context.getDataDirectory() + File.separator + "jna-4.2.2.jar"),false);
        syncSRC.syncFile(url,"lib/jna-platform-4.2.2.jar",new File(context.getDataDirectory() + File.separator + "jna-platform-4.2.2.jar"),false);
        syncSRC.syncFile(url,"lib/jnidispatch.jar",new File(context.getDataDirectory() + File.separator + "jnidispatch.jar"),false);        
        
        syncSRC.syncFile(url,"files/background.jpg",new File(context.getDataDirectory() + File.separator + "background.jpg"),false);
        syncSRC.syncFile(url,"files/logo.png",new File(context.getDataDirectory() + File.separator + "logo.png"),false);
        syncSRC.syncFile(url,"files/ShutdownTool.exe",new File(context.getDataDirectory() + File.separator + "ShutdownTool.exe"),false);
        syncSRC.syncFile(url,"files/ShutdownTool.png",new File(context.getDataDirectory() + File.separator + "ShutdownTool.png"),false);
	}	
	
	protected String getvalue(String propertyname)
	{
		String propertyvalue = null;
		if(DEBUG) System.out.println(DebugInfo() + "Searching for property: " + propertyname);
		propertyvalue = chConfig.getProperty(propertyname);
		if(DEBUG) System.out.println(DebugInfo() + "Retrieved value of " + propertyname + " is " + propertyvalue);
		return propertyvalue;
	}
	
	protected void setvalue(String propertyname, String propertyvalue)
	{	
		boolean setvaluesuccess = false;
		chConfig.setProperty(propertyname, propertyvalue);
		setvaluesuccess = chConfig.getProperty(propertyname).equals(propertyvalue);
		if(setvaluesuccess)	System.out.println(DebugInfo() + "Successfully updated " + propertyname + " with " + propertyvalue);
        else System.out.println(DebugInfo() + "Unable to update " + propertyname);
	}
	
	protected boolean isProcessRunning(String serviceName) 
	{
		try
		{
			 Process p = Runtime.getRuntime().exec(TASKLIST);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 String line;
			 while ((line = reader.readLine()) != null) 
			 {
				 //if(DEBUG)System.out.println(DebugInfo() + line);
				 if (line.toLowerCase().startsWith(serviceName.toLowerCase())) 
				 {	
					 System.out.println(DebugInfo() + serviceName + " is running...");
					 return true;	
				 }				  
			 }
			 System.out.println(DebugInfo() + serviceName + " is not running");
			 return false;
		}
		catch (IOException io) {io.printStackTrace();} 
		return false;
	}

	protected void killProcess() 
	{
		try	
		{
			System.out.println(DebugInfo() + "Retrieved Process Kill List ");
			LIST_PROCESS = new ArrayList(Arrays.asList(getvalue("dsg.lenovosystemupdate.terminateprocess.list").split("\\s*,\\s*")));
			for (String processname : LIST_PROCESS) 
			{
				//if(DEBUG) System.out.println(DebugInfo() + processname);
				if (isProcessRunning(processname)) 
				{
					if(DEBUG) System.out.println(DebugInfo() + processname);
					Runtime.getRuntime().exec(KILL + processname);	
					System.out.println(DebugInfo() + processname + " is terminated");
				}
			}
		}
		catch (IOException io) {io.printStackTrace();}			
	}
	
	//Update Registryentries
	protected void setRegKey(String Node, String Key, String Value)
	{
		String[] addregkey = {"REG", "ADD", Node , "/v", Key, "/d", Value, "/f"};
		//Command To Update Registry Entries
		if(DEBUG)	
		{	
			System.out.print(DebugInfo() + "Registry Add Command: " );
			for (String element : addregkey) {System.out.print(element + " ");}
			System.out.println();
		}
		ProcessBuilder pb = new ProcessBuilder(addregkey);
		try	{pb.start();} 
		catch (IOException e)	{e.printStackTrace();}
		if(DEBUG) System.out.println(DebugInfo() + "Registry Key: " + Node + " updated with " + Key + " along with " + Value);
	}
	
	//Registry Macro
	protected String RegistryMacro() 
	{
		// Windows OS Verification
		String Macro = null;
		if (System.getProperty("os.name").contains("Windows")) 
		{
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
			if (is64bit == false)	{	Macro = "HKLM\\Software\\";		if(DEBUG)System.out.println(DebugInfo() + "OS Architecture: 32-bit OS");	} 
			else {	Macro = "HKLM\\Software\\Wow6432Node\\";	if(DEBUG)System.out.println(DebugInfo() + "OS Architecture: 64-bit OS");	}
		}
		else {	is64bit = (System.getProperty("os.arch").indexOf("64") != -1);	}
		return Macro;
	}
	
	//ProgramFiles Macro
	protected String ProgramFilesMacro() 
	{
		String Macro = null;
		//Windows OS Verification
		if (System.getProperty("os.name").contains("Windows")) 
		{
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
			if (is64bit == false)	{	Macro = "C:/Program Files";		if(DEBUG) System.out.println(DebugInfo() + "OS Architecture: 32-bit OS");	}
			else {	Macro = "C:/Program Files (x86)";	if(DEBUG) System.out.println(DebugInfo() + "OS Architecture: 64-bit OS");	}
		}
		else {	is64bit = (System.getProperty("os.arch").indexOf("64") != -1);	}
		return Macro;
	}
	
	//MultiLine Wordwrap
	protected String convertToMultiline(String originalmessage)
	{
	    return "<html><div style='text-align: center;'>" + originalmessage.replaceAll("\n", "<br>") + "</html>";
	}
	
	protected String DebugInfo() 
	{
		String currenttime = new java.text.SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z] ").format(new Date()); 
		String logtimestamp = currenttime	+ "- Client Engineering Info - " + System.getProperty("user.name") + " ";
		return logtimestamp;
	}	
}
