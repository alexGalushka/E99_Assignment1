package edu.harvard.cscie99.clustering.io;


import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The Class KmeansAlgorithmImpl. It implements ClusteringMehod interface.
    Algorithm summary:
	1. Start with k seeds as cluster centroids (Normally taken as k data points)
	2. Calculate distance (Euclidean) of all points from cluster centers. Assign each data point to closest cluster
    3. Re-calculate cluster centroids from all data points in the cluster (averages in each cluster)
	4. Repeat from 2
 */
public class KmeansAlgorithmImpl implements ClusteringMethod
{

	private Integer maxiterations;           // maximum algoritm iterations
	private Integer k;                       // number of clusters
	private String initialmethod = "";       //{random, initialindices, initialcentroids};"
	private List<Double[]> initialCentroids; // centroids to start with
	private List<Integer> initialIndices;    // Indices to start with
	private boolean ifDataTallAndNarrow;     // flag to use convergence method: Tall and Narrow vs. Short and Wide

	private List<BitSet> initialCentroidsBs = new ArrayList<>();
	private List<BitSet> previousinitialcentroidsBs = new ArrayList<>();
	private List<String> initialIndicesBs;
	
	private final static Double omega = 0.22;     // convergence theshhold, percent of movement between old and new version of 
    											  // distances of the old and new cetnroids
	
