package edu.harvard.cscie99.clustering.impl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * The Class JarvisPatrickAlgorithmImpl.
 * Implementation of Jarvis-Patrick algorithm for with 2 diferent interfaces
 */
public class JarvisPatrickAlgorithmImpl implements ClusteringMethod
{
    private static Integer numNeighbors;
    private static Integer commonNeighbors;
    
    public JarvisPatrickAlgorithmImpl()
    {
    	//default values for algorithms properties
    	numNeighbors = 7;
    	commonNeighbors = 5;
    }
    
    /**
     * Determines clusters, groups data points per each cluster. 
	 * For each row, find the closest numNeighbors 
	 * Two items cluster together if they are in each other’s list and have common_neighbors in 
	 * common. 
     * @param rowLabels list of row labels
     * @param data input data, represented by Double[][]
     * @param clusterParams algorithm parameters
     * @return ClusteringResult object
     */
	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String, Object> clusterParams) 
	{
		// initialize algorithm parameters
		initializeParameters( clusterParams );
        
		Map<String,Integer> pointsClusterMarkingMap = runAlgorithmMatrix( rowLabels, data );
	
		ClusteringResult result = new ClusteringResult( pointsClusterMarkingMap, (String) clusterParams.get("outpath"), "JarvisPatrick" );
		return result;	
	}
	
	/**
	 * actual algorithm logic for matrix interface
	 * @param data
	 * @return pointsClusterMarkingMap
	 */
	private Map<String,Integer> runAlgorithmMatrix( List<String> rowLabels, Double[][] data )
	{
		ValueComparator vc = new ValueComparator();	
		// For each object, find its J-nearest neighbors where ‘J’ corresponds to the Neighbors to Examine parameter
		// on the Partitional Clustering dialog			
		// Calculate nearest Neighbors for each data point
		Map<Integer,Double> sortedCloseNeighbors;
		Set<Integer> pointNeighbors;
		Map<Integer, Set<Integer>> eachPointNeighbors = new HashMap<>();
		Map<Integer, Set<Integer>> clustersMap = new HashMap<>();
		Set<Integer> points;
		for (Integer i = 0; i < data.length; i++)
		{
			sortedCloseNeighbors = getSortedCloseNeighborsMap(i, vc, data);
			pointNeighbors = getNeighborsSetPerNumOfNeighbors(numNeighbors, sortedCloseNeighbors);
			eachPointNeighbors.put(i, pointNeighbors);
			// let a data point to have its stand alone cluster
			points = new LinkedHashSet<>();
			points.add(i);
			clustersMap.put(i,points);
		}
		
		// iterate through eachPointNeighbors for eachPointNeighbors and modify/merge clusters of clustersMap
		clustersMap = createClusterMap(eachPointNeighbors, clustersMap );
	
		// create final "clean" cluster map with sequentially ordered cluster ID numbers
		Map<String,Integer> pointsClusterMarkingMap = createFinalCleanPointsToClusterMap(clustersMap, rowLabels);
		
		return pointsClusterMarkingMap;
	}
     
    /**
     * Determines clusters, groups data points per each cluster.
     * 	For each row, find the closest numNeighbors 
	 *	Two items cluster together if they are in each other’s list and have common_neighbors in 
	 *	common. 
     * @param data input data, represented by Map<List,BitSet>
     * @param clusterParams algorithm parameters
     * @return ClusteringResult object
     */
	public ClusteringResult cluster(Map<String, BitSet> data, Map<String, Object> clusterParams)
	{
		// initialize algorithm parameters
		initializeParameters( clusterParams );
		
		Map<String,Integer> pointsClusterMarkingMap = runAlgorithmBs( data );
		
		ClusteringResult result = new ClusteringResult( pointsClusterMarkingMap, (String) clusterParams.get("outpath"), "JarvisPatrick" );
		return result;	
 	    
	}
	
	/**
	 * actual algorithm logic for BitSet interface
	 * @param data
	 * @return pointsClusterMarkingMap
	 */
	private Map<String,Integer> runAlgorithmBs( Map<String, BitSet> data )
	{
		
		ValueComparator vc = new ValueComparator();
        // Determine closest neighbors for each point 
		Map<Integer,Double> sortedCloseNeighbors;
		Set<Integer> pointNeighbors;
		Map<Integer, Set<Integer>> eachPointNeighbors = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> clustersMap = new HashMap<Integer, Set<Integer>>();
		Set<Integer> points;
		Iterator<Entry<String, BitSet>> itera = data.entrySet().iterator();
		Integer countCluster = 0;
	    while (itera.hasNext())
	    {
			pointNeighbors = new HashSet<Integer>();
	        Entry<String, BitSet> pairs = itera.next();
	        BitSet bs = pairs.getValue();       
	        
			// sort and get up to numNeighbors 
			sortedCloseNeighbors = getSortedCloseNeighborsMapBs ( countCluster, vc, bs,   data);
			
			pointNeighbors = getNeighborsSetPerNumOfNeighbors(numNeighbors, sortedCloseNeighbors);
			eachPointNeighbors.put(countCluster, pointNeighbors);
			// make a data point to have its stand alone cluster
			points = new LinkedHashSet<Integer>();
			points.add(countCluster);
			clustersMap.put(countCluster,points);
	        countCluster++;
	    }
		
		// iterate through eachPointNeighbors for eachPointNeighbors and modify/merge clusters of clustersMap
		clustersMap = createClusterMap(eachPointNeighbors, clustersMap );
				
		Set<String> rowLabelsSet = data.keySet();
		List<String> rowLabels = new ArrayList<String>();
		rowLabels.addAll(rowLabelsSet);
		
		// create final "clean" cluster map with sequentially ordered cluster ID numbers
		Map<String,Integer> pointsClusterMarkingMap = createFinalCleanPointsToClusterMap(clustersMap, rowLabels);
		
		return pointsClusterMarkingMap;
	}
	
	/**
	 * Determine closest neighbors for each point 
	 * @param countCluster
	 * @param vc
	 * @param bs
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer,Double> getSortedCloseNeighborsMapBs ( Integer countCluster, ValueComparator vc, BitSet bs,  Map<String, BitSet> data)
	{
		Iterator<Entry<String, BitSet>> iteraInner = data.entrySet().iterator();
		Map<Integer,Integer> closeNeighbors = new HashMap<>();
		Map<Integer,Double> sortedCloseNeighbors;
        Integer countClusterInner = 0;
        while (iteraInner.hasNext())
        {
        	if(!countCluster.equals(countClusterInner))
        	{
		        Entry<String, BitSet> pairsInner = iteraInner.next();
		        BitSet bsInner = pairsInner.getValue(); 
		        
		        bsInner.xor(bs);
		        Integer distance = bsInner.cardinality();
		        closeNeighbors.put(countClusterInner,distance);
        	}
	        
	        countClusterInner++;
        }
        
		// sort and get up to numNeighbors 
		sortedCloseNeighbors = vc.sortByValue(closeNeighbors);
	    return sortedCloseNeighbors;
	}
	
	/**
	 * Creates a final points to cluster map
	 * @param clustersMap
	 * @param rowLabels
	 * @return
	 */
	private	Map<String,Integer> createFinalCleanPointsToClusterMap(Map<Integer, Set<Integer>> clustersMap, List<String> rowLabels)
	{
		Map<Integer, Set<Integer>> clustersMapFinal = new LinkedHashMap<Integer, Set<Integer>>();
		Map<String,Integer> pointsClusterMarkingMap = new LinkedHashMap<String,Integer>();
		for (String rowL : rowLabels)
		{
			pointsClusterMarkingMap.put(rowL,null);
		}
		
		Iterator<Entry<Integer, Set<Integer>>> itera = clustersMap.entrySet().iterator();
		Integer countCluster = 0;
		while (itera.hasNext())
		{
			Entry<Integer, Set<Integer>> pairs = itera.next();
			Set<Integer> setik = pairs.getValue();       
			clustersMapFinal.put(countCluster, setik);
			
			for(Integer in : setik)
			{
				pointsClusterMarkingMap.put(rowLabels.get(in),countCluster);
			} 
			countCluster++;
		}
		return pointsClusterMarkingMap;
	}
	
	/**
	 * Create a cluster map: key - point index, value - cluster ID
	 * @param eachPointNeighbors
	 * @param clustersMap - key - point index, value - the same pint index (each point has its own cluster) 
	 * @return clustersMap
	 */
	private Map<Integer, Set<Integer>> createClusterMap( Map<Integer, Set<Integer>> eachPointNeighbors, Map<Integer, Set<Integer>> clustersMap )
	{
		Iterator<Entry<Integer, Set<Integer>>> it = eachPointNeighbors.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Integer, Set<Integer>> pairs = it.next();
			Integer checkPoint = pairs.getKey();
			Set<Integer> checkPointsNeighbors = pairs.getValue();       
			Iterator<Entry<Integer, Set<Integer>>> itInner = eachPointNeighbors.entrySet().iterator();
			while (itInner.hasNext())
			{
				Entry<Integer, Set<Integer>> pairsInner = itInner.next();
				Integer checkPointInner = pairsInner.getKey();
				Set<Integer> checkPointsNeighborsInner = pairsInner.getValue();

				Set<Integer> intersection = new HashSet<>(checkPointsNeighborsInner);
				intersection.retainAll(checkPointsNeighbors);
				// check if 2 point are together
				 if (intersection.size() >= commonNeighbors) // 3 constraints have been broken into 2 condition checks for optimization purposes
				 {
					if(checkPointsNeighborsInner.contains(checkPoint) &&
					   checkPointsNeighbors.contains(checkPointInner))
					{
						if(clustersMap.containsKey(checkPointInner)&&clustersMap.containsKey(checkPoint))
						{
							// merge points to one cluster
							Set<Integer> tempSetPointsInner = clustersMap.get(checkPointInner);
							Set<Integer> tempSetPoints = clustersMap.get(checkPoint);
							tempSetPoints.addAll(tempSetPointsInner);
							clustersMap.put(checkPoint,tempSetPoints);
							clustersMap.remove(checkPointInner);
						}
						
					}
				 }
			}
		}
		return clustersMap;
	}
	
	/**
	 * Creates a limited set of neighbors from sortedCloseNeighbors map per countBreak
	 * @param countBreak
	 * @param sortedCloseNeighbors
	 * @return pointNeighbors
	 */
	private HashSet<Integer> getNeighborsSetPerNumOfNeighbors(Integer countBreak, Map<Integer,Double> sortedCloseNeighbors)
	{	
		HashSet<Integer> pointNeighbors = new HashSet<>();	
		for (Integer key: sortedCloseNeighbors.keySet())
		{
			if(countBreak.equals(0))
			{
				break;
			}
			countBreak--;
			pointNeighbors.add(key);
		}
		return pointNeighbors;
	}
	
	/**
	 * Creates a map of pointIndexes to the distance pairs, sorted by distance in acsending order
	 * @param pointIndex - index number of the point whom the "neighbor points" which would be determined by this method would belong to 
	 * @param vc - instance of the ValueComparator class for map sorting purpos
	 * @param data - initial matrix data
	 * @return sortedCloseNeighbors
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer,Double>	getSortedCloseNeighborsMap(Integer pointIndex, ValueComparator vc, Double[][] data)
	{		
		Map<Integer,Double> sortedCloseNeighbors;
		Map<Integer,Double> closeNeighbors = new HashMap<Integer,Double>();
		Double[] point = data[pointIndex];
		for (Integer j = 0; j < data.length; j++)
		{
			Double[] neighbors = data[j];
			if (!pointIndex.equals(j))
			{
				closeNeighbors.put(j,euclidianDistance(point,neighbors));
			}
		}
		// sort and get up to numNeighbors 
		sortedCloseNeighbors = vc.sortByValue(closeNeighbors);
		return sortedCloseNeighbors;
	}
	
	  /**
	   * 
	   * @param clusterParams
	   */
      private static void initializeParameters( Map<String, Object> clusterParams)
      {
  		if (clusterParams.containsKey("numneighbors"))
  		{
  			numNeighbors = (Integer) clusterParams.get("numneighbors"); //7 //the number of Neighbors to Examine
  		}
  		if (clusterParams.containsKey("commonneighbors"))
  		{
  			commonNeighbors = (Integer) clusterParams.get("commonneighbors"); //5 // the minimum required number of Neighbors in Common.
  		}
  				
      }
      
	  /**
	   * Calculates the Euclidean distance between two points represented by vector.
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

	/**
	 * @return the numNeighbors
	 */
	public static Integer getNumNeighbors() {
		return numNeighbors;
	}

	/**
	 * @return the commonNeighbors
	 */
	public static Integer getCommonNeighbors() {
		return commonNeighbors;
	}

	/**
	 * @param numNeighbors the numNeighbors to set
	 */
	public static void setNumNeighbors(Integer numNeighbors) {
		JarvisPatrickAlgorithmImpl.numNeighbors = numNeighbors;
	}

	/**
	 * @param commonNeighbors the commonNeighbors to set
	 */
	public static void setCommonNeighbors(Integer commonNeighbors) {
		JarvisPatrickAlgorithmImpl.commonNeighbors = commonNeighbors;
	}
	
}
