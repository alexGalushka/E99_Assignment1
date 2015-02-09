package edu.harvard.cscie99.clustering.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Clustering
{
	final Map<String, List<String>> clusterParams = new HashMap<>();
	

	public static void main(String[] args)
	{
		Map<String, Object> clusterParam = new HashMap<String,Object>();
	    ClusteringResult result;
	    ClusteringMethod algorithm;
		if(args.length == 1)
		{
			if (args[0].toLowerCase().equals("-help"))
			{
				String s = "***K-Means Clustering Algorithm User Manual***\n\n"
						+ "About:\n"
						+ "This analytical tool supports two clustering algorithms: K-Means and Patrick-Javis\n"
						+ "It works with matrix and fingerprint data sets\n\n"
						+ "Parameters usage:\n"
						+ "first input parameter <-mtxfile> or <-fpfile>:\n"
						+ "if <-mtxfile> is passed in, matrix interface algorithm implemenatation will be used, if <-fpfile> - fingerprint\n\n"
						+ "second input parameter <-outpath>:\n"
						+ "allows to specify the path of the output file\n\n"
						+ "third input parameter <-algorithm>:\n"
						+ "allows to specify algorithm: [kmeans] or [patrick_javis]\n\n"
						+ "fourth input parameter <-k>:\n"
						+ "number of clusters\n\n"
						+ "fifth input paramter <-distanceMetric>:\n"
						+ "method to calculated distance between 2 data, by default is [Euclidean]; it has to be specified though\n\n"
						+ "sixth input parameter <-initialMethod>:\n"
						+ "method to initialize centroids, 3 options to pick from: [random], [initialIndices], [initialCentroids]\n\n"
						+ "seventh input parameter <-maxiterations>:\n"
						+ "specify the maximum algorithm iterations e.g. -maxiterations 8 "
						+ "eighth input parameter <-initIndices> or <-initialCentroids>:\n"
						+ "it is allowed to specify centroids, if <-initIndices>, specify number of centroids, e.g. 2,3,4\n"
						+ "if <-initialCentroids> actual points centroids points have to be specified, e.g. 0.2,0.4,0.1,1|0.2,0.4,0.1,1\n\n\n"
						+ "***Jarvis-Patrick Clustering Algorithm User Manual***\n\n"
						+ "Comply with following for matrix:\n"
						+ "-mtxfile iris.txt -outpath [provide your path] -algorithm jarvis_patrick -distanceMetric Euclidean -numNeighbors 7 -commonNeighbors 5\n"
						+ "Comply with following for bitset:\n"
						+ "-fpfile bbb2_daylight.fp.txt -outpath [rovide your path] -algorithm jarvis_patrick -distanceMetric Euclidean -numNeighbors 7 -commonNeighbors 5";
				System.out.println(s);
			}
		}
		// for K-means clustering algorithm expect 14 or 12 entries
		else if ( (args.length == 14 || args.length == 12) && args[5].toLowerCase().equals("kmeans") )
		{
			clusterParam.put("algorithm", "kmeans");
			
			String dataType = null;
			boolean ifMatrix = false;
			if (args[0].toLowerCase().equals("-mtxfile"))
			{
				dataType = "matrix";
				ifMatrix = true;
			}
			else if (args[0].toLowerCase().equals("-fpfile"))
			{
				dataType = "fingerprint";
			}
			else
			{
			     System.err.println("Illegal CLI usage: Choose the correct data type, <-mtxfile> for matrix, <-fpfile> for fingerprint");
			     return;
			}
			clusterParam.put("datatype", dataType);
			clusterParam.put("datafile",args[1]);
			
			if (args[2].toLowerCase().equals("-outpath"))
			{
				clusterParam.put("outpath", args[3]);           
			}
			else
			{
				System.err.println("Illegal CLI usage: Second input parameter should be <-outpath>");
				return;
			}
			
			if (args[6].toLowerCase().equals("-k"))
			{
				clusterParam.put("k", Integer.parseInt(args[7]));           
			}
			else
			{
				System.err.println("Illegal CLI usage: Fourth input parameter should be <-k>");
			}
			
			if (args[8].toLowerCase().equals("-distancemetric"))
			{
				clusterParam.put("distancemetric", args[9]);           
			}
			else
			{
				System.err.println("Illegal CLI usage: Fifth input parameter should be <-distanceMetric>");
				return;
			}
			
			boolean ifToProvideCentroids = false;
			
			if (args[10].toLowerCase().equals("-initialmethod"))
			{
				if (args[11].toLowerCase().equals("random"))
				{
					clusterParam.put("initialmethod", args[11]);           
				}
				else if(args[11].toLowerCase().equals("initialindices"))
				{
					clusterParam.put("initialmethod", args[11]);
					ifToProvideCentroids = true;
				}
				else if(args[11].toLowerCase().equals("initialcentroids"))
				{
					ifToProvideCentroids = true;
				}
				else
				{
					System.err.println("Illegal CLI usage: values for <-initialMethod> parameter input are [random], [initialIndices], [initialCentroids]");
					return;
				}
					
			}
			else
			{
				System.err.println("Illegal CLI usage: Sixth input parameter should be <-initialMethod>");
				return;
			}
			
			if (args[12].toLowerCase().equals("-maxiterations")) 
			{
				Integer maxIter = Integer.parseInt(args[13]);
				clusterParam.put("maxiterations", maxIter);           

			}
			else
			{
				System.err.println("Illegal CLI usage: Seventh input parameter should be <-maxiterations>");
				return;
			}
			
			if (ifToProvideCentroids)
			{
				if (args[14].toLowerCase().equals("-initindices"))
				{
					if(ifMatrix)
					{
						if (args[15].contains(",") || args[13].length() == 1)
						{
							List<Integer> indicies = getIndiciesAsArrayOfInts (",",args[15]);
							
							clusterParam.put("initindices", indicies);
						}
						else
						{
							System.err.println("Illegal CLI usage: Provided indicies have to be comma separated");
							return;
						}
					}
					else
					{
						if (args[15].contains(",") || args[15].length() == 1)
						{
							List<String> indicies = getIndiciesAsArrayOfStrings (",",args[15]);
							
							clusterParam.put("initindices", indicies);
						}
						else
						{
							System.err.println("Illegal CLI usage: Provided indicies have to be comma separated");
							return;
						}
					}
				}
				else if (args[14].equals("-initialcentroids"))
				{
					if(ifMatrix)
					{
						if (args[15].contains("|") || args[15].length() == 1)
						{
							String[] ss = args[15].split("|");
							Double[] tempCentroids;
							List<Double[]> listOfCentroids = new ArrayList<Double[]>();
							for (int i = 0; i< ss.length; i++)
							{
								tempCentroids =  getCentroidsAsArrayOfDoubles (",",ss[i]);
								listOfCentroids.add(tempCentroids);
							}
							
							clusterParam.put("initialcentroids", listOfCentroids);
						}
						else
						{
							System.err.println("Illegal CLI usage: Provided centroid points have to be bar separated");
							return;
						}
					}
					else
					{
						if (args[15].contains("|") || args[15].length() == 1)
						{
							String[] ss = args[15].split("|");
							BitSet tempCentroids;
							List<BitSet> listOfCentroids = new ArrayList<BitSet>();
							for (int i = 0; i< ss.length; i++)
							{
								tempCentroids =  getCentroidsAsBitSet(",",ss[i]);
								listOfCentroids.add(tempCentroids);
							}
							
							clusterParam.put("initialcentroids", listOfCentroids);
						}
						else
						{
							System.err.println("Illegal CLI usage: Provided centroid points have to be bar separated");
							return;
						}
					}
				}
				else
				{
					System.err.println("Illegal CLI usage: Last input parameter should be <-initIndices> or <-initialCentroids>");
					return;
				}
			}
			
			
			String FILENAME = "C:/Users/apgalush/Documents/Personal/Harvard/Spring2015/CapStone/HW1CodeData/testdata/"+clusterParam.get("datafile");
			algorithm = new KmeansAlgorithmImpl();
		    
		    if (ifMatrix)
		    {
				MatrixReader instanceM = new MatrixReader();
			    try 
			    {
			        instanceM.loadData(FILENAME);
			    } 
			    catch(IOException ioe)
			    {
			        System.out.println("IOError : " + ioe.getMessage());
			        return;
			    }
			    
			    Double[][] data = instanceM.getNormalizedMatrix();
			    List<String> rowLabels = instanceM.getRowHeaders();
			    result = algorithm.cluster(rowLabels, data, clusterParam);
			    try 
			    {
					result.writeClusterLabels();
				} catch (IOException e) 
			    {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		    else
		    {
		        FingerprintReader instanceF = new FingerprintReader();
		        try 
		        {
		        	instanceF.loadData(FILENAME);
		        }
		        catch(IOException ioe) 
		        {
		        	System.out.println("IOError : " + ioe.getMessage());
		        }
		        
 
				Map<String, BitSet> dataBs = instanceF.getFingerprintMap();
				result = algorithm.cluster(dataBs, clusterParam);
			    try 
			    {
					result.writeClusterLabels();
				} catch (IOException e) 
			    {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		    }

		}
		else if ( (args.length == 12) && args[5].equals("jarvis_patrick") )
		{
			clusterParam.put("algorithm", "jarvis_patrick");
			
			String dataType = null;
			boolean ifMatrix = false;
			if (args[0].toLowerCase().equals("-mtxfile"))
			{
				dataType = "matrix";
				ifMatrix = true;
			}
			else if (args[0].toLowerCase().equals("-fpfile"))
			{
				dataType = "fingerprint";
			}
			else
			{
			     System.err.println("Illegal CLI usage: Choose the correct data type, <-mtxfile> for matrix, <-fpfile> for fingerprint");
			     return;
			}
			
			clusterParam.put("datatype", dataType);
			clusterParam.put("datafile",args[1]);
			
			if (args[2].toLowerCase().equals("-outpath"))
			{
				clusterParam.put("outpath", args[3]);           
			}
			else
			{
				System.err.println("Illegal CLI usage: Second input parameter should be <-outpath>");
				return;
			}
			if (args[6].toLowerCase().equals("-distancemetric"))
			{
				clusterParam.put("distancemetric", args[7]);           
			}
			else
			{
				System.err.println("Illegal CLI usage: Fifth input parameter should be <-distanceMetric>");
				return;
			}
			if (args[8].toLowerCase().equals("-numneighbors"))
			{
				Integer numN = Integer.parseInt(args[9]);
				clusterParam.put("numneighbors", numN);
				
			}
			else
			{
				System.err.println("Illegal CLI usage: Nineth input parameter should be <-numneighbors>");
				return;
			}
			if (args[10].toLowerCase().equals("-commonneighbors"))
			{
				Integer commN = Integer.parseInt(args[11]);
				clusterParam.put("commonneighbors", commN);
				
			}
			else
			{
				System.err.println("Illegal CLI usage: Eleventh input parameter should be <-commonNeighbors>");
				return;
			}
			
			String FILENAME = "C:/Users/apgalush/Documents/Personal/Harvard/Spring2015/CapStone/HW1CodeData/testdata/"+clusterParam.get("datafile");
			algorithm = new JarvisPatrickAlgorithmImpl();
			
			if (ifMatrix)
		    {
				MatrixReader instanceM = new MatrixReader();
			    try 
			    {
			        instanceM.loadData(FILENAME);
			    } 
			    catch(IOException ioe)
			    {
			        System.out.println("IOError : " + ioe.getMessage());
			        return;
			    }
			    
			    Double[][] data = instanceM.getNormalizedMatrix();
			    List<String> rowLabels = instanceM.getRowHeaders();
			    result = algorithm.cluster(rowLabels, data, clusterParam);
			    try 
			    {
					result.writeClusterLabels();
				} catch (IOException e) 
			    {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		    else
		    {
		        FingerprintReader instanceF = new FingerprintReader();
		        try 
		        {
		        	instanceF.loadData(FILENAME);
		        }
		        catch(IOException ioe) 
		        {
		        	System.out.println("IOError : " + ioe.getMessage());
		        }
		        
 
				Map<String, BitSet> dataBs = instanceF.getFingerprintMap();
				result = algorithm.cluster(dataBs, clusterParam);
			    try 
			    {
					result.writeClusterLabels();
				} catch (IOException e) 
			    {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		    }
		}
		
		else
		{
		     System.err.println("Illegal CLI usage: Make sure all parameters and its values have been entered");
		     return;
		}
	    
	}
   
	/**
	 * converts String to List of Integers
	 * @param split
	 * @param s
	 * @return list of integers 
	 */
	private static List<Integer> getIndiciesAsArrayOfInts (String split, String s)
	{
		String[] ss = s.split(split);
		List<Integer> indicies = new ArrayList<Integer>();
		for (int i = 0; i<ss.length; i++)
		{
			indicies.add(0);
			indicies.set(i, Integer.parseInt(ss[i]));
		}
		return indicies;
	}
	
	/**
	 * spits and converts String to List of Strings
	 * @param split
	 * @param s
	 * @return list of strings
	 */
	private static List<String> getIndiciesAsArrayOfStrings (String split, String s)
	{
		String[] ss = s.split(split);
		List<String> indicies = new ArrayList<String>();
		for (int i = 0; i<ss.length; i++)
		{
			indicies.add(null);
			indicies.set(i, ss[i]);
		}
		return indicies;
	}
	
	/**
	 * creates centroid as an array of doubles 
	 * @param split
	 * @param s
	 * @return array of doubles
	 */
	private static Double[] getCentroidsAsArrayOfDoubles (String split, String s)
	{
		String[] ss = s.split(split);
		Double[] centr = new Double[ss.length];
		for (int i = 0; i<ss.length; i++)
		{
			centr[i] = Double.parseDouble(ss[i]);
		}
		return centr;
	}
	
	/**
	 * creates centroid as a BitSet
	 * @param split
	 * @param s
	 * @return BistSet centroid
	 */
	private static BitSet getCentroidsAsBitSet (String split, String s)
	{
		BitSet result  = new BitSet();
		String[] ss = s.split(split);
		for (int i = 0; i<ss.length; i++)
		{
			result.set(Integer.parseInt(ss[i]));
		}
		return result;
	}
}
