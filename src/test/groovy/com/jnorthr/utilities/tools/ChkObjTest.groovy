package com.jnorthr.utilities.tools;

import spock.lang.*
import org.springframework.boot.test.system.OutputCaptureRule

class ChkObj extends spock.lang.Specification  
{
	def home = System.getProperty("user.home");
	boolean tf = false;
	ChkObj test;
	
	@org.junit.Rule
	OutputCaptureRule capture = new OutputCaptureRule()

	// run before the first feature method
	def setupSpec() 
	{
	} // end of setupSpec()     

	// run before every feature method
	def setup() 
	{
	}          

	// run after every feature method
	def cleanup() 
	{

	}        

	// run after the last feature method	
	def cleanupSpec() 
	{
		println "end of testing for ChkObj"
	}   
 
/*
can't get gradle to 'see' this class on test classpath !
  	def "Build default ChkObj"() {
  		when:     'default ChkObj has been built'
	    String home = System.getProperty("user.home");
		test = new ChkObj();

		then:     test != null;
				  test.chooserpath==home
  	} // end of feature method
*/
  	 
} // end of class

/*
when:
stack.pop()

then:
def e = thrown(EmptyStackException)
e.cause == null
*/