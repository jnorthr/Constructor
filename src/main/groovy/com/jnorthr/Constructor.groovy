package com.jnorthr;
// more ideas fro MrHaki here: http://mrhaki.blogspot.com/2009/11/groovy-goodness-building-gui-with.html
// Check Configuration Property Is Set In ConfigObject = http://mrhaki.blogspot.com/2014/05/groovy-goodness-check-configuration.html -

import groovy.transform.ToString;
import groovy.swing.SwingBuilder  
import groovy.beans.Bindable
import groovy.swing.*

import static javax.swing.JFrame.EXIT_ON_CLOSE  
import java.awt.*
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.*
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SpinnerListModel;
import javax.swing.UIManager;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import javax.swing.KeyStroke;

import com.jnorthr.utilities.*
import com.jnorthr.utilities.tools.*

/** 
 * Constructor class description
 *
 * Groovy class to do something. 
 * 
 */  
 @ToString
public class Constructor
{
    /** is the Make task running ? */
    boolean running = false;

    /** last known call to Make gave this response */
    int returncode = 0;

    /** Icon with warning image */
    Icon warnIcon

    /** Link to the ch environment */
    ConfigHandler ch; 

    /** Handle to the Swing Constructor */
    SwingBuilder swingBuilder;

    /** Handle to the file chooser */
    JFileChooser fc;

    /** true if we must update the .make.properties file ? */
    boolean forceWrite = false;

    /** an O/S specific char. as a file path divider */
    String fs = java.io.File.separator;

    /** is at least one template selected ? */
    boolean allow = false;

    /** Method to decide whether to keep the project title for documentation purposes only */
    boolean ptFlag = true;


   // ======================================================= 
   /** 
    * Default Constructor 
    *
    * @return Constructor object
    */     
    public Constructor()
    {
        ch = new ConfigHandler();

        try {
            warnIcon = new ImageIcon(".${fs}resources${fs}ajax-loader.gif");
            // from within project/src/resources dir : 
            // Image img = ImageIO.read(getClass().getResource("resources/water.bmp"));
        } 
        catch (IOException e) 
        {
            say "==> could not find image to load for Continue button"; 
        }

	allow = true;  // ch.getFlag('coreFlag') || ch.getFlag('testFlag')        
        buildWindow();
        //setup();
            
    } // end of constructor



   /** 
    * Method to print audit log.
    * 
    * @param the text to be said
    * @return void
    */     
    def say(txt){ println txt; }



   /** 
    * Private Internal Method to pick a folder in local file system.
    * 
    * @param the event causing this call
    * @return void
    */     
    private void selectFile( event = null ) 
    {
        Chooser obj = new Chooser();
	obj.selectFolders()
        def selectedFolder = obj.getChoice();
        
        if (selectedFolder!=null && selectedFolder.trim().size() > 0 )
        {
        	ch.update( selectedFolder, "" ); 
        	swingBuilder.pathField.text = selectedFolder;
        	swingBuilder.sample.text= getPathName(selectedFolder);
	        forceWrite = true;    
        } // end of if
        swingBuilder.buttonMaker.revalidate()
        swingBuilder.pathField.requestFocus()
        swingBuilder.pathField.requestFocusInWindow();
    } // end of method


   /** 
    * Private Internal Method to pick a template file in local file system.
    * 
    * @param the event causing this call
    * @return void
    */     
    private void selectTemplate( event = null ) 
    {
        ComboBox obj = new ComboBox(true);
        def selectedFile = obj.getChoice();
        if (selectedFile!=null && selectedFile.trim().size() > 0 )
        {
	        ch.put( "templatefilename", selectedFile ); 
        	swingBuilder.tfn.text = selectedFile;
	        forceWrite = true;    
	} // end of if
        swingBuilder.buttonMaker.revalidate()
        swingBuilder.tfn.requestFocus()
        swingBuilder.tfn.requestFocusInWindow();
    } // end of method
  
