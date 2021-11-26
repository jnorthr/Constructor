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
 * Processor class description; 
 * see: http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 * and https://coderwall.com/p/nswp1q/calling-other-processes-from-groovy
 *
 * A class to execute individual commands for the local O/S.
 *
 * Groovy class to do something. 
 * 
 */ 
 @Canonical 
 public class Processor
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;


    /** an O/S specific location for the user's home folder name */ 
    String home = System.getProperty("user.home");

    
   /** 
    * Variable Value of any messages produced so far.
    */  
    public String log = "";


   /** 
    * Variable to execute individual commands on the current O/S.
    */  
    Process process;


   /** 
    * Variable holding the exit code from the most recently run individual command on the current O/S.
    */  
    public int returncode = 0;

	        
   /** 
    * Variable holding the current local folder pointer on the current O/S.
    */  
    File dir = new File("${home}${fs}Dropbox${fs}Constructor${fs}temp");
    
    
    /** holds the task starting and ending times */
    def start;

    /** holds the task ending time in millesec.s */
    def endtime;
    
    
    /** holds all the system-wide environmental variables as a map */
    def env = System.getenv().collect { k, v -> "$k=$v" }
        
        
   /** 
    * Default Constructor 
    * 
    * @return Processor object
    */     
    public Processor()
    {
        log = "running Processor constructor\n"
    } // end of constructor

    
   /** 
    * Non-Default Constructor 
    * 
    * @param dirname string name of new folder location to use when executing subsequenct commands
    * @return Processor object
    */     
    public Processor(String dirname)
    {
        log = "running Processor constructor using folder ${dirname}\n"
	setPath(dirname);
    } // end of constructor


   /** 
    * Method to set path to current working directory.
    * 
    * @param name path to use as current working directory
    * @return true if path exists
    */     
    public setPath(String name)
    {
        dir = new File(name);
        return dir.exists();
    }  // end of method


   /** 
    * Method to produce audit log.
    * 
    * @param txt the text to be logged
    * @return void
    */     
    public say(txt)
    {
        log += txt+"\n";
        return log;
    }  // end of method


   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """log=${log}
user.home=${home}
returncode=${returncode}
java.io.File.separator=${java.io.File.separator}
"""
    }  // end of method


   /** 
    * Method to run a single command with a current working directory - pwd.
    * 
    * @param cmd the command to be run
    * @param dir File pointing to the local file system folder to be run in
    * @return log of messages from running this command
    */     
    public run(def cmd, File dir)
    {
	startTask(cmd.toString(), 1)

	try{
                process = cmd.execute(env, dir); 
		InputStream stdin = process.getInputStream();
            	InputStreamReader isr = new InputStreamReader(stdin);
            	BufferedReader br = new BufferedReader(isr);
            	String line = null;
            	while ( (line = br.readLine()) != null)
                	say(line);
	        returncode = process.waitFor();
		say "return code: ${returncode}"
                if (process.err.text.trim().size() > 0) { say "stderr: ${process.err.text}" }
	        if (process.in.text.trim().size() > 0) { say "stdout: ${process.in.text}" }   
	}
        catch(Exception x)
        {
        	say "could not run $cmd : "+x.message;
        } // end of catch

	endTask();            
        return log;
    }  // end of method


   /** 
    * Method to prepare to execute a single command.
    * 
    * @param cmd the command to be run
    * @param runner a number indicating which run() method was used for the command to be run
    * @return exection log
    */     
    public startTask(String cmd, def runner)
    {
	log = "\n=============================================\n";
        say("<OUTPUT>");
	start = System.currentTimeMillis();
	say "running ${runner} ="+cmd+"\n";
    }  // end of method


   /** 
    * Method to complete execution of a single command.
    * 
    * @return execution log
    */     
    public endTask()
    {
	def elapsed = System.currentTimeMillis() - start;
	def time = elapsed/1000
	say "It took "+time+" milleseconds to run"
        say("</OUTPUT>");
	say "=============================================\n";
	return log;
    }  // end of method



   /** 
    * Method to execute a single command.
    * 
    * @param cmd the command to be run
    * @return execution log
    */     
    public run(String cmd)
    {
	startTask(cmd.toString(), 2);

	try{
        	process = cmd.execute(env, dir)
		InputStream stdin = process.getInputStream();
            	InputStreamReader isr = new InputStreamReader(stdin);
            	BufferedReader br = new BufferedReader(isr);
            	String line = null;
            	while ( (line = br.readLine()) != null)
                	say line;

	        returncode = process.waitFor();
		say "return code: ${returncode}"
                if (process.err.text.trim().size() > 0) { say "stderr: ${process.err.text}" }
	        if (process.in.text.trim().size() > 0) { say "stdout: ${process.in.text}" }   
	}
        catch(Exception x)
        {
        	say "could not run $cmd : "+x.message;
        } // end of catch
        
	endTask();            
        return log;
    }  // end of method



// cannot use this variant as there are blanks within the command string and this is a known groovy bug           
// def process2 = "git -m 'Initial Commit' commit".execute(null, new File(originaloutputpathname))
//Process process2 = ["git", "commit", "--allow-empty", "-m \"Initial_Commit\""].execute(null, new File(pathname) )

   /** 
    * Method to execute a single command from a String[] tokens sequence.
    * 
    * <p>dir must be set to the local file system folder to be run in
    *
    * <p>Cannot use following variant as when blanks within the command string and this is a known groovy bug   
    *        
    * <p>def process2 = "git -m 'Initial Commit' commit".execute(null, new File(pathname))
    *
    * @param tokens a series of string tokens that form a command to be run
    * @return execution log
    */     
    public run(List tokens)
    {
	startTask(tokens.toString(), 3);
	try{
        	process = tokens.execute(env, dir);
        	
    		def out = new StringBuffer()
		def err = new StringBuffer()
    		process.consumeProcessOutput( out, err )
    		returncode = process.waitFor()
    		
		say "return code: ${returncode}"
    		if( out.size() > 0 ) say out;
    		if( err.size() > 0 ) say err;
	}
        catch(Exception x)
        {
        	say "could not run ${tokens.toString()} : "+x.message;
        } // end of catch
        
	endTask();            
        return log;
    }  // end of method
        

   /** 
    * Method to read a sequence of commands from a script file.
    * 
    * @param inputfilename string naming the text script with commands to be run
    * @return execution log
    */     
    public runScript(def inputfilename)
    {
	def fi = new File(inputfilename)
	inputfilename = fi.getCanonicalPath();
	fi = new File(inputfilename);
	say "reading from $inputfilename ";

	fi.eachLine{e->
    		def re = e.trim();
    		boolean flag = (re.size() > 0 && !(re.startsWith("//")) )
    		if (flag) 
    		{
		    say run(re);
		} // end of if
	} // end of eachLine
	
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
        println "--- starting Processor ---"
	String fs = java.io.File.separator;
	String home = System.getProperty("user.home");

        Processor obj = new Processor();
  	println "log:"+obj.log;
  	      
        println "Processor = [${obj.toString()}]";
        obj.runScript(".${fs}resources${fs}git.txt");

	def fi = new File("${home}${fs}Dropbox${fs}Constructor${fs}temp");        
        println obj.run("git ", fi);


        obj = new Processor();
        println obj.run("./gradlew check");


        obj = new Processor("${home}${fs}Dropbox${fs}Constructor${fs}temp");
	def tokens = ["git", "commit", "--allow-empty", "-m \"Initial_Commit\""];
        println obj.run(tokens);

	
        println "--- the end of Processor ---"
    } // end of main

} // end of class