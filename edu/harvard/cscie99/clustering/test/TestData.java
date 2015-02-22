package edu.harvard.cscie99.clustering.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.cscie99.clustering.impl.FingerprintReader;

public class TestData
{

	private String filename= "bbb2_daylight.fp.txt";
	private FingerprintReader fingerprintReader;
	
	public TestData()
	{
		fingerprintReader = new FingerprintReader();
        try 
        {
			fingerprintReader.loadData(filename);
		} catch (IOException e)
        {
			System.out.print("File cannot be opened");
		}
	}
	
	public Map<String, BitSet> createSimpleDataBs()
	{
		Map<String, BitSet> result = new HashMap<>();
        result.put ("Alcohol",fingerprintReader.getBitSet("Alcohol"));
        result.put ("Isopropyl",fingerprintReader.getBitSet("Isopropyl"));
        result.put ("Glutamine",fingerprintReader.getBitSet("Glutamine"));
        result.put ("Mannitol",fingerprintReader.getBitSet("Mannitol"));
        result.put ("Paraldehyde",fingerprintReader.getBitSet("Paraldehyde"));
        result.put ("Putrescine",fingerprintReader.getBitSet("Putrescine"));
        result.put ("Spermidine",fingerprintReader.getBitSet("Spermidine"));
        result.put ("Spermine",fingerprintReader.getBitSet("Spermine"));
        result.put ("Hydrocortisone",fingerprintReader.getBitSet("Hydrocortisone"));
        result.put ("Histamine",fingerprintReader.getBitSet("Histamine"));
		
        return result;
		
	}
	
	public Double[][] createSimpleDataMatrix()
	{
		Double[][] matrix = {{1d,1d,1d,1d},
				             {-1d,-1d,-1d,-1d},
				             {1d,2d,3d,4d},
				             {0d,1d,2d,3d},
				             {7d,8d,9d,10d},
				             {5d,6d,1d,7d},
				             {10d,11d,12d,10d}};
		return matrix;
		
	}
	
	public List<String> getRowLabels()
	{
		String[] elements = {"A","B","C","D","E","F","G"};
		List<String> rowLabels = new ArrayList<>(Arrays.asList(elements));
		return rowLabels;
	}
	
}
