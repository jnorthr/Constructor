package com.jnorthr.utilities.tools;

import groovy.transform.*;

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
 * LineProcessor class description
 *
 * A simple class to return answers from a process() method. Each input text string is examined as to whether it is a possible candidate for inclusion as a valid file address.
 * <p>The process() method returns a Result class object.

 *
 * Groovy class to do something. 
 * 
 */ 
 @Canonical 
 public class LineProcessor
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** an O/S specific location for the user's home folder name */ 
    String home = System.getProperty("user.home");
    
   /** 
    * Variable title Value of a variable.
    */  
    String title = "";


   /** 
    * Default Constructor 
    * 
    * @return LineProcessor object
    */     
    public LineProcessor()
    {
        title = "running LineProcessor constructor"
    } // end of constructor


   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """title=${title}
user.home=${home}
java.io.File.separator=${java.io.File.separator}
"""
    }  // end of string

   /** 
    * Method to see if string starts with template declaration char. of :.
    * 
    * @return string after : has been removed
    */     
    def has(def tx) 
    {     
    	int j = tx.indexOf(':');
    	return (j < 0) ? tx.trim() : tx.substring(j+1).trim(); 
    } // end of method

   /** 
    * Method to examine input text string for possible inclusion as a source file to be copied.
    * 
    * The possible input file might be a template file that needs a simple translate with a binding 
    * @return Result object describing results of the scan
    */     
    public Result process(def e)
    {
    	Result r = new Result();
    	if (e)
    	{	
        	int i = e.indexOf("//");
        	def word = (i<0) ? e.trim() : e.substring(0,i).trim() ;
        
	        // word now has comments removed after //
        	def flag = (word.startsWith(':')) ? true : false;
        	// flag means : template translate signal char found so take off leading :
        	if (flag) { word = has(word); }
                
        	print "i = $i  e=[$e] starts with=$flag sz=${word.size()} word=[$word] "
        
	        def wl = word.tokenize();   
        	print " tokens = "+wl.size();
        	r.tokens = word.tokenize();
        	r.toks = wl.size();
        	if (wl.size()) 
        	{	
            		r.process = true;
            		r.template = flag;
        	} // end of if     
    	} // end of if
    
	return r;
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
        println "--- starting LineProcessor ---"

        LineProcessor obj = new LineProcessor();
        
        println "LineProcessor = [${obj.toString()}]"
        
	def list = ["", "  ", " hi kids ", " hello // world", " // this is a note?", "//=", " = //", " : //",
 	" ./resources/fred.groovy // sample", null, ":./resources/skel.groovy ", " : ./resources/skel.groovy max.groovy",
	 "./resources/skel.groovy : ", "./resources/skel.groovy"," :./resources/skel.groovy  // hi kids",]

	int i = -1;
	list.each{e->
    		def re = obj.process(e);
    		if (re.process) print " - process ! ";
		println ''
	} // end of each

        println "--- the end of LineProcessor ---"
    } // end of main

} // end of class
