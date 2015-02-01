package edu.harvard.cscie99.clustering.io;


import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
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
	ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String,Object> clusterParams) 
	{
		Integer featuresNum = data[0].length;
		Integer maxiterations = 8;
		Integer k = 7;
		@SuppressWarnings("unused")
		String distanceMetric = "Euclidian";
		String InitialMethod = ""; //{random, initialIndices, initialCentroids};"
		List<Double[]> InitialCentroids = new ArrayList<Double[]>();
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
				InitialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
			}
			else if (InitialMethod.equals("initialCentroids"))
			{
				InitialCentroids = (ArrayList<Double[]>) clusterParams.get("InitialCentroids");
				k = InitialCentroids.size();
			}
			else
			{
				// randomly choose k indices from the matrix
				initialIndices = getListOfRandomIndicies (0, data.length, k);
				InitialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
			}
			
		}
		
		initialIndices = getListOfRandomIndicies (0, data.length, k);
		InitialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
		
		// calculate Euclidean distances to all data points for each centroid
		// each centroid represents the center of the cluster
		
		List<Integer> dataToclusterMap = new ArrayList<Integer>();
		List<Integer> previousDataToclusterMap = new ArrayList<Integer>();
		List<List<Integer>> clusterToDataList = new ArrayList<List<Integer>>();
		List<Integer> listOfInts = new ArrayList<Integer>();
		for (int i = 0; i<data.length; i++)
		{
			dataToclusterMap.set(i, null);
			previousDataToclusterMap.set(i, null);
		}
		
		for (int i = 0; i<k; i++ )
		{
			clusterToDataList.set(i, null);
		}
		
		boolean converged = false;
		while ( maxiterations!=0 && converged == false )
		{
			maxiterations--;
			List<Double> tempArrayList = new ArrayList<Double>();
			
			Integer clusterNum;
			Double closestD;
			
			for (Integer i = 0; i < data.length; i++) 
			{
				//String inputLabel = "inputLabel" + i.toString();
				
				for (Double[] centr : InitialCentroids)
				{
					tempArrayList.add(euclidianDistance(centr, data[i]));
				}
				// determine what centroid is closest to the data current point
				
				closestD = Collections.min(tempArrayList);
				
				clusterNum = tempArrayList.indexOf(closestD);
				listOfInts = clusterToDataList.get(clusterNum);
				listOfInts.add(i);
				clusterToDataList.set(clusterNum, listOfInts);
				
				dataToclusterMap.set(i, clusterNum);
				
			}
			// check if converged
			converged = ifCentroidsConverged( previousDataToclusterMap, dataToclusterMap );
			
			previousDataToclusterMap = dataToclusterMap;
			
			// get mean value of all points belong a specific cluster to update initial centroid
			// if one of the centroid appears null after calculations, cluster is a single point (leave the initial value for this centroid)
			Double[] tempMeanCentr;
			
			Integer checkNullEntry = 0;
			for (List<Integer> clusterPoints : clusterToDataList)
			{
				checkNullEntry++;
				
				if ( !clusterPoints.isEmpty() )
				{
					tempMeanCentr = getAverageCentroid(getListOfDataPointsPerIndices (data, clusterPoints));
					InitialCentroids.set(checkNullEntry, tempMeanCentr);
				}
			}
	
		}	
		
		return null;
		
	}
	
	
	private static boolean ifCentroidsConverged( List<Integer> previousDataToclusterMap, List<Integer> dataToclusterMap )
	{
		boolean result = true;
		// compare current map of data to cluster with previous one
		for (int i = 0; i<previousDataToclusterMap.size(); i++)
		{
			
			if ( !previousDataToclusterMap.get(i).equals(dataToclusterMap.get(i)) )
			{
				result = false;
			}
		}
		return result;
	}
	
	private static Double[] getAverageCentroid (List<Double[]> ListOfClusterDataPoints)
	{
		Integer featNum = ListOfClusterDataPoints.get(0).length;
		Double[] sumCentrVal = new Double[featNum];
		
		for (int m = 0; m<sumCentrVal.length; m++)
		{
			sumCentrVal[m] = (double) 0;
		}
		
		for (Double[] centroid : ListOfClusterDataPoints)
		{
			for (int i = 0; i<centroid.length; i++)
			{
				sumCentrVal[i] = sumCentrVal[i] + centroid[i];
			}
		}
		
		for (int m = 0; m<sumCentrVal.length; m++)
		{
			sumCentrVal[m] = sumCentrVal[m]/ListOfClusterDataPoints.size();
		}
		
		return sumCentrVal;
	}
	
	
	private static List<Double[]> getListOfDataPointsPerIndices(Double[][] data, List<Integer> initialIndices)
	{
		List<Double[]> InitialCentroids = new ArrayList<Double[]>();;
		
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
		List<Integer> result = new ArrayList<Integer>();
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
	
		ClusteringResult cluster(Map<String, List<Integer>> data, Map<String,Object> clusterParams)
		{
			return null;
		}
}
