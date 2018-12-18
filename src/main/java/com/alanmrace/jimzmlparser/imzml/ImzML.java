package com.alanmrace.jimzmlparser.imzml;

import com.alanmrace.jimzmlparser.exceptions.FatalParseIssue;
import com.alanmrace.jimzmlparser.exceptions.ImzMLParseException;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.alanmrace.jimzmlparser.mzml.CVParam;
import com.alanmrace.jimzmlparser.mzml.DoubleCVParam;
import com.alanmrace.jimzmlparser.mzml.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzml.FileContent;
import com.alanmrace.jimzmlparser.mzml.IntegerCVParam;
import com.alanmrace.jimzmlparser.mzml.MzML;
import com.alanmrace.jimzmlparser.mzml.Scan;
import com.alanmrace.jimzmlparser.mzml.ScanSettings;
import com.alanmrace.jimzmlparser.mzml.ScanSettingsList;
import com.alanmrace.jimzmlparser.mzml.Software;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.util.HexHelper;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class capturing {@literal <mzML>} tag within an imzML file, extending 
 * {@link MzML} to mass spectrometry imaging specific methods.
 * 
 * @author Alan Race
 */
public class ImzML extends MzML implements MassSpectrometryImagingData {

    /**
     * Serialisation version ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ImzML.class.getName());

    /**
     * Width of mass spectrometry image in pixels.
     */
    private int width;
    
    /**
     * Height of mass spectrometry image in pixels.
     */
    private int height;
    
    /**
     * Depth of mass spectrometry image in pixels.
     */
    private int depth;

    /**
     * IBD file containing the binary data for imzML.
     */
    private File ibdFile;

    /**
     * List of all possible m/z values associated with this dataset.
     */
    private double[] fullmzList;
    
    /**
     * Total ion count image of the entire ImzML file.
     */
    private double[][] ticImage;

    /**
     * 3-D Array of Spectrum instances with each Spectrum in the location in the 
     * array as defined by the x, y, and z coordinates of the Spectrum.
     */
    private Spectrum[][][] spectrumGrid;

    /**
     * Array of the pixel locations that have an associated Spectrum.
     */
    private PixelLocation[] pixelLocations;
    
    /**
     * Minimum m/z value detected within this ImzML file.
     */
    private double minMZ = Double.MAX_VALUE;
    
    /**
     * Maximum m/z value detected within this ImzML file.
     */
    private double maxMZ = Double.MIN_VALUE;

    /**
     * Dimensionality of the ImzML file (normal imaging = 2, 3D data = 3).
     */
    private int dimensionality = -1;

    /**
     * Create basic ImzML with specified version.
     * 
     * @param version ImzML version
     */
    public ImzML(String version) {
        super(version);
    }

    /**
     * Copy constructor, create new ImzML from supplied ImzML instance.
     * 
     * @param imzML ImzML to make copy of
     */
    public ImzML(ImzML imzML) {
        super(imzML);
    }

    /**
     * Copy constructor, create new ImzML from supplied MzML instance.
     * 
     * @param mzML MzML to make copy of
     */
    public ImzML(MzML mzML) {
        super(mzML);
    }
    
    public PixelLocation[] getPixelList() {
        // Useful for protein id script
        if(pixelLocations == null) {
            pixelLocations = new PixelLocation[getRun().getSpectrumList().size()];
            int index = 0;
            
            for(Spectrum spectrum : getRun().getSpectrumList()) {
                pixelLocations[index++] = spectrum.getPixelLocation();
            }
        }
        
        return pixelLocations;
    }
    
