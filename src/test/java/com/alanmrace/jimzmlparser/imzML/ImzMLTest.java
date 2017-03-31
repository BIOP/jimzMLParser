/*
 * 
 */
package com.alanmrace.jimzmlparser.imzml;

import com.alanmrace.jimzmlparser.exceptions.ImzMLParseException;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Alan Race
 */
public class ImzMLTest {
    
    private static final Logger logger = Logger.getLogger(ImzMLTest.class.getName());
    
    public static final String TEST_RESOURCE = "/MatrixTests_N2.imzML"; // "/2012_5_2_medium(120502,20h18m).wiff"; 
    
    private ImzML instance;
    
       
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Setting up MzMLToImzMLConverterTest");
        assertNotNull("Test file missing", ImzMLTest.class.getResource(TEST_RESOURCE));
    }

    @Before
    public void setUp() {
        try {
            instance = ImzMLHandler.parseimzML(ImzMLTest.class.getResource(TEST_RESOURCE).getPath());
        } catch (ImzMLParseException ex) {
            Logger.getLogger(ImzMLTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("ImzMLParseException: " + ex);
        }
    }

    /**
     * Test of getFullmzList method, of class ImzML.
     */
    @Test
    public void testGetFullmzList() {
        System.out.println("getFullmzList");
        
        double[] result = instance.getFullmzList();
        
        assertNotNull(result);
        
        assertEquals(50, result[0], 1);
        assertEquals(2000, result[result.length-1], 1);
        
        
//        assertArrayEquals(expResult, result, 0.01);
    }

    /**
     * Test of getSpectrum method, of class ImzML.
     */
    @Test
    public void testGetSpectrumIntInt() {
        System.out.println("getSpectrum");
        int x = 0;
        int y = 0;
        
        Spectrum expResult = null;
        Spectrum result = instance.getSpectrum(x, y);
        
        assertEquals(expResult, result);
        
        result = instance.getSpectrum(1, 1);
        
        assertNotNull(result);
    }

    /**
     * Test of getSpectrum method, of class ImzML.
     */
    @Test
    public void testGetSpectrum3args() {
        System.out.println("getSpectrum");
        
        int x = 0;
        int y = 0;
        int z = 0;
        
        Spectrum expResult = null;
        Spectrum result = instance.getSpectrum(x, y, z);
        assertEquals(expResult, result);
        
        result = instance.getSpectrum(1, 1, 1);
        
        assertNotNull(result);
    }

    /**
     * Test of getWidth method, of class ImzML.
     */
    @Test
    public void testGetWidth() {
        System.out.println("getWidth");
        
        int expResult = 1;
        int result = instance.getWidth();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getHeight method, of class ImzML.
     */
    @Test
    public void testGetHeight() {
        System.out.println("getHeight");
        
        int expResult = 4;
        int result = instance.getHeight();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDepth method, of class ImzML.
     */
    @Test
    public void testGetDepth() {
        System.out.println("getDepth");
        
        int expResult = 1;
        int result = instance.getDepth();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMinimumDetectedmz method, of class ImzML.
     */
    @Test
    @Ignore
    public void testGetMinimumDetectedmz() {
        System.out.println("getMinimumDetectedmz");
        
        double expResult = 0.0;
        double result = instance.getMinimumDetectedmz();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getMaximumDetectedmz method, of class ImzML.
     */
    @Test
    @Ignore
    public void testGetMaximumDetectedmz() {
        System.out.println("getMaximumDetectedmz");
        
        double expResult = 0.0;
        double result = instance.getMaximumDetectedmz();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getIBDFile method, of class ImzML.
     */
    @Test
    public void testGetIBDFile() {
        System.out.println("getIBDFile");
        
        File result = instance.getIBDFile();
        
        assertNotNull(result);
    }

    /**
     * Test of isProcessed method, of class ImzML.
     */
    @Test
    public void testIsProcessed() {
        System.out.println("isProcessed");
        
        boolean expResult = true;
        boolean result = instance.isProcessed();
        assertEquals(expResult, result);
    }

    /**
     * Test of isContinuous method, of class ImzML.
     */
    @Test
    public void testIsContinuous() {
        System.out.println("isContinuous");
        
        boolean expResult = false;
        boolean result = instance.isContinuous();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBinnedmzList method, of class ImzML.
     */
    @Test
    @Ignore
    public void testGetBinnedmzList() {
        System.out.println("getBinnedmzList");
        double minMZ = 0.0;
        double maxMZ = 0.0;
        double binSize = 0.0;
        ImzML instance = null;
        double[] expResult = null;
        double[] result = instance.getBinnedmzList(minMZ, maxMZ, binSize);
        assertArrayEquals(expResult, result, 0.01);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateTICImage method, of class ImzML.
     */
    @Test
    @Ignore
    public void testGenerateTICImage() {
        System.out.println("generateTICImage");
        ImzML instance = null;
        double[][] expResult = null;
        double[][] result = instance.generateTICImage();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setibdFile method, of class ImzML.
     */
    @Test
    @Ignore
    public void testSetibdFile() {
        System.out.println("setibdFile");
        File ibdFile = null;
        ImzML instance = null;
        instance.setibdFile(ibdFile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class ImzML.
     */
    @Test
    @Ignore
    public void testWrite_0args() throws Exception {
        System.out.println("write");
        ImzML instance = null;
        instance.write();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class ImzML.
     */
    @Test
    @Ignore
    public void testWrite_String() throws Exception {
        System.out.println("write");
        String filename = "";
        ImzML instance = null;
        instance.write(filename);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
