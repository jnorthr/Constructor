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
 * Result class description
 *
 * A simple class to return answers from a process() method.
 *
 * Groovy class to do something. 
 * 
 */ 
 @Canonical 
 public class Result
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** an O/S specific location for the user's home folder name */ 
    String home = System.getProperty("user.home");
    
   /** 
    * Variable title Value of a variable.
    */  
    String title = "";

    /** true if text string has potential to be used as an input */ 
    boolean process = false;

    /** true when indicator says input file needs template translation */ 
    boolean template = false;

    /** a count of words found within valid text string */ 
    int toks = 0;

    /** a list of words within submitted string */ 
    def tokens = []
    
   /** 
    * Default Constructor 
    * 
    * @return Result object
    */     
    public Result()
    {
        title = "running Result constructor"
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
process=${process}
template=${template}
toks=${toks}
tokens=${tokens}
java.io.File.separator=${java.io.File.separator}
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
        println "--- starting Result ---"

        Result obj = new Result();
        
        println "Result = [${obj.toString()}]"
        println "--- the end of Result ---"
    } // end of main

} // end of class
