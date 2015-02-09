package edu.harvard.cscie99.clustering.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClusteringResult 
{
    private Map<String,Integer> dataToclusterMap;
    private String outputFilename;
    private String algorithm;
    
    public ClusteringResult( Map<String,Integer> dataToclusterMap, String outputFilename, String algorithm )
    {
    	this.dataToclusterMap = dataToclusterMap;
    	this.outputFilename = outputFilename;
    	this.algorithm = algorithm; 
    }
    
	/**
	 * 
	 * @param outputPath
	 * @throws IOException
	 */
	public void writeClusterLabels(String outputPath) throws IOException
	{

        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String name = outputPath+"\\ClusteringResult"+algorithm+"_" + time + ".txt";
        writeFile(name);

	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void writeClusterLabels() throws IOException
	{

        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String name = outputFilename+"\\ClusteringResult"+algorithm+"_" + time + ".txt";
        writeFile(name);

	}
	
	private void writeFile(String name) throws IOException
	{
		BufferedWriter writer = null;
	    try {

	        File clusteringResultFile = new File(name);
	        writer = new BufferedWriter(new FileWriter(clusteringResultFile));
	        
	        NumberAwareStringComparator compar = new NumberAwareStringComparator();
	               
	        List<String> sortedKeys=new ArrayList<String>(dataToclusterMap.keySet());
	        
	        Collections.sort(sortedKeys,compar);
	        
	        for (String label : sortedKeys) 
	        {
	        	Integer spaceCharachters = 30;
	        	spaceCharachters = spaceCharachters -  label.length();
	        	String space = new String(new char[spaceCharachters]).replace('\0', ' ');
	            String entryLine = label + space + dataToclusterMap.get(label)+"\n";
	            writer.write(entryLine);
	        }
	    } 
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    } 
	    finally
	    {
	       if (writer != null)
	       {
	           writer.close();
	       }
	    }
	}
	
}