   /** 
    * Private Internal Method to pick a test template file in local file system.
    * 
    * @param the event causing this call
    * @return void
    */     
    private void selectTestTemplate( event = null ) 
    {
        ComboBox obj = new ComboBox(false);
        def selectedFile = obj.getChoice();
        if (selectedFile!=null && selectedFile.trim().size() > 0 )
        {       
        	ch.put( "testtemplatefilename", selectedFile ); 
        	swingBuilder.ttfn.text = selectedFile;
	        forceWrite = true;    
	} // end of if
        swingBuilder.buttonMaker.revalidate()
        swingBuilder.ttfn.requestFocus()
        swingBuilder.ttfn.requestFocusInWindow();
    } // end of method


   /** 
    * Method to present a dialog to the user.
    * 
    * @param the text to show in this window
    * @return void
    */     
    def display(String msg)
    {
        int i = msg.indexOf('|');
        String tl = (i > -1) ? msg.substring(0,i) : "For Your Information" ;
        String tx = (i > -1) ? msg.substring(i+1) : msg;

        def pane = swingBuilder.optionPane(message:tx)
        def dialog = pane.createDialog(null, tl)
        dialog.show()                
    }
  

   /** 
    * Method to present an ABOUT dialog to the user.
    * 
    * @param the event causing this method
    * @return void
    */     
    def register(event) 
    {
        def msg = "About Constructor|Welcome to the wonderful world of Constructor V1.0\n\nThe output path identifies a storage point in your directory structure. This folder is used to hold your new project. \n\nThe result is a translated core template file and optionally a test template file too.\nDepending on your requirements, a full folder structure might be written if it does not already exist."  
        display(msg);
    } // end of register


   /** 
    * Method to handle GRADLE folder checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseGRADLE(event,choiceMaker) 
    {
        swingBuilder.arGRADLE.enabled = event.source.selected;
        ch.put('gradleFlag', event.source.selected );  
        forceWrite = true;  
        choiceMaker.revalidate()
    } // end of chooseGRADLE


   /** 
    * Method to handle GRADLE WRAPPER tool checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseWRAPPER(event,choiceMaker) 
    {
        ch.put('wrapperFlag', event.source.selected );  
        forceWrite = true;  
        choiceMaker.revalidate()
    } // end of chooseGRADLE


   /** 
    * Method to handle GRADLE Script checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseGRADLES(event,choiceMaker) 
    {
        swingBuilder.GRADLES.enabled = event.source.selected;
        ch.put('scriptsFlag', event.source.selected );  
        forceWrite = true;  
        choiceMaker.revalidate()
    } // end of chooseGRADLE


   /** 
    * Method to handle TRAVIS checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseTRAVIS(event,choiceMaker) 
    {
        swingBuilder.arTRAVIS.enabled = event.source.selected;
        ch.put('travisFlag', event.source.selected );  
        forceWrite = true;
        choiceMaker.revalidate()
    } // end of chooseTRAVIS


   /** 
    * Method to handle ASCIIDOC checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseASCIIDOC(event,choiceMaker) 
    {
        ch.put('asciidocFlag', event.source.selected );  
        forceWrite = true;
        choiceMaker.revalidate()
    } // end of chooseASCIIDOC


   /** 
    * Method to handle CORE checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseCORE(event,input) 
    {
        ch.put('coreFlag', event.source.selected );  
        forceWrite = true;
        allow = event.source.selected;
        swingBuilder.go.enabled = event.source.selected; // || swingBuilder.TEST.selected; 
        input.revalidate()
    } // end of chooseCORE


   /** 
    * Method to handle TEST checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseTEST(event,input) 
    {
        ch.put('testFlag', event.source.selected );  
        allow = event.source.selected;
        swingBuilder.go.enabled = event.source.selected; // || swingBuilder.CORE.selected; 
        forceWrite = true;
        input.revalidate()
    } // end of chooseTEST


   /** 
    * Method to handle AUDIT checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseAUDIT(event,choiceMaker) 
    {
        ch.put('auditFlag', event.source.selected );  
        forceWrite = true;
        choiceMaker.revalidate()
    } // end of chooseAUDIT


   /** 
    * Method to handle GIT checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseGIT(event,choiceMaker) 
    {
        ch.put('gitFlag', event.source.selected );  
        swingBuilder.arGIT.enabled = event.source.selected;
        swingBuilder.TRAVIS.enabled = event.source.selected;

        swingBuilder.arTRAVIS.enabled = event.source.selected;
        forceWrite = true;
        choiceMaker.revalidate()
    } // end of chooseGIT


   /** 
    * Method to handle GIT auto remove checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseGitAR(event,autoRM) 
    {
        ch.put('arGitFlag', event.source.selected );  
        forceWrite = true;
        autoRM.revalidate()
    } // end of chooseGIT


   /** 
    * Method to handle Gradle auto remove checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseGradleAR(event,autoRM) 
    {
        ch.put('arGradleFlag', event.source.selected );  
        forceWrite = true;
        autoRM.revalidate()
    } // end of chooseGIT


   /** 
    * Method to handle Travis auto remove checkbox from a dialog to the user.
    * 
    * @param the event causing this method
    * @param a handle to the panel id of the event calling this method
    * @return void
    */     
    def chooseTravisAR(event,autoRM) 
    {
        ch.put('arTravisFlag', event.source.selected );  
        forceWrite = true;
        autoRM.revalidate()
    } // end of chooseGIT


