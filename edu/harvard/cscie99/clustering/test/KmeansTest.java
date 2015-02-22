package edu.harvard.cscie99.clustering.test;

import java.lang.reflect.Method;

import edu.harvard.cscie99.clustering.impl.KmeansAlgorithmImpl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class KmeansTest
{

	private static KmeansAlgorithmImpl kmeans;
	
    public KmeansTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {	 
    }
    
    @AfterClass
    public static void tearDownClass() 
    {
    }
    
    @Before
    public void setUp()
    {
    	kmeans = new KmeansAlgorithmImpl(true);
    }
    
    @After
    public void tearDown() 
    {
    }
	
	
	
    @Test
    public void test_createDataToClusterMap() throws Exception
    {
    	
    	Method privateStringMethod = KmeansAlgorithmImpl.class.getDeclaredMethod("say", new Class[] { String.class, String.class });
    	privateStringMethod.setAccessible(true);
    	
    	privateStringMethod.invoke(kmeans, new Object[] { "Hello","World" });
    	
    }

}
