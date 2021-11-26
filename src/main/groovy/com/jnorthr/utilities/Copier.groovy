package com.jnorthr.utilities;

import groovy.transform.*;
import groovy.text.SimpleTemplateEngine;
import com.jnorthr.utilities.*;
import com.jnorthr.utilities.tools.*;

/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** 
 * Copier class description
 *
 * A Tool to copy individual files from a known location to a target folder declared in the constructor. 
 * It will use the file name(s) of the donor as the filename of the copied file.
 * <p>No changes are made to text content as it is copied. 
 *
 * 
 */ 
 @Canonical 
 public class Copier
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** an O/S specific location for the user's home folder name */ 
    String home = System.getProperty("user.home");
    
   /** 
    * Variable targetpath Value of a variable provided in the constructor.
    */  
    String targetpath = "";

   /** 
    * Variable counts number of successful copies to target folder.
    */  
    def copycount = 0;

   /** 
    * Variable counts number of failed copies to target folder.
    */  
    def failcount = 0;


   /** 
    * Variable engine Handle to the template translation engine.
    */  
     def engine


   /** 
    * Variable Config Handler to the .make.properties configuration handler and binding.
    */  
    ConfigHandler ch = new ConfigHandler();


   /** 
    * Default Constructor 
    * 
    * @param output target folder path provided for methods in this class
    * @return Copier object
    */     
    public Copier(String target)
    {
        targetpath = target;
        def fi = new File(targetpath);
        targetpath = fi.getCanonicalPath();
        println "running Copier constructor with folder [$targetpath]"
        assert true == new File(targetpath).exists(), "Copier constructor failed to find target path of $targetpath";
    } // end of constructor


   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """targetpath=${targetpath}
user.home=${home}
copycount=${copycount}
failcount=${failcount}
java.io.File.separator=${java.io.File.separator}
"""
    }  // end of string


   /** 
    * Method to copy a list of filenames to a target folder.
    * 
    * @param list of names of donor files to copy to path provided in the class constructor
    * @return log of messages of success or failure during method execution
    */     
    public String copy(def input)
    {
	def msgs = "";
	copycount = 0;
	failcount = 0;
	
	input.each{e->
		msgs += copy(e);
		msgs += '\n'
	} // end of each

	msgs += "copied $copycount files and failed to copy $failcount files\n"
	return msgs;
    }  // end of method



   /** 
    * Method to copy one file to a target folder.
    * 
    * @param name of one donor file to copy to path provided in the class constructor
    * @return log of messages of success or failure during method execution
    */     
    public String copy(String input)
    {
    	String msg = "$input copied to $targetpath successfully\n"
        try{
        	def fi = new File(input);
	        String outname = fi.getCanonicalPath();

	        int i = outname.lastIndexOf(fs);
	        if (i > -1 ) { outname = outname.substring(i+1) };
        	def payload = fi.text;
        	
        	outname = targetpath + fs + outname;
	        new File(outname).write(payload);
	        copycount += 1;
	}
	catch(Exception x)
	{
		msg = "Copier copy($input) not copied to $targetpath due to ";
		msg += x.message;
		msg += '\n';
		println msg;
		failcount += 1;
	} // end of catch
	
	return msg;
    }  // end of method




   /** 
    * Method to translate a template file into a target folder.
    * 
    * @param name of donor template file to copy from
    * @param binding map of key:value pairs for template replacement
    * @param name of output script filename with no path declaration e.g. Fred.java
    * @return log of messages of success or failure during method execution
    */     
    public String copy(String input, def binding, String scriptname)
    {
    	String msg = "$input converted as $scriptname for $targetpath successfully\n"
        def engine = new groovy.text.SimpleTemplateEngine();

        try{
        	def fi = new File(input);
	        String outname = fi.getCanonicalPath();

	        int i = outname.lastIndexOf(fs);
	        if (i > -1 ) { outname = outname.substring(i+1) };
	        outname = scriptname;
        	def payload = fi.text;

	        def template = engine.createTemplate(payload).make(binding);
        	payload = template.toString();
        	
        	outname = targetpath + fs + outname;
	        new File(outname).write(payload);
	        copycount += 1;
	}
	catch(Exception x)
	{
		msg = "Copier copy($input) not translated to $targetpath as $scriptname due to ";
		msg += x.message;
		msg += '\n';
		println msg;
		failcount += 1;
	} // end of catch
	
	return msg;
    }  // end of method


   /** 
    * Support method to copy a resource file without template translate and write to the target outputpathname
    * 
    * @param    outname - the full absolute path+file name of the new file to be written 
    * @param    tfn - key of binding entry with name of a file with the content to be written as output 
    * @return   joblog
    */     
    public copyContent(String outname, String tfn)
    {
        String tfni = ch.get(tfn);
        File fi = new File(tfni);
        String msg = "";

        try
        {
	        String payload = fi.text;
	        new File(outname).write(payload);
	        copycount += 1;
		    msg = "Copier copyContent($tfn = $tfni) copied to $outname";
	    }
	    catch(Exception x)
	    {
		  msg = "Copier copyContent($tfn = $tfni) not copied to $outname due to ";
		  msg += x.message.trim();
	   	  msg += '\n';
		  println msg;
		  failcount += 1;
	    } // end of catch

	    return msg;
    } // end of method



   /** 
    * Method to copy a series of files to a target folder. This version reads from an external file
    * named for example ./resources/copyme.txt
    * 
    * For each non-blank line not starting with the comment marker //  then the canoncical name of one file per line 
    * is found for any relatively named file
    * like ./samples/Filter.groovy making an absolute filename.
    
    * @param name of donor file of filenames to copy from 
    * where each non-blank uncommented line is taken as a relative or absolute filename
    *
    * @return log of messages of success or failure during method execution
    */     
    public String copyFiles(String inputfilename)
    {
        LineProcessor lp = new LineProcessor();
	copycount = 0;
	failcount = 0;
	
	def fi = new File(inputfilename)
	inputfilename = fi.getCanonicalPath();
	fi = new File(inputfilename);
	def msgs = "reading from $inputfilename \n";

	fi.eachLine{e->
    		def re = lp.process(e);
    		if (re.process) 
    		{
		        msgs += copy(e);
		        //msgs += '\n';
		} // end of if
	} // end of eachLine
	
	msgs += "copied $copycount files and failed to copy $failcount files\n"
	return msgs;
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
        println "--- starting Copier ---"

        String fs = java.io.File.separator;
        String home = System.getProperty("user.home");
	String outn = home + fs + "Dropbox" + fs + "Constructor" + fs + "temp" + fs + "samples";

	ChkObj co = new ChkObj();
	assert true == co.chkobj(outn);

        Copier obj = new Copier(outn);
        
        println "Copier = [${obj.toString()}]"
        
        String cn = "${fs}Users${fs}jim${fs}Dropbox${fs}Constructor${fs}resources${fs}Chooser.txt";
        def msg = obj.copy(cn);
        println msg;
        
        cn = ".${fs}resources${fs}gitignore";
        msg = obj.copy(cn);
        println msg;
        
        def cna = [".${fs}resources${fs}build.gradle", ".${fs}resources${fs}gradle.properties", ".${fs}resources${fs}settings.gradle"];
        msg = obj.copy(cna);
        println msg;
        
        println "\n----------------------------\n"
        
        cn = ".${fs}resources${fs}copyme.txt";
        msg = obj.copyFiles(cn);
        println msg;
        
        
	ConfigHandler ch = new ConfigHandler();
	msg = obj.copy(".${fs}resources${fs}main${fs}skeleton.groovy", ch.binding, "Clown.groovy");
        println msg;

        
        println "--- the end of Copier ---"
    } // end of main
} // end of class