   /** 
    * Method to compose and display the swingbuilder panel dialog to the user.
    * 
    * @return void
    */     
    def buildWindow()
    { 
	System.setProperty("apple.laf.useScreenMenuBar", "true")
	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Constructor")
	//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	JFrame.setDefaultLookAndFeelDecorated(true);
	def createShortcutWithModifier = { key, modifier -> 
		KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | modifier) 
	} // end of create
	
	def createShortcut = { key -> createShortcutWithModifier(key, 0) }
	
        swingBuilder = new SwingBuilder()
        swingBuilder.edt 
        {  
            // edt method makes sure UI is build on Event Dispatch Thread.
            //lookAndFeel 'system'  //'system'   // Simple change in look and feel.

            frame(id:'frame', title: 'Constructor', size: [1060, 640], minimumSize: [1020, 600],show: true, 
            	locationRelativeTo: null, defaultCloseOperation: EXIT_ON_CLOSE, background:java.awt.Color.BLACK) 
            {     
                borderLayout(vgap: 5, hgap:2)
                menuBar() 
                {
                    menu(text: "File", mnemonic: 'F') 
                    {
                        menuItem(text: "Open",  mnemonic: 'O', actionPerformed:  this.&selectFile,accelerator: createShortcut(KeyEvent.VK_O) )
                        menuItem(text: "About", mnemonic: 'A', actionPerformed:  this.&register, accelerator: createShortcut(KeyEvent.VK_A) )
                        menuItem(text: "Exit",  mnemonic: 'X', actionPerformed: {System.exit(0) })
                    } // end of menu
                } // end of menuBar

	        // NORTH
		panel(id:"input", constraints: BorderLayout.NORTH, border: titledBorder('Output Choices'), background:java.awt.Color.cyan ) 
        	{
            		tableLayout(background:java.awt.Color.cyan) {
                        tr {
                            td {
                                label 'Project Title ( Mandatory ) :'  // text property is default, so it is implicit.
                            }
                            td(colspan:2) {
                                pt = textField(id: 'projectTitle', name:'projectTitle', columns: 20, text:ch.binding.projecttitle, 
                                toolTipText:'Enter a project name for documentation purposes',
                                focusLost:{ setProjectTitle(projectTitle.text); forceWrite = true; })
                            }
                        } // end of tr

                        

                        tr {
                            td {
                                label 'Output path:'  // text property is default, so it is implicit.
                            }
                            td{
                                fred = textField(id: 'pathField', name:'pathField', columns: 40, text:ch.binding.pathname, 
                                actionPerformed:{ ch.put('pathname', pathField.text ); forceWrite = true; }, 
                                toolTipText:'Your output path is here or change it, use CHOOSE button ',
                                enabled:false, focusable:false, 
                                focusLost:{ ch.put('pathname', pathField.text); forceWrite = true; })
                            }
                            td(align:'left'){ button text: 'Choose', enabled:true, toolTipText:'Click or SPACE BAR to identify a folder to hold your project', actionPerformed: this.&selectFile; }
                        } // end of tr
                
                
                        tr {
                            td {
                                label 'Group Folder ( Optional ):'  // text property is default, so it is implicit.
                            }
                            td(colspan:2) {
                                fred = textField(id: 'newFolderField', name:'newFolderField', columns: 20, text:ch.binding.projectfoldername, 

                                actionPerformed:{ ch.put('projectfoldername', newFolderField.text ); forceWrite = true; }, 
                                toolTipText:'Optionally, enter a grouping folder to build within your output path chosen above',
                                focusLost:{ ch.put('projectfoldername', newFolderField.text); forceWrite = true; })
                            }
                        } // end of tr
                
                        tr {
                            td {
                                label 'Gradle Folders :'  // text property is default, so it is implicit.
                            }
                            td(colfill:true)   {
checkBox(id:"GRADLE", text:"Generate Folders ?", selected:getBoolean('gradleFlag'), focusable:true, toolTipText:'Check this to include the maven/gradle folders in the result', actionPerformed:{e->chooseGRADLE(e,choiceMaker)} );
                            }
                        } // end of tr

                        tr {
                            td {
                                label 'Package name:'  
                            }
                            td(colspan:2)  {
                                pan = textField(id: 'packageField', name:'packageField', columns: 20, text:ch.get('packagename'), 
                                actionPerformed:{ ch.put('packagename', packageField.text ); forceWrite = true; }, 
                                toolTipText:'Key the package name ofor your java/groovy class, like com.tools.hammer or /com/tools/hammer',
                                focusLost:{ ch.put('packagename', packageField.text); forceWrite = true;  })
                            }
                        } // end of tr
                        
                        tr {
                            td(colspan:3) {
                            label ' ' 
                            } // end of td
			} // end of tr
                                        
                        tr {
                            td {
                                label 'Example :'  // text property is default, so it is implicit.
                            }
                            td(colspan:2) {
                            	label(id:'sample', text:getPathName() )
			    }
			} // end of tr
			
	    } // end of table
    	} // end of NORTH



                // WEST
                panel(id:"choiceMaker", constraints: BorderLayout.WEST, 
                	border: compoundBorder([emptyBorder(5), titledBorder('Includes ?')]) , background:java.awt.Color.LIGHT_GRAY)  
                 {
                    vbox
                    {   
                        glue()
                        vstrut(10)
                        vglue()
                        vbox(){
                        label(text:'-- Gradle -- ') 
checkBox(id:"GRADLES", text:"Scripts", selected:getBoolean('scriptsFlag'), focusable:false,  toolTipText:'Check this to generate maven/gradle scripts', actionPerformed:{e->chooseGRADLES(e,choiceMaker)} );

checkBox(id:"WRAPPER", text:"Wrapper", enabled:getWrapper(), selected:getBoolean('wrapperFlag'), focusable:false,  toolTipText:'Check this to generate a stand-alone grade wrapper tool in your new folders', actionPerformed:{e->chooseWRAPPER(e,choiceMaker)} );

 			} // end of vbox
 			
                        vstrut(10)
                        vglue()
                        separator()
                        vstrut(10)
                        vglue()
                        checkBox(id:"GIT", text:"GitHub", selected:getBoolean('gitFlag'), enabled:true, focusable:false,   toolTipText:'Check here to create or update a github repo', actionPerformed:{e->chooseGIT(e,choiceMaker)});

                        vstrut(10)
                        vglue()
                        checkBox(id:"TRAVIS", text:"Travis C/I", selected:getBoolean('travisFlag'), enabled:getBoolean('gitFlag'), focusable:false,  toolTipText:'Check this to write a .travis.yml config.file', actionPerformed:{e->chooseTRAVIS(e,choiceMaker)});
       

                        vstrut(10)
                        vglue()
                        separator()
                        vstrut(10)
                        vglue()
                        checkBox(id:"ASCIIDOC", text:"Asciidoctor", selected:getBoolean('asciidocFlag'), enabled:true, focusable:false,  toolTipText:'Check this to  write an asciidoctor-formatted README file', actionPerformed:{e->chooseASCIIDOC(e,choiceMaker)});

                        vstrut(10)
                        vglue()
                        separator()
                        vstrut(10)
                        vglue()
                        checkBox(id:"AUDIT", text:"Joblog", selected:getBoolean('auditFlag'), enabled:true, focusable:false,  toolTipText:'Check this to produce a hardcopy log', actionPerformed:{e->chooseAUDIT(e,choiceMaker)});
                    } // end of vbox
                } // end of panel



                // EAST
                panel(id:"autoRM", constraints: BorderLayout.EAST, 
                	border: compoundBorder([emptyBorder(5), titledBorder('Auto Remove')]) , background:java.awt.Color.LIGHT_GRAY)  
                 {
                    vbox
                    {   
                        label(text:'Automatically') 
                        label(text:'Remove Files') 
                        glue()
                        vstrut(10)
                        vglue()
                        checkBox(id:"arGRADLE", text:"Gradle", selected:getBoolean('arGradleFlag'), enabled:getBoolean('gradleFlag'), focusable:false, 
                        toolTipText:'Check this to over-write existing maven/gradle scripts in the output (folders cannot be removed )', 
                            actionPerformed:{e->chooseGradleAR(e,autoRM)} );
 

                        vstrut(10)
                        vglue()
                        separator()
                        vstrut(10)
                        vglue()
                        checkBox(id:"arGIT", text:"GitHub", selected:getBoolean('arGitFlag'), enabled:getBoolean('gitFlag'), focusable:false,  
                        toolTipText:'Check this to automatically over-write existing github repos', actionPerformed:{e->chooseGitAR(e,autoRM)});

                        vstrut(10)
                        vglue()
                        separator()
                        vstrut(10)
                        vglue()
                        checkBox(id:"arTRAVIS", text:"Travis C/I", selected:getBoolean('arTravisFlag'), enabled:getBoolean('travisFlag'),  			
                        focusable:false, toolTipText:'Check this to over-write existing Travis files', actionPerformed:{e->chooseTravisAR(e,autoRM)});
       
                    } // end of vbox
                } // end of panel


                // CENTER
                panel(constraints: BorderLayout.CENTER,  border: compoundBorder([emptyBorder(5),titledBorder('Output Details Here :')]), background:java.awt.Color.WHITE ) 
                {
                    tableLayout(background:java.awt.Color.WHITE) 
                    {
                	tr {
                    			td { checkBox(id:"CORE", text:"Core Template", selected:getBoolean('coreFlag'),  toolTipText:'Check this to generate a core class from a template', actionPerformed:{e->chooseCORE(e,input)} );}
                    			
                    			
                    			td(colfill:true) 
                    			{
						tfn = textField(id: 'tfn', name:'tfn', columns: 40, text:ch.binding.templatefilename, 
                                		actionPerformed:{ ch.put('templatefilename', tfn.text ); forceWrite = true; }, 
                                		focusable:true,
                                		toolTipText:'Key or paste your input template name here or use CHOOSE button',
                                		enabled: bind(source:CORE, sourceProperty:'selected'),
                                		focusLost:{ ch.put('templatefilename', tfn.text); forceWrite = true; })        
                    			} // end of td
                    			
                    			td { button text: 'Choose', enabled: bind(source:CORE, sourceProperty:'selected'), toolTipText:'Click or SPACEBAR to find a core template', actionPerformed: this.&selectTemplate }
		        } // end of tr

                        tr {
                            td {
                                label ' Core Class Name:'  
                            }
                            td(colspan:2) {
                                pan = textField(id: 'classField', name:'classField', 
                                columns: 20, text:ch.get('classname'), 
                                actionPerformed:{ ch.put('classname', classField.text ); 
					forceWrite = true; 
					swingBuilder.testClassField.text = classField.text.trim()+"Test";
                                	swingBuilder.testClassField.text = classField.text.trim()+"Test";  
				 }, 
                                enabled: bind(source:CORE, sourceProperty:'selected'),
                                toolTipText:'Put a simple class name here like Hammer or Finder without any suffix like .java etc',
                                focusLost:{ ch.put('classname', classField.text); 
                                	forceWrite = true;
                                	swingBuilder.testClassField.text = classField.text.trim()+"Test";  
					ch.put('testclassname', swingBuilder.testClassField.text ); 
                                })
                            } // end of td
                        } // end of tr
                        
                        tr {
                            td(colspan:4) {
                            label ' '
                            } // end of td
			} // end of tr


	                tr {
                    	    td { checkBox(id:"TEXT", text:"Edit Description :", toolTipText:'Check here to enable editor for class documentation',
                    	    focusLost:{  classdesc.requestFocus(); });
                    	    } // end of td

                    	    td(colfill:true)  {
                                cd = textArea(id: 'classdesc', name:'classdesc', columns: 40, 
                                rows:6, 
                                text:ch.getDesc(),
                                lineWrap:true,
                                editable:bind(source:TEXT, sourceProperty:'selected'),
                                focusable:bind(source:TEXT, sourceProperty:'selected'), 
                                border: lineBorder(color:Color.GRAY, thickness:1),
                                toolTipText:'Describe your class here for the javadocs (TAB key wont work)',
                                focusLost:{ ch.putDesc(classdesc.text); forceWrite = true;  })
                            } // end of td
                            
                            td { button text: 'Save', toolTipText:'click to keep your text',  
                            actionPerformed: { ch.putDesc(classdesc.text); forceWrite = true; swingBuilder.TEXT.selected=false;  }, 
                            enabled: bind(source:TEXT, sourceProperty:'selected') 
			    } // end of td
                	} // end of tr

                        tr {
                            td(colspan:4) {
                            label ' ' 
                            } // end of td
			} // end of tr
                
                	tr {
                    			td { checkBox(id:"TEST", text:"Test Template", selected:getBoolean('testFlag'),  
                    				toolTipText:'Check this to generate a test class from template', 	
                    				actionPerformed:{e->chooseTEST(e,input)} ); }
                    			
                    			td(colfill:true)  
                    			{
						ttfn = textField(id: 'ttfn', name:'ttfn', columns: 40, text:ch.binding.testtemplatefilename, 
                                		actionPerformed:{ ch.put('testtemplatefilename', ttfn.text ); forceWrite = true; }, 
                                		toolTipText:'Key or paste your test input template name here or use CHOOSE button right',
                                		enabled: bind(source:TEST, sourceProperty:'selected'),
                                		focusLost:{ ch.put('testtemplatefilename', ttfn.text); forceWrite = true; })        
                    			} // end of td

			                td { button text: 'Choose', toolTipText:'Click or SPACEBAR to find a test template', actionPerformed: this.&selectTestTemplate,
			                	enabled: bind(source:TEST, sourceProperty:'selected') 
			                } // end of td
                	} // end of tr
                
                        tr {
                            td {
                                label ' Test Class Name :'  
                            }
                            td(colspan:2)  {
                                tcf = textField(id: 'testClassField', name:'testClassField', columns: 20, text:ch.get('testclassname'), 
                                actionPerformed:{ ch.put('testclassname', testClassField.text ); forceWrite = true; }, 
                                enabled: bind(source:TEST, sourceProperty:'selected'),
                                toolTipText:'Put a simple name of your test class here like HammerTest without any suffix like .java etc',
                                focusLost:{ ch.put('testclassname', testClassField.text); forceWrite = true;  })
                            }
                        } // end of tr

                        tr {
                            td(colspan:4) {
                            label ' ' 
                            } // end of td
			} // end of tr
			
	                tr {
	                    td {
        	                label ' Return Code :'  // text property is implicit.
                	    }

	                    td(colspan:2)  {
        	                label(id:'label1')  // text property is implicit.
                	    }
                	} // end of tr
                
	                tr {
        	            td {
                	        label ' Reason :'  // text property is implicit.
                    		}	

	                    td(colfill:true)  {
	                        textArea(id:'label2',columns:40,rows:2,lineWrap:true,editable:false,focusable:false)  // text property is implicit.
        	            }
                	} // end of tr
                
            } // end of table
            
        } // end of panel
        
        
        // SOUTH
        panel(id:"buttonMaker", constraints: BorderLayout.SOUTH, border:emptyBorder(1), background:java.awt.Color.LIGHT_GRAY) 
        {
            button text: 'Save',  toolTipText:'Keep these setting for next time', mnemonic: 'S', actionPerformed: 
            {        
                    doLater{ 
                                ch.writeConfig();
                                forceWrite = false;
                                buttonMaker.revalidate()
                                swingBuilder.go.requestFocus()
                                swingBuilder.go.requestFocusInWindow();
                    } // end of doLater
            } // end of actionPer..


            button id:'go', text: 'Go',  toolTipText:'Do a save first then click here to create a project', 
            	enabled:allow, mnemonic: 'G', 
            actionPerformed: 
            {    
                String fn = pathField.text;   //ch.get('pathname');
                println "go--->"+fn
                ChkObj co = new ChkObj();
                boolean flag = co.chkobj(fn);
                swingBuilder.label1.setText(" ");
                swingBuilder.label2.setText(" ");

                if (!flag)
                {
                    display("Bad Path Alert|Your output path '$fn' is not valid - pls choose another")      
                } // end of if

		if (flag)
		{
			if (swingBuilder.projectTitle.text==null || swingBuilder.projectTitle.text.trim().size() < 1)
			{
	                    display("Missing Data Alert|Your project title is missing - pls choose one");
	                    flag = false;      
	                    swingBuilder.projectTitle.requestFocus()
			    swingBuilder.projectTitle.requestFocusInWindow();
			} // end of if
			 		
		} // end of if

                if (running)
                {
                    display "Constructor is already running - please wait";
                }
                else 
                {
                    if (flag)
                    {
                        running = true;
                        doOutside{ 

                        	go.setIcon(warnIcon);
                        	ch.writeConfig();
                                forceWrite = false;
                                
                        	com.jnorthr.Make make = new com.jnorthr.Make(); 

				//------------------------------------------

	                        running = false;
                            	this.returncode = make.getReturnCode();
                            	swingBuilder.label1.setText("completed with return code " + this.returncode);
                            	swingBuilder.label2.setText(make.returnmsg);
                            	go.setIcon(null);
                            	buttonMaker.revalidate()
                            	swingBuilder.quit.requestFocus()
                            	swingBuilder.quit.requestFocusInWindow();
                        	} // end of do..
                    } // end of if flag
                
                } // end of else
            } // end of button GO


            button id:'quit', text: 'Exit', toolTipText:'end this session without saving your settings', mnemonic: 'Q', actionPerformed: 
            {
                if (running)
                {
                    display "Constructor is already running - please wait";
                }
                else
                {
                    if (forceWrite)
                    {
                        ch.writeConfig();
                    } // end of if

                    System.exit(0);
                   } // end of else

            } // end of button
            
    	} // end of SOUTH
        
	//swingBuilder.GRADLE.setAccelerator(KeyStroke.getKeyStroke('G', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
	 
        }  // end of complete frame

        } // end of swingBuilder

    } // end of method
    
    

   /** 
    * Method to find a true/false map key within the configuration handler
    * 
    * @param a string key into the binding
    * @return boolean yes / no - true / false
    */     
    def getBoolean(String key)
    { 	
    	boolean yn = ch.binding[key];
        return yn;
    } // end of method


   /** 
    * Method to decide whether to allow the gradle wrapper checkbox 
    * 
    * @return boolean yes / no - true / false
    */     
    def getWrapper()
    { 	
    	boolean yn = true;  //ch.binding['gradleFlag'];
        return yn;
    } // end of method


   /** 
    * Method to get an example pathname 
    * 
    * @return text of latest full path name
    */     
    def getPathName()
    { 	
        NameGenerator ng = new NameGenerator();
	ng.getPath(true);
	String justbuilt = ng.getProjectBuildPackagePath();
    } // end of method

   /** 
    * Method to get an example pathname 
    * 
    * @return text of latest full path name
    */     
    def getPathName(def pn)
    { 	
        NameGenerator ng = new NameGenerator();
	ng.getPath(true);
	String justbuilt = ng.setPath(pn);
	return justbuilt;
    } // end of method


   /** 
    * Method to decide whether to keep the project title for documentation purposes only
    * 
    * @return none
    */     
    public setProjectTitle(def txt)
    { 	
    	if (txt==null || txt.trim().size() < 1) 
    	{
    		if (ptFlag)
    		{
    			display("A Project title is mandatory");
    			swingBuilder.projectTitle.requestFocus()
    			swingBuilder.projectTitle.requestFocusInWindow();
    			ptFlag  = false;
    		} // end of if
	}
    	else
	{
		ptFlag = true;
		ch.put('projecttitle', txt );
	    	swingBuilder.pathField.requestFocus()
        	swingBuilder.pathField.requestFocusInWindow();
	}
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
        Constructor gui = new Constructor();
        gui.say "--- the end ---"
    } // end of main
} // end of class




