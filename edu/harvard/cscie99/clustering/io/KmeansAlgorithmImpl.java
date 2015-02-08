package edu.harvard.cscie99.clustering.io;


import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
//import org.apache.mahout.common.distance;

public class KmeansAlgorithmImpl implements ClusteringAlgorithm
{
	// Algorithm summary:
	
	// 1. Start with k seeds as cluster centroids (Normally taken as k data points)
	
	// 2. Calculate distance (Euclidean) of all points from cluster centers. Assign each data point to closest cluster
	
    // 3. Re-calculate cluster centroids from all data points in the cluster (averages in each cluster)
	
	// 4. Repeat from 2
	

	@SuppressWarnings("unchecked")
	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String,Object> clusterParams) 
	{
		Integer maxiterations = 8;
		Integer k = 7;
		@SuppressWarnings("unused")
		String distanceMetric = "Euclidian";
		String InitialMethod = ""; //{random, initialIndices, initialCentroids};"
		List<Double[]> initialCentroids = new ArrayList<Double[]>();
		List<Double[]> previousInitialCentroids = new ArrayList<Double[]>();
		List<Integer> initialIndices;

		// switch in between 2 methods for convergence
		boolean ifDataTallAndNarrow = true;
		
		if (clusterParams.containsKey("maxiterations"))
		{
			maxiterations = (Integer) clusterParams.get("maxiterations");
		}
		
		if (clusterParams.containsKey("k"))
		{
			k = (Integer) clusterParams.get("k");
		}
		
		//get initial centroids
		boolean skipRandom = true;
		if (clusterParams.containsKey("initialMethod"))
		{
			InitialMethod = (String) clusterParams.get("initialMethod");
			if (InitialMethod.equals("initialIndices"))
			{
				if ( clusterParams.containsKey("initIndices") )
				{
				initialIndices = (List<Integer>) clusterParams.get("initIndices");
				k = initialIndices.size();
				initialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
				skipRandom = false;
				}
			}
			else if (InitialMethod.equals("initialCentroids"))
			{
				if ( clusterParams.containsKey("initialCentroids") )
				{
					initialCentroids = (ArrayList<Double[]>) clusterParams.get("initialCentroids");
					k = initialCentroids.size();
					skipRandom = false;
				}
			}
			else
			{
				// randomly choose k indices from the matrix
				initialIndices = getListOfRandomIndicies (0, data.length, k);
				initialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
				skipRandom = false;
			}
			
		}
		if (skipRandom)
		{
			// over kill, but a fool proof, in case initialMethod was missed completely
			initialIndices = getListOfRandomIndicies (0, data.length, k);
			initialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
		}
		
		// calculate Euclidean distances to all data points for each centroid
		// each centroid represents the center of the cluster
		
		List<Integer> dataToclusterList = new ArrayList<Integer>();
		List<Integer> previousDataToclusterList = new ArrayList<Integer>();
		
		List<List<Integer>> clusterToDataList = new ArrayList<List<Integer>>();
		
		for (int i = 0; i<data.length; i++)
		{
			dataToclusterList.add(0);
			dataToclusterList.set(i, null);
			previousDataToclusterList.add(0);
		}
		
		for (int i = 0; i<k; i++ )
		{
			clusterToDataList.add(null);
			clusterToDataList.set(i, null);
		}
		
		boolean converged = false;
		Integer iterCount = 0;
		while ( maxiterations!=0 && converged == false )
		{

			Integer clusterNum;
			Double closestD;
			List<Double> tempArrayList;
			List<Integer> listOfInts;
			for (Integer i = 0; i < data.length; i++) 
			{
				tempArrayList = new ArrayList<Double>();
				listOfInts = new ArrayList<Integer>();
				
				for (Double[] centr : initialCentroids)
				{
					tempArrayList.add(euclidianDistance(centr, data[i]));
				}
				
				// determine what centroid is closest to the data current point
				
				closestD = Collections.min(tempArrayList);
				
				clusterNum = tempArrayList.indexOf(closestD);
				
				if (clusterToDataList.get(clusterNum) != null)
				{
					listOfInts = clusterToDataList.get(clusterNum);
				}
				listOfInts.add(i);
				clusterToDataList.set(clusterNum, listOfInts);

				dataToclusterList.set(i, clusterNum);
				
			}
			
			// get mean value of all points belong a specific cluster to update initial centroid
			// if one of the centroid appears to be null after calculations,
			// cluster is represented by just a single point (leave the initial value for this centroid)
			
			Double[] tempMeanCentr;
			Integer checkNullEntry = 0;
			for (List<Integer> clusterPoints : clusterToDataList)
			{
				if ( !clusterPoints.isEmpty() )
				{
					tempMeanCentr = getAverageCentroid(getListOfDataPointsPerIndices (data, clusterPoints));
					initialCentroids.set(checkNullEntry, tempMeanCentr);
				}
				checkNullEntry++;
			}
			
			// check if converged
			if (iterCount!=0)
			{
				if (ifDataTallAndNarrow)
				{
					Double convDelta = 0.09;
					converged = ifCentroidsConvergedTallNarrow( previousInitialCentroids, initialCentroids, convDelta);
				}
				else
				{
					converged = ifCentroidsConvergedShortWide( previousDataToclusterList, dataToclusterList );
				}
			}
	         
			previousInitialCentroids = initialCentroids;
			
			iterCount ++;
			maxiterations--;
		}	
		
		Map<String,Integer> dataToclusterMap = new HashMap<String,Integer>();
		
		int i = 0;
		for (String rowL : rowLabels)
		{
			
			dataToclusterMap.put(rowL, dataToclusterList.get(i));
			i++; 
		}
			
		ClusteringResult result = new ClusteringResult( dataToclusterMap, (String) clusterParams.get("outpath") );
		return result;
		
	}
	
	
	private static boolean ifCentroidsConvergedShortWide( List<Integer> previousdataToclusterList, List<Integer> dataToclusterList )
	{
		boolean result = true;
		// compare current map of data to cluster with previous one
		for (int i = 0; i<previousdataToclusterList.size(); i++)
		{
			
			if ( !previousdataToclusterList.get(i).equals(dataToclusterList.get(i)) )
			{
				result = false;
				return result;
			}
		}
		return result;
	}
	
	private static boolean ifCentroidsConvergedTallNarrow( List<Double[]> previousinitialCentroids, List<Double[]> initialCentroids, Double delta )
	{
		boolean result = true;
		
		for (int i = 0; i<previousinitialCentroids.size(); i++)
		{
			Double[] prevCentr = previousinitialCentroids.get(i);
			Double[] centr = initialCentroids.get(i);
			
			Double prevEntry;
			Double currEntry;
			Double resultDelta;
			for (int j = 0; j<prevCentr.length; j++)
			{
				prevEntry = prevCentr[j];
				currEntry = centr[j];
				 
				if (prevEntry>currEntry)
				{
					resultDelta = (prevEntry-currEntry)/prevEntry;
				}
				else
				{
					resultDelta = (currEntry-prevEntry)/currEntry;
				}
			
				if (resultDelta <= delta)	
				{
					result = false;
					return result;
				}	
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
		List<Double[]> initialCentroids = new ArrayList<Double[]>();;
		
		Double[] tempArr;
		for (Integer ini : initialIndices )
		{
			tempArr = data[ini];
			initialCentroids.add(tempArr);
		}
		
		return initialCentroids;
		
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
	
	  public ClusteringResult cluster(Map<String, BitSet> data, Map<String,Object> clusterParams)
	  {
		  Integer maxiterations = 8;
			Integer k = 7;
			@SuppressWarnings("unused")
			String distanceMetric = "Euclidian";
			String InitialMethod = ""; //{random, initialIndices, initialCentroids};"
			List<BitSet> initialCentroids = new ArrayList<BitSet>();
			List<BitSet> previousInitialCentroids = new ArrayList<BitSet>();
			List<String> initialIndices;

			// switch in between 2 methods for convergence
			boolean ifDataTallAndNarrow = true;
			
			if (clusterParams.containsKey("maxiterations"))
			{
				maxiterations = (Integer) clusterParams.get("maxiterations");
			}
			
			if (clusterParams.containsKey("k"))
			{
				k = (Integer) clusterParams.get("k");
			}
			
			//get initial centroids
			boolean skipRandom = true;
			if (clusterParams.containsKey("initialMethod"))
			{
				InitialMethod = (String) clusterParams.get("initialMethod");
				if (InitialMethod.equals("initialIndices"))
				{
					if ( clusterParams.containsKey("initIndices") )
					{
					initialIndices = (List<String>) clusterParams.get("initIndices");
					k = initialIndices.size();
					initialCentroids = getListOfDataPointsPerIndices2(data, initialIndices);	
					skipRandom = false;
					}
				}
				else if (InitialMethod.equals("initialCentroids"))
				{
					if ( clusterParams.containsKey("initialCentroids") )
					{
						List<ArrayList<Integer>> listOCentroids;
						listOCentroids = (ArrayList<ArrayList<Integer>>) clusterParams.get("initialCentroids");
						BitSet bitSet;
						for (ArrayList<Integer> centr : listOCentroids)
						{
							for (Integer value : centr)
							{
								bitSet = new BitSet();
								bitSet.set(value);
							}
							initialCentroids.add(bitSet);
						}
						k = listOCentroids.size();
						skipRandom = false;
					}
				}
				else
				{
					initialCentroids = getRandomCentroids (data);
					skipRandom = false;
				}
				
			}
			if (skipRandom)
			{
				// over kill, but a fool proof, in case initialMethod was missed completely
				initialCentroids = getRandomCentroids (data);
			}
			
	
			// calculate Euclidean distances to all data points for each centroid
			// each centroid represents the center of the cluster
			
			List<BitSet> dataToclusterList = new ArrayList<BitSet>();
			List<BitSet> previousDataToclusterList = new ArrayList<BitSet>();
			
			List<List<BitSet>> clusterToDataList = new ArrayList<List<BitSet>>();
			
			for (int i = 0; i<data.size(); i++)
			{
				dataToclusterList.add(i, null);
				dataToclusterList.set(i, null);
				previousDataToclusterList.add(i, null);
			}
			
			for (int i = 0; i<k; i++ )
			{
				clusterToDataList.add(null);
				clusterToDataList.set(i, null);
			}
			
			boolean converged = false;
			Integer iterCount = 0;
			while ( maxiterations!=0 && converged == false )
			{
				@SuppressWarnings("unchecked")
				List<String> allLables =  (List<String>) data.keySet();
				BitSet dataBs;
				BitSet dataBsPreserved;
				Integer distance;
				Integer closestD;
				Integer clusterNum;
				List<BitSet> listOfBitSet;
				List<Integer> listOfDistancePoints = new ArrayList<Integer>();
	
				for (String label : allLables)
				{
					dataBs = data.get(label);
					listOfBitSet = new ArrayList<BitSet>();
					for (BitSet clusterBs : initialCentroids)
					{
						dataBsPreserved = (BitSet) dataBs.clone();
						dataBsPreserved.xor(clusterBs);
						distance = dataBsPreserved.cardinality();
						listOfDistancePoints.add(distance);
					}
					
					// determine what centroid is the closest to current data point
					closestD = Collections.min(listOfDistancePoints);
					clusterNum = listOfDistancePoints.indexOf(closestD);
					
					if (clusterToDataList.get(clusterNum) != null)
					{
						listOfBitSet = clusterToDataList.get(clusterNum);
					}
					listOfBitSet.add(data.get(label));
					
					clusterToDataList.set(clusterNum, listOfBitSet);
				}
				
				// recalculate initial centroids
							BitSet oneBigCluster;
				List<BitSet> listOfAllBitsInCluster = new ArrayList<BitSet>();
				List<List<Integer>> listOfAllIntsInCluster = new ArrayList<List<Integer>>();
				List<LinkedHashMap<Integer,Integer>> listOfValueToCountMaps = new ArrayList<LinkedHashMap<Integer,Integer>>();
				
				LinkedHashMap<Integer,Integer> allFeaturesCountMap;
				List<LinkedHashMap<Integer,Integer>> listOfAllFeaturesCountMaps = new ArrayList<LinkedHashMap<Integer,Integer>>();
				
				for (List<BitSet> clusterBsList : clusterToDataList)
				{
					oneBigCluster = new BitSet();
					allFeaturesCountMap = new LinkedHashMap<Integer,Integer>();
					for (BitSet dataPointsBs : clusterBsList)
					{
						oneBigCluster.or(dataPointsBs);
					}
					List<Integer> intList= new ArrayList<>();
			        for (Integer i = oneBigCluster.nextSetBit(0); i >= 0; i = oneBigCluster.nextSetBit(i + 1)) 
			        {
			            intList.add(i);
			            allFeaturesCountMap.put(i, 0);
			        }
			        listOfAllBitsInCluster.add(oneBigCluster);
			        listOfAllFeaturesCountMaps.add(allFeaturesCountMap);
				}
	
	
	            Integer countOuter = 0;
	            List<LinkedHashMap<Integer,Integer>> listOfMapsForNewCenroids = new ArrayList<LinkedHashMap<Integer,Integer>>();
				for (LinkedHashMap<Integer,Integer> map : listOfAllFeaturesCountMaps)
				{
					LinkedHashMap<Integer,Integer> newMap = map;
					List<BitSet> bsInBuckets = clusterToDataList.get(countOuter);
					Integer countOfBsInBuckets = bsInBuckets.size();
					for (Map.Entry<Integer, Integer> entry : map.entrySet())
					{
						Integer key = entry.getKey();
						Integer value = entry.getValue();
						
						for (BitSet bs : bsInBuckets)
						{
							if(bs.get(key))
							{
								value = value + 1;
								newMap.put(key, value);
							}
						}	
					}
	
					initialCentroids = new ArrayList<BitSet>();
					BitSet newCentroid = new BitSet();
					for (Map.Entry<Integer, Integer> entry : newMap.entrySet())
					{
						Integer key = entry.getKey();
						Integer value = entry.getValue();
						value = value/countOfBsInBuckets;
						if (value < 0.5)
						{
							value = 0;
						}
						else
						{
							value = 1;
							newCentroid.set(key);
						}
						newMap.put(key, value);
					}
					listOfMapsForNewCenroids.add((LinkedHashMap<Integer, Integer>) newMap);
					initialCentroids.add(newCentroid);
					countOuter++;
				}
				
				converged = ifCentroidsConverged(initialCentroids,initialCentroids);
			}
			
			
			
	  }
	  
	  
	  private static boolean ifCentroidsConverged( List<BitSet> previousInitialCentroids, List<BitSet> initialCentroids )
	  {
		  boolean result = true;
		  Integer count = 0;
		  for (BitSet bs : previousInitialCentroids) 
		  {
			  if(!initialCentroids.get(count).equals(bs))
			  {
				  result = false;
			  }
			  count ++;
		  }
		  return result;
	  }
	  
	  	private static List<BitSet> getRandomCentroids(Map<String, BitSet> data)
	  	{
	  		List<BitSet> initialCentroids;
			List<String> allLables =  (List<String>) data.keySet();
			List<Integer> randomIndicies = getListOfRandomIndicies (0, data.size(), k);
			for (Integer ri : randomIndicies)
			{
				initialCentroids.add(data.get(allLables.get(ri)));
			}
	  	}
		private static List<BitSet> getListOfDataPointsPerIndices2(Map<String, BitSet> data, List<String> initialIndices)
		{
			List<BitSet> initialCentroids = new ArrayList<BitSet>();;
			for (String ini : initialIndices )
			{
				initialCentroids.add(data.get(ini));
			}
			return initialCentroids;
		}
}
