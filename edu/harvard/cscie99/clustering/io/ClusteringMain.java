package edu.harvard.cscie99.clustering.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClusteringMain
{
	final Map<String, List<String>> clusterParams = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		Map<String, Object> clusterParam = new HashMap<String,Object>();
		
		if(args.length == 1)
		{
			if (args[0].toLowerCase().equals("-help"))
			{
				String s = "***Clustering Algorithm Tool User Manual***\n\n"
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
						+ "seventh input parameter <-initIndices> or <-initialCentroids>:\n"
						+ "it is allowed to specify centroids, if <-initIndices>, specify number of centroids, e.g. 2,3,4\n"
						+ "if <-initialCentroids> actual points centroids points have to be specified, e.g. 0.2,0.4,0.1,1|0.2,0.4,0.1,1";
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
			clusterParam.put("dataType", dataType);
			clusterParam.put("dataFile",args[1]);
			
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
				clusterParam.put("distanceMetric", args[9]);           
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
					clusterParam.put("initialMethod", args[11]);           
				}
				else if(args[11].toLowerCase().equals("initialindices"))
				{
					clusterParam.put("initialMethod", args[11]);
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
				System.err.println("Illegal CLI usage: Fifth input parameter should be <-initialMethod>");
				return;
			}
			
			if (ifToProvideCentroids)
			{
				if (args[12].toLowerCase().equals("-initindices"))
				{
					if (args[13].contains(",") || args[13].length() == 1)
					{
						List<Integer> indicies = getIndiciesAsArrayOfInts (",",args[13]);
						
						clusterParam.put("initIndices", indicies);
					}
					else
					{
						System.err.println("Illegal CLI usage: Provided indicies have to be comma separated");
						return;
					}
				}
				else if (args[12].equals("-initialcentroids"))
				{
					if (args[13].contains("|") || args[13].length() == 1)
					{
						String[] ss = args[13].split("|");
						Double[] tempCentroids;
						List<Double[]> listOfCentroids = new ArrayList<Double[]>();
						for (int i = 0; i< ss.length; i++)
						{
							tempCentroids =  getCentroidsAsArrayOfDoubles (",",ss[i]);
							listOfCentroids.add(tempCentroids);
						}
						
						clusterParam.put("initialCentroids", listOfCentroids);
					}
					else
					{
						System.err.println("Illegal CLI usage: Provided centroid points have to be bar separated");
						return;
					}
				}
				else
				{
					System.err.println("Illegal CLI usage: Last input parameter should be <-initIndices> or <-initialCentroids>");
					return;
				}
			}
			
			
			String FILENAME = "C:/Users/apgalush/Documents/Personal/Harvard/Spring2015/CapStone/HW1CodeData/testdata/"+clusterParam.get("dataFile");
	
			
		    ClusteringResult result;
		    ClusteringAlgorithm algorithm = new KmeansAlgorithmImpl();
		    
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
			    //List<String> rowLabels = createLables(data.length);
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
		        
		
		        
				Map<String, BitSet> bs = instanceF.getFingerprintMap();
		        BitSet ace = bs.get("Acebutolol");
		        BitSet alco = bs.get("Alcohol");
		        
		        BitSet new1 = new BitSet();

		        new1.or(alco);
		        new1.or(ace);
		        alco.xor(ace);
		   
		        
		        
		        Integer iniCountAce = ace.cardinality();
		        Integer iniCountalco = ace.cardinality();
		        
		        ace.and(alco);
		        Integer common = ace.cardinality();
		        
		        
		        
		        System.out.println("$$$$$$$$$$$$$");
		        Integer leftAce = iniCountAce - common;
		        Integer leftalco = iniCountalco - common;
		        Integer sum = leftAce + leftalco;
		        
		        System.out.println(leftAce);
		        System.out.println("*************************************************************");
		        
		        //ace.


		        System.out.println("Confirm with Cardinal");
		        System.out.println(alco.cardinality());
		        
		        
		        ace.and(alco);
		        //ace.

		       System.out.println(ace.cardinality());
		        System.out.println("*************************************************************");

		        //cardinace = ace.cardinality();
		        
		        @SuppressWarnings("unused")
		        
				Double[][] m = instanceF.getRawMatrix();
		    }

		}
		else if ( (args.length == 6) && args[5].equals("patrick_javis") )
		{
			
		}
		
		else
		{
		     System.err.println("Illegal CLI usage: Make sure all 7 parameters and its values have been entered");
		     return;
		}
	    
	}
   
	/*
	private static List<String> createLables (int length)
	{
	    List<String> rowLabels = new ArrayList<String>();
	    String label = "inputLabel";
	    for (Integer i = 1; i<= length; i++)
	    {
	    	rowLabels.add(label+i.toString());
	    }
	    return rowLabels;
	}
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
}
