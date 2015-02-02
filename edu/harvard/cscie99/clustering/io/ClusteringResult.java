package edu.harvard.cscie99.clustering.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class ClusteringResult 
{
    private Map<String,Integer> dataToclusterMap;
    
    public ClusteringResult( Map<String,Integer> dataToclusterMap )
    {
    	this.dataToclusterMap = dataToclusterMap;
    }
    
    /**
     * 
     * @param outputFilename
     * @throws IOException
     */
	public void writeClusterLabels(String outputFilename) throws IOException
	{
		BufferedWriter writer = null;
	    try {
	        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	        String name = "ClusteringResultKMeans_" + time + ".txt";
	        File clusteringResultFile = new File(name);
	        writer = new BufferedWriter(new FileWriter(clusteringResultFile));
	        Iterator<String> it = dataToclusterMap.keySet().iterator();
	        while (it.hasNext()) 
	        {
	            String line = it.next();
	            String entryLine = line + " -> " + dataToclusterMap.get(line);
	            writer.write(entryLine);
	        }
	    } catch (Exception e)
	    {
	        e.printStackTrace();
	    } finally
	    {
	       if (writer != null)
	       {
	           writer.close();
	       }
	    }
	}
	
}
