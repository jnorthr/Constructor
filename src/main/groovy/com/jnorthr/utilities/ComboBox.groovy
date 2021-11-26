package com.jnorthr.utilities;
import groovy.io.FileType
import javax.swing.JDialog;
import javax.swing.JOptionPane;

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
 * ComboBox class description
 *
 * Groovy class to allow user to choose an input template file from either the core or the test set of templates.
 * 
 */  
 public class ComboBox
 {
    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;
    

    /** an O/S specific location for the user's home folder name */ 
    String userhome = System.getProperty("user.home");


    /** a flag if true to choose a template from the typical normal core template directory
    * or false to choose from test template directory 
    */ 
    boolean title = true;    


    /** holds the user selected name of the template to be used in the make process */ 
    String chosenTemplate = null;    


   /** 
    * Variable handle to the current environmental file named .make.properties.
    */  
    ConfigHandler ch = new ConfigHandler();


    /** an O/S specific location for the user's core template folder name */ 
    String path = ch.get("templatepathname");


   /** 
    * Variable handle to the current list of available templates for this category.
    */  
    String[] list = []


   /** 
    * Default Constructor 
    * 
    * @return Chooser object
    */     
    public ComboBox()
    {
        getFiles();
    } // end of constructor


   /** 
    * NonDefault Constructor 
    * 
    * @param string indicating the starting template folder location for this chooser to begin at
    * @return ComboBox object
    */     
    public ComboBox(boolean which)
    {
        title = which;
        String key = (!which) ? "testtemplatepathname" : "templatepathname" ;

        println "key=$key and ch=\n"+ch.toString()
        path = ch.get(key);
        getFiles();
    } // end of constructor



   /** 
    * NonDefault Constructor 
    * 
    * @param string indicating the starting template category for this chooser to begin at
    * @param handle to a ConfigBuilder holding current working environment
    * @return ComboBox object
    */     
    public ComboBox(boolean which, ConfigHandler nch)
    {
        title = which;
        String key = (!which) ? "testtemplatepathname" : "templatepathname" ;
        ch = nch;
        path = ch.get(key);
        getFiles();
    } // end of constructor


   /** 
    * Method to display internal variables.
    * 
    * @return formatted content of internal variables
    */     
    public String toString()
    {
        return """title=${title}
user.home=${userhome}
java.io.File.separator=${java.io.File.separator}
chosenTemplate=${chosenTemplate}
list=${list}
"""
    }  // end of string



   /** 
    * Method to get user choice of files available as input templates.
    * 
    * @return the filename of the user selected target
    */     
    public String getFiles()
    {
        def dir = new File(path);
        if (!dir.exists() )
        {
            println "--> cannot locate path $path"
            JOptionPane.showMessageDialog(null, "Cannot locate path $path");
	    System.exit(1);
        } // end of if
        
        String str="";
        dir.eachFileRecurse (FileType.FILES) { file ->
              str = file.toString()
              int i = str.lastIndexOf(fs);
              i+=1;
              String xxx = str.substring(i);
              if (!xxx.startsWith('.'))  { list += xxx; }              
        } // end of each

        chosenTemplate = path+fs+list[0];
        chosenTemplate = new File(chosenTemplate).getCanonicalFile();
        println "getFiles() chosen "+chosenTemplate
    }  // end of method        


   /** 
    * Method to get user choice of template to use as input.
    * 
    * @return the filename of the user selected target
    */     
    public String getChoice()
    {
        JDialog.setDefaultLookAndFeelDecorated(true);
        String tx = (title) ? "core" : "test"; 
        Object selection = JOptionPane.showInputDialog(null, "Please choose which $tx template to use as input",
        "Template Choice", JOptionPane.QUESTION_MESSAGE, null, list, null);

        if (selection==null) 
        { 
            chosenTemplate = ""; 
        }
        else
        {
            chosenTemplate = path+fs+selection.toString();
            chosenTemplate = new File(chosenTemplate).getCanonicalFile();
        } // end of else3
        
        String key = (!title) ? "testtemplatefilename" : "templatefilename";
        ch.put(key, chosenTemplate);
        ch.writeConfig();

        return chosenTemplate;    
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
        String name = "ComboBox"
        println "--- starting ${name} ---"

        ComboBox obj = new ComboBox(false);
        println "${name} = [${obj.toString()}]"

        String fn = obj.getChoice();
        println "Chosen test template is [$fn]\n\n";

        obj = new ComboBox(true);
        println "${name} = [${obj.toString()}]"
        fn = obj.getChoice();
        println "Chosen core template is [$fn]\n\n";

        obj = new ComboBox();
        println "${name} = [${obj.toString()}]"
        fn = obj.getChoice();
        println "Chosen default core template is [$fn]\n\n";


          println "\n----------------------\nGet new ComboBox using the pathname in the ConfigHandler binding"
        ConfigHandler ch = new ConfigHandler();
        obj = new ComboBox(false, ch);
        println "${name} = [${obj.toString()}]"
        fn = obj.getChoice();
        println "Chosen test template is [$fn]\n\n";



        println "--- the end of ${name} ---"
    } // end of main

} // end of class