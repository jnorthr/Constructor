package com.jnorthr.utilities;

import java.awt.*
import javax.swing.*;

import javax.swing.JOptionPane;
import groovy.transform.ToString;

/** 
 * Asker class description
 *
 * Groovy class to use JOptionPane and Dialogs to get user guidance. 
 * 
 */  
 @ToString
 public class Ask
 {

    class Answer{
        int returncode = 0;
        String note="";
    }


   /** 
    * Method to get user response.
    * 
    * @param the title of the dialog
    * @param the message text of the dialog
    * @param the closure logic to be executed on YES
    * @return Answer class where returncode = 1 if user chose YES
    */     
    def Answer ask(String title, String msg, Closure logic)
    {
        def a = new Answer(returncode: 1)
            int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_CANCEL_OPTION);
            if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
            { 
                //System.exit(0);
                a.returncode = -1;
            } // end of if

            if(result == JOptionPane.YES_OPTION)
            { 
                    a.returncode = 0;
                    logic();
                //copyContent(travisFileName, "travisfilename");
            } // end of if
        return a;
    } // end of ask



   /** 
    * Method to get user response.
    * 
    * @param the title of the dialog
    * @param the message text of the dialog
    * @return Answer class where returncode = 1 if user chose YES
    */     
    def Answer ask(String title, String msg)
    {
        return ask(title, msg, {});
    } // end of ask


   /** 
    * Method to get user response.
    * 
    * @param the message text of the dialog
    * @return Answer class where returncode = 1 if user chose YES
    */     
    public Answer ask(String msg)
    {
        return ask("Question", msg, {});
    } // end of ask


   /** 
    * Method to get user response.
    * 
    * @param the message text of the dialog
    * @return int where returncode = 1 if user chose YES
    */     
    public int ask(String msg, Closure logic)
    {
        Answer a =  ask("Question", msg, logic);
        return a.returncode;
    } // end of ask




   /** 
    * Method to tell user of a condition where response not rquired.
    * 
    * @param the title of the dialog
    * @param the message text of the dialog
    * @return void
    */     
    public tell(String msg)
    {
        tell("Error Condition", msg);
    } // end of tell


   /** 
    * Method to tell user of a condition where response not rquired.
    * 
    * @param the title of the dialog
    * @param the message text of the dialog
    * @return void
    */     
    public tell(String title, String msg)
    {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
    } // end of tell


    /*  see: http://docs.oracle.com/javase/7/docs/api/javax/swing/JOptionPane.html
        messageType
        Defines the style of the message. The Look and Feel manager may lay out the dialog differently 
        depending on this value, and will often provide a default icon. The possible values are:
            JOptionPane.ERROR_MESSAGE
            JOptionPane.INFORMATION_MESSAGE
            JOptionPane.WARNING_MESSAGE
            JOptionPane.QUESTION_MESSAGE
            JOptionPane.PLAIN_MESSAGE    
    */
    
   /** 
    * Method to tell user of a condition where response not rquired.
    * 
    * @param the title of the dialog
    * @param the message text of the dialog
    * @return void
    */     
    public tell(String title, String msg, def type)
    {
        JOptionPane.showMessageDialog(null, msg, title, type);
    } // end of tell

   /** 
    * Method to tell user of a condition where response not rquired.
    * 
    * @param the title of the dialog
    * @param the message text of the dialog
    * @return void
    */     
    public int show(String title, String msg)
    {
        Object[] options = [ "OK", "CANCEL" ];
        int i = JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if ( i  == JOptionPane.OK_OPTION ) return 0;     
        return i;
    } // end of show


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

        Ask n = new Ask();
        Answer a = n.ask("Overwrite Existing File ?", ".Travis.yml already exists - \nreplace it with this new version ?"){x-> println x; };    
        println "ask said "+a.returncode;
        
        a = n.ask("Title", "Hi fred")
        println "ask said "+a.returncode;
        
        a = n.ask("Hi school")
        println "ask said "+a.returncode;

        // Closure sample follows
        //def i = n.ask("Hello World"){ copyContent(travisFileName, "travisfilename");}
        //println "called and returned "+i;
        
        n.tell("Who da man ?");

        n.tell("BMOC", "Who da man ?");

        n.tell("Harry Pitts", "Who da man ?", JOptionPane.PLAIN_MESSAGE);
        
        def i = n.show("Favorite Film ?", "It was Star Wars");
        println "i=${i}"
        
        println "--- the end ---"
    } // end of main

} // end of class