    @Override
    public double[] getFullmzList() {
        LOGGER.entering(ImzML.class.getName(), "getFullmzList");

        if (fullmzList == null) {
            Software imzMLConverter = this.getSoftwareList().getSoftware("imzMLConverter");

            LOGGER.log(Level.FINE, "Software found: {0}", imzMLConverter);

            if (imzMLConverter != null) {
                CVParam offsetParam = imzMLConverter.getCVParam(BinaryDataArray.EXTERNAL_OFFSET_ID);
                CVParam encodedLengthParam = imzMLConverter.getCVParam(BinaryDataArray.EXTERNAL_ENCODED_LENGTH_ID);

                LOGGER.log(Level.FINE, "Found CVParams: {0}, {1}", new Object[]{offsetParam, encodedLengthParam});

                if (offsetParam != null) {
                    try {
                        byte[] fullmzListBytes = dataStorage.getData(offsetParam.getValueAsLong(), encodedLengthParam.getValueAsInteger());

                        LOGGER.log(Level.FINE, "Read in {0} bytes", fullmzListBytes.length);

                        int numBytesPerDouble = Double.SIZE / Byte.SIZE;
                        // The below is only available in Java 1.8
                        //Double.BYTES

                        fullmzList = new double[fullmzListBytes.length / numBytesPerDouble];

                        ByteBuffer buffer = ByteBuffer.wrap(fullmzListBytes);
                        DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();

                        if(fullmzList.length > 0) {
                            LOGGER.log(Level.FINE, "First double is {0}", doubleBuffer.get(0));

                            for (int i = 0; i < fullmzList.length; i++) {
                                fullmzList[i] = doubleBuffer.get(i);
                            }

                            LOGGER.log(Level.FINE, "First double in array is {0}", fullmzList[0]);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        return fullmzList;
    }

    @Override
    public int getSpatialDimensionality() {
        int spatialDimensionality = 0; 
        spatialDimensionality += (getWidth() > 1) ? 1 : 0;
        spatialDimensionality += (getHeight() > 1) ? 1 : 0;
        spatialDimensionality += (getDepth() > 1) ? 1 : 0;

        return spatialDimensionality;
    }

    @Override
    public int getDimensionality() {
        // Determine the dimensionality of the data
        if (dimensionality <= 0) {
            dimensionality = 1; // At least 1 dimension from m/z 
            dimensionality += getSpatialDimensionality();

            // Check if we have MS/MS or mobility by looking to see if we have two or more spectra with same x, y location
            Spectrum spectrum1 = getRun().getSpectrumList().getSpectrum(0);
            Spectrum spectrum2 = getRun().getSpectrumList().getSpectrum(1);

            if (spectrum1.getPixelLocation().equals(spectrum2.getPixelLocation())) {
                dimensionality++;
            }
        }

        return dimensionality;
    }

    @Override
    public int getNumberOfSpectraPerPixel() {
        Spectrum firstSpectrum = getRun().getSpectrumList().getSpectrum(0);
        PixelLocation location = firstSpectrum.getPixelLocation();
        
        int numberOfSpectraPerPixel = 1;
        
        int numSpectra = getRun().getSpectrumList().size();
        SpectrumList spectrumList = getRun().getSpectrumList();
        
        for(int i = 1; i < numSpectra; i++) {
            if(spectrumList.getSpectrum(i).getPixelLocation().equals(location))
                numberOfSpectraPerPixel++;
        }
        
        return numberOfSpectraPerPixel;
    }
    
    @Override
    public synchronized Spectrum getSpectrum(int x, int y) {
        return getSpectrum(x, y, 1);
    }

    @Override
    public synchronized Spectrum getSpectrum(int x, int y, int z) {
        if (spectrumGrid == null) {
            spectrumGrid = new Spectrum[getWidth()][getHeight()][getDepth()];

            for (Spectrum spectrum : getRun().getSpectrumList()) {
                for (Scan curScan : spectrum.getScanList()) {
                    int curX = curScan.getCVParam(Scan.POSITION_X_ID).getValueAsInteger();
                    int curY = curScan.getCVParam(Scan.POSITION_Y_ID).getValueAsInteger();
                    int curZ = 1;

                    CVParam zPosCVParam = curScan.getCVParam(Scan.POSITION_Z_ID);

                    if (zPosCVParam != null) {
                        curZ = zPosCVParam.getValueAsInteger();
                    }

                    if (curX - 1 < 0 || curX - 1 > spectrumGrid.length || curY - 1 < 0 || curY - 1 > spectrumGrid[0].length || curZ - 1 < 0 || curZ - 1 > spectrumGrid[0][0].length) {
                        return null;
                    }

                    spectrumGrid[curX - 1][curY - 1][curZ - 1] = spectrum;
                }
            }
        }

        if (spectrumGrid.length >= 1 && (x - 1) < spectrumGrid.length && x >= 1 &&
                (y - 1) < spectrumGrid[0].length && y >= 1 &&
                (z - 1) < spectrumGrid[0][0].length && z >= 1) {
            return spectrumGrid[x - 1][y - 1][z - 1];
        }
        
        return null;
    }

    @Override
    public int getWidth() {
        if (width != 0) {
            return width;
        }

        // Check scan settings first
        ScanSettingsList scanSettingsList = getScanSettingsList();

        if (getScanSettingsList() != null) {
            for (ScanSettings scanSettings : scanSettingsList) {
                CVParam maxCountPixelX = scanSettings.getCVParam(ScanSettings.MAX_COUNT_PIXEL_X_ID);

                if (maxCountPixelX != null) {
                    width = maxCountPixelX.getValueAsInteger();
                }
            }
        }

        // TODO: Nothing in scan settings, so should look at all spectra
        return width;
    }

    @Override
    public int getHeight() {
        if (height != 0) {
            return height;
        }

        // Check scan settings first
        ScanSettingsList scanSettingsList = getScanSettingsList();

        if (getScanSettingsList() != null) {
            for (ScanSettings scanSettings : scanSettingsList) {
                CVParam maxCountPixelY = scanSettings.getCVParam(ScanSettings.MAX_COUNT_PIXEL_Y_ID);

                if (maxCountPixelY != null) {
                    height = maxCountPixelY.getValueAsInteger();
                }
            }
        }

        // TODO: Nothing in scan settings, so should look at all spectra
        return height;
    }

    @Override
    public int getDepth() {
        if (depth != 0) {
            return depth;
        }

        depth = 1;

        SpectrumList spectrumList = getRun().getSpectrumList();

        if (spectrumList != null) {
            for (Spectrum spectrum : spectrumList) {
                CVParam maxCountPixelZ = spectrum.getScanList().get(0).getCVParam(Scan.POSITION_Z_ID);

                if (maxCountPixelZ != null) {
                    int curDepth = maxCountPixelZ.getValueAsInteger();

                    if (curDepth > depth) {
                        depth = curDepth;
                    }
                }
            }
        }

        return depth;
    }

    @Override
    public double getMinimumDetectedmz() {
        if (minMZ != Double.MAX_VALUE) {
            return minMZ;
        }

        for (Spectrum spectrum : getRun().getSpectrumList()) {
            CVParam minDetectedMZ = spectrum.getCVParam(Spectrum.LOWEST_OBSERVED_MZ_ID);

            if (minDetectedMZ == null) {
                break;
            }

            double spectrumMinMZ = minDetectedMZ.getValueAsDouble();

            if (minMZ > spectrumMinMZ) {
                minMZ = spectrumMinMZ;
            }
        }

        if (minMZ == Double.MAX_VALUE) {
            return Double.NaN;
        }

        return minMZ;
    }

    @Override
    public double getMaximumDetectedmz() {
        if (maxMZ != Double.MIN_VALUE) {
            return maxMZ;
        }

        for (Spectrum spectrum : getRun().getSpectrumList()) {
            CVParam maxDetectedMZ = spectrum.getCVParam(Spectrum.HIGHEST_OBSERVED_MZ_ID);

            if (maxDetectedMZ == null) {
                break;
            }

            double spectrumMaxMZ = maxDetectedMZ.getValueAsDouble();

            if (maxMZ < spectrumMaxMZ) {
                maxMZ = spectrumMaxMZ;
            }

        }

        if (maxMZ == Double.MIN_VALUE) {
            return Double.NaN;
        }

        return maxMZ;
    }

    /**
     * Get the IBD file containing the binary data.
     * 
     * @return IBD data file
     */
    public File getIBDFile() {
        return ibdFile;
    }

    @Override
    public boolean isProcessed() {
        return getFileDescription().getFileContent().getCVParam(FileContent.BINARY_TYPE_PROCESSED_ID) != null;
    }

    @Override
    public boolean isContinuous() {
        return getFileDescription().getFileContent().getCVParam(FileContent.BINARY_TYPE_CONTINUOUS_ID) != null;
    }

    /**
     * Generate an m/z axis starting at minMZ, ending at maxMZ and with each bin 
     * size being binSize. If the number of bins that fit within the specified range
     * with the specified size is not exact, then the number of bins will be rounded up.
     * 
     * @param minMZ     Minimum m/z
     * @param maxMZ     Maximum m/z
     * @param binSize   Bin size (delta m/z)
     * @return          Generated m/z axis as double[]
     */
    public static double[] getBinnedmzList(double minMZ, double maxMZ, double binSize) {
        // Round the min m/z down to the next lowest bin, and the max m/z up to the next bin
        double minMZRounded = minMZ - (minMZ % binSize);
        double maxMZRounded = maxMZ + (binSize - (maxMZ % binSize));

        int numBins = (int) Math.ceil((maxMZRounded - minMZRounded) / binSize);
        double[] mzs = new double[numBins];

        for (int i = 0; i < numBins; i++) {
            mzs[i] = minMZRounded + (i * binSize);
        }

        return mzs;
    }

    @Override
    // TODO: Add in exception for failure to generate image - this should be caught to avoid 
    // issues where ArrayIndexOutOfBounds is thrown then the specified image dimensions is not
    // correct for the data
    public double[][] generateTICImage() {
        if(ticImage == null) {
            ticImage = new double[getHeight()][getWidth()];

            if(getRun().getSpectrumList() != null) {
                for (Spectrum spectrum : getRun().getSpectrumList()) {
                    int x = spectrum.getScanList().get(0).getCVParam(Scan.POSITION_X_ID).getValueAsInteger() - 1;
                    int y = spectrum.getScanList().get(0).getCVParam(Scan.POSITION_Y_ID).getValueAsInteger() - 1;
                    
                    
                    try {
                        double tic = spectrum.getCVParam(Spectrum.TOTAL_ION_CURRENT_ID).getValueAsDouble();

                        ticImage[y][x] = tic;
                    } catch (NullPointerException ex) {
                        try {
                            double[] intensityArray = spectrum.getIntensityArray();

                            if(intensityArray != null) {
                                for (double intensity : intensityArray) {
                                    ticImage[y][x] += intensity;
                                }
                                
                                spectrum.addCVParam(new DoubleCVParam(OBO.getOBO().getTerm(Spectrum.TOTAL_ION_CURRENT_ID), ticImage[y][x]));
                            }
                        } catch (FileNotFoundException e) {
                            Logger.getLogger(ImzML.class.getName()).log(Level.SEVERE, null, e);
                        } catch (IOException ex1) {
                            Logger.getLogger(ImzML.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                }
            }
        }
        
        return ticImage;
    }

    /**
     * Set the IBD file containing the data of the ImzML.
     * 
     * @param ibdFile IBD file
     */

    public void setibdFile(File ibdFile) {
        this.ibdFile = ibdFile;
    }
    
    /**
     * Calculate the checksum of a given file using a given algorithm. Examples 
     * include SHA-1 and MD5.
     * 
     * @param filename  File on which to calculate the checksum
     * @param algorithm Hash algorithm to perform
     * @return          Hash of file
     * @throws ImzMLParseException  Issue opening IBD file
     */
    public static String calculateChecksum(String filename, String algorithm) throws ImzMLParseException {
        byte[] hash;

        try {
            DataInputStream dataStream = new DataInputStream(new FileInputStream(filename));
            
            try {
                byte[] buffer = new byte[1024 * 1024];
                int bytesRead;

                MessageDigest md = MessageDigest.getInstance(algorithm);

                do {
                    bytesRead = dataStream.read(buffer);

                    if (bytesRead > 0) {
                        md.update(buffer, 0, bytesRead);
                    }
                } while (bytesRead > 0);

                hash = md.digest();
            } finally {
                dataStream.close();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ImzMLParseException(new FatalParseIssue("Generation of " + algorithm + " hash failed. No " + algorithm + " algorithm. " + e.getLocalizedMessage(), e.getLocalizedMessage()), e);
        } catch (FileNotFoundException e2) {
            throw new ImzMLParseException(new FatalParseIssue("Could not open file " + filename, e2.getLocalizedMessage()), e2);
        } catch (IOException e) {
            throw new ImzMLParseException(new FatalParseIssue("Failed generating " + algorithm + " hash. Failed to read data from " + filename + e.getMessage(), e.getLocalizedMessage()), e);
        }

        return HexHelper.byteArrayToHexString(hash);
    }
    
    /**
     * Calculate the SHA-1 hash of the specified file.
     * 
     * @param filename  Filename to hash
     * @return          SHA-1 hash of the file
     * @throws ImzMLParseException  Issue with opening the IBD file
     */
    public static String calculateSHA1(String filename) throws ImzMLParseException {
        return calculateChecksum(filename, "SHA-1");
    }

    /**
     * Calculate the MD5 hash of the specified file.
     * 
     * @param filename  Filename to hash
     * @return          MD5 hash of the file
     * @throws ImzMLParseException  Issue with opening the IBD file
     */
    public static String calculateMD5(String filename) throws ImzMLParseException {
        return calculateChecksum(filename, "MD5");
    }
    
    /**
     * Create default valid ImzML. Calls {@link ImzML#createDefaults(com.alanmrace.jimzmlparser.mzml.MzML)}.
     *  
     * @return Default ImzML instance
     */
    public static ImzML create() {
        ImzML imzML = new ImzML(CURRENT_VERSION);
        
        createDefaults(imzML);
        
        return imzML;
    }
    
    /**
     * Adds default MS imaging parameters values to supplied mzML. Assumes flyback,
     * top down, horizontal, left to right line scan.
     * 
     * @param mzML MzML to add parameters to.
     */
    protected static void createDefaults(MzML mzML) {
        MzML.createDefaults(mzML);
        
        // Add in scan setting defaults
        ScanSettingsList scanSettingsList = new ScanSettingsList(1);
        ScanSettings scanSettings = new ScanSettings("globalScanSettings");
        scanSettingsList.add(scanSettings);
        
        scanSettings.addCVParam(new EmptyCVParam(OBO.getOBO().getTerm(ScanSettings.SCAN_PATTERN_FLYBACK_ID)));
        scanSettings.addCVParam(new EmptyCVParam(OBO.getOBO().getTerm(ScanSettings.SCAN_DIRECTION_TOP_DOWN_ID)));
        scanSettings.addCVParam(new EmptyCVParam(OBO.getOBO().getTerm(ScanSettings.SCAN_TYPE_HORIZONTAL_ID)));
        scanSettings.addCVParam(new EmptyCVParam(OBO.getOBO().getTerm(ScanSettings.LINE_SCAN_DIRECTION_LEFT_RIGHT_ID)));
        
        scanSettings.addCVParam(new IntegerCVParam(OBO.getOBO().getTerm(ScanSettings.MAX_COUNT_PIXEL_X_ID), 0));
        scanSettings.addCVParam(new IntegerCVParam(OBO.getOBO().getTerm(ScanSettings.MAX_COUNT_PIXEL_Y_ID), 0));
        
        mzML.setScanSettingsList(scanSettingsList);
    }
}
