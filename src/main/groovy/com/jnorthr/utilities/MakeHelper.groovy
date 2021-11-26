package com.jnorthr.utilities;

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
 * MakeHelper class description
 *
 * Groovy class to do somethings that we don't want to clutter up the mainline code. 
 * 
 */  
@ToString
public class MakeHelper
{
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

   /** 
    * Variable generateTestFlag True indicates the generation process should produce 
    * a spock framework test script for the generated class
    */  
    boolean generateTestFlag = false;


    /** Handle to construct an input template file */
    String templatefilename = "";


    /** Handle to construct an input template file for spock tests */
    String testtemplatefilename = "";


   /** 
    * Variable Handler to the name generator utility.
    */  
    NameGenerator ng;

   /** 
    * Variable Config Handler to the .make.properties configuration handler and binding.
    */  
    ConfigHandler ch = new ConfigHandler();


   // ======================================================= 
   /** 
    * Default Constructor 
    *
    * @return MakeHelper object
    */     
    public MakeHelper()
    {
        /** must only construct NameGenerator after binding has been loaded  */
        ng = new NameGenerator();

    } // end of method


   /** 
    * Method to compose a valid classname. It's a suport method for write() and writeTest() methods above.
    * 
    * @param    classname - must be a simple class name like Fred or Abc
    *
    * @return   outputfilename - complete absolute name of class source file 
    *
    */     
    public String composeClassName(String classname, boolean doTestFlag)
    {
        assert classname!=null, "composeClassName() null classname - cannnot continue";
        assert classname!="", "composeClassName() blank classname - cannnot continue";

        int count = 0;
        classname.each{c-> if (c=='.') count+=1; }
        assert count<1, "composeClassName() classname [$classname] cannot contain dots (.) - cannnot continue";

        String ofn = classname.trim();
        String fsux = (generateTestFlag && doTestFlag) ? ch.get('testfilesuffix') : ch.get('filesuffix') ;

        if (fsux!="")
        {
            if (!fsux.startsWith('.')) 
            { 
                fsux = '.' + fsux; 
            } // end of if

        } // end of if

        ofn += fsux;

        say "composeClassName(String $classname, boolean $doTestFlag) + fsux=[${fsux}] =[$ofn]"

        String key1 = (doTestFlag) ? 'testoutputfilename' : 'outputfilename' ;
        String key2 = (doTestFlag) ? 'testclassname' : 'classname' ;

        ch.put( key1, ofn );
        ch.put( key2, classname );
        ch.put( 'name', ofn );

        // returns something like <classname>.<suffix> i.e. Fred.groovy
        return ofn;
    } // end of method


   /** 
    * Method to compose a valid full path name without the classname. 
    * It's a suport method for write() and writeTest() methods above.
    * 
    * @return   full output path name - complete absolute path name of target 
    * where the class source files will be written 
    */     
    public String composePathName(String originaloutputpathname, boolean doTestFlag)
    {
        String ofn = originaloutputpathname;

        def ok = ch.getFlag('gradleFlag');
        if ( ok )
        {
	    NameGenerator ng = new NameGenerator();        
            String gradleoutputpathname = ng.getPath(!doTestFlag);
            ofn = gradleoutputpathname; 
            println "composePathName(String $originaloutputpathname, boolean $doTestFlag) for gradle gives [$ofn]"
        } // end of if


        if (ch.get('packagename')!="")
        {
            String  pn = ch.get('packagename')

            def sb = "";
            pn.each{c-> 
                sb += (c=='.') ? fs : c; 
            } // end of each

	    if (!ofn.endsWith(sb.toString().trim()))
	    {	    
                ofn += fs+sb.toString().trim() 
	    } // end of if
	} // end of if
	        
        return ofn;
    } // end of method




