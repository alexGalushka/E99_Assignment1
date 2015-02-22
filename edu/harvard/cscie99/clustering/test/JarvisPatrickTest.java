package edu.harvard.cscie99.clustering.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.cscie99.clustering.impl.JarvisPatrickAlgorithmImpl;
import edu.harvard.cscie99.clustering.impl.ValueComparator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class JarvisPatrickTest
{
	private TestData testdata;
	private JarvisPatrickAlgorithmImpl jarvis;
	private Map<String, BitSet> dataBs;
	private Double[][] dataMatrix;
	private List<String> rowLabels;
	private static Map<Integer,Double> resultNum1;
	private static HashSet<Integer> resultNum2;
	private static Map<Integer,Double> resultNum3;
	private static Double resultNum4;
	private static Map<String,Integer> resultNum5; 
	private static Map<String,Integer> resultNum6;
		
    public JarvisPatrickTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass()  
    {
    }
    
    @SuppressWarnings("static-access")
	@Before
    public void setUp() 
    {
    	
    	jarvis = new JarvisPatrickAlgorithmImpl();
    	jarvis.setNumNeighbors(4);
    	jarvis.setCommonNeighbors(2);
    	testdata = new TestData();
    	dataBs = testdata.createSimpleDataBs();
    	dataMatrix = testdata.createSimpleDataMatrix();
    	rowLabels = testdata.getRowLabels();
    }
    
    @After
    public void tearDown() {
    }


    @SuppressWarnings("unchecked")
	@Test
    public void test_getSortedCloseNeighborsMapBs() throws Exception
    {
    	ValueComparator vc = new ValueComparator();
    	BitSet bs = dataBs.get("Alcohol");
    	Method privateGetSortedCloseNeighborsMapBs = JarvisPatrickAlgorithmImpl.class.getDeclaredMethod("getSortedCloseNeighborsMapBs",
    			                                                                         new Class[] { Integer.class,
    			                                                                         ValueComparator.class, BitSet.class, Map.class });
    	privateGetSortedCloseNeighborsMapBs.setAccessible(true);
    	
    	
    	resultNum1 = (Map<Integer, Double>) privateGetSortedCloseNeighborsMapBs.invoke(jarvis, new Object[] { 1, vc, bs, dataBs });
    	
    	Set<Integer> resultSet = resultNum1.keySet();

    	Integer elements[] = { 6, 2, 8, 5, 7, 4, 3, 10, 0, 9 };
		Set<Integer> expectedResultSet = new HashSet<>(Arrays.asList(elements));
    	assertEquals(resultSet, expectedResultSet);
    }
	
    @SuppressWarnings("unchecked")
    @Test
    public void test_getNeighborsSetPerNumOfNeighbors() throws Exception
    {
    	// 	private HashSet<Integer> getNeighborsSetPerNumOfNeighbors(Integer countBreak, Map<Integer,Double> sortedCloseNeighbors)

    	Method privategetNeighborsSetPerNumOfNeighbor = JarvisPatrickAlgorithmImpl.class.getDeclaredMethod("getNeighborsSetPerNumOfNeighbors",
    			                                                                         new Class[] { Integer.class, Map.class });
    	privategetNeighborsSetPerNumOfNeighbor.setAccessible(true);

    	resultNum2 = (HashSet<Integer>) privategetNeighborsSetPerNumOfNeighbor.invoke(jarvis, new Object[] { 4, resultNum1 });
    	
    	Integer elements[] = { 6, 2, 8, 5 };
		Set<Integer> expectedResultSet = new HashSet<>(Arrays.asList(elements));
    	assertEquals(resultNum2, expectedResultSet);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void test_getSortedCloseNeighborsMap() throws Exception
    {
    	ValueComparator vc = new ValueComparator();

    	Method privateGetSortedCloseNeighborsMap = JarvisPatrickAlgorithmImpl.class.getDeclaredMethod("getSortedCloseNeighborsMap",
    			                                                                         new Class[] { Integer.class,
    			                                                                         ValueComparator.class, Double[][].class });
    	privateGetSortedCloseNeighborsMap.setAccessible(true);
    	
    	resultNum3 = (Map<Integer,Double>) privateGetSortedCloseNeighborsMap.invoke(jarvis, new Object[] { 1, vc, dataMatrix });
    	
    	Set<Integer> resultSet = resultNum3.keySet();

    	Integer elements[] = { 0, 3, 2, 5, 4, 6 };
		Set<Integer> expectedResultSet = new HashSet<>(Arrays.asList(elements));
    	assertEquals(resultSet, expectedResultSet);
    }
    
    
    
    @Test
    public void test_euclidianDistance() throws Exception
    {
    	Method privateEuclidianDistance = JarvisPatrickAlgorithmImpl.class.getDeclaredMethod("euclidianDistance",
    			                                                                         new Class[] { Double[].class, Double[].class });
    	privateEuclidianDistance.setAccessible(true);
    	
    	resultNum4 = (Double) privateEuclidianDistance.invoke(jarvis, new Object[] { dataMatrix[0], dataMatrix[1] });
    	
    	Double expectedResult = 4.0d;
    	
    	assertEquals(resultNum4, expectedResult);
    }
    

    @SuppressWarnings("unchecked")
	@Test
    public void test_runAlgorithmBs() throws Exception
    {
    	Method privateRunAlgorithmBs = JarvisPatrickAlgorithmImpl.class.getDeclaredMethod("runAlgorithmBs",
    			                                                                         new Class[] { Map.class });
    	privateRunAlgorithmBs.setAccessible(true);
    	
    	resultNum5 = ( Map<String,Integer>) privateRunAlgorithmBs.invoke(jarvis, new Object[] { dataBs });
    	
    	boolean result = false;
    	result = resultNum5.get("Mannitol").equals(resultNum5.get("Alcohol"));
    	
    	result =  result && resultNum5.get("Alcohol").equals(resultNum5.get("Isopropyl"));		
    			
    	assertEquals(result, true);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void test_runAlgorithmMatrix() throws Exception
    {
    	Method privateRunAlgorithmMatrix = JarvisPatrickAlgorithmImpl.class.getDeclaredMethod("runAlgorithmMatrix",
    			                                                                         new Class[] {List.class, Double[][].class });
    	privateRunAlgorithmMatrix.setAccessible(true);                                    
    	
    	resultNum6 = ( Map<String,Integer>) privateRunAlgorithmMatrix.invoke(jarvis, new Object[] { rowLabels, dataMatrix });
    	
    	boolean result = false;
    	
    	result = resultNum6.get("E").equals(resultNum6.get("G"));
    			
    	assertEquals(result, true);
    }
}
