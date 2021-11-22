package com.jnorthr.utilities;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import javax.swing.JOptionPane;

import com.jnorthr.utilities.*
import com.jnorthr.utilities.tools.*
import groovy.transform.ToString;

/** 
 * GradleHelper class description
 *
 * Groovy class to read gradle template build.gradle and write new version with a valid name of a class being created. 
 * This way you should be able to just run the gradle script immediately assuming you have gradle build tool installed.
 * Also optionally creates a gradle wrapper and optionally other scriptsfor gradle
 */  
 @ToString
 public class GradleHelper
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;
    
   /** 
    * Variable auditFlag True to cause a joblog to be produced
    */  
    boolean auditFlag = true;

   /** 
    * Variable engine Handle to the template translation engine.
    */  
     def engine

   /** 
    * Variable payload Holds the resuling string used in template translation.
    */  
    String payload = "";

   /** 
    * Variable template Holds the string used in template translation.
    */  
    Template template;

   /** 
    * Handle ask provides a bunch of JOptionPane dialogs.
    */  
    Ask ask = new Ask();


   /** 
    * Variable Config Handler to the .make.properties configuration handler and binding.
    */  
    ConfigHandler ch = new ConfigHandler();


    /** Handle to confirm that certain software modules exist on this platform. */
    ModuleChecker mc = new ModuleChecker();


   /** 
    * Variable True to cause gradle local folder directory components to be produced
    */  
    boolean gradleFlag = ch.needsGradleFolders();

   /** 
    * Variable Identifier of the class to be produced
    */  
    String classname = ch.get('classname');

   /** 
    * Variable True to cause gradle test components to be produced
    */  
    boolean testFlag = ch.getFlag('testFlag');

    /** Handle to construct several possibilities of path names to  be produced */
    NameGenerator ng;

    /** Handle to confirm objects exist or not */
    ChkObj co = new ChkObj();
        
    /** Handle to construct a single full path with gradle, package and file name bits */
    String originaloutputpathname = "";


   // =====================================================================================
   /** 
    * Non-Default Constructor 
    * 
    * @param must have full name of the build.gradle template file from the /resources folder
    * @param must identify the root output path for this build.gradle template file from the /resources folder
    * @return GradleHelper object
    */     
    public GradleHelper(String buildgradlename, String oopn)
    {
        assert buildgradlename!=null, "must provide non-null name of build.gradle resource script";
        assert buildgradlename!="", "must provide non-blank name of build.gradle  resource script";
        assert oopn!=null, "GradleHelper needs output path target - found null"
        assert oopn!="", "GradleHelper constructor needs name of output path folder target"

        def co = new ChkObj();
	boolean tf = co.chkobj(oopn);
        assert true==tf,"GradleHelper output path $oopn does not exist"
	println ""

        if ( !co.chkobj(buildgradlename) )
        {
            def msg = "--> sorry to report that resource ${buildgradlename} does not exist";
            say msg;
            ask.tell(msg);    
            assert co.chkobj(buildgradlename), "file ${buildgradlename} missing"
        } // end of if 
        else
        {        
            File fi = new File(buildgradlename);
            engine = new groovy.text.SimpleTemplateEngine();
            payload = fi.text;
            template = engine.createTemplate(payload);
            this.originaloutputpathname = oopn;
            say "GradleHelper(${buildgradlename}) found payload of ${payload.size()} bytes to be written to ${oopn}"
        } // end of else
        
     } // end of default constructor
    
    
   /** 
    * Method to produce audit log when the auditFlag is true.
    * 
    * @param tx Value of text string to print. 
    * @return void
    */     
    public void say(String tx)
    {    
        if (auditFlag) println tx;
    } // end of say
    

   /** 
    * Support method to produce and write a translated build.gradle
    * 
    * @Param    map of all runtime environmental var.s - 
    *		deferred passing binding until now so later variables can be passed from Make.groovy 
    * @return   true when a new build.gradle script has been written
    */     
    public boolean writeGradle(Map binding)
    {
	gradleFlag = binding['scriptsFlag'];
	classname = binding['classname'];
	testFlag = binding['testFlag'];

        boolean continueflag = gradleFlag;
        
        if (continueflag)
        {   
		// build gradle/maven folders
	        buildGradleFolders();        
	                                	                                
	    	String outname = originaloutputpathname+fs+"build.gradle" 
	        boolean ok = co.chkobj(outname);
        	say "\n\nwriteGradle(Map binding) to outname=[${outname}] ok=$ok \n"        
             
	     	/** ask about over-writing existing build.gradle file */
        	if (ok)
        	{
            		String theMessage = "build.gradle already exists -\nreplace it with this new version ?";
            		def a = ask.ask("Overwrite Existing File ?", theMessage){x->  };    
	    		int result = a.returncode;
            		say "writeGradle(Map binding) ask result=[${result}]\n\n\n"        
			switch (result)
			{
				case -1 :	System.exit(0);
						break;
				case 0	:	ok = false;
						break;
			} // end of switch        
	        } // end of if
                        
        	if ( !(ok) )
        	{
            		say "... ok to write gradle file "+outname;
		        copyGradleProperties();

			try{
				String pn = binding['packagename'].trim();
                		binding['fullclassname'] = ( pn.size() > 0 ) ? pn+'.'+binding['classname'] : binding['classname'];
		                binding['projectname'] = binding['projectfoldername'].trim()+"Project";
		            	payload = template.make(binding).toString();
			        new File(outname).write(payload);
        			say "... build.gradle written to "+outname;

		                // now do settings file
                		String classname = binding['classname'].trim();
                		payload = """rootProject.name = '${classname}Project'
""".toString();
                		new File(originaloutputpathname+fs+"settings.gradle").write(payload);
				updateGradle(outname,binding);
            		}
			catch(Exception x) 
            		{ 
				continueflag = false;
            			say "build.gradle template failed with ${x.message}"; 
            		} // end of catch

			if (continueflag && mc.hasGradle() && binding['wrapperFlag'])
	    	 	{
		              try
		              {
                		println "----------------------------------------------------------------------"
                		println "--- Start of gradle wrapper execute for ${classname} in path ${outname} ---"
                		println "----------------------------------------------------------------------"
                		println "gradle wrapper --profile -b ${outname}  .execute()"
                		def p = "gradle wrapper --profile -b ${outname}".execute()
                		p.waitFor();
                		
		                println "return code: ${ p.exitValue()}"
                		println "stderr: ${p.err.text}"
	                	println "stdout: ${p.in.text}"

				String cmd = "${originaloutputpathname}${fs}gradlew --profile -b ${outname} ";
				println cmd; 
		                p = cmd.execute()
                		p.waitFor();

		                println "return code: ${ p.exitValue()}"
                		println "stderr: ${p.err.text}"
		                println "stdout: ${p.in.text}"
                		println "----------------------------------------------------"
                		println "--- end of ${classname} sysout ---"
                		println "----------------------------------------------------"

			        continueflag = true;
	              	      }
              		      catch(Exception x) 
              		      { 
              			say "gradle wrapper or build of new class $classname failed with ${x.message}"; 
              		      } // end of catch
	            	} // end of if
            
	        } // end of if
        } // end of if continue

        return continueflag;
    } // end of method
    
    
    
   /** 
    * Support method to do run gradle on generated templates and 
    * possibly compile them
    * 
    * @param    outname - the full absolute path+file name of the new class 
    * @return   null or process exit value
    */     
    public runGradle(String outname)
    {
        // yes we have gradle - use it to build our framework
        if ( ch.needsGradleFolders() && ch.get('scriptsFlag') )
        {
            if ( mc.hasGradle() )
            {
                // ok - gradle on - let's do it !!
                say "\n-------------------------------------\n... running-> 'gradle check run' .execute in path "+outname;
                
                // give execute() a null environmental list plus current working directory ---
                // then check the full project and run the default main job
                def p = "gradle  --profile  check run ".execute(null, new File(outname)); 
                p.waitFor();
                
                say "build.gradle exitValue()="+p.exitValue()
                say "${p.in.text.trim()}"
                say "... gradle complete on "+outname;
                say "---------------------------------------\n\n\n"
		return p.exitValue();
            } // end of if

        } // end of if
        
        return -1;
    } // end of method
    

   /** 
    * Method to construct gradle folders if necessary.
    * 
    * @return void 
    */     
    protected void buildGradleFolders()
    {
	println "buildGradleFolders() starting ....."

	/** Handle to construct a single full path plus gradle main name */
    	NameGenerator ng = new NameGenerator();        

	String gradleoutputpathname = ng.getProjectBuildPath();
	gradleoutputpathname = ng.build();
	println "buildGradleFolders() using $gradleoutputpathname ....."


	if (testFlag)
	{
		ng = new NameGenerator();
		ng.setup(!testFlag);
		String gradletestoutputpathname = ng.getProjectBuildPath();
		gradletestoutputpathname = ng.build();
		println "buildGradleFolders() test using $gradletestoutputpathname ....."
	} // end of if
        
	println "buildGradleFolders() ending ....."	
    } // end of getGradle method
    
    

   /** 
    * Support method to append a gradle run task to the current build.gradle if not already present
    * 
    * @Param    binding map of all runtime environmental var.s - 
    * @return   true when a new build.gradle script has been extended by a new run task
    */     
    private boolean updateGradle(String outname, Map binding)
    {
	say "\nNow update existing build.gradle with another run task"
        boolean continueflag = false;                                    
        boolean ok = co.chkobj(outname);
        say "updateGradle() to outname=[${outname}] ok=$ok ? \n"        
        
        // if build.gradle exists
	if (ok)
	{
		def tx = new File(outname).text;
		String ky = "run"+binding.classname.trim();
		int ix = tx.indexOf(ky)	

		// if current build.gradle does not already have runXXX task 
		if (ix < 0)
		{
	    		def runner = """
        
task run\${classname}(type: JavaExec, dependsOn: 'classes') {
    main = '\${fullclassname}'
    classpath = configurations.runtime
    classpath+=sourceSets.main.runtimeClasspath
} // end of run

        
"""
        		engine = new groovy.text.SimpleTemplateEngine();
        		Template template2 = engine.createTemplate(runner);
        		String payload2 = template2.make(binding).toString();
			say "updateGradle payload2=[${payload2.toString()}]\n ky= $ky and ix=$ix";
	
			continueflag = new File(outname).append(payload2.toString())
		} // end of if
	} // end of ok
	
	return continueflag;
    } // end of method


   /** 
    * Support method to copy a gradle properties file to this output folder
    * 
    * @return   true if copy was successful
    */     
    public boolean copyGradleProperties()
    {
        boolean flag = false;
	def name = "./resources/gradle.properties"
	def oname = "${originaloutputpathname}${fs}gradle.properties"

	try{
		def tx = new File(name).text
		new File(oname).write(tx+'\n');
	
	        say "copied gradle.properties";
        	flag = true;
	}
	catch(Exception x)  {say "failed to copy gradle.properties due to "+x.message; }
	
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
        println "--- starting Make ---"
        
        /** a  directory name that points to the user's home directory */
    	String home = System.getProperty("user.home");
    	String fs = java.io.File.separator;

        ConfigHandler ch = new ConfigHandler();
        boolean flag = true;


        // Variable buidlgradlename Describes the name of the gradle template skeleton to clone from.
        String buildgradlename = "${home}${fs}Dropbox${fs}Constructor${fs}resources${fs}build.gradle";

        // Variable describes the name of the target output path where the gradle assets are written.        
        String originaloutputpathname = "${home}${fs}Dropbox${fs}Constructor${fs}temp";

        println "new GradleHelper($buildgradlename) to [${originaloutputpathname}]"
        GradleHelper gm = new GradleHelper(buildgradlename, originaloutputpathname);
	        
        println "starting gm.writeGradle() binding="+ch.binding+" to write to "+originaloutputpathname;
        flag =  gm.writeGradle(ch.binding);

        String msg = (flag) ? "yes - built ok" : "no, could not build" ;
        println "was build.gradle successful ? "+msg;

	println "\n\n=================================================\n"
	def ans = gm.runGradle(originaloutputpathname)
	println ans;
	
        println "\n------------------------------------\n\n--- the end ---"
    } // end of main

} // end of class