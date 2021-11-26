package com.jnorthr.utilities;

import com.jnorthr.utilities.*
import groovy.transform.ToString;
import java.awt.*
import javax.swing.*;
import groovy.swing.SwingBuilder  
import javax.swing.JOptionPane;

/** 
 * NameGenerator class description
 *
 * Groovy class to compose output path and file names. 
 * 
 */  
@ToString
public class NameGenerator
{
    /** current file separator for this O/S */
    String fs = java.io.File.separator;

    /** a  directory name that points to the user's home directory */
    String home = System.getProperty("user.home");

    /** Handle to the current environment */
    ConfigHandler ch = new ConfigHandler();

    /** Handle to the Swing GUI */
    SwingBuilder swingBuilder;

    /** current output directory /Users/jim/Dropbox */
    String corePath = "";


    /** an optional new project folder to be created within the current output directory e.g. ToolsProject  */
    String projectFolderName = "";
    
    
    /** An optional gradle/maven folder component e.g. (src/main/java) 
    * to be created within the optional new project folder 
    * within the current output directory 
    */    
    String gradlePath = "";
    
    
    /** An optional java / groovy etc. folder component e.g. (com.apache.tools) 
    * to be created within the optional gradle/maven folder 
    * within the optional new project folder 
    * within the current output directory 
    */    
    String packagePath = "";

    
    /** a mandatory simple name for this class to be used as output template name e.g. Hammer */
    String className = "";


    /** a mandatory simple name for the type of script used as output template name e.g. .java  */
    String suffixName = "";

    /** a folder name from the most recent get() call  */
    String buildPath = "";

    /** a handle to the logic to verify a given folder path exists or build it if missing  */
    FolderBuilder fb = new FolderBuilder();



   /** 
    * Combined results of all the above components would look like this : 
    * 
    *  /Users/jim/Dropbox /ToolsProject /src/main/java com.apache.tools Hammer  .java
    *  which converts to the file separator of this O/S giving a final result like :
    *
    *  /Users/jim/Dropbox/ToolsProject/src/main/java/com/apache/tools/Hammer.java
    * 
    */     


   // ======================================================= 
   /** 
    * Default Constructor 
    * 
    * @return NameGenerator object
    */     
    public NameGenerator()
    {
    	// true forces gradle ( if used ) to be core / main
    	setup(true);
    } // end of constructor


   /** 
    * Method to build an output path name so far
    * 
    * @Param true to make a 'main' gradle path ; false to make a 'test' path
    * @return void
    */     
    public setup(boolean flag)  
    { 
        corePath = ch.get('pathname').trim();        
        if ( corePath == null ) { corePath = home }
        if (!corePath.startsWith(fs)) { corePath = fs + corePath }
        if (corePath.endsWith(fs))  {corePath = corePath.substring(0, corePath.size() - 1 ); }

	getProjectFolderName();

        gradlePath = ch.getGradlePath(flag);
        if ( gradlePath == null ) { gradlePath = ""; }
        if (!gradlePath.startsWith(fs)) { gradlePath = fs + gradlePath }
        if ( gradlePath.endsWith(fs) ) { gradlePath = gradlePath.substring(0, gradlePath.size() - 1 ); }

        packagePath = ch.get('packagename').trim();
        if ( packagePath == null ) { packagePath = ""; }
        packagePath=packagePath.replace('.',fs);
        if (!packagePath.startsWith(fs)) { packagePath = fs + packagePath }
        if ( packagePath.endsWith(fs) ) { packagePath = packagePath.substring(0, packagePath.size() - 1 ); }

        className = ch.get('classname').trim();
        if ( className == null ) { className = "DummyName"; }
        if ( className == "" ) { className = "DummyName"; }
        
        String key = (flag) ? 'filesuffix' : 'testfilesuffix' ;
        suffixName = ch.get(key).trim();
	if ( suffixName == null ) { suffixName = "groovy"; }
        if ( suffixName == "" ) { suffixName = "groovy"; }
        if ( suffixName.startsWith('.')) { suffixName = suffixName.substring(1) }
        if ( suffixName=="" )  { suffixName="txt" }
    } // end of method
    

   /** 
    * Method to report joblog so far
    * 
    * @Param the text of what should be said
    * @return void
    */     
    public say(String tx)  
    { 
        println tx; 
    } // end of method


