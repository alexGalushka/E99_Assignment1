/*
 * Reads in the fingerprint and matrix styles of data with or without headers
 */

package edu.harvard.cscie99.clustering.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author henstock
 */
public class FingerprintReader {
    private final int FIRST_LINE_COUNT = 5;
    private String nameDelimiter = "";
    private String fpDelimiter = "";
    public final static Double EPSILON = 1E-8;
    private final ArrayList<String> rowHeaders = new ArrayList<String>();
    public final static String[] DELIMITERS = {",",";","\\t","\\s","\\s+"};

    private int maxFpValue = -1;
    private final Map<String,BitSet> name2fp = new LinkedHashMap<String,BitSet>();
    private Map<String,List<Integer>> name2intList = null;
    

    /**
     * Main routine to load in matrix format of data from filename
     * @param filename
     * @throws IOException 
     */
    public void loadData(String filename) throws IOException {
        FileReader fr = null;
        BufferedReader br = null;
        clearData();
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);
            ArrayList<String> firstLines = readFirstLines(br, FIRST_LINE_COUNT);
            computeDelimiters(firstLines);
            
            parseFirstLines(firstLines, nameDelimiter, fpDelimiter);
            String line;
            while((line = br.readLine()) != null) {
                parseLine(line, nameDelimiter, fpDelimiter);
            }
        } catch(IOException ioe) {
            System.out.println("IOException reading file " + filename);
            System.out.println(ioe.getMessage());
            throw ioe;
        } finally {
            if(br != null) {
                br.close();
            } 
            if(fr != null) {
                fr.close();
            }
        }
    }
    
    public void clearData() {
        maxFpValue = -1;
        rowHeaders.clear();
        name2fp.clear();
        name2intList = null;
    }
    
    /**
     * Reads from br until it reads linesToRead or EOF and returns ArrayList of line strings
     * @param br
     * @param linesToRead
     * @return
     * @throws IOException 
     */
    protected ArrayList<String> readFirstLines(BufferedReader br, int linesToRead) throws IOException {
        String line;
        int linenum = 0;
        ArrayList<String> firstLines = new ArrayList<String>();
        while(linenum < linesToRead && (line = br.readLine()) != null) {
            firstLines.add(line);
            linenum++;
        }
        return firstLines;
    }
    
    
    protected void computeDelimiters(ArrayList<String> firstLines) {
        fpDelimiter = computeBestFpDelimiter(firstLines);
        nameDelimiter = computeBestNameDelimiter(firstLines, fpDelimiter);
        
    }
    
    /**
     * Returns the delimiter that produces the longest lists for the firstLines
     * @param firstLines
     * @return 
     */
    protected String computeBestFpDelimiter(ArrayList<String> firstLines) {
        int maxCount = 0;
        String bestDelimiter = "";
        for(String delimiter : DELIMITERS) {
            int currCount = 0;
            for(String line : firstLines) {
                String[] tokens = line.split(delimiter);
                currCount += tokens.length;
            }
            if(currCount > maxCount) {
                maxCount = currCount;
                bestDelimiter = delimiter;
            } 
        }
        return bestDelimiter;
    }
    
    /**
     * Searches through firstLInes for an alternate delimiter in case the 
     * separator between the name and the fingerprints is different than the
     * separator between the fingerprints.  Returns the delimiter if it occurs
     * in each line and the 2nd tuple is an integer.  otherwise, it returns
     * fpDelimiter
     * @param firstLines
     * @param fpDelimiter
     * @return 
     */
    protected String computeBestNameDelimiter(ArrayList<String> firstLines, String fpDelimiter) {
        int maxCount = 0;
        String bestDelimiter = "";
        for(String delimiter : DELIMITERS) {
            int currCount = 0;
            for(String line : firstLines) {
                String[] tokens = line.split(fpDelimiter);
                String[] firstTokens = tokens[0].split(delimiter);
                if(firstTokens.length == 2) {
                    try {
                        currCount++;
                    } catch(NumberFormatException nfex) {
                        // ignore
                    }
                }
            }
            if(currCount > maxCount) {
                maxCount = currCount;
                bestDelimiter = delimiter;
            }
        }
        if(maxCount ==firstLines.size()) {
            return bestDelimiter;
        } else {
            return fpDelimiter;
        }
    }
    
    /**
     * Row header is numeric values that are not sequential excluding the first line that
     * could have a column header
     * @param firstLines
     * @param nameDelimiter
     * @return 
     */
    protected boolean determineIfRowHeader(ArrayList<String> firstLines, String nameDelimiter) {
        int linenum = 0;
        boolean isOrdered = true;
        Double prevNumber= 0.0;
        for(String line : firstLines) {
            if(linenum != 0) {
                String firstToken = line.split(nameDelimiter)[0];
                try {
                    Double number = Double.parseDouble(firstToken);
                    if(linenum > 1) {
                        if(Math.abs(Math.round(number) - number) > EPSILON ||
                            Math.abs(number - prevNumber - 1.0) > EPSILON) {
                            isOrdered = false;
                        }
                    }
                    prevNumber = number;
                } catch(NumberFormatException nfex) {
                    // if first row is not a float, it must be a label so return true
                    return true;
                }
            }
            linenum++;
        }
        return isOrdered;
    }
    
    
        
    protected void parseFirstLines(ArrayList<String> firstLines, String nameDelimiter, String fpDelimiter) throws NumberFormatException {
        for(String line : firstLines) {
            parseLine(line, nameDelimiter, fpDelimiter);
        }
    }
    
    /**
     * Fills the rowHeaders if applicable, increments rowIndex, and adds the ArrayList<Double> to the list.
     * @param line
     * @param delimiter
     * @param hasRowHeader
     * @throws NumberFormatException 
     */
    protected void parseLine(String line, String nameDelimiter, String fpDelimiter) throws NumberFormatException {
        String[] tokens = line.split(nameDelimiter);
        BitSet bitSet = new BitSet();
        String rowName = tokens[0];
        String[] fpTokens;
        int firstFpIndex = 0;
        if(nameDelimiter.equals(fpDelimiter)) {
            fpTokens = tokens;
            firstFpIndex = 1;
        } else {
            fpTokens = tokens[1].split(fpDelimiter);
            firstFpIndex = 0;
        }
        int value;
        int numFpTokens = fpTokens.length;
        for (int fpIndex = firstFpIndex; fpIndex < numFpTokens; fpIndex++) {
            try {
                value = Integer.parseInt(fpTokens[fpIndex]);
                bitSet.set(value);
            } catch (NumberFormatException nfex) {
                System.out.println("Illegal value " + fpTokens[1] + " for line " + rowName);
                throw nfex;
            }
        }
        int lastBit = Integer.parseInt(fpTokens[numFpTokens-1]);
        if(lastBit > maxFpValue) {
            maxFpValue = lastBit;
        }
        rowHeaders.add(rowName);
        name2fp.put(rowName, bitSet);
    }

    
    public ArrayList<String> getRowHeaders() {
        return rowHeaders;
    }
    
    /**
     * Returns a matrix of raw values
     */
    public BitSet getVector(int index) {
        return name2fp.get(rowHeaders.get(index));
    }
    
    public BitSet getVector(String name) {
        return name2fp.get(name);
    }
    
   
    /**
     * Returns a matrix of raw values
     */
    public Double[][] getRawMatrix() {
        int numRows = name2fp.size();
        int numCols = maxFpValue+1;
        Double[][] mtx = new Double[numRows][numCols];
        for(int row = 0; row < numRows; row++) {
            BitSet bitSet = name2fp.get(rowHeaders.get(row));
            for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
                mtx[row][i] = 1.0;
            }
        }
        return mtx;
    }
    
    /**
     * Returns a matrix of N(0,1) normalized values
     * (Generally don't use this for fingerprints but it's available)
     */
    public Double[][] getNormalizedMatrix() {
        int numRows = name2fp.size();
        int numCols = maxFpValue+1;
        Double[] means = new Double[numCols];
        Double[] stds  = new Double[numCols];
        for(int col = 0; col < numCols; col++) {
            means[col] = 0.0;
            stds[col] = 0.0;
        }
        for(int row = 0; row < numRows; row++) {
            BitSet bitSet = name2fp.get(rowHeaders.get(row));
            for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
                means[i] += 1.0;
                stds[i]  += 1.0;
            }
        }

        for(int col = 0; col < numCols; col++) {
            means[col] /= numRows;
            stds[col] = Math.sqrt( stds[col]/numRows - means[col]*means[col]);
        }
        
        Double[][] normMtx = new Double[numRows][numCols];
        for(int row = 0; row < numRows; row++) {
            BitSet bitSet = name2fp.get(rowHeaders.get(row));
            for(int col = 0; col < numCols; col++) {
                if(bitSet.get(col)) {
                    normMtx[row][col] = ( 1.0 - means[col] ) / stds[col];
                } else {
                    normMtx[row][col] = ( 0.0 - means[col] ) / stds[col];
                }
            }
        }
        return normMtx;
    }
    
    public BitSet getBitSet(String name) {
        return name2fp.get(name);
    }
    
    /**
     * Main routine to fetch the fingerprints for each row entry by name 
     */
    public Map<String,BitSet> getFingerprintMap() {
        return name2fp;
    }

    /**
     * REturns Map from row labels to an increasing order of integers corresponding
     * to the fingerprints.  If it doesn't exist, it will be created from the
     * name2fp
     */
    public Map<String, List<Integer>> getFingerprintMapIntList() {
        if(name2intList == null) {
            name2intList = new HashMap<String, List<Integer>>();
            for(String key : name2fp.keySet()) {
                BitSet bitSet = name2fp.get(key);
                List<Integer> intList = bitSet2IntList(bitSet);
                name2intList.put(key, intList);
            }
        }
        return name2intList;
    }

    /**
     * Converts bitSet into an increasing ordered list of integers corresponding
     * to the bits set.
     * @param bitSet
     * @return 
     */
    protected List<Integer> bitSet2IntList(BitSet bitSet) {
        List<Integer> intList = new ArrayList<Integer>();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            intList.add(i);
        }
        return intList;

    }

}


