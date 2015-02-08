package edu.harvard.cscie99.clustering.io;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public interface ClusteringMethod

{

	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String,Object> clusterParams);
	
	public ClusteringResult cluster(Map<String, BitSet> data, Map<String,Object> clusterParams);
	
}