   /** 
    * Method to present a dialog to the user.
    * 
    * @param the text to show in this window
    * @return void
    */     
    def display(String msg)
    {
	JOptionPane.showMessageDialog(swingBuilder, msg);
    } // end of method
  

   /** 
    * Method to create missing folders using name of most recent get...() method
    * 
    * @return void
    */     
    def build()
    {
	fb.confirmPath(buildPath);
	return buildPath;
    } // end of method


   /** 
    * Method to create missing folders using name of most recent get...() method
    * 
    * @param the folder to build
    * @return void
    */     
    def build(String folder)
    {
	fb.confirmPath(folder);
	return folder;
    } // end of method


   /** 
    * Method to get user selected path name for output before adding any gradle/maven/package ids
    * 
    * @return  full name of output file as a string
    */     
    public String getCorePath()
    {
	return corePath;
    } // end of method




   /** 
    * Method to build full path name for output 
    * 
    * @Param true to make a 'main' gradle path ; false to make a 'test' path
    * @return  full name of output file as a string
    */     
    public String getPath(boolean flag)
    {
	setup(flag);
	buildPath = getPath();
	return buildPath;
    } // end of method



   /** 
    * Method to override path name 
    * 
    * @Param true to make a 'main' gradle path ; false to make a 'test' path
    * @return  full name of output file as a string
    */     
    public String setPath(def pn)
    {
	corePath = pn;
	return getPath();
    } // end of method


   /** 
    * Method to build full path name for output - if it includes gradle/maven, 
    * then that path was set prior to this call
    * 
    * @return  full name of output file as a string
    */     
    public String getPath()
    {
        say "corePath=[$corePath]\nprojectFolderName=[${projectFolderName}]\ngradlePath=[$gradlePath]\npackagePath=[$packagePath]\nclassName=[$className]\nsuffixName=[$suffixName]"    
    
        String fullFileName = "${corePath}"

        // include optional projectFolderName as a new group folder for this project
        if ( projectFolderName != "" )
        {
            fullFileName += projectFolderName;
        } // end of if    


        // include path components for maven/gradle folders
        if ( ch.get('gradleFlag') )
        {
            if (gradlePath!="")
            {
                fullFileName += gradlePath;
            } // end of if
        } // end of if
    
    
    	// com.fred.tools would now be com/fred/tools
        if (packagePath!="")
        {
            fullFileName += packagePath;
        } // end of if
    
        say "getPath() will now return :"+fullFileName
        buildPath = fullFileName;
        return buildPath;
    } // end of method


   /** 
    * Method to build full path name for output - if it includes gradle/maven, 
    * then that path was set prior to this call
    * 
    * @return  full name of output file as a string
    */     
    public String getProjectPath()
    {    
        String fullFileName = "${corePath}"
        fullFileName += projectFolderName;
        buildPath = fullFileName;
        return buildPath;
    } // end of method


   /** 
    * Method to build original path name plus optional project name plus option build path for output - if it includes gradle/maven
    * 
    * @return  full name of output file as a string
    */     
    public String getProjectBuildPath()
    {    
        String fullFileName = "${corePath}"
        fullFileName += projectFolderName;
        
        // include path components for maven/gradle folders
        if ( ch.get('gradleFlag') )
        {
            if (gradlePath!="")
            {
                fullFileName += gradlePath;
            } // end of if
        } // end of if

        buildPath = fullFileName;
        return buildPath;
    } // end of method

   /** 
    * Method to build original path name plus optional project name plus option build path 
    * plus optional class package path for output 
    * 
    * @return  full name of output file as a string
    */     
    public String getProjectBuildPackagePath()
    {    
        String fullFileName = "${corePath}"
        fullFileName += projectFolderName;
        
        // include path components for maven/gradle folders
        if ( ch.get('gradleFlag') )
        {
            if (gradlePath!="")
            {
                fullFileName += gradlePath;
            } // end of if
            
        } // end of if        

        
    	// com.fred.tools would now be com/fred/tools
        if (packagePath!="")
        {
            fullFileName += packagePath;
        } // end of if

        buildPath = fullFileName;
        return buildPath;
    } // end of method



