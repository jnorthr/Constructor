package com.jnorthr.utilities

import javax.swing.JOptionPane;
import com.jnorthr.utilities.tools.*

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
 * GitHelper.groovy class description
 *
 * Groovy class to construct a Git repo in the target folder
 * mv gitignore.txt .gitignore
 */ 
 @Canonical 
 public class GitHelper
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** an O/S specific location for the user's home folder name */ 
    String home = System.getProperty("user.home");
    
    
   /** 
    * Variable Value of the originaloutputpathname before adding any suffix.
    */  
    String originaloutputpathname = "";


    /** Handle to confirm that certain software modules exist on this platform. */
    ModuleChecker mc = new ModuleChecker();

   /** 
    * Boolean Value if this platform has a command-line Git client.
    */  
    boolean hasGitClient = false;


   /** 
    * Default Constructor uses the path declared in .make.properties
    * 
    * @return GitHelper.groovy object
    */     
    public GitHelper()
    {
	ConfigHandler ch = new ConfigHandler();
	String oopn = ch.get('pathname');
    	say "\nGitHelper() constructor for String [$oopn] from make.properties file"
	assert oopn!=null, "GitHelper needs output path target - found null"

	setup(oopn);
    } // end of constructor


   /** 
    * Non-Default Constructor 
    * 
    * @param the destination folder name before adding any suffix.
    * @return GitHelper.groovy object
    */     
    public GitHelper(String oopn)
    {
    	say "\nGitHelper(String [$oopn]) constructor"
	assert oopn!=null, "GitHelper needs output path target - found null"

	setup(oopn);
    } // end of constructor


    
   /** 
    * Method to establish internal variables.
    * 
    * @param the destination folder name before adding any suffix.
    * @return formatted content of internal variables
    */     
    public boolean setup(String oopn)
    {
	assert false!=new File(oopn).exists(), "GitHelper needs output path $oopn does not exist"

	originaloutputpathname = oopn;
	hasGitClient = mc.hasGitClient();
        
        if (!hasGitClient)
	{
	        say "GitHelper - user requested Git Repo but no Git client found !"
	        JOptionPane.showMessageDialog(null,"Your request to build a Git repo failed\nas no Git client can be found.\n\nPls install Git then retry.","No Git Command Found",JOptionPane.WARNING_MESSAGE);            
		say "GitHelper needs a git client but none found"
		System.exit( 1 );
	} // end of if
        
        def ans = ask();
        say "ask() about ${originaloutputpathname} said "+ans;
    
    }  // end of setup
    
    
   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """originaloutputpathname=${originaloutputpathname}
user.home=${home}
hasGitClient=${hasGitClient}
java.io.File.separator=${java.io.File.separator}
"""
    }  // end of string


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


   /** 
    * Method to ask user about using Git.
    * 
    * @return true if we will be using git
    */     
    public boolean ask()
    {
        // ===================================================================
        /** Now - ask about generating a Git repository */
        String theMessage = "Generate a Git repo in ${originaloutputpathname} ?";
        boolean b = hasGitClient;

        int result = JOptionPane.showConfirmDialog(null, theMessage, "Generate Git Repo ?", JOptionPane.YES_NO_CANCEL_OPTION);
        if(result == JOptionPane.CLOSED_OPTION)
        { 
            say "GitHelper cancelled due to user request"
            System.exit(0);
        } // end of if

        /** avoid building a Git repo if user declines */
        if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
        {
            say "GitHelper - user requested no Git Repo"
            b = false;
        } // end of if

        /** User said 'let's build' !*/
        else
        { 
	    b = process();	    
        } // end of if

        return b;
    } // end of method


   /** 
    * Method to init a repo using Git.
    * 
    * @return true if we will be using git
    */     
    public boolean process()
    {
	boolean b = hasGitRepo();
        say "checking to see if Git repo exists in [$originaloutputpathname] - found it ? "+b;

        // no git repo found in chosen target folder - so build it
        if (!b)
        {
        	b = buildGit();
                say "building for Git repo in [$originaloutputpathname] - did it ? "+b;
                addToGit();
        } // end of if


        /** git repo was already there! what do we do now ?  */
        else
        {
                String theMessage = "Over-write existing Git repo in ${originaloutputpathname}?";
                int result = JOptionPane.showConfirmDialog(null, theMessage, "Replace Git Repo ?", JOptionPane.YES_NO_CANCEL_OPTION);
                if(result == JOptionPane.CLOSED_OPTION)
                { 
                    say "GitHelper cancelled due to user request"
                    System.exit(0);
                } // end of if

                /** user replied something - was it a YES ? */
                else
                {
                    if(result == JOptionPane.YES_OPTION)
                    { 
			b = new File(originaloutputpathname+fs+".git").deleteDir();
                        say "deleted Git repo in [$originaloutputpathname] - did it ? "+b;
			
                        b = buildGit();
                        say "Writing new Git repo in [$originaloutputpathname] - did it ? "+b;
                        addToGit();
                    } // end of if
                    else
                    {
                        say "user did not want to over-writing Git repo in [$originaloutputpathname]"; 
                        b = false;                       
                    } // end of else

                    say "----------------------------"
                } // end of else

        } // end of else

        return b;
    }  // end of method


   /** 
    * Support method to establish a git repo if it does not already exist for this output folder
    * 
    * @param    path - the full absolute path where this git repo should be setup 
    * @return   true if git init was successful
    */     
    public boolean buildGit()
    {
        boolean flag = false;

        say "GitHelper initializing repo in $originaloutputpathname..."
        def process = "git init".execute(null, new File(originaloutputpathname))
        process.waitFor();
        say "buildGit exitValue()="+process.exitValue();
        if (process.exitValue()==0)
        {
	        flag = copyGitIgnore();
	} // end of if
	
	// cannot use this variant as there are blanks within the command string and this is a known groovy bug           
        //def process2 = "git -m 'Initial Commit' commit".execute(null, new File(originaloutputpathname))
        
	Process process2 = ["git", "commit", "--allow-empty", "-m \"Initial_Commit\""].execute(null, new File(originaloutputpathname) )
        
        process2.waitForOrKill(15000);
        say process2.err.text
        say "git commit -m 'Initial Commit' exitValue()="+process2.exitValue();
        say "->${process2.text.trim()}\n"                    
        say "----------------------------"
        if (process2.exitValue()==0)
        {
	        flag = true;
	} // end of if
	
        return flag;
    } // end of method



   /** 
    * Support method to copy a git ignore file to this output folder
    * 
    * @return   true if git copy was successful
    */     
    public boolean copyGitIgnore()
    {
        boolean flag = false;
	def oname = "${originaloutputpathname}${fs}.gitignore"
	
	Copier c = new Copier(originaloutputpathname);
        def cn = ".${fs}resources${fs}.gitignore";
        def msg = c.copy(cn);
        say msg;

        return flag;
    } // end of method


   /** 
    * Support method to ask user whether to establish a git repo if it does not already exist for this output folder
    * 
    * @return   true if check was successful - meaning a .git folder already exists
    */     
    public boolean hasGitRepo()
    {
        boolean flag = false;

        if (hasGitClient)
        {
            ChkObj co = new ChkObj(originaloutputpathname);        
            flag = co.has(".git");
            say "hasGitRepo() found that .git exists ? "+flag
        }  // end of if

        return flag;
    } // end of method



   /** 
    * Support method to add new files into  a git repo 
    * 
    * @return   true if adding all those files was successful
    */     
    public boolean addToGit()
    {
// git branch projectSetup
// git checkout projectSetup
// git add settings.gradle 
// git commit -m 'include setup files'
// git checkout master
// git merge projectSetup

        boolean flag = false;
        def process;
        boolean yn = true;
	def msg = "";
	
        def gitAddFiles = [".travis.yml", "build.gradle", "gradle/", "gradlew", "gradlew.bat", "settings.gradle", "src/", ".gitignore", "README.md", "gradle.properties"];

        gitAddFiles.each{
            String fn = originaloutputpathname+fs+it;

	    ChkObj co = new ChkObj(originaloutputpathname);
            yn = co.has(fn);

	    if (yn)
	    {
	            process = "git add ${it} ".execute(null, new File(originaloutputpathname))
        	    process.waitFor();
            	    msg += "gitAddFiles(String $it) exitValue()="+process.exitValue()+"\n"
            	    def s = process.in.text.trim()
            	    if (s.size()>0) { println s; }
            } // end of if                    
        } // end of each

        // do a commit of newly added core files
        process = "git commit -am 'initial-commit'".execute(null, new File(originaloutputpathname))
        process.waitFor();
        msg += "git commit exitValue()="+process.exitValue()+"\n"
        msg += process.in.text.trim()+"\n"                    
        say "$msg\n----------------------------"

        return flag;
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
        println "--- starting GitHelper.groovy ---"

	/** an O/S specific char. as a file path divider */
    	String fs = java.io.File.separator;

	/** an O/S specific location for the user's home folder name */ 
    	String home = System.getProperty("user.home");

        GitHelper obj = new GitHelper("${home}${fs}Dropbox${fs}Constructor${fs}temp");
        println "GitHelper = [${obj.toString()}]";
        
	boolean b = obj.hasGitRepo();	
	println "hasGitRepo() now ? = "+b;
	

        println "--- the end of GitHelper.groovy ---"
    } // end of main

} // end of class
