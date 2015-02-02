package edu.harvard.cscie99.clustering.io;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;


public class ClusteringAlgorithmTest {
	
	/*
	 * 				String[] ss= clusterParams.get("initialIndices").split(","); //passed in in form of 4,5,6,7
				for(int i=0;i<ss.length;i++)
				{
                    
				}
				
				= new ArrayList<Float[]>();//new Float[featuresNum]
	 */

	public static void main(String[] args)
	{
		String FILENAME = "C:/Users/apgalush/Documents/Personal/Harvard/Spring2015/CapStone/HW1CodeData/testdata/iris.txt";

		MatrixReader instance = new MatrixReader();
	    try 
	    {
	        instance.loadData(FILENAME);
	    } 
	    catch(IOException ioe)
	    {
	        System.out.println("IOError : " + ioe.getMessage());
	        fail("Could not load " + FILENAME);
	    }
	    
	    ClusteringResult result;
	    KmeansAlgorithm algorithm = new KmeansAlgorithm();
	    
	    result = algorithm.cluster(rowLabels, data, clusterParams);
	    
	}
    
}
