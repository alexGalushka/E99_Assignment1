/*
 * Reads in the fingerprint and matrix styles of data with or without headers
 */

package edu.harvard.cscie99.clustering.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class MatrixReader.
 *
 * @author henstock
 */
public class MatrixReader {
    
    /** The first line count. */
    private int FIRST_LINE_COUNT = 5;
    
    /** The Constant EPSILON. */
    public final static Double EPSILON = 1E-8;
    
    /** The col headers. */
    private final ArrayList<String> colHeaders = new ArrayList<String>();
    
    /** The row headers. */
    private final ArrayList<String> rowHeaders = new ArrayList<String>();
    
    /** The Constant DELIMITERS. */
    public final static String[] DELIMITERS = {",",";","\\t","\\s","\\s+"};
    
    /** The row index. */
    private int rowIndex = 0;
    
    /** The matrix data. */
    private final ArrayList<ArrayList<Double>> matrixData = new ArrayList<ArrayList<Double>>();
    
    /** The num rows. */
    private int numRows = 0;
    
    /** The num cols. */
    private int numCols = 0;
    

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
            String delimiter = determineDelimiter(firstLines);
            boolean hasRowHeader = determineIfRowHeader(firstLines, delimiter);
            parseFirstLines(firstLines, delimiter, hasRowHeader);
            String line;
            while((line = br.readLine()) != null) {
                parseLine(line, delimiter, hasRowHeader);
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
        numRows = 0;
        numCols = 0;
        colHeaders.clear();;
        rowHeaders.clear();
        matrixData.clear();
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
     * Returns the delimiter in DELIMITERS that divides the line most conistentl
     * across each of the lines.
     *
     * @param lines the lines
     * @return the string
     */
    protected String determineDelimiter(ArrayList<String> lines) {
        int maxCnt = -1;
        int maxNumTokens = -1;
        String maxTestDelimiter = null;
        for(String testDelimiter : DELIMITERS) {
            HashMap<Integer,Integer> numToken2count = new HashMap<Integer, Integer>();
            for(String line : lines) {
                int numTokens = line.split(testDelimiter).length;
                Integer currCount = numToken2count.get(numTokens);
                if(currCount == null) {
                    numToken2count.put(numTokens, 1);
                } else {
                    numToken2count.put(numTokens, currCount+1);
                    if(currCount+1 > maxCnt || (currCount+1 == maxCnt && numTokens > maxNumTokens)) {
                        maxCnt = currCount+1;
                        maxTestDelimiter = testDelimiter;
                        maxNumTokens = numTokens;
                    }
                }
            }
        }
        return maxTestDelimiter;
    }
    
    
    /**
     * Row header is numeric values that are not sequential excluding the first line that
     * could have a column header.
     *
     * @param firstLines the first lines
     * @param delimiter the delimiter
     * @return true, if successful
     */
    protected boolean determineIfRowHeader(ArrayList<String> firstLines, String delimiter) {
        int linenum = 0;
        boolean isOrdered = true;
        Double prevNumber= 0.0;
        for(String line : firstLines) {
            if(linenum != 0) {
                String firstToken = line.split(delimiter)[0];
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
     * @param delimiter the delimiter
     * @param hasRowHeader the has row header
     * @throws NumberFormatException the number format exception
     */
    protected void parseFirstLines(ArrayList<String> firstLines, String delimiter, boolean hasRowHeader) throws NumberFormatException {
        int linenum = 0;
        for(String line : firstLines) {
            if(linenum == 0) {
                boolean firstLineIsHeader = parseFirstLineAsHeader(firstLines.get(0).split(delimiter), hasRowHeader);
                if( ! firstLineIsHeader) {
                    parseLine(line, delimiter, hasRowHeader);
                }
            } else {
                parseLine(line, delimiter, hasRowHeader);
            }
            linenum++;
        }
    }
    
    /**
     * Examines he tokens and converts it into a header if it's ordered or
     * text.  It fills rowHeaders regardless with the text or col#.  If the
     * first line contains data values, it returns false else true
     *
     * @param tokens the tokens
     * @param hasRowHeader the has row header
     * @return true, if successful
     */
    protected boolean parseFirstLineAsHeader(String[] tokens, boolean hasRowHeader) {
        int startCol = 0;
        int numTokens = tokens.length;
        if(hasRowHeader) {
            startCol = 1;
        }
        boolean allTokensNumeric = true;
        boolean isOrdered     = true;
        Double prevValue = 0.0;
        for(int col = startCol; col < numTokens && allTokensNumeric && isOrdered; col++) {
            try {
                Double value = Double.parseDouble(tokens[col]);
                if( ! isInteger(value)) {
                    isOrdered = false;
                } 
                if(! oneLarger(value, prevValue)) {
                    if(col == startCol && Math.abs(value) > EPSILON) {
                        isOrdered = false;
                    } else {
                        isOrdered = false;
                    }
                }
                prevValue = value;
            } catch(NumberFormatException nfe) {
                allTokensNumeric = false;
            }
        }
        colHeaders.clear();
        if(! allTokensNumeric) {
            for(int col = startCol; col < numTokens; col++) {
                colHeaders.add(tokens[col]);
            }
            numCols = colHeaders.size();
            return true;
        } else if(isOrdered) {
            for(int col = startCol; col < numTokens; col++) {
                colHeaders.add("col" + col);
            }
            numCols = colHeaders.size();
            return isOrdered;
        } else {
            return false;
        }
    }


    /**
     * Checks if is integer.
     *
     * @param value the value
     * @return true, if is integer
     */
    private boolean isInteger(Double value) {
        return Math.abs(value - Math.round(value)) < EPSILON;
    }
    
    /**
     * Returns true if value = prevValue + 1.
     *
     * @param value the value
     * @param prevValue the prev value
     * @return true, if successful
     */
    private boolean oneLarger(Double value, Double prevValue) {
        return Math.abs(value - prevValue - 1) < EPSILON;
    }
    
    /**
     * Fills the rowHeaders if applicable, increments rowIndex, and adds the ArrayList<Double> to the list.
     *
     * @param line the line
     * @param delimiter the delimiter
     * @param hasRowHeader the has row header
     * @throws NumberFormatException the number format exception
     */
    protected void parseLine(String line, String delimiter, boolean hasRowHeader) throws NumberFormatException {
        String[] tokens = line.split(delimiter);
        @SuppressWarnings("unused")
		int numTokens = tokens.length;
        int startIndex = 0;
        String currRowHeader;
        if(hasRowHeader) {
            currRowHeader = tokens[0];
            startIndex = 1;
        } else {
            currRowHeader = "row " + rowIndex;
        }
        rowHeaders.add(currRowHeader);
        ArrayList<Double> rowData = new ArrayList<Double>();
        for(int index = startIndex; index < tokens.length; index++) {
            try {
                rowData.add(Double.parseDouble(tokens[index]));
            } catch(NumberFormatException nfex) {
                System.out.println("Invalid number: " + tokens[index] + " in row " + currRowHeader);
                throw nfex;
            }
        }
        numRows = rowHeaders.size();
        matrixData.add(rowData);
        rowIndex++;
    }

    
    /**
     * Gets the column headers.
     *
     * @return the column headers
     */
    public ArrayList<String> getColumnHeaders() {
        return colHeaders;
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
     * @return the raw matrix
     */
    public Double[][] getRawMatrix() {
        Double[][] mtx = new Double[numRows][numCols];
        int row = 0;
        for(ArrayList<Double> rowValues : matrixData) {
            for(int col = 0; col < numCols; col++) {
                mtx[row][col] = rowValues.get(col);
            }
            row++;
        }
        return mtx;
    }
    
    /**
     * Returns a matrix of N(0,1) normalized values.
     *
     * @return the normalized matrix
     */
    public Double[][] getNormalizedMatrix() {
        Double[] means = new Double[numCols];
        Double[] stds  = new Double[numCols];
        for(int col = 0; col < numCols; col++) {
            means[col] = 0.0;
            stds[col] = 0.0;
        }
        int row = 0;
        for(ArrayList<Double> rowValues : matrixData) {
            for(int col = 0; col < numCols; col++) {
                Double value = rowValues.get(col);
                means[col] += value;
                stds[col] += (value*value);
            }
            row++;
        }
        
        for(int col = 0; col < numCols; col++) {
            means[col] /= numRows;
            stds[col] = Math.sqrt( stds[col]/(numRows-1) - means[col]*means[col]*numRows/(numRows-1));
        }
        
        Double[][] normMtx = new Double[numRows][numCols];
        row = 0;
        for(ArrayList<Double> rowValues : matrixData) {
            for(int col = 0; col < numCols; col++) {
                normMtx[row][col] = (rowValues.get(col) - means[col] ) / stds[col];
            }
            row++;
        }
        return normMtx;
    }
}