   /** 
    * Method to get see if user wants gradle/maven folder structures included in the result.
    * 
    * @return void
    */     
    public boolean askAboutGradle(String originaloutputpathname)
    {
 	/** ask about generating a gradle-compatible folder structure */
        String theMessage = "Include src/main/groovy & src/test/groovy in results ?";

        int result = JOptionPane.showConfirmDialog(null, theMessage, "Generate Gradle/Maven Folder Structure ?", JOptionPane.YES_NO_CANCEL_OPTION);
	if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
	{ 
        	System.exit(0);
        } // end of if

        if(result == JOptionPane.YES_OPTION)
        { 
            ch.put('gradleFlag', true);
            ch.put('testFlag', false);

            /** now build path for the build.gradle file */

	    /** Handle to build missing paths in a directory structure */
    	    FolderBuilder fb = new FolderBuilder();
            fb.confirmPath(originaloutputpathname);

            GradleHelper gm = new GradleHelper( ch.get('buildgradlename'), originaloutputpathname );
        } // end of if
        else
        {
            ch.put('gradleFlag', false);            
            ch.put('testFlag', false);
        } // end of else


        /** identify the template skeleton to use */
        templatefilename = identifyTemplateRequired();

     	/** ask about generating a test skeleton */
        def ok = ch.getFlag('gradleFlag');
        if ( ok )
        {
            testtemplatefilename = confirmTestRequired();            
        } // end of if

        ch.writeConfig();            

	return ch.getFlag('gradleFlag')
    } // end of method


   /** 
    * Method to confirm which input template should be used and that it exists
    * 
    * @return   name of actual input template to be used
    */     
    private String identifyTemplateRequired() 
    {
    	String templatename = ch.get('templatepathname')+ch.get('templatefilename');
        String theMessage = templatename;
        int result = JOptionPane.showConfirmDialog(null, theMessage, "Use This Template Script as Input ?", JOptionPane.YES_NO_CANCEL_OPTION);
	if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
	{ 
            	/** ok, re-write new outputpathname */
        	ch.writeConfig();            
        	System.exit(0);
        } // end of if

        if(result == JOptionPane.NO_OPTION)
        { 
            Chooser obj = new Chooser();
	    obj.selectFiles();
            templatename = obj.getChoice();
        } // end of if

        ChkObj co = new ChkObj();
        def ok = co.chkobj(templatename)
        if ( !ok )
        {
            JOptionPane.showMessageDialog(null, "Cannot proceed: input template file '${templatename}' is missing -",
            "Make Component Is Missing", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } // end of if

       return templatename;        
    } // end of method

   /** 
    * Method to confirm test module required, confirm name of test template and that it exists
    * 
    * @return   name of test template
    */     
    private String confirmTestRequired() 
    {
        boolean ok = ch.get('testFlag');
        generateTestFlag = ok;

    	String testtemplatename = ch.get('testtemplatepathname')+ch.get('testtemplatefilename');

        if(ok)
        { 
            String theMessage = testtemplatename;
            int result = JOptionPane.showConfirmDialog(null, theMessage, "Use This Test Script ?", JOptionPane.YES_NO_CANCEL_OPTION);
            if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
            { 
                /** ok, re-write new outputpathname */
                ch.writeConfig();            

                System.exit(0);
            } // end of if

            if(result == JOptionPane.NO_OPTION)
            { 
                Chooser obj = new Chooser();
                testtemplatename = obj.getChoice("test", "Test Templates");
            } // end of if

	    ChkObj co = new ChkObj();
            ok = co.chkobj(testtemplatename)

            if ( !ok )
            {
                JOptionPane.showMessageDialog(null, "Cannot proceed: input test template '${testemplatename}'' is missing -",
                "Make Component Is Missing", JOptionPane.ERROR_MESSAGE);

                /** ok, re-write new outputpathname */
                ch.writeConfig();            
                System.exit(1);
            } // end of if
        } // end of if YES_OPTION

        return testtemplatename;        
    } // end of method

   /** 
    * Method to print audit log.
    * 
    * @param the text to be said
    * @return void
    */     
    public void say(txt)
    {
        println txt;
    }  // end of method




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

        /** an O/S specific char. as a file path divider */
        String fs = java.io.File.separator;

        /** an O/S specific location for the user's home folder name */ 
        String home = System.getProperty("user.home");


        // ==================================================================
        // Variable originaloutputpathname - Describes the name of the output folder for this sample
        String originaloutputpathname = "${home}${fs}Dropbox${fs}Constructor${fs}temp";
        
        MakeHelper n = new MakeHelper();
        n.askAboutGradle(originaloutputpathname);
        
        String x = n.composePathName(originaloutputpathname, true);
        println "composePathName(true)="+x;

        x = n.composePathName(originaloutputpathname, false);
        println "composePathName(false)="+x;
        
        x = n.composeClassName("FredTest",true);
        println "composeClassName(FredTest,true)="+x;
        
        x = n.composeClassName("Fred",false);
        println "composeClassName(Fred,false)="+x;

        println "--- the end ---"
        System.exit(0);
    } // end of main

} // end of class
