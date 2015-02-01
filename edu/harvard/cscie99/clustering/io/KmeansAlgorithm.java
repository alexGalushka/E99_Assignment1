package edu.harvard.cscie99.clustering.io;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
//import org.apache.mahout.common.distance;

public class KmeansAlgorithm
{
	// 1. Start with k seeds as cluster centroids (Normally taken as k data points)
	
	// 2. Calculate distance (Euclidean) of all points from cluster centers. Assign each data point to closest cluster
	
    // 3. Re-calculate cluster centroids from all data points in the cluster (averages in each cluster)
	
	// 4. Repeat from 2
	

	@SuppressWarnings("unchecked")
	ClusteringResult cluster(Double[][] data, Map<String,Object> clusterParams) 
	{
		Integer featuresNum = data[0].length;
		Integer maxiterations = 8;
		Integer k = 7;
		@SuppressWarnings("unused")
		String distanceMetric = "Euclidian";
		String InitialMethod = ""; //{random, initialIndices, initialCentroids};"
		List<Double[]> InitialCentroids;
		List<Integer> initialIndices;

		if (clusterParams.containsKey("maxiterations"))
		{
			maxiterations = (Integer) clusterParams.get("maxiterations");
		}
		
		if (clusterParams.containsKey("k"))
		{
			k = (Integer) clusterParams.get("k");
		}
		
		//get initial centroids
		if (clusterParams.containsKey("InitialMethod"))
		{
			InitialMethod = (String) clusterParams.get("InitialMethod");
			if (InitialMethod.equals("initialIndices"))
			{
				initialIndices = (List<Integer>) clusterParams.get("initialIndices");
				k = initialIndices.size();
				InitialCentroids = getListOfIniCentroids(data, initialIndices);	
			}
			else if (InitialMethod.equals("initialCentroids"))
			{
				InitialCentroids = (List<Double[]>) clusterParams.get("InitialCentroids");
				k = InitialCentroids.size();
			}
			else
			{
				// randomly choose k indices from the matrix
				initialIndices = getListOfRandomIndicies (0, data.length, k);
				InitialCentroids = getListOfIniCentroids(data, initialIndices);	
			}
			
		}
		
		//calculate Euclidean distances to all data points for each centroid
		
		
		
		return null;
		
	}
	
	
	List<Double[]> getListOfIniCentroids(Double[][] data, List<Integer> initialIndices)
	{
		List<Double[]> InitialCentroids = new LinkedList<Double[]>();;
		
		Double[] tempArr;
		for (Integer ini : initialIndices )
		{
			tempArr = data[ini];
			InitialCentroids.add(tempArr);
		}
		
		return InitialCentroids;
		
	}
	
	/**
	 * Create a list of random indices of the data set
	 * @param min
	 * @param max
	 * @param k
	 * @return result: list of indices
	 */
	private static List<Integer> getListOfRandomIndicies(Integer min, Integer max, Integer k)
	{
		List<Integer> result = new LinkedList<Integer>();
		max = max - 1;
		Integer randomNum;
		for (int i = 0; i < k; i++)
		{
			Random rn = new Random();
			int n = max - min + 1;
			int r = rn.nextInt(n);
			randomNum =  min + r;
			if (!result.contains(randomNum))
			{
				result.add(randomNum);
			}
		}
		return result;
	}
	  
	  
	ClusteringResult cluster(Map<String, List<Integer>> data, Map<String,Object> clusterParams)
	{
		return null;
	}
	
	  /**
	   * Calculates the Euclidean distance between two points represented by  vector.
	   * @param point1  first point.
	   * @param point2  second point.
	   * @return Euclidean distance.
	   */
	  private static Double euclidianDistance(Double[] point1, Double[] point2)
	  {
	      Double sum = (Double) 0.0;
	      for (int i = 0; i < point1.length; i++)
	      {
	        sum = sum + (point2[i] - point1[i])*(point2[i] - point1[i]);
	      }
	      return (Double) Math.sqrt(sum);

	  }
	
	
}