   /** 
    * Method to discover if there is a project-level identifier for this build
    * 
    * this method does NOT update buildPath so you cannot call .build() method after this get...
    *
    * @return projectFolderName ( if any ) as a string
    */     
    public String getProjectFolderName()
    {      
        projectFolderName = ch.get('projectfoldername')?.trim();
        if ( projectFolderName == null ) { projectFolderName = ""; }
        if (!projectFolderName.startsWith(fs)) {projectFolderName = fs + projectFolderName }
        if ( projectFolderName.endsWith(fs) ) { projectFolderName = projectFolderName.substring(0, projectFolderName.size() - 1 ); }

        say "getProjectFolderName()="+projectFolderName;
        return projectFolderName;
    } // end of method


   /** 
    * Method to build full file name for some root output file
    * 
    * 
    * this method does NOT update buildPath so you cannot call .build() method after this get...
    *
    * @return  full name of output file as a string
    */     
    public String getName()
    {      
        String fullFileName = getProjectBuildPackagePath();
        fullFileName += "${fs}${className}.${suffixName}";
        say "getName()="+fullFileName;
        return fullFileName;
    } // end of method


   /** 
    * Method to build full file name for some root output file
    * 
    * 
    * this method does NOT update buildPath so you cannot call .build() method after this get...
    *
    * @return  full name of output file as a string
    */     
    public String getNameWithSuffix(String sfx)
    {      
        assert className!=null, "getNameWithSuffix(String $sfx) classname cannot be null";
        assert className!="", "getNameWithSuffix(String $sfx) classname cannot be empty";
        assert sfx!=null, "getNameWithSuffix(String $sfx) suffix cannot be null";
        assert sfx!="", "getNameWithSuffix(String $sfx) suffix cannot be empty";

        if ( sfx.startsWith('.')) { sfx = sfx.substring(1) }

        String fullFileName = getProjectBuildPackagePath();
        fullFileName += "${fs}${className}.${sfx}";
        say "getName()="+fullFileName;
        return fullFileName;
    } // end of method


   /** 
    * Method to build full file name for some root output file
    * 
    * 
    * this method does NOT update buildPath so you cannot call .build() method after this get...
    *
    * @param name of output filename and suffix - ya gotta supply your own suffix on this one like Fred.java
    * @return  full name of output file as a string
    */     
    public String getName(String className)
    {   
        assert className!=null, "getName(String) classname cannot be null";
        assert className!="", "getName(String) classname cannot be empty";
   
        String fullFileName = getProjectBuildPackagePath();
        fullFileName += "${fs}${className}";
        say "getName(${className})="+fullFileName;
        return fullFileName;
    } // end of method


   // ======================================
   /** 
    * Method to run class tests.
    * 
    * @param args Value is string array - possibly empty - of command-line values. 
    * @return void
    */     
    public static void main(String[] args)
    {
        println "--- starting NameGenerator ---"

        NameGenerator ng = new NameGenerator();
        
        println "\ngetPath() gave result of [${ng.getPath()}]\n"

        println "\ngetPath(false) gave result of [${ng.getPath(false)}]\n"

        println "\ngetPath(true) gave result of [${ng.getPath(true)}]\n"

        println "\ngetCorePath() gave result of [${ng.getCorePath()}]\n"


        println "\ngetName() gave result of [${ng.getName()}]\n"
        
        println "\ngetNameWithSuffix(groovy) gave result of [${ng.getNameWithSuffix('groovy')}]\n"

        println "\ngetName(build.gradle) gave result of [${ng.getName('build.gradle')}]\n"
                
	println "\n----------------------------------" 
	               
	println "getProjectFolderName()="+ng.getProjectFolderName();
	println "\n----------------------------------\n"
        println "\ngetPath() gave result of [${ng.getPath()}]"
        println "\ngetProjectPath() gave result of [${ng.getProjectPath()}]"
        println "\ngetProjectBuildPath() gave result of [${ng.getProjectBuildPath()}]"
        println "\ngetProjectBuildPackagePath() gave result of [${ng.getProjectBuildPackagePath()}]"
	println "building " + ng.getProjectBuildPackagePath();
	String justbuilt = ng.build();
	justbuilt = ng.build(justbuilt);
	println "justbuilt="+justbuilt+"\n\ngrdale test folder build follows ---";

	// try build gradle 'test' folders ...	 
	ng.getPath(false);
	justbuilt = ng.getProjectBuildPackagePath();
        println "getProjectBuildPackagePath() gave result of [${justbuilt}]"
	justbuilt = ng.build(justbuilt);
	println "justbuilt="+justbuilt+"\n\n";

        println "--- the end ---"
    } // end of main

} // end of class