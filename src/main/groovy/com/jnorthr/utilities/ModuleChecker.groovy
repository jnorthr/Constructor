package com.jnorthr.utilities

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
 * ModuleChecker.groovy class description
 *
 * Groovy class to see if certain software modules exist on this platform. 
 * 
 */ 
 @Canonical 
 public class ModuleChecker
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** an O/S specific location for the user's home folder name */ 
    String home = System.getProperty("user.home");
    
   /** 
    * Variable title Value of a variable.
    */  
    String title = "";


    /** a list of possible command-line modules that can be used for this platform */
    String[] modules = ["gradle","git","java","javac","groovy","groovyc"];

    /** a map of confirmed modules that can be used for this platform */
    Map programs = [:]


   /** 
    * Default Constructor 
    * 
    * @return ModuleChecker.groovy object
    */     
    public ModuleChecker()
    {
        title = "running ModuleChecker constructor"

        modules.each{pgm->
            try{
                def process = "${pgm} -h".execute()
                process.waitFor();
                programs[pgm]=true;
            }
            catch(Exception x)
            {
                println "Did not find $pgm:"+x.message;
            } // end of catch

        } // end of each

    } // end of constructor


   /** 
    * Method to a map to certains bits of software that exist.
    * 
    * @return formatted content of internal variables
    */     
    public Map process()
    {
        return programs;
    }  // end of method


   /** 
    * Method to get a value from the map using key as binding entry key.
    * 
    * @param value of key to module confirmation map
    * @return true if the map has an entry with this name; non-existent modules are not in this map;
    */     
    public boolean get(String key)
    {
        return programs[key];
    }  // end of method


   /** 
    * Method to see if a Git client is binding entry key.
    * 
    * @return true if the map has an entry with the Git name; non-existent modules are not in this map;
    */     
    public boolean hasGitClient()
    {
        return programs['git'];
    }  // end of method

   /** 
    * Method to see if gradle command is binding entry key.
    * 
    * @return true if the map has an entry with the gradle command name; non-existent modules are not in this map;
    */     
    public boolean hasGradle()
    {
        return programs['gradle'];
    }  // end of method


   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """title=${title}
user.home=${home}
modules=${modules}
programs=${programs}
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
        println "--- starting ModuleChecker.groovy ---"

        ModuleChecker obj = new ModuleChecker();
        Map m = obj.process();
        m.each{p-> println "$p exists"}

        println "is 'groovy' an existing module on this platform ? "+obj.get('groovy');
        println "hasGitClient() on this platform ? "+obj.hasGitClient();

        println "ModuleChecker = [${obj.toString()}]"
        println "--- the end of ModuleChecker.groovy ---"
    } // end of main

} // end of class
