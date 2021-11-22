package com.jnorthr.utilities;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import com.jnorthr.utilities.FolderBuilder;

import static javax.swing.JFrame.EXIT_ON_CLOSE  
import java.awt.*
import javax.swing.*;
import javax.swing.JOptionPane;
import com.jnorthr.utilities.tools.*

/** 
 * TemplateMaker class description
 *
 * Groovy class to read template script and write new version with a valid name being created. 
 * This way you should be able to just run the output script immediately assuming you have gradle build tool installed.
 */  
 public class TemplateMaker
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;
    

   /** 
    * Handle ch connects to current configuration.
    */  
    ConfigHandler ch = new ConfigHandler();

   /** 
    * Variable auditFlag True to cause a joblog to be produced
    */  
    boolean auditFlag = true;


   /** 
    * Variable packagename Holds the string used when a class is part of a package of code. Can be blank.
    */  
    String mypackagename = "";


   /** 
    * Variable classname Holds the string used when a class name for a part of code.
    */  
    String myclassname = "";


   /** 
    * Variable myname Holds the string used to describe the class. Non-blank entries take precedence over binding['name'] 
    */  
    String myname = "This is a Funny way to see tomorrow.";

   /** 
    * Variable template Holds the string used in template translation.
    */  
    Template template;

   /** 
    * Variable gradleFlag Indicates if the generation process should include gradle's extra folder names of src/main/groovy 
    * and src/test/groovy that will be written.
    */  
    def gradleFlag = false;


   /** 
    * Variable testFlag True indicates the generation process should use src/test/groovy rather than the src/main/groovy structure and that will be used for gradle. Ignored when gradleFlag = false 
    */  
    boolean testFlag = false;


   /** 
    * Handle fb connects to tool to write missing pieces of an output path.
    */  
    FolderBuilder fb = new FolderBuilder();


    // =====================================================================

   /** 
    * Default Constructor 
    * 
    * @return TemplateMaker object
    */     
    public TemplateMaker()
    {
        setup();    
    } // end of default constructor


   /** 
    * Non-Default Constructor 
    * 
    * @param  ConfigHandler setup by calling module
    * @return TemplateMaker object
    */     
    public TemplateMaker(ConfigHandler nch)
    {
	ch = nch;
        auditFlag = ch.get("auditFlag");
    	setup();    
    } // end of default constructor


   /** 
    * Confirm the name of the input template skeleton to be used in writeTemplate(). Get text from that and construct template ready to bind
    * 
    * @return TemplateMaker object
    */     
    public setup()
    {
    	gradleFlag = ch.get("gradleFlag");
    	testFlag = ch.get("testFlag");
    	
    	String key = (testFlag) ? 'testtemplatefilename' : 'templatefilename';
        setup(ch.get(key));
    } // end of setup method



   /** 
    * Provide the name of the input template skeleton to be used in writeTemplate(). 
    * Get text from that and construct template ready to bind
    * 
    * @param  filename of file holding template text to be used from now on. 
    * @return void
    */     
    public void setup(String filename)
    {
        assert filename!=null, "must provide full name of template script";

        ChkObj co = new ChkObj();
        def ok = co.chkobj(filename)

        if (!ok)
        {
            say "--> sorry to report that template ${filename} does not exist"    
            System.exit(0);
        } // end of if 
        else
        {        
            def engine = new groovy.text.SimpleTemplateEngine();
            String payload = fi.text;
            template = engine.createTemplate(payload);
            say "TemplateMaker(${filename}) found payload of ${payload.size()} bytes"
        } // end of else        
     } // end of setup method
    

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
    * Method to provide new ConfigHandler if needed.
    * 
    * @param conhan Handle pointing to updated ConfigHandler. 
    * @return void
    */     
    public void updateCH(ConfigHandler conhan)
    {    
        ch = conhan;
    } // end of method
    
    
   /** 
    * Method to ask FolderBuilder to construct missing path components.
    * 
    * @param conhan Handle pointing to updated ConfigHandler. 
    * @return void
    */     
    public void buildPath(String var)
    {    
        print "FolderBuilder.processPath($var)"
        String nop = fb.confirmPath(var);
        println " said $nop"
    } // end of method
    

   /** 
    * Method to ask user if they want to remove pre-existing output file b4 we overwrite it.
    * 
    * @param fn is the full name of the output template to be written. 
    * @return ok true if dup file was successfully removed
    */     
    public boolean askUser(String fn)
    {    
        boolean ok = false; 
        String theMessage = "Output file \n${fn}\n already exists - can it be over-written ?";
        int result = JOptionPane.showConfirmDialog(null, theMessage, "Output File Already Present", JOptionPane.YES_NO_CANCEL_OPTION);
        if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
        { 
            System.exit(0);
        } // end of if

        if(result == JOptionPane.YES_OPTION)
        { 
            try{ 
                ok = new File(fn).delete();
            }
            catch(Exception x) { }
        } // end of if
        else
        {
            return true
        }

        return ok
    } // end of method
 

   /** 
    * Support method to produce and write a translated template
    * 
    * @param    outname - the full absolute path+file name of the new script that will be written from the input skeleton template
    *           like:  /Users/eve/Dropbox/Constructor/src/test/groovy
    *           It must contain the proper path separator char.s like / or \
    *           It forms the left-hand side of the output filename and must not contain the classname
    * 
    * @param    classname - NOT the full absolute path+file name of the class being generated - with package name too;
    *           path separator char. are converted to dot/period char.s instead
    *           ex.: /com/jnorthr/utilities/Chooser.groovy  becomes .com.jnorthr.utilities.Chooser 
    * 		    which looses leading . to become com.jnorthr.utilities.Chooser
    *
    * @return   true when a new script script has been written
    */     
    public boolean writeTemplate( String outname, String classname)
    {
        boolean flag = false;
        assert classname.indexOf('.')<0, "Classname [$classname] cannot have package name or file suffix";
        say "\nwriteTemplate(String $outname, String $classname)"
	myclassname = classname;
    	this.buildPath(outname)

        String ouna = outname + fs + myclassname + '.' + ch.get('filesuffix');
        
        say "ouna="+ouna;
        
        ChkObj co = new ChkObj();
	boolean ok = co.chkobj(ouna);
        
        // if already exists - ask user what to do
        if (ok)
        {
        	ok = askUser(ouna);
        } // end of if
                
        // if output file does NOT already exist, or user says 'yes' reuse it - then comtinue
        if (!ok)
        {
            say "\n... writing template "+outname+" for class $myclassname";
            mypackagename = 'com.fred'
            boolean bindingOk = checkBinding();
            boolean worked = buildTemplate(ouna, myclassname);
            String work = (worked) ? "yes" : "no";
            say "... template complete for "+ouna+"; worked ? "+work;

            flag = worked;
        } // end of if

        return flag;
    } // end of method
    
    
   /** 
    *  Support method to build and write a translated template
    * 
    * @param    newfilename - the full absolute path+file name of the output destination 
    *		where the new script will be written from the input skeleton template
    *           like:  /Users/eve/Dropbox/Constructor/src/test/groovy/com/jnorthr/utilities/Chooser.groovy
    *           It must contain the proper path separator char.s like / or \
    *
    *           The assumption is that these var.s have been set: packagename, classname, binding 
    * 		plus any others declared in the skeleton templates
    *           if not in binding then translate will fail 
    * @return   true when a new script script has been written
    */     
    public boolean buildTemplate(String newfilename, String cn)
    {
        boolean ok = false;


        try{
            // build binding for template                        
            String payload = template.make(ch.binding).toString();
            new File( newfilename).write(payload);
            ok = true;
            println "buildTemplate($newfilename $cn).write(${payload.size()} bytes)"
        }
        catch(Exception x)
        {
            def tx = "TemplateMaker could not create output file named [${newfilename}] due to '"+x.message+"';";
            say tx;
            new File( newfilename).write(tx);
        } // end of catch

        return ok;
    } // end of method




   /** 
    * Support method to see if binding has needed keys
    * 
    * @return  true when all binding entries are found
    */     
    public boolean checkBinding()
    {
        boolean ok = true;

        // confirm classname
        def na = ch.get('classname');

        // if myclassname has something, use that first 
        if (myclassname.size() > 0 )
        {
            ch.put( 'classname', myclassname );
        } // end of if
        else
        {
            if (na==null || na.size()<1)
            {
                ch.put( 'classname', "Undeclared" );
                ok = false
            } // end of if
        } // end of if
        

        // confirm packagename but can legally be blank too
        // if mypackagename has something, use that first 
        if (mypackagename.size() > 0 )
        {
            ch.put( 'packagename', "package $mypackagename;" );
        } // end of if

        else
        {
            na  = ch.get('packagename');
            if ( na==null || na.size() < 1 )
            {
                say "no packagename declared";  // legal not to have a packagename
                ch.put( 'packagename', "" );
            } // end of if
            else
            {
                if ( !na.trim().toLowerCase().startsWith('package')  )
                {
                    na = "package ${na}; " 
                    ch.put( 'packagename', na );
                } // end of if
            } // end of else

        } // end of else
        

        // if myname has something, use that first 
        if (myname.size() > 0 )
        {
            ch.put( 'name', myname );
        } // end of if
        else
        {
            na  = ch.get('name');
            if (na==null || na.size()<1)
            {
                ch.put( 'name', ch.get('classname') );
            } // end of if
        } // end of if
        
        return ok;
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
        println "--- starting TemplateMaker ---"
        /** an O/S specific char. as a file path divider */
        String fs = java.io.File.separator;

        /** an O/S specific location for the user's home folder name */ 
        String home = System.getProperty("user.home");


        // ==================================================================
        // Variable templatefilename -  Describes the name of the template skeleton to clone from - test in this case
        String templatefilename= "${home}${fs}Dropbox${fs}Constructor${fs}resources${fs}test${fs}skeleton.groovy";
        TemplateMaker tem = new TemplateMaker();

        println "starting tem.setup($templatefilename) ";
        tem.setup(templatefilename);
        
        String outpath = "${home}${fs}Dropbox${fs}Constructor${fs}temp${fs}src${fs}test${fs}groovy";

        println "\nstarting tem.writeTemplate() for test sample";
        def flag =  tem.writeTemplate( outpath, "SampleTest123" );
        
        String msg = (flag) ? "yes - built ok" : "no, could not build" ;
        println "was it successful ? "+msg;
        println "=========================================\n"

        // ==================================================================
        // now build main template    
        FolderBuilder fb = new FolderBuilder(true);

        templatefilename= "${home}${fs}Dropbox${fs}Constructor${fs}resources${fs}main${fs}skeleton.groovy";
        tem = new TemplateMaker();
        tem.setup(templatefilename);
        
        outpath = "${home}${fs}Dropbox${fs}Constructor${fs}temp${fs}src${fs}main${fs}groovy";
        def nop = fb.confirmPath(outpath);
        println "FolderBuilder processPath($outpath) said $nop"
        //outname = "${outpath}${fs}mainsample.groovy";

        // compose full main path name and build it
        flag =  tem.writeTemplate( outpath, "Sample12345" );
        msg = (flag) ? "yes - built ok" : "no, could not build" ;
        println "was it successful ? "+msg;

        println "--- the end ---"
    } // end of main

} // end of class