package edu.harvard.cscie99.clustering.io;

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

public class JarvisPatrickAlgorithmImpl implements ClusteringMethod
{


	
    /**
     * Determines clusters, groups data points per each cluster
     * @param rowLabels list of row labels
     * @param data input data, represented by Double[][]
     * @param clusterParams algorithm parameters
     * @return ClusteringResult object
     */
	@SuppressWarnings({ "unchecked", "unused" })
	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String, Object> clusterParams) 
	{
		/*
		JarvisPatrickimplements ClusterMethod 
		For each row, find the closest numNeighbors 
		Two items cluster together if they are in each other’s list and have common_neighbors in 
		common. 
		*/
		ValueComparator vc = new ValueComparator();
		
        // default values
		Integer numNeighbors = 7;
		Integer commonNeighbors = 5;
		
		String distanceMetric = "Euclidian";
		if (clusterParams.containsKey("numneighbors"))
		{
			numNeighbors = (Integer) clusterParams.get("numneighbors"); //7 //the number of Neighbors to Examine
		}
		if (clusterParams.containsKey("commonneighbors"))
		{
			commonNeighbors = (Integer) clusterParams.get("commonneighbors"); //5 // the minimum required number of Neighbors in Common.
		}
				
		// For each object, find its J-nearest neighbors where ‘J’ corresponds to the Neighbors to Examine parameter on the Partitional Clustering dialog
				
		// Calculate nearest Neighbors for each data point
		Map<Integer,Double> closeNeighbors;
		Map<Integer,Double> sortedCloseNeighbors;
		Set<Integer> pointNeighbors;
		Map<Integer, Set<Integer>> eachPointNeighbors = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> clustersMap = new HashMap<Integer, Set<Integer>>();
		Set<Integer> points;
		for (Integer i = 0; i < data.length; i++)
		{
			closeNeighbors = new HashMap<Integer,Double>();
			pointNeighbors = new HashSet<Integer>();
			Double[] point = data[i];
			for (Integer j = 0; j < data.length; j++)
			{
				Double[] neighbors = data[j];
				if (!i.equals(j))
				{
					closeNeighbors.put(j,euclidianDistance(point,neighbors));
				}
			}
			
			// sort and get up to numNeighbors 
			sortedCloseNeighbors = vc.sortByValue(closeNeighbors); // hope the sort works correctly!!!
			
			Integer countBreak = numNeighbors;
			for (Integer key: sortedCloseNeighbors.keySet())
			{
				if(countBreak.equals(0))
				{
					break;
				}
				countBreak--;
				pointNeighbors.add(key);
			}
			eachPointNeighbors.put(i, pointNeighbors);
			// make a data point to have its stand alone cluster
			points = new LinkedHashSet<Integer>();
			points.add(i);
			clustersMap.put(i,points);
		}
		
		// traverse through the eachPointNeighbors
		
		Iterator<Entry<Integer, Set<Integer>>> it = eachPointNeighbors.entrySet().iterator();

		Set<Object> setOfPairs =  new LinkedHashSet<Object>();
		
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
		        
		        
		        Set<Integer> intersection = new HashSet<Integer>(checkPointsNeighborsInner);
		        intersection.retainAll(checkPointsNeighbors);
		        // check if 2 point are together
		        if(checkPointsNeighborsInner.contains(checkPoint) &&
		           checkPointsNeighbors.contains(checkPointInner) &&
		           intersection.size() >= commonNeighbors)
		        {
		        	if(clustersMap.containsKey(checkPointInner)&&clustersMap.containsKey(checkPoint))
		        	{
		        		// merge points to one cluster
		        		Set<Integer> temptSetPointsInner = clustersMap.get(checkPointInner);
		        		Set<Integer> temptSetPoints = clustersMap.get(checkPoint);
		        		temptSetPoints.addAll(temptSetPointsInner);
		        		clustersMap.put(checkPoint,temptSetPoints);
		        		clustersMap.remove(checkPointInner);
		        	}
		        	
		        }
		    }
	    }
	    
	
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
	        Integer p = pairs.getKey();
	        Set<Integer> setik = pairs.getValue();       
	        clustersMapFinal.put(countCluster, setik);
	        
	        for(Integer in : setik)
	        {
	        	pointsClusterMarkingMap.put(rowLabels.get(in),countCluster);
	        } 
	        countCluster++;
	    }
	    
			
		ClusteringResult result = new ClusteringResult( pointsClusterMarkingMap, (String) clusterParams.get("outpath"), "JarvisPatrick" );
		return result;	
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
     * Determines clusters, groups data points per each cluster
     * @param data input data, represented by Map<List,BitSet>
     * @param clusterParams algorithm parameters
     * @return ClusteringResult object
     */
	@SuppressWarnings({ "unchecked", "unused" })
	public ClusteringResult cluster(Map<String, BitSet> data, Map<String, Object> clusterParams)
	{
		/*
		JarvisPatrickimplements ClusterMethod 
		For each row, find the closest numNeighbors 
		Two items cluster together if they are in each other’s list and have common_neighbors in 
		common. 
		*/
		ValueComparator vc = new ValueComparator();
		
        // default values
		Integer numNeighbors = 7;
		Integer commonNeighbors = 5;
		
		String distanceMetric = "Euclidian";
		if (clusterParams.containsKey("numneighbors"))
		{
			numNeighbors = (Integer) clusterParams.get("numneighbors"); //7 //the number of Neighbors to Examine
		}
		if (clusterParams.containsKey("commonneighbors"))
		{
			commonNeighbors = (Integer) clusterParams.get("commonneighbors"); //5 // the minimum required number of Neighbors in Common.
		}
		
		
		//LinkedHashMap<Integer, BitSet> eachPointNeighbors = new LinkedHashMap<Integer, BitSet>();
		
		Integer countCluster = 0;
		
		Map<Integer,Integer> closeNeighbors;
		Map<Integer,Double> sortedCloseNeighbors;
		Set<Integer> pointNeighbors;
		Map<Integer, Set<Integer>> eachPointNeighbors = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> clustersMap = new HashMap<Integer, Set<Integer>>();
		Set<Integer> points;
		Iterator<Entry<String, BitSet>> itera = data.entrySet().iterator();
	    while (itera.hasNext())
	    {
			closeNeighbors = new HashMap<Integer,Integer>();
			pointNeighbors = new HashSet<Integer>();
	    	
	        Entry<String, BitSet> pairs = itera.next();
	        String row = pairs.getKey();
	        BitSet bs = pairs.getValue();       
	        
	        Iterator<Entry<String, BitSet>> iteraInner = data.entrySet().iterator();
	        
	        Integer countClusterInner = 0;
	        while (iteraInner.hasNext())
	        {
	        	if(!countCluster.equals(countClusterInner))
	        	{
			        Entry<String, BitSet> pairsInner = iteraInner.next();
			        String rowInner = pairsInner.getKey();
			        BitSet bsInner = pairsInner.getValue(); 
			        
			        bsInner.xor(bs);
			        Integer distance = bsInner.cardinality();
			        closeNeighbors.put(countClusterInner,distance);
	        	}
		        
		        countClusterInner++;
	        }
	        
			// sort and get up to numNeighbors 
			sortedCloseNeighbors = vc.sortByValue(closeNeighbors);
	        
			Integer countBreak = numNeighbors;
			for (Integer key: sortedCloseNeighbors.keySet())
			{
				if(countBreak.equals(0))
				{
					break;
				}
				countBreak--;
				pointNeighbors.add(key);
			}
			
			eachPointNeighbors.put(countCluster, pointNeighbors);
			// make a data point to have its stand alone cluster
			points = new LinkedHashSet<Integer>();
			points.add(countCluster);
			clustersMap.put(countCluster,points);
	        countCluster++;
	    }
		
	 // traverse through the eachPointNeighbors
		
 		Iterator<Entry<Integer, Set<Integer>>> it = eachPointNeighbors.entrySet().iterator();

 		Set<Object> setOfPairs =  new LinkedHashSet<Object>();
 		
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
 		        
 		        
 		        Set<Integer> intersection = new HashSet<Integer>(checkPointsNeighborsInner);
 		        intersection.retainAll(checkPointsNeighbors);
 		        // check if 2 point are together
 		        if(checkPointsNeighborsInner.contains(checkPoint) &&
 		           checkPointsNeighbors.contains(checkPointInner) &&
 		           intersection.size() >= commonNeighbors)
 		        {
 		        	if(clustersMap.containsKey(checkPointInner)&&clustersMap.containsKey(checkPoint))
 		        	{
 		        		// merge points to one cluster
 		        		Set<Integer> temptSetPointsInner = clustersMap.get(checkPointInner);
 		        		Set<Integer> temptSetPoints = clustersMap.get(checkPoint);
 		        		temptSetPoints.addAll(temptSetPointsInner);
 		        		clustersMap.put(checkPoint,temptSetPoints);
 		        		clustersMap.remove(checkPointInner);
 		        	}
 		        	
 		        }
 		    }
 	    }
		
		Map<Integer, Set<Integer>> clustersMapFinal = new LinkedHashMap<Integer, Set<Integer>>();
		Map<String,Integer> pointsClusterMarkingMap = new LinkedHashMap<String,Integer>();
		Set<String> rowLabelsSet = data.keySet();
		List<String> rowLabels = new ArrayList<String>();
		rowLabels.addAll(rowLabelsSet);
		for (String rowL : rowLabels)
		{
			pointsClusterMarkingMap.put(rowL,null);
		}
		
		Iterator<Entry<Integer, Set<Integer>>> iteraN = clustersMap.entrySet().iterator();
		Integer countClusterN = 0;
	    while (iteraN.hasNext())
	    {
	        Entry<Integer, Set<Integer>> pairs = iteraN.next();
	        Integer p = pairs.getKey();
	        Set<Integer> setik = pairs.getValue();       
	        clustersMapFinal.put(countClusterN, setik);
	        
	        for(Integer in : setik)
	        {
	        	pointsClusterMarkingMap.put(rowLabels.get(in),countClusterN);
	        } 
	        countClusterN++;
	    }
	    
			
		ClusteringResult result = new ClusteringResult( pointsClusterMarkingMap, (String) clusterParams.get("outpath"), "JarvisPatrick" );
		return result;	
 	    
	}

}
