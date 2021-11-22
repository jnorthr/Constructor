package com.jnorthr.utilities;

import com.jnorthr.utilities.ConfigHandler;
import groovy.transform.ToString
import com.jnorthr.utilities.tools.*

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
 * FolderBuilder class description
 *
 * Groovy class to build any missing path components in a directory path name. Requires ConfigHandler to be passed into the constructor
 * to be able to track packagenames and file suffixes. 
 */  

@ToString(includeNames=true) 
public class FolderBuilder
 {
   /** 
    * Variable auditFlag True indicates the joblog should be written to println
    */  
    boolean auditFlag = false;

   /** 
    * Variable keeps running joblog of what's happened so far
    */  
    def msg = "";


   /** 
    * Default Constructor 
    *
    * @return FolderBuilder object
    */  
    public FolderBuilder()
    {
        say "\nFolderBuilder ------------------"
        say "--- end of FolderBuilder constructor ---"
    } // end of constructor


   /** 
    * Non-Default Constructor 
    *
    * @param true to cause joblog to be kept and possibly printed
    * @return FolderBuilder object
    */  
    public FolderBuilder(boolean audit)
    {
        auditFlag = audit;
        say "\nFolderBuilder ------------------"
        say "--- end of FolderBuilder constructor  ---"
    } // end of constructor


    /** method to dump internal values of variables */
    public String toString()
    {
        return """auditFlag=${auditFlag}
msg=[$msg]
"""
    }  // end of string


    /** method to return joblog so far */
    public String getJobLog()
    {
        return msg;
    }  // end of string


   /** 
    * Method to confirm the directory exists and mkdir if it's missing. 
    *
    * @param path to see if it exists and build the full path hierarchy if not. Any missing subfolders
    * along the path are also created
    * @return message of what happened during this logic
    */     
    public String confirmPath(String p)
    {
        ChkObj co = new ChkObj();
        if ( !co.chkobj(p) )
        {   
            try{
                msg += "[$p] does not exist so will try to build it "
                new File(p).mkdirs();
                msg += "- yes did it !\n"
            }
            catch(Exception x)
            {
                msg += " - no could not create this path due to "+x.message+"\n";
            } // end of catch
        }
        else
        {
            msg += "[$p] already there\n";
        } // end of else   

        return msg;
    } // end of method


   /** 
    * Method to print audit log.
    * 
    * @param text to show in log
    * @return void
    */     
    public void say(txt)
    {
        if (auditFlag) 
        { 
            msg += txt;
            msg += '\n'; 
        } // end of if
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
        println "--- starting FolderBuilder ---"

        String fs = java.io.File.separator;
        String home = System.getProperty("user.home");


        // start class with audit flag true
        FolderBuilder fb = new FolderBuilder(true);
        println fb.getJobLog();
        println "\n===============================================\n"

        // compose full path name and build it
        String samplefilename = "${home}${fs}Dropbox${fs}Constructor${fs}temp${fs}FolderBuilder";
        String nop = fb.confirmPath(samplefilename);
        println nop;
        println fb.getJobLog();
        new File(samplefilename).deleteDir();
        println "\n===============================================\n"


        println  "// ----------------------------------------"
        println  "// ok let's repeat those tests with no package names"

        // compose full path name and build it with gradle and no package
        samplefilename = "${home}${fs}Dropbox${fs}Constructor${fs}temp";
        nop = fb.confirmPath(samplefilename);
        println  "new non-gradle output path with no package:"+nop;
        println fb.getJobLog();
        println "\n===============================================\n"


        // ask for gradle 'main' flow with package names
        samplefilename = "${home}${fs}Dropbox${fs}Constructor${fs}temp${fs}src${fs}main${fs}groovy${fs}com${fs}jnorthr";
        nop = fb.confirmPath(samplefilename);
        println "new gradle main output path with package:"+nop;
        println fb.getJobLog();
        println "\n===============================================\n"


        // ask for gradle 'test' flow
        samplefilename = "${home}${fs}Dropbox${fs}Constructor${fs}temp${fs}src${fs}test${fs}groovy${fs}com${fs}jnorthr";
        nop = fb.confirmPath(samplefilename);
        println "new gradle test output path with package:"+nop;
        println fb.getJobLog();
        println "\n===============================================\n"

        // try to make a gradle 'test' flow duplicate
        samplefilename = "${home}${fs}Dropbox${fs}Constructor${fs}temp${fs}src${fs}test";
        nop = fb.confirmPath(samplefilename);
        println "new gradle test output path with package:"+nop;
        println fb.getJobLog();
        println "\n===============================================\n"



        println "--- the end ---"
    } // end of main

} // end of class