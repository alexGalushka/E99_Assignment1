package edu.harvard.cscie99.clustering.io;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class JarvisPatrickAlgorithmImpl implements ClusteringMethod
{


	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String, Object> clusterParams) 
	{
		/*
		JarvisPatrickimplements ClusterMethod 
		a.  “distanceMetric”  {“Euclidian”} 
		b.  “numNeighbors”  7 
		c.  “commonNeighbors”  5 
		For each row, find the closest numNeighbors 
		Two items cluster together if they are in each other’s list and have common_neighbors in 
		common. 
		
		*/
		return null;
	}


	public ClusteringResult cluster(Map<String, BitSet> data, Map<String, Object> clusterParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
