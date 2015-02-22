package edu.harvard.cscie99.clustering.impl;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface ClusteringMethod.
 */
public interface ClusteringMethod

{

	/**
	 * Cluster.
	 *
	 * @param rowLabels the row labels
	 * @param data the data
	 * @param clusterParams the cluster params
	 * @return the clustering result
	 */
	public ClusteringResult cluster(List<String> rowLabels, Double[][] data, Map<String,Object> clusterParams);
	
	/**
	 * Cluster.
	 *
	 * @param data the data
	 * @param clusterParams the cluster params
	 * @return the clustering result
	 */
	public ClusteringResult cluster(Map<String, BitSet> data, Map<String,Object> clusterParams);
	
}
