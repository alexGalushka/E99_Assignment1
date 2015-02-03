/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.cscie99.clustering.io;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author henstock
 */
public class FingerprintReaderTest {
    //private String FILENAME = "./testdata/bbb2_daylight.fp.txt";
	private String FILENAME = "C:/Users/apgalush/Documents/Personal/Harvard/Spring2015/CapStone/HW1CodeData/testdata/bbb2_daylight.fp.txt";
    public FingerprintReaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of loadData method, of class FingerprintReader.
     */
    @Test
    public void testLoadData() throws Exception {
        System.out.println("loadData");
        String filename = FILENAME;
        FingerprintReader instance = new FingerprintReader();
        instance.loadData(filename);
        assertEquals(80, instance.getRowHeaders().size());
        assertEquals("Acebutolol", instance.getRowHeaders().get(0));
        assertEquals("Zonisamide", instance.getRowHeaders().get(79));
    }

    /**
     * Test of clearData method, of class FingerprintReader.
     */
    @Test
    public void testClearData() {
        System.out.println("clearData");
        FingerprintReader instance = new FingerprintReader();
        try {
            instance.loadData(FILENAME);
        } catch(IOException ioe) {
            fail("Could not load file " + FILENAME + "\n" + ioe.getMessage());
        }
        instance.clearData();
        assertEquals(0, instance.getRowHeaders().size());
    }

    /**
     * Test of computeBestFpDelimiter method, of class FingerprintReader.
     */
    @Test
    public void testComputeBestFpDelimiter() {
        System.out.println("computeBestFpDelimiter");
        ArrayList<String> firstLines = new ArrayList<String>();
        firstLines.add("name1\t16,20,27,28,44");
        firstLines.add("name2\t20,30,55,78");
        firstLines.add("name3\t278,541,611,808,832,1372,1428");
        FingerprintReader instance = new FingerprintReader();
        String expResult = ",";
        String result = instance.computeBestFpDelimiter(firstLines);
        assertEquals(expResult, result);
    }

    /**
     * Test of computeBestNameDelimiter method, of class FingerprintReader.
     */
    @Test
    public void testComputeBestNameDelimiter() {
        System.out.println("computeBestNameDelimiter");
        ArrayList<String> firstLines = new ArrayList<String>();
        firstLines.add("name1\t16,20,27,28,44");
        firstLines.add("name2\t20,30,55,78");
        firstLines.add("name3\t278,541,611,808,832,1372,1428");
        String fpDelimiter = ",";
        FingerprintReader instance = new FingerprintReader();
        String expResult = "\\t";
        String result = instance.computeBestNameDelimiter(firstLines, fpDelimiter);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRowHeaders method, of class FingerprintReader.
     */
    @Test
    public void testGetRowHeaders() {
        System.out.println("getRowHeaders");
        FingerprintReader instance = new FingerprintReader();
        try {
            instance.loadData(FILENAME);
            ArrayList<String> rowHeaders = instance.getRowHeaders();
            assertEquals(80, rowHeaders.size());
            assertEquals("Acebutolol", rowHeaders.get(0));
            assertEquals("Zonisamide", rowHeaders.get(79));
        } catch(IOException ioe) {
            fail("Could not load file " + FILENAME + "\n" + ioe.getMessage());
        }
    }


    /**
     * Test of getRawMatrix method, of class FingerprintReader.
     */
    @Test
    public void testGetRawMatrix() {
        System.out.println("getRawMatrix");
        FingerprintReader instance = new FingerprintReader();
        try {
            instance.loadData(FILENAME);
            Double[][] result = instance.getRawMatrix();
            assertEquals(80, result.length);
            assertEquals(2045, result[0].length);
            assertEquals(1, result[0][16], 1E-5);
            assertEquals(1, result[0][20], 1E-5);
            assertEquals(1, result[0][27], 1E-5);
            assertEquals(1, result[0][2044], 1E-5);
            assertEquals(0, result[0][2043], 1E-5);
            assertEquals(1, result[79][28], 1E-5);
            assertEquals(1, result[79][44], 1E-5);
            assertEquals(1, result[79][72], 1E-5);
            assertEquals(1, result[79][2044], 1E-5);
            assertEquals(0, result[79][2043], 1E-5);

        } catch(IOException ioe) {
            fail("Could not load file " + FILENAME + "\n" + ioe.getMessage());
        }        @SuppressWarnings("unused")
		Double[][] expResult = null;
    }
    
}