	public KmeansAlgorithmImpl(boolean tallAndNarrow)
	{
		initialCentroids = new ArrayList<>();
		initialIndicesBs = new ArrayList<>();
		this.ifDataTallAndNarrow = tallAndNarrow;
	}
    /**
	 * Determines clusters, groups data points per each cluster.
	 * @param rowLabels list of row labels
	 * @param data input data, represented by Double[][]
	 * @param clusterParams algorithm parameters
	 * @return ClusteringResult object
	 */
	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String,Object> clusterParams) 
	{
		// Note: each centroid represents the center of the cluster
        // previous centroids for convergence comparison
		
		initializeAlgorithmParametersFirstInterface(data, clusterParams);
		
		List<Double[]> previousInitialCentroids = new ArrayList<>();
		List<Integer> dataToclusterList = new ArrayList<>();
		List<Integer> previousDataToclusterList = new ArrayList<>();
		List<List<Integer>> clusterToDataList = new ArrayList<>();
		List<Double> currAndPrevCentrDistancesNew = new ArrayList<>();
		List<Double> currAndPrevCentrDistancesOld = new ArrayList<>();
		// initialize and prefill the arrays with initial values
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
		// main algorithm loop
		while ( maxiterations!=iterCount && converged == false )
		{
			Integer clusterNum;
			Double closestD;
			List<Double> fromEachCentroidToPointArray;
			List<Integer> listOfInts;
			// calculate Euclidean distances to all data points for each centroid
			for (Integer i = 0; i < data.length; i++) 
			{
				fromEachCentroidToPointArray = new ArrayList<>();
				listOfInts = new ArrayList<>();
				
				for (Double[] centr : initialCentroids)
				{
					fromEachCentroidToPointArray.add(euclidianDistance(centr, data[i]));
				}
				// determine what centroid is closest to the data current point
				closestD = Collections.min(fromEachCentroidToPointArray);
				clusterNum = fromEachCentroidToPointArray.indexOf(closestD);
				if(clusterNum<0)
				{
					System.out.print("Jopa");
				}
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
			Integer numCentr = 0;
			for (List<Integer> clusterPoints : clusterToDataList)
			{
				if ( !clusterPoints.isEmpty() && !clusterPoints.equals(null))
				{
					tempMeanCentr = getAverageCentroid(getListOfDataPointsPerIndices (data, clusterPoints));
					initialCentroids.set(numCentr, tempMeanCentr);
				}
				numCentr++;
			}
			
			// check if converged
			// in order to check for the proper convergence min 2 iterations have to occur:
			// on first iterations the old version of the centroids is preserved 
			// on the second distance between old and new cetroids is calculated, then it's also preserved
			// on the following iterations the current and preserved distances between old and new versions are compared and decision is made if converged
			if (iterCount > 0)
			{
				currAndPrevCentrDistancesNew = getEuclDistancesForAllCentroids (previousInitialCentroids, initialCentroids);
			}
			if (iterCount > 1)
			{
				if (ifDataTallAndNarrow)
				{
					converged = ifCentroidsConvergedTallNarrow(currAndPrevCentrDistancesOld, currAndPrevCentrDistancesNew);
				}
				else
				{
					converged = ifCentroidsConvergedShortWide( previousDataToclusterList, dataToclusterList );
				}
			}
	         
			previousInitialCentroids = new ArrayList<>(initialCentroids);
			currAndPrevCentrDistancesOld = new ArrayList<>(currAndPrevCentrDistancesNew);
			
			iterCount ++;
		}	
		
		Map<String,Integer> dataToclusterMap = createDataToClusterMap(dataToclusterList, rowLabels);
		ClusteringResult result = new ClusteringResult( dataToclusterMap, (String) clusterParams.get("outpath"), "KMeans");
		flushFirstInterface();
		return result;
	}
	
	/**
	 * Creates a
	 * @param dataToclusterList
	 * @param rowLabels
	 * @return Map: data to cluster
	 */
	private Map<String,Integer> createDataToClusterMap(List<Integer> dataToclusterList, List<String> rowLabels)
	{
		Map<String,Integer> dataToclusterMap = new LinkedHashMap<>();
		
		int i = 0;
		for (String rowL : rowLabels)
		{
			
			dataToclusterMap.put(rowL, dataToclusterList.get(i));
			i++; 
		}
		return dataToclusterMap;
	}
	private Map<String,Integer> createDataToClusterMap(List<Integer> dataToclusterList, Set<String> rowLabels)
	{
		Map<String,Integer> dataToclusterMap = new LinkedHashMap<>();
		
		int i = 0;
		for (String rowL : rowLabels)
		{
			
			dataToclusterMap.put(rowL, dataToclusterList.get(i));
			i++; 
		}
		return dataToclusterMap;
	}
	
	/**
	 * Initializes algorithm main parameters
	 * @param clusterParams
	 */
	@SuppressWarnings("unchecked")
	private void initializeAlgorithmParametersFirstInterface(Double[][] data, Map<String,Object> clusterParams)
	{
		maxiterations = (Integer) clusterParams.get("maxiterations");
		if(maxiterations.equals(null))
		{
			maxiterations = 8;  // default value
		}
		
		k = (Integer) clusterParams.get("k");
		if(k.equals(null))
		{
			k = 7;             // default value
		}
		
		initialmethod = (String) clusterParams.get("initialmethod");
		if ("initialindices".equals(initialmethod))
		{
			initialIndices = (List<Integer>) clusterParams.get("initindices");
			if ( initialIndices!= null )
			{
				k = initialIndices.size();
				initialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
			}
			{
				defaultToRandomInitialMethod(data);
			}
		}
		else if ("initialcentroids".equals(initialmethod))
		{
			initialCentroids = (ArrayList<Double[]>) clusterParams.get("initialcentroids");
			if ( initialCentroids!= null )
			{
				k = initialCentroids.size();
			}
			else
			{
				defaultToRandomInitialMethod(data);
			}

		}
		else
		{
			// it covers both the null check and "random" initialmethod
			// randomly choose k indices from the matrix
			defaultToRandomInitialMethod(data);
		}
	}
	
	/**
	 * defaults to Random initial method
	 * @param data
	 */
	private void defaultToRandomInitialMethod(Double[][] data)
	{
		initialIndices = getListOfRandomIndicies (0, data.length, k);
		initialCentroids = getListOfDataPointsPerIndices(data, initialIndices);	
	}
	/**
	 * Reinitializes all the class properties
	 */
	private void flushFirstInterface()
	{
		maxiterations = 8;
		k = 7;
		initialmethod = ""; //{random, initialindices, initialcentroids};"
		initialCentroids = null;
		initialIndices = null;
	}
	
	/**
	 * check if the cluster data has been changed.
	 *
	 * @param previousdataToclusterList the previousdata tocluster list
	 * @param dataToclusterList the data tocluster list
	 * @return true or false
	 */
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
	
	/**
	 * check how much centroids distances have been changed, omega is a threshold
	 * @param currAndPrevCentrDistancesOld
	 * @param initialcentroids currAndPrevCentrDistancesNew
	 * @return the convergence result
	 */
	private static boolean ifCentroidsConvergedTallNarrow( List<Double> currAndPrevCentrDistancesOld, List<Double> currAndPrevCentrDistancesNew)
	{
		boolean result = true;
		Double absVal;
		Double divResult;
		for (int i = 0; i<currAndPrevCentrDistancesOld.size(); i++)
		{
			if (currAndPrevCentrDistancesNew.get(i) > currAndPrevCentrDistancesOld.get(i))
			{
				absVal = Math.abs(currAndPrevCentrDistancesNew.get(i) - currAndPrevCentrDistancesOld.get(i));
				divResult = absVal/currAndPrevCentrDistancesNew.get(i);
			}
			else
			{
				absVal = Math.abs(currAndPrevCentrDistancesOld.get(i) - currAndPrevCentrDistancesNew.get(i));
				divResult = absVal/currAndPrevCentrDistancesOld.get(i);
			}
			if (divResult >= omega)
			{
				result = false;
				return result;
			}
		}
		return result;
	}
	
	
	/**
	 * returns list of Euclidean distances between prev and curr centroids
	 * @param previousinitialcentroids
	 * @param initialcentroids
	 * @param omega
	 * @return
	 */
	private List<Double> getEuclDistancesForAllCentroids( List<Double[]> previousinitialcentroids, List<Double[]> initialcentroids)
	{
		List<Double> result = new ArrayList<>(); 
		for (int i = 0; i<previousinitialcentroids.size(); i++)
		{
			Double[] prevCentr = previousinitialcentroids.get(i);
			Double[] centr = initialcentroids.get(i);
			
			// compute distance between prevCentr and  centr
			result.add(euclidianDistance (centr, prevCentr));
		}
		return result;
	}
	
	/**
	 * calculated averaged centroid.
	 *
	 * @param listOfClusterDataPoints the list of cluster data points
	 * @return averaged centroid
	 */
	private static Double[] getAverageCentroid (List<Double[]> listOfClusterDataPoints)
	{
		Integer featNum = listOfClusterDataPoints.get(0).length;
		Double[] avgCentrVal = new Double[featNum];
		Integer pointsNum = listOfClusterDataPoints.size();
		for (int m = 0; m<featNum; m++)
		{
			avgCentrVal[m] = 0d;
		}		
		for (int m = 0; m<featNum; m++)
		{
			for (Double[] point : listOfClusterDataPoints)
			{
				avgCentrVal[m] += point[m];
			}
			avgCentrVal[m] = avgCentrVal[m]/pointsNum;
		}
		return avgCentrVal;
	}
	
	/**
	 * retrieves data points per indices, list of array of doubles.
	 *
	 * @param data the data
	 * @param initialIndices the initial indices
	 * @return data points per indices, list of array of doubles
	 */
	private static List<Double[]> getListOfDataPointsPerIndices(Double[][] data, List<Integer> initialIndices)
	{
		List<Double[]> initialcentroids = new ArrayList<>();

		for (Integer ini : initialIndices )
		{
			initialcentroids.add(data[ini]);
		}
		return initialcentroids;
	}
	
	/**
	 * Create a list of random indices of the data set.
	 *
	 * @param min the min
	 * @param max the max
	 * @param k the k
	 * @return result: list of indices
	 */
	private static List<Integer> getListOfRandomIndicies(Integer min, Integer max, Integer k)
	{
		Set<Integer> setResult = new HashSet<>();
		max = max - 1;
		Integer randomNum;
		Random rn = new Random();
		for (int i = 0; i < k; i++)
		{
			int n = max - min + 1;
			int r = rn.nextInt(n);
			randomNum =  min + r;
			setResult.add(randomNum);
		}
		List<Integer> result = new ArrayList<>(setResult);
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
	      Double sum = 0d;
	      for (int i = 0; i < point1.length; i++)
	      {
	        sum = sum + Math.pow((point2[i] - point1[i]), 2);
	      }
	      return Math.sqrt(sum);
	  }
	
    /**
     * Determines clusters, groups data points per each cluster.
     * @param data input data, represented by Map<List,BitSet>
     * @param clusterParams algorithm parameters
     * @return ClusteringResult object
     */

	public ClusteringResult cluster(Map<String, BitSet> data, Map<String,Object> clusterParams)
	  {
		    initializeAlgorithmParametersSecondInterface(data, clusterParams);
			// calculate Euclidean distances to all data points for each centroid
			// each centroid represents the center of the cluster
			
			List<Integer> dataToclusterList = new ArrayList<>();
			List<BitSet> previousDataToclusterList = new ArrayList<>();
			List<List<BitSet>> clusterToDataList = new ArrayList<>();
			// initialize and prefill the arrays with initial values
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
			Integer iteration = 0;
			while ( iteration < maxiterations && converged == false )
			{
				Set<String> allLabelsSet = data.keySet();
				List<String> allLabels = new ArrayList<>();
				allLabels.addAll(allLabelsSet);
				BitSet dataBs;
				BitSet dataBsPreserved;
				Integer distance;
				Integer closestD;
				Integer clusterNum;
				List<BitSet> listOfBitSet;
				List<Integer> listOfDistancePoints;
				// calc data point for initial centroids
				Integer countDataP = 0;
				for (String label : allLabels)
				{
					dataBs = data.get(label);
					listOfBitSet = new ArrayList<>();
					listOfDistancePoints = new ArrayList<>();
					for (BitSet clusterBs : initialCentroidsBs)
					{
						// calculate Euclidean distance, it's actually a Hamming distance, but we treat it
						// as our qualifier for the distance between 2 points (centroid and data point)
						dataBsPreserved = (BitSet) dataBs.clone();
						dataBsPreserved.or(clusterBs);
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
					
					dataToclusterList.set(countDataP, clusterNum);
					countDataP++;
				}
				
				// recalculate initial centroids
				BitSet oneBigCluster; // cluster that holds all data points features available in the cluster
				List<BitSet> listOfAllBitsInCluster = new ArrayList<>();	
				LinkedHashMap<Integer,Integer> allFeaturesCountMap; // map which holds count for each feature
				List<LinkedHashMap<Integer,Integer>> listOfAllFeaturesCountMaps = new ArrayList<>();
				
				for (List<BitSet> clusterBsList : clusterToDataList) // iterate through the list of clusters (each cluster is a list of BitSets)
				{
					if (!clusterBsList.equals(null))
					{	
						oneBigCluster = new BitSet();
						allFeaturesCountMap = new LinkedHashMap<>();
						for (BitSet dataPointsBs : clusterBsList)
						{
							oneBigCluster.or(dataPointsBs); // populate oneBigCluster
						}
						List<Integer> intList= new ArrayList<>();
				        for (Integer i = oneBigCluster.nextSetBit(0); i >= 0; i = oneBigCluster.nextSetBit(i + 1)) 
				        {
				            intList.add(i);
				            allFeaturesCountMap.put(i, 0); // initialize allFeaturesCountMap
				        }
				        listOfAllBitsInCluster.add(oneBigCluster);
				        listOfAllFeaturesCountMaps.add(allFeaturesCountMap);
					}
				}

	            
	            initialCentroidsBs = new ArrayList<BitSet>();
	            // for each cluster, since we already got the master BitSet (loop or operation that allowed us to get all features in one cluster),
	            // calculate how many times we see the particular feature (I call the fingerprint a feature)
	            List<LinkedHashMap<Integer,Integer>> listOfMapsForNewCenroids = new ArrayList<>();
	            List<Integer> newDistances = new ArrayList<>();
	            List<Integer> oldDistances = new ArrayList<>();
	            Integer countOuter = 0;
				for (LinkedHashMap<Integer,Integer> map : listOfAllFeaturesCountMaps)
				{
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
								// count 1's
								value = map.get(key);
								value += 1;
								map.put(key, value);
							}
						}	
					}
	                
					// finally calculate the average centroid
					BitSet newCentroid = new BitSet();
					for (Map.Entry<Integer, Integer> entry : map.entrySet())
					{
						Double conversion = 0.0;
						Integer key = entry.getKey();
						Integer value = entry.getValue();
						conversion = value.doubleValue()/countOfBsInBuckets.doubleValue();
						// transform averaged centroid results to 1's and 0's
						if (conversion < 0.5)
						{
							value = 0;
						}
						else
						{
							value = 1;
							newCentroid.set(key);
						}
						map.put(key, value);
					}
					listOfMapsForNewCenroids.add((LinkedHashMap<Integer, Integer>) map);
					initialCentroidsBs.add(newCentroid);
					countOuter++;
				}
				if (iteration>0)
				{
					//converged = ifCentroidsConverged(previousinitialcentroidsBs,initialCentroidsBs);
					newDistances = calDistancesBetweenCurrentAndPreviousCentr(previousinitialcentroidsBs,initialCentroidsBs);
				}
				if (iteration>1)
				{
					converged = ifConvergedBs(oldDistances, newDistances);
					if (converged)
					{
						System.out.print("Hi");
					}
				}
				previousinitialcentroidsBs = new ArrayList<>(initialCentroidsBs);
				oldDistances = new ArrayList<>(newDistances);
				iteration++;		
			}
			
			Map<String,Integer> dataToclusterMap = createDataToClusterMap(dataToclusterList, data.keySet());		
			ClusteringResult result = new ClusteringResult( dataToclusterMap, (String) clusterParams.get("outpath"), "KMeans" );
			flushSecondInterface();
			return result;
			
	  }
	

	/**
	 * Reinitializes all the class properties
	 */
	private void flushSecondInterface()
	{
		maxiterations = 8;
		k = 7;
		initialmethod = ""; //{random, initialindices, initialcentroids};"
		initialCentroidsBs = null;
		initialIndicesBs = null;
	}
	
	@SuppressWarnings("unchecked")
	private void initializeAlgorithmParametersSecondInterface(Map<String, BitSet> data, Map<String,Object> clusterParams)
	{
		initialCentroidsBs = new ArrayList<>();
		previousinitialcentroidsBs = new ArrayList<>();


		maxiterations = (Integer) clusterParams.get("maxiterations");
		if (maxiterations.equals(null))
		{
			maxiterations = 8;
		}

		k = (Integer) clusterParams.get("k");
		if (k.equals(null))
		{
			k = 7;
		}

		//get initial centroids

		initialmethod = (String) clusterParams.get("initialmethod");
		if ( !initialmethod.equals(null) )
		{
			if (initialmethod.equals("initialindices"))
			{
				if ( clusterParams.containsKey("initindices") )
				{
				
				initialIndicesBs = (List<String>) clusterParams.get("initindices");
				
				k = initialIndicesBs.size();
				initialCentroidsBs = getListOfDataPointsPerIndicesBs(data, initialIndicesBs);	
				}
			}
			else if (initialmethod.equals("initialcentroidsBs"))
			{
				if ( clusterParams.containsKey("initialcentroidsBs") )
				{
					List<ArrayList<Integer>> listOCentroids;
					listOCentroids = (ArrayList<ArrayList<Integer>>) clusterParams.get("initialcentroidsBs");
					BitSet bitSet = new BitSet();
					for (ArrayList<Integer> centr : listOCentroids)
					{
						for (Integer value : centr)
						{
							bitSet = new BitSet();
							bitSet.set(value);
						}
						initialCentroidsBs.add(bitSet);
					}
					k = listOCentroids.size();
				}
			}
			else
			{
				initialCentroidsBs = getRandomCentroids (data,k);
			}
		}	
		else
		{
			// overkill, but a fool proof, in case initialmethod was missed completely
			initialCentroidsBs = getRandomCentroids (data,k);
		}
		
		
	}
	
	/**
	 * check if centroids are equal
	 * @param previousinitialcentroidsBs the previousinitialcentroidsBs
	 * @param initialcentroidsBs the initialcentroidsBs
	 * @return true or falls
	 */
	private static boolean ifCentroidsConverged( List<BitSet> previousinitialcentroidsBs, List<BitSet> initialcentroidsBs )
	{
		boolean result = true;
		Integer count = 0;
		for (BitSet bs : previousinitialcentroidsBs) 
		{
			if(!initialcentroidsBs.get(count).equals(bs))
			{
				result = false;
			}
			count ++;
		}
		return result;
	}
	
	/**
	 * check if centroids are equal
	 * @param previousinitialcentroidsBs the previousinitialcentroidsBs
	 * @param initialcentroidsBs the initialcentroidsBs
	 * @return true or falls
	 */
	private static boolean ifConvergedBs( List<Integer> oldtDistances, List<Integer> newDistances )
	{
		boolean result = true;
		Integer count = 0;
		for (Integer distancePrev : oldtDistances) 
		{
			Double distanceCur = newDistances.get(count).doubleValue();
			Double absVal;
			Double divResult;
			if (distancePrev > distanceCur)
			{
				absVal = Math.abs(distancePrev.doubleValue()-distanceCur);
				divResult = absVal/distancePrev.doubleValue();
			}
			else
			{
				absVal = Math.abs(distanceCur - distancePrev.doubleValue());
				divResult = absVal/distanceCur;
			}
			
			if (divResult >= omega)
			{
				result = false;
				return result;
			}
			
			count ++;
		}
		return result;
	}
	
	/**
	 * calculates distances between current and previous centroids
	 * @param previousinitialcentroidsBs
	 * @param initialCentroidsBs
	 * @return
	 */
	private static List<Integer> calDistancesBetweenCurrentAndPreviousCentr(List<BitSet> previousinitialcentroidsBs, List<BitSet> initialCentroidsBs)
	{
		List<Integer> result = new ArrayList<>();
		
		for (Integer i = 0; i < initialCentroidsBs.size(); i++)
		{
			BitSet prev = previousinitialcentroidsBs.get(i);
			BitSet curr = initialCentroidsBs.get(i);
			prev.xor(curr);
			result.add(prev.cardinality());
		}
		return result;
	}
	
	
	
	/**
	 * retrieves random centroids from the data set.
	 *
	 * @param data the data
	 * @param k the k
	 * @return list of BitSets
	 */
	private static List<BitSet> getRandomCentroids(Map<String, BitSet> data, Integer k)
	{
		List<BitSet> initialcentroidsBs = new ArrayList<>();
		Set<String> allLabelsSet =  data.keySet();
		List<String> allLabels = new ArrayList<>();
		allLabels.addAll(allLabelsSet);
		List<Integer> randomIndicies = getListOfRandomIndicies (0, data.size(), k);
		for (Integer ri : randomIndicies)
		{
			initialcentroidsBs.add(data.get(allLabels.get(ri)));
		}
		return initialcentroidsBs;
	}
	
	/**
	 * retrieves the list of BistSets per indices passed.
	 *
	 * @param data the data
	 * @param initialIndicesBs the initial indices
	 * @return  list of BitSets
	 */
	private static List<BitSet> getListOfDataPointsPerIndicesBs(Map<String, BitSet> data, List<String> initialIndicesBs)
	{
		List<BitSet> initialcentroidsBs = new ArrayList<>();;
		for (String ini : initialIndicesBs )
		{
			initialcentroidsBs.add(data.get(ini));
		}
		return initialcentroidsBs;
	}
}
