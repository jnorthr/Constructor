package com.jnorthr;

import groovy.text.SimpleTemplateEngine;

import groovy.swing.SwingBuilder  
import groovy.beans.Bindable
import groovy.swing.*

import static javax.swing.JFrame.EXIT_ON_CLOSE  
import java.awt.*
import javax.swing.*;

import javax.swing.JOptionPane;
import groovy.transform.ToString;

import com.jnorthr.utilities.*
import com.jnorthr.utilities.tools.*

/** 
 * Make class description
 *
 * Groovy class to do something. 
 * 
 */  
 @ToString
 public class Make
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** a  directory name that points to the user's home directory */
    String home = System.getProperty("user.home");


   /** 
    * Variable Config Handler to the .make.properties configuration handler and binding.
    */  
    ConfigHandler ch = new ConfigHandler();


    /** Handle to build missing paths in a directory structure */
    FolderBuilder fb = new FolderBuilder();


    /** Handle to construct a single full path plus file name */
    NameGenerator ng = new NameGenerator();        


    /** Handle to construct the gradle assets if required */
    GradleHelper gm; 


    /** Handle to confirm that certain software modules exist on this platform. */
    ModuleChecker mc = new ModuleChecker();


    /** Handle to establish a Github repo on this platform target folder. */
    GitHelper gh;

              
    /** Handle to helper methods stored in a support class. */
    MakeHelper mh = new MakeHelper();
              
              
    /** Handle to user dialog helper methods stored in a support class. */
    Ask ask = new Ask();

              
   /** 
    * Variable engine Handle to the template translation engine.
    */  
     def engine

   /** 
    * Variable payload Holds the string result of the template translation.
    */  
    String payload = "";


   /** 
    * Variable fi Holds handle to the template skeleton.
    */  
    def fi



    /** a directory name that points to the input template file to be read this time */
    String tfn = ch.get('templatefilename');

    /** a directory name that points to the input test template file to be read this time */
    String ttfn = ch.get('testtemplatefilename');


   /** 
    * Variable packagename Holds the string to be used in some script types like java as the name 
    * of the package e.g. com.fred.tools
    */  
    String packagename = ch.get('packagename');

   /** 
    * Variable classname Holds the string to be used in some script types like java as the simple name 
    * of the class with no package name nor suffix script type e.g. Hammer
    */  
    String classname = ch.get('classname');

   /** 
    * Variable testpackagename Holds the string to be used in some script types like java as the name 
    * of the package for the spock test framework e.g. com.fred.tools
    */  
    String testpackagename = ch.get('testpackagename');

   /** 
    * Variable testclassname Holds the string to be used in some script types like java as the simple name 
    * of the class with no package name nor suffix script type for the spock test framework e.g. HammerTest
    */  
    String testclassname = ch.get('testclassname');
    


    /** Handle to construct a single full path but without gradle, package or file name bits */
    String originaloutputpathname = ch.get('pathname');

    /** Handle to construct a single full path with gradle, but not package and file name bits */
    String gradleoutputpathname = ch.get('pathname');

    /** Handle to construct a single full path with gradle, but not package and file name bits */
    String gradletestoutputpathname = ch.get('pathname');


   /** 
    * Variable coreFlag True indicates the generation process should produce a main core 
    * script as the generated class
    */  
    boolean coreFlag = ch.getFlag('coreFlag');

   /** 
    * Variable testFlag True indicates the generation process should produce a spock framework 
    * test script for the generated class
    */  
    boolean testFlag = ch.getFlag('testFlag');


   /** 
    * Variable gitFlag True indicates the generation process should produce a git repository framework 
    * for the generated folder
    */  
    boolean gitFlag = ch.getFlag('gitFlag');



   /** 
    * Variable travisFlag True indicates the generation process should produce a .travis.yml C/I 
    * integration script for the generated folder
    */  
    boolean travisFlag = ch.getFlag('travisFlag');


   /** 
    * Variable theMessage A temporary var. for JOptionPane.showMessageDialog
    */  
    String theMessage = "";


    /** last known call to Make gave this response */
    int returncode = 0;


    /** last known call to Make gave this text reply */
    String returnmsg ="ok";

    boolean yn = true;

   // ======================================================= 
   /** 
    * Default Constructor 
    * 
    * Confirms 1) input template file exists, 2) outputpathname exists and has something like 		
    * /Users/eve/Dropbox/SkeletonMaker  3) gradle file structure to be built and 
    * 4) updates ~/.make.properties file with latest outputpathname encoded as base64
    *
    * @return Make object
    */     
    public Make()
    {
        /** must only construct NameGenerator after binding has been loaded  */
        ng = new NameGenerator();

		if (classname=="") { classname = "Dummy"; }
	
		if (testclassname=="") { testclassname = "DummyTest"; }

		originaloutputpathname = ng.getProjectBuildPackagePath();
		ng.build();
		getTravis();
		getGradle();
		process();
		cleanup(ng.getProjectPath());
    } // end of method


   /** 
    * Method to get gradle flag and if true then to produce the result.
    * 
    * @return boolean true if Gradle folders were built 
    */     
    public boolean getGradle()
    {
        def ok = ch.needsGradleFolders();
		say "\ngetGradle() starting ....."

        /** finish writing build.gradle if needed */
        if (ok)
        {
		    gradleoutputpathname = ng.getProjectPath();
            say "gonna build gradle folders now in ${gradleoutputpathname}"
            GradleHelper gm = new GradleHelper( ch.get('buildgradlename'), gradleoutputpathname );
            ok = gm.writeGradle(ch.binding);
        } // end of if
        
	println "getGradle() ending .....ok=$ok \n-------------------------------\n\n "
	return ok;	
    } // end of getGradle method
    

   /** 
    * Method to get travis flag and if true then to produce the result.
    * 
    * @return boolean true if Travis C/I is to be used
    */     
    public boolean getTravis()
    {
        def ok = travisFlag;

        /** finish writing travis if needed */
        if (ok)
        {
	    /** Now - what about generating a Travis Continuous Integration Server script */
            String theMessage = "\nGenerate a Travis Continuous Integration Server script too ? "+ok;
	    	say theMessage;
	
            say "gonna write .travis.yml now"
            String travisFileName = originaloutputpathname + fs + ".travis.yml";

            ChkObj co = new ChkObj(originaloutputpathname);
	    	if (co.has(".travis.yml"))
	    	{
	        	theMessage = ".Travis.yml already exists - replace it with this new version ?";
				def result = ask.ask(theMessage);
				say "getTravis() result="+result.returncode;
				switch (result.returncode)
				{
					case -1 :	System.exit(0);
							break;
						
					case 0	:	ok = false;
							Copier c = new Copier(originaloutputpathname);
					        c.copyContent(travisFileName, "travisfilename");
							break;
				} // end of switch        
	    } // end of if
	    else
	    {
			Copier c = new Copier(originaloutputpathname);
			c.copyContent(travisFileName, "travisfilename");
	    } // end of else

        } // end of if
        
		return ok;
    } // end of getTravis method


   /** 
    * Method to produce the project.
    * 
    * @return void
    */     
    public void process()
    {
        engine = new groovy.text.SimpleTemplateEngine();

        String fullpackagename = (ch.get('packagename')) ? "package ${ch.get('packagename')};" : "" ;
        ch.put('fullpackagename', fullpackagename);

        /** generate a test skeleton script ? */
        if (testFlag)
        {
            writeTest(testclassname);
        } // end of if


       /** adjust user chosen path with additional path components 
        * - but it's not the final name as the skeleton package name may be added too 
        */
        if (coreFlag)
        {
        	say "Make() writing template as classname=$classname";
	        write(classname);
        } // end of if

        /** ok, re-write all environment values */
        ch.writeConfig();


        /** generate a gradle folder structure ? */
        if ( ch.needsGradleFolders() && ch.get('scriptsFlag'))
        {
		    gm = new GradleHelper( ch.get('buildgradlename'), originaloutputpathname );
		    gm.runGradle(originaloutputpathname);
        } // end of if


        /** generate a git repository ? */
        if ( gitFlag )
        {
            gh = new GitHelper(ng.getCorePath());
        } // end of if

        
    } // end of method


   /** 
    * Method to finish last steps in the generate phase.
    * 
    * @return void
    */     
    public cleanup(String outputpath)
    {
		say "\n================================================="
        if ( ch.needsGradleFolders() && ch.get('scriptsFlag') )
        {
			boolean ok = gm.runGradle(outputpath);
		}
    }  // end of method
	    	    

   /** 
    * Method to print audit log.
    * 
    * @param the text to be said
    * @return void
    */     
    public void say(txt)
    {
        if (yn) { println txt; }
    }  // end of method



   /** 
    * Method to confirm outputpathname points to a valid existing directory
    * 
    * @param  pathnametobechecked - name of path that must exit and be a directory (not a file)
    * @return Make object
    */     
    public boolean checkPick(String pathnametobechecked)
    {
        /** confirm chosen output path name exists */
        ChkObj co = new ChkObj();
        boolean ok = co.chkobj(pathnametobechecked);

        /** confirm chosen output path name exists and is a directory name */
        if ( (!ok) || (!co.isDir()) )
        {
                JOptionPane.showMessageDialog(null, "The path choice of '${pathnametobechecked}'' is missing or is not a directory folder !",
                "Output Path Must be A Directory", JOptionPane.ERROR_MESSAGE);

                /** ok, re-write new outputpathname */
                ch.writeConfig();            
                System.exit(1);
        };

        return ok;
    } // end of method



   // Produce the results using the following logic
   /** 
    * Method to compose and write a skeleton to outputpathname plus classname
    * 
    * @param    classname - can be either a single class name like Fred 
    * or a simple file name like Fred.groovy; in either case 'Fred' is used
    * 
    * @return Make object
    */     
    public write(String classname)
    { 
        String ocn = mh.composeClassName(classname, false);
        String outputpathname = originaloutputpathname
        ch.put('outputpathname',originaloutputpathname);
        String opn = mh.composePathName(outputpathname, false);
        say "write(String $classname) to create a path [$opn] for class [${ocn}]"
        fb.confirmPath(opn);
        String fon = opn + fs + ocn;
        ch.put('outputpathname', opn)
        say "write(String $classname) = [$fon]"
        writePayload(fon, 'templatefilename');
    } // end of method
    
    
   /** 
    * Method to compose and write a test skeleton to the test outputpathname plus classname
    * 
    * @param    classname - must be a single class name like Fred 
    * @return Make object
    */     
    public writeTest(String testclassname)
    {
        String ocn = mh.composeClassName(testclassname, true);
        String outputpathname = originaloutputpathname
        ch.put('outputpathname',originaloutputpathname);
        String opn = mh.composePathName(outputpathname,true);
        say "gonna create a test path [$opn] "
        fb.confirmPath(opn);
        String fon = opn + fs + ocn;
        ch.put('testoutputpathname', opn)
        say "write(String $testclassname) = [$fon]"
        writePayload(fon, 'testtemplatefilename');

    } // end of method
    
    
   /** 
    * Support method to do a template translate and write translated skeleton text to the core/test outputpathname 
    * plus classname and possibly compile it
    * 
    * @param    outname - the full absolute path+file name of the new class 
    * @param    tfn - a string name of the skeleton script to be written as output 
    * @return 	void
    */     
    private void writePayload(String outname, String tfn)
    {
        String tfni = ch.get(tfn);
        File fi = new File(tfni);
        def ok = true;
        tfni = fi.getCanonicalPath(); 
        say "writePayload(String $outname, String $tfn) = [$tfni]";
        fi = new File(tfni);
        String payload = ch.convertDesc();
		ch.put("description",payload);

		payload = "";
	
        try{
        	payload = fi.text;
	        def template = engine.createTemplate(fi.text).make(ch.binding);
        	payload = template.toString().trim();
        	def sb=""
			payload.each{ch->  
    			sb+=(ch=='|') ? '\n' : ch;
			}
	        new File(outname).write(sb);
		}
		catch(Exception x)
		{
			payload = x.message + '\n';
			say "writePayload failed :"+x.message;
			ok = false; // avoid further logic below 
		} // end of catch	
    } // end of method



   /** 
    * Method to passback the results of the most recent session.
    * 
    * @return the value of the most recent execution
    */     
    public int getReturnCode()
    {   
        return returncode
    } // end of method


   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """fs=${fs}
user.home=${home}
payload=${payload}
java.io.File.separator=${java.io.File.separator}
tfn=${tfn}
ttfn=${ttfn}
originaloutputpathname=${originaloutputpathname}
testFlag=${testFlag}
gradleFlag=${ch.needsGradleFolders()}
gitFlag=${gitFlag}
travisFlag=${travisFlag}
theMessage=${theMessage}
returncode=${returncode}
returnmsg=${returnmsg}
packagename=${packagename}
classname=${classname}
testpackagename=${testpackagename}
testclassname=${testclassname}
gradleoutputpathname=${gradleoutputpathname}
gradletestoutputpathname=${gradletestoutputpathname}
"""
    }  // end of string


   // ======================================
   /** 
    * Method to run class tests.
    * 
    * @param args Value is string array - possibly empty - of command-line values. 
    * @return void
    */     
    public static void main(String[] args)
    {
        println "--- starting Make ---"

        Make n = new Make();
		println n.toString();
	
        String cp = n.getPath();
        println "\n\n=======================================\n--> the chosen path is :[${cp}]"

        boolean b = n.hasGit(cp);
        println "--> hasGit($cp) ? :[${b}]"

        boolean c = n.buildGit(cp);
        println "--> buildGit() ? :[${c}]"
        
        if (b) { n.addToGit(cp); }

        n.askAboutGradle();
        n.getPackageAndClass()
        n.process();

        println "--- the end ---"
        System.exit(0);
    } // end of main

} // end of class