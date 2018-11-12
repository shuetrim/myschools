package net.galexy.myschools;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the MySchools data extration routine.
 */

class MyschoolsTest extends TestCase {
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MyschoolsTest( String testName ) {
        super( testName );
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MyschoolsTest.class );
    }

    /**
     * Change the file to whatever works on your system.
     */
    public void testApp()
    {
        DataExtractor extractor = new DataExtractor(new File("/home/parallels/myschools.csv"));

        Integer[] domains = {DataExtractor.DOMAIN_NUMERACY};
        DataExtractor.setDomains(domains);

        Integer[] yearGroups = {9};
        DataExtractor.setYearGroups(yearGroups);
        
    	Integer[] years = {2017};
    	DataExtractor.setYears(years);

    	DataExtractor.MIN_SCHOOL_ID = 40000;
    	DataExtractor.MAX_SCHOOL_ID = 40000;
    	
        extractor.run();
    }
    
}
