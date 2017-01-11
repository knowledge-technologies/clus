
package test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


public class BaseTestCase {

    // https://www.tutorialspoint.com/junit/junit_test_framework.htm

    protected static String m_DataFolder;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        m_DataFolder = "resources/unitTests"; // folder where test data/settings files are stored
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    // method, which runs before every test invocation.
    @Before
    public void setUp() throws Exception {
    }


    // method, which runs after every test method
    @After
    public void tearDown() throws Exception {
    }
}
