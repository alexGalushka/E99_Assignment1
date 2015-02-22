package edu.harvard.cscie99.clustering.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import edu.harvard.cscie99.clustering.impl.KmeansAlgorithmImpl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class KmeansTest
{

	private TestData testdata;
	private KmeansAlgorithmImpl kmeans;
	private Map<String, BitSet> dataBs;
	private Double[][] dataMatrix;
	private List<String> rowLabels;
	private static Double resultNum4;
	private static Map<String,Integer> resultNum5; 
	private static Map<String,Integer> resultNum6;
	private static boolean resultNum7;
	
    public KmeansTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass()  
    {
    }
    
	@Before
    public void setUp() 
    {	
    	kmeans = new KmeansAlgorithmImpl(true);
    	kmeans.setInitialmethod("initindices");
    	kmeans.setInitialIndices(createInitialInd());
    	kmeans.setK(2);
    	kmeans.setMaxiterations(8);
    	kmeans.setInitialIndicesBs(createInitialIndBs());
    	testdata = new TestData();
    	dataBs = testdata.createSimpleDataBs();
    	dataMatrix = testdata.createSimpleDataMatrix();
    	rowLabels = testdata.getRowLabels();
    }
    
    @After
    public void tearDown() {
    }
		
    private List<Integer> createInitialInd()
    {
    	List<Integer> initialIndices = new ArrayList<>();
    	initialIndices.add(0);
    	initialIndices.add(2);
		return initialIndices;
    }
    
	public List<String> createInitialIndBs()
	{
		String[] elements = {"Alcohol","Spermine"};
		List<String> list = new ArrayList<>(Arrays.asList(elements));
		return list;
	}
    
    private ArrayList<Integer> returnArrayListInts(int n)
    {
    	Integer[] elementsIn;
        if(1==n)
        {
        	Integer[] elements = {1,0,3,4};
        	elementsIn = elements;
        }
        else
        {
        	Integer[] elements = {1,3,3,5};
        	elementsIn = elements;
        }    
        
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(elementsIn)); 
        return list;
    }
    
    private ArrayList<Double> returnArrayListDoubles(int n)
    {
    	Double[] elementsIn;
        if(1==n)
        {
        	Double[] elements = {1d,0d,3d,4d};
        	elementsIn = elements;
        }
        else
        {
        	Double[] elements = {1d,3d,3d,5d};
        	elementsIn = elements;
        }    
        
        ArrayList<Double> list = new ArrayList<>(Arrays.asList(elementsIn)); 
        return list;
    }
    
    @Test
    public void test_euclidianDistance() throws Exception
    {
    	Method privateEuclidianDistance = KmeansAlgorithmImpl.class.getDeclaredMethod("euclidianDistance",
    			                                                                         new Class[] { Double[].class, Double[].class });
    	privateEuclidianDistance.setAccessible(true);
    	
    	resultNum4 = (Double) privateEuclidianDistance.invoke(kmeans, new Object[] { dataMatrix[2], dataMatrix[3] });
    	
    	Double expectedResult = 2.0d;
    	
    	assertEquals(resultNum4, expectedResult);
    }

    
    
    @Test
    public void test_ifCentroidsConvergedShortWide() throws Exception
    {
    	Method privateIfCentroidsConvergedShortWide = KmeansAlgorithmImpl.class.getDeclaredMethod("ifCentroidsConvergedShortWide",
    			                                                                         new Class[] { List.class, List.class });
    	privateIfCentroidsConvergedShortWide.setAccessible(true);
    	
    	resultNum7 = (boolean) privateIfCentroidsConvergedShortWide.invoke(kmeans, new Object[] { returnArrayListInts(1), returnArrayListInts(2) });
    	
    	assertEquals(resultNum7, false);
    	
    	resultNum7 = (boolean) privateIfCentroidsConvergedShortWide.invoke(kmeans, new Object[] { returnArrayListInts(1), returnArrayListInts(1) });
    	
    	assertEquals(resultNum7, true);
    }
    
    
    @Test
    public void test_ifCentroidsConvergedTallNarrow() throws Exception
    {
    	Method privateIfCentroidsConvergedTallNarrow = KmeansAlgorithmImpl.class.getDeclaredMethod("ifCentroidsConvergedTallNarrow",
    			                                                                         new Class[] { List.class, List.class });
    	privateIfCentroidsConvergedTallNarrow.setAccessible(true);
    	
    	resultNum7 = (boolean) privateIfCentroidsConvergedTallNarrow.invoke(kmeans, new Object[] { returnArrayListDoubles(1), returnArrayListDoubles(2) });
    	
    	assertEquals(resultNum7, false);
    	
    	resultNum7 = (boolean) privateIfCentroidsConvergedTallNarrow.invoke(kmeans, new Object[] { returnArrayListDoubles(1), returnArrayListDoubles(1) });
    	
    	assertEquals(resultNum7, true);
    }

    @Test
    public void test_ifConvergedBs() throws Exception
    {
    	Method privateIfConvergedBs = KmeansAlgorithmImpl.class.getDeclaredMethod("ifConvergedBs",
    			                                                                         new Class[] { List.class, List.class });
    	privateIfConvergedBs.setAccessible(true);
    	
    	resultNum7 = (boolean) privateIfConvergedBs.invoke(kmeans, new Object[] { returnArrayListInts(1), returnArrayListInts(2) });
    	
    	assertEquals(resultNum7, false);
    	
    	resultNum7 = (boolean) privateIfConvergedBs.invoke(kmeans, new Object[] { returnArrayListInts(1), returnArrayListInts(1) });
    	
    	assertEquals(resultNum7, true);
    }
   

    @SuppressWarnings("unchecked")
	@Test
    public void test_runAlgorithmBs() throws Exception                             
    {
    	Method privateRunAlgorithmBs = KmeansAlgorithmImpl.class.getDeclaredMethod("runAlgorithmBs", new Class[] { Map.class, boolean.class });
    	privateRunAlgorithmBs.setAccessible(true);
    	
    	resultNum5 = ( Map<String,Integer> ) privateRunAlgorithmBs.invoke(kmeans, new Object[] { dataBs, true });
    	
    	boolean result = false;
    	
    	result = resultNum5.get("Spermine").equals(resultNum5.get("Spermidine"));
    			
    	assertEquals(result, true);
    }

    
    @SuppressWarnings("unchecked")
	@Test
    public void test_runAlgorithmMatrix() throws Exception
    {
    	Method privateRunAlgorithmMatrix = KmeansAlgorithmImpl.class.getDeclaredMethod("runKmeansAlgorithmMatrix",
    			                                                                         new Class[] {List.class, 
    			                                                                         Double[][].class, boolean.class });
    	privateRunAlgorithmMatrix.setAccessible(true);                                    
    	
    	resultNum6 = ( Map<String,Integer>) privateRunAlgorithmMatrix.invoke(kmeans, new Object[] { rowLabels, dataMatrix, true });
    	
    	boolean result = false;
    	
    	result = resultNum6.get("E").equals(resultNum6.get("G"));
    	result = result && resultNum6.get("G").equals(resultNum6.get("F"));
    	
    	assertEquals(result, true);
    }
    

    
}
