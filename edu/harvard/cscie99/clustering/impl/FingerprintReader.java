/*
 * Reads in the fingerprint and matrix styles of data with or without headers
 */

package edu.harvard.cscie99.clustering.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class FingerprintReader.
 *
 * @author henstock
 */
public class FingerprintReader {
    
    /** The first line count. */
    private final int FIRST_LINE_COUNT = 5;
    
    /** The name delimiter. */
    private String nameDelimiter = "";
    
    /** The fp delimiter. */
    private String fpDelimiter = "";
    
    /** The Constant EPSILON. */
    public final static Double EPSILON = 1E-8;
    
    /** The row headers. */
    private final ArrayList<String> rowHeaders = new ArrayList<String>();
    
    /** The Constant DELIMITERS. */
    public final static String[] DELIMITERS = {",",";","\\t","\\s","\\s+"};

    /** The max fp value. */
    private int maxFpValue = -1;
    
    /** The name2fp. */
    private final Map<String,BitSet> name2fp = new LinkedHashMap<String,BitSet>();
    
    /** The name2int list. */
    private Map<String,List<Integer>> name2intList = null;
    

    /**
     * Main routine to load in matrix format of data from filename.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
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
    
    /**
     * Clear data.
     */
    public void clearData() {
        maxFpValue = -1;
        rowHeaders.clear();
        name2fp.clear();
        name2intList = null;
    }
    
    /**
     * Reads from br until it reads linesToRead or EOF and returns ArrayList of line strings.
     *
     * @param br the br
     * @param linesToRead the lines to read
     * @return the array list
     * @throws IOException Signals that an I/O exception has occurred.
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
    
    
    /**
     * Compute delimiters.
     *
     * @param firstLines the first lines
     */
    protected void computeDelimiters(ArrayList<String> firstLines) {
        fpDelimiter = computeBestFpDelimiter(firstLines);
        nameDelimiter = computeBestNameDelimiter(firstLines, fpDelimiter);
        
    }
    
    /**
     * Returns the delimiter that produces the longest lists for the firstLines.
     *
     * @param firstLines the first lines
     * @return the string
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
     *
     * @param firstLines the first lines
     * @param fpDelimiter the fp delimiter
     * @return the string
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
     * could have a column header.
     *
     * @param firstLines the first lines
     * @param nameDelimiter the name delimiter
     * @return true, if successful
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
    
    
        
    /**
     * Parses the first lines.
     *
     * @param firstLines the first lines
     * @param nameDelimiter the name delimiter
     * @param fpDelimiter the fp delimiter
     * @throws NumberFormatException the number format exception
     */
    protected void parseFirstLines(ArrayList<String> firstLines, String nameDelimiter, String fpDelimiter) throws NumberFormatException {
        for(String line : firstLines) {
            parseLine(line, nameDelimiter, fpDelimiter);
        }
    }
    
    /**
     * Fills the rowHeaders if applicable, increments rowIndex, and adds the ArrayList<Double> to the list.
     *
     * @param line the line
     * @param nameDelimiter the name delimiter
     * @param fpDelimiter the fp delimiter
     * @throws NumberFormatException the number format exception
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

    
    /**
     * Gets the row headers.
     *
     * @return the row headers
     */
    public ArrayList<String> getRowHeaders() {
        return rowHeaders;
    }
    
    /**
     * Returns a matrix of raw values.
     *
     * @param index the index
     * @return the vector
     */
    public BitSet getVector(int index) {
        return name2fp.get(rowHeaders.get(index));
    }
    
    /**
     * Gets the vector.
     *
     * @param name the name
     * @return the vector
     */
    public BitSet getVector(String name) {
        return name2fp.get(name);
    }
    
   
    /**
     * Returns a matrix of raw values.
     *
     * @return the raw matrix
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
     * (Generally don't use this for fingerprints but it's available).
     *
     * @return the normalized matrix
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
    
    /**
     * Gets the bit set.
     *
     * @param name the name
     * @return the bit set
     */
    public BitSet getBitSet(String name) {
        return name2fp.get(name);
    }
    
    /**
     * Main routine to fetch the fingerprints for each row entry by name.
     *
     * @return the fingerprint map
     */
    public Map<String,BitSet> getFingerprintMap() {
        return name2fp;
    }

    /**
     * REturns Map from row labels to an increasing order of integers corresponding
     * to the fingerprints.  If it doesn't exist, it will be created from the
     * name2fp
     *
     * @return the fingerprint map int list
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
     *
     * @param bitSet the bit set
     * @return the list
     */
    protected List<Integer> bitSet2IntList(BitSet bitSet) {
        List<Integer> intList = new ArrayList<Integer>();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            intList.add(i);
        }
        return intList;

    }

}