// ==================================================================
/* ------------------------------------------------------------------
spare code follows :
	// Binding of textfield's to ch object.
    	//bean ch, 
	        //path:         bind { path.text },
		//sortStrategy:     bind{strategy.value},            
    		//makeINDEX:     bind{INDEX.selected},
        	//makeHTML:      bind{HTML.selected},
		//makeHTMLrule:    bind{makeHTMLrule},
    		//makePDF:       bind{PDF.selected},
        	//makePDFrule:    bind{makePDFrule},
        	//auditFlag:    bind{AUDIT.selected}
        	

   ** 
    * Method to find a map key within the configuration handler
    * 
    * @param a string key into the binding
    * @return void
    *
    def getValue(def na)
    { 	
        say "getValue($na)="+ch.get(na);
        return ch.get(na);
    }

    def getAll(event)
    {
        //swingBuilder.adoc.selected = true;    
        //swingBuilder.asciidoc.selected = true;    
        //swingBuilder.asc.selected = true;    
        //swingBuilder.ad.selected = true;    
        //swingBuilder.txt.selected = true;    
        //swingBuilder.html.selected = true;    
        //swingBuilder.pdf.selected = true;    
        //swingBuilder.md.selected = true;    
        
        swingBuilder.pickMaker.revalidate()
        swingBuilder.pathField.requestFocus()
        swingBuilder.pathField.requestFocusInWindow();
    } // end of methods
    

    def setStrategy(event) { 
        //ch.sortStrategy = swingBuilder.strategy.value 
    }

    
    def setup()
    {
        ['never','always','missing','old','both'].each{
          if (ch.get(it)!=null)              //hasHTMLRule(it))
          { 
            swingBuilder."html${it}".selected = true;
          }
          
          if (ch.get(it)!=null)      //hasPDFRule(it))
          {
            swingBuilder."pdf${it}".selected = true;
          } // end of if
          
        }  // end of each
    } // end of setup
    
    
    // revise ch rules here rather than in updateConfigHandler() method
    def setRules(event) 
    {     
        if (event.source.name.startsWith("html"))
        {
        //ch.setHTMLRule(event.source.name.substring(4))
        } // end of if
  
        if (event.source.name.startsWith("pdf"))
        {
        //ch.setPDFRule(event.source.name.substring(3))
        } // end of if
          
    } // end of setRules


    ==========================================
    def updateConfigHandler()
    {
        ch.path             = swingBuilder.pathField.text
        ch.adoc             = swingBuilder.adoc.selected
        ch.asciidoc         = swingBuilder.asciidoc.selected
        ch.asc              = swingBuilder.asc.selected
        ch.ad               = swingBuilder.ad.selected
        ch.usetxt           = swingBuilder.txt.selected
        ch.md               = swingBuilder.md.selected
        ch.html             = swingBuilder.html.selected
        ch.pdf              = swingBuilder.pdf.selected
        ch.parseSubFolders     = swingBuilder.subFolders.selected
        ch.sortStrategy         = swingBuilder.strategy.value
        ch.makeINDEX         = swingBuilder.INDEX.selected
        ch.makeHTML         = swingBuilder.HTML.selected
        ch.makePDF          = swingBuilder.PDF.selected
        ch.makeAUDIT          = swingBuilder.AUDIT.selected
        //say "updateConfigHandler() complete"
    } // end of updateRunTime()

-----------------

        String starthere = (ch.get('path')==null ) ? ch.get('userhome') : ch.get('path') ;
        fc = new JFileChooser(starthere);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            //FILES_AND_DIRECTORIES);

        int answer = fc.showOpenDialog(swingBuilder.frame)
        switch(answer)
        { 
            case JFileChooser.APPROVE_OPTION:
            File file = fc.getSelectedFile();
            String selectedFile = file.toString();
            ch.putPathName( selectedFile ); 
            swingBuilder.pathField.text = selectedFile;
            swingBuilder.buttonMaker.revalidate()
            swingBuilder.pathField.requestFocus()
            swingBuilder.pathField.requestFocusInWindow();
            forceWrite = true;
            break;

            case JFileChooser.CANCEL_OPTION:
            case JFileChooser.ERROR_OPTION:
            break;
            
        } // end of switch
---      
        
*/


    //SpinnerListModel listModel = new SpinnerListModel(["red","green", "blue"]);
    //String[] days = ["red","green", "blue"];
    //def model1 = new SpinnerListModel(days);
    // http://www.java2s.com/Tutorial/Java/0240__Swing/StringsbasedSpinner.htm