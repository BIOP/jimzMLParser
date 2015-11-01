package com.alanmrace.jimzmlparser.imzML;

import com.alanmrace.jimzmlparser.exceptions.CVParamAccessionNotFoundException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLWriteException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;

import com.alanmrace.jimzmlparser.mzML.CVParam;
import com.alanmrace.jimzmlparser.mzML.DoubleCVParam;
import com.alanmrace.jimzmlparser.mzML.FileContent;
import com.alanmrace.jimzmlparser.mzML.MzML;
import com.alanmrace.jimzmlparser.mzML.Scan;
import com.alanmrace.jimzmlparser.mzML.ScanSettings;
import com.alanmrace.jimzmlparser.mzML.ScanSettingsList;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImzML extends MzML implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int width;
	private int height;
	private int depth;
	
	private File ibdFile;
	
	private Spectrum[][][] spectrumGrid;
	
	private static double minMZ = Double.MAX_VALUE;
	private static double maxMZ = Double.MIN_VALUE;
	
	// REMOVED - zero filling code because it caused more issues. Alternative 
	// (and faster) work around is to go to mzML and then to imzML.
	// Default to no zero indexing, however if we find a 0, then turn it on
//	private boolean zeroIndexing = false;
	
	public ImzML(String version) {
		super(version);
	}
	
	public ImzML(ImzML imzML) {
		super(imzML);
	}
	
	
	public synchronized Spectrum getSpectrum(int x, int y) {	
		return getSpectrum(x, y, 1);
	}
	
	public synchronized Spectrum getSpectrum(int x, int y, int z) {
		if(spectrumGrid == null) {
			spectrumGrid = new Spectrum[getWidth()][getHeight()][getDepth()];
			
			for(Spectrum spectrum : getRun().getSpectrumList())
				for(Scan curScan : spectrum.getScanList()) {
					int curX = curScan.getCVParam(Scan.positionXID).getValueAsInteger();
					int curY = curScan.getCVParam(Scan.positionYID).getValueAsInteger();
					int curZ = 1;
					
					CVParam zPosCVParam = curScan.getCVParam(Scan.positionZID);
					
					if(zPosCVParam != null)
						curZ = zPosCVParam.getValueAsInteger();
					
//					if(curY <= 0 || curX <= 0) {
//						zeroIndexing = true;
//						break;
//					}
					
					if(curX-1 < 0 || curX-1 > spectrumGrid.length || curY-1 < 0 || curY-1 > spectrumGrid[0].length || curZ-1 < 0 || curZ-1 > spectrumGrid[0][0].length)
						return null;
					
					spectrumGrid[curX-1][curY-1][curZ-1] = spectrum;
				}
			
//			if(zeroIndexing) {
//				for(int i = 0; i < getWidth(); i++)
//					for(int j = 0; j < getHeight(); j++)
//						spectrumGrid[i][j] = null;
//				
//				for(Spectrum spectrum : getRun().getSpectrumList())
//					for(Scan curScan : spectrum.getScanList()) {
//						int curX = Integer.parseInt(curScan.getCVParam(Scan.positionXID).getValue());
//						int curY = Integer.parseInt(curScan.getCVParam(Scan.positionYID).getValue());
//						
//						spectrumGrid[curX][curY] = spectrum;
//					}
//			}
		}
		
//		if(zeroIndexing) {
//			if(spectrumGrid.length >= 1 && x < spectrumGrid.length && (y) < spectrumGrid[0].length) {
//				return spectrumGrid[x][y];
//			}
//		} else {
			if(spectrumGrid.length >= 1 && (x-1) < spectrumGrid.length && (y-1) < spectrumGrid[0].length && (z-1) < spectrumGrid[0][0].length) {
				return spectrumGrid[x-1][y-1][z-1];
			}
//		}
		
//		for(Spectrum spectrum : getRun().getSpectrumList()) {
//			for(Scan curScan : spectrum.getScanList())
//				if(Integer.parseInt(curScan.getCVParam(Scan.positionXID).getValue()) == x && 
//						Integer.parseInt(curScan.getCVParam(Scan.positionYID).getValue()) == y)
//					return spectrum;
//		}
		
		// Can't find the spectrum
		return null;
	}
	
	public int getWidth() {
		if(width != 0)
			return width;
		
		// Check scan settings first
		ScanSettingsList scanSettingsList = getScanSettingsList();
		
		if(getScanSettingsList() != null) {
			for(ScanSettings scanSettings : scanSettingsList) {
				CVParam maxCountPixelX = scanSettings.getCVParam(ScanSettings.maxCountPixelXID);
				
				if(maxCountPixelX != null)
					width = maxCountPixelX.getValueAsInteger();
			}
		}
		
		// TODO: Nothing in scan settings, so should look at all spectra
		return width;
	}
	
	public int getHeight() {
		if(height != 0)
			return height;
		
		// Check scan settings first
		ScanSettingsList scanSettingsList = getScanSettingsList();
				
		if(getScanSettingsList() != null) {
			for(ScanSettings scanSettings : scanSettingsList) {
				CVParam maxCountPixelY = scanSettings.getCVParam(ScanSettings.maxCountPixelYID);
						
				if(maxCountPixelY != null)
					height = maxCountPixelY.getValueAsInteger();
			}
		}
				
		// TODO: Nothing in scan settings, so should look at all spectra
		return height;
	}
	
	public int getDepth() {
		if(depth != 0)
			return depth;
		
		depth = 1;
		
		SpectrumList spectrumList = getRun().getSpectrumList();
		
		if(spectrumList != null) {
			for(Spectrum spectrum : spectrumList) {
				//for(ScanSettings scanSettings : spectrum.getScanList().getScan(0)) {
					CVParam maxCountPixelZ = spectrum.getScanList().getScan(0).getCVParam(Scan.positionZID);
							
					if(maxCountPixelZ != null) {
						int curDepth = maxCountPixelZ.getValueAsInteger();
						
						if(curDepth > depth)
							depth = curDepth;
					}
				//}
			}
		}
				
		// TODO: Nothing in scan settings, so should look at all spectra
		return depth;
	}
	
	public double getMinimumDetectedmz() {
		if(minMZ != Double.MAX_VALUE)
			return minMZ;		
		
		for(Spectrum spectrum : getRun().getSpectrumList()) {
			CVParam minDetectedMZ = spectrum.getCVParam(Spectrum.lowestObservedmzID);
			
			if(minDetectedMZ == null)
				break;

			double spectrumMinMZ = minDetectedMZ.getValueAsDouble();
			
			if(minMZ > spectrumMinMZ)
				minMZ = spectrumMinMZ;				
		}
		
		if(minMZ == Double.MAX_VALUE)
			return Double.NaN;
					
		return minMZ;
	}
	
	public double getMaximumDetectedmz() {
		if(maxMZ != Double.MIN_VALUE)
			return maxMZ;
		
		for(Spectrum spectrum : getRun().getSpectrumList()) {
			CVParam maxDetectedMZ = spectrum.getCVParam(Spectrum.highestObservedmzID);
			
			if(maxDetectedMZ == null)
				break;
			
			double spectrumMaxMZ = maxDetectedMZ.getValueAsDouble();
			
			if(maxMZ < spectrumMaxMZ)
				maxMZ = spectrumMaxMZ;
				
		}
		
		if(maxMZ == Double.MIN_VALUE)
			return Double.NaN;
		
		return maxMZ;
	}
	
	public File getIBDFile() {
		return ibdFile;
	}
	
//	public void generateSpectralData() throws CVParamAccessionNotFoundException {
//		try {
//			RandomAccessFile raf = new RandomAccessFile(ibdFile, "r");
//			
//			double[] mzArray = null;
//			
//			for(Spectrum spectrum : getRun().getSpectrumList()) {
//				// If the data is processed then we need to get the new m/z list
//				if(isProcessed() || mzArray == null)
//					mzArray = spectrum.getmzArray(raf);
//				
//				if(mzArray == null)
//					continue;
//				
//				double[] intensityArray = spectrum.getIntensityArray(raf);
//				
//				// Calculate the total ion current 
//				double totalIonCurrent = 0;
//				double basePeakMZ = 0;
//				double basePeakIntensity = 0;
//				
//				for(int i = 0; i < intensityArray.length; i++) {
//					totalIonCurrent += intensityArray[i];
//					
//					if(intensityArray[i] > basePeakIntensity) {
//						basePeakIntensity = intensityArray[i];
//						basePeakMZ = mzArray[i];
//					}
//				}
//				
//				spectrum.removeCVParam(Spectrum.totalIonCurrentID);
//				spectrum.addCVParam(new DoubleCVParam(getOBO().getTerm(Spectrum.totalIonCurrentID), totalIonCurrent));
//				
//				spectrum.removeCVParam(Spectrum.basePeakMZID);
//				spectrum.addCVParam(new DoubleCVParam(getOBO().getTerm(Spectrum.basePeakMZID), basePeakMZ));
//				
//				spectrum.removeCVParam(Spectrum.basePeakIntensityID);
//				spectrum.addCVParam(new DoubleCVParam(getOBO().getTerm(Spectrum.basePeakIntensityID), basePeakIntensity));
//				
//				double minMZ = Double.MAX_VALUE;
//				double maxMZ = Double.MIN_VALUE;
//				
//				for(int i = 0; i < mzArray.length; i++) {
//					if(mzArray[i] > maxMZ)
//						maxMZ = mzArray[i];
//					if(mzArray[i] < minMZ)
//						minMZ = mzArray[i];
//				}
//				
//				spectrum.removeCVParam(Spectrum.lowestObservedmzID);
//				spectrum.addCVParam(new DoubleCVParam(getOBO().getTerm(Spectrum.lowestObservedmzID), minMZ));
//				
//				spectrum.removeCVParam(Spectrum.highestObservedmzID);
//				spectrum.addCVParam(new DoubleCVParam(getOBO().getTerm(Spectrum.highestObservedmzID), maxMZ));
//			}
//				
//			raf.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public boolean isProcessed() {
		return getFileDescription().getFileContent().getCVParam(FileContent.binaryTypeProcessedID) != null;
	}
	
	public boolean isContinuous() {
		return getFileDescription().getFileContent().getCVParam(FileContent.binaryTypeContinuousID) != null;
	}
	
	public double[] getBinnedmzList(double minMZ, double maxMZ, double binSize) {
		// Round the min m/z down to the next lowest bin, and the max m/z up to the next bin
		minMZ = minMZ - (minMZ % binSize);
		maxMZ = maxMZ + (binSize - (maxMZ % binSize));
		
		int numBins = (int) Math.ceil((maxMZ - minMZ) / binSize);
		double[] mzs = new double[numBins];		
		
		for(int i = 0; i < numBins; i++) 
			mzs[i] = minMZ + (i * binSize);
		
		return mzs;
	}
	
//	public double[][][] generateDatacube(double minMZ, double maxMZ, double binSize) {
//		double[] mzs = getBinnedmzList(minMZ, maxMZ, binSize);
//		double[][][] datacube = new double[getHeight()][getWidth()][mzs.length];
//		
//		try {
//			RandomAccessFile raf = new RandomAccessFile(ibdFile, "r");
//			
//			double[] mzArray = null;
//			
//			for(Spectrum spectrum : getRun().getSpectrumList()) {
//				// If the data is processed then we need to get the new m/z list
//				if(isProcessed() || mzArray == null)
//					mzArray = spectrum.getmzArray(raf);
//				
//				if(mzArray == null)
//					continue;
//				
//				double[] intensityArray = spectrum.getIntensityArray(raf);
//				
//				int x = spectrum.getScanList().getScan(0).getCVParam(Scan.positionXID).getValueAsInteger() - 1;
//				int y = spectrum.getScanList().getScan(0).getCVParam(Scan.positionYID).getValueAsInteger() - 1;				
//				
//				for(int i = 0; i < mzArray.length; i++) {
//					int index = (int)Math.floor((mzArray[i] - mzs[0]) / binSize);
//					
//					if(index >= 0 && index < mzs.length)
//						datacube[y][x][index] += intensityArray[i];
//				}
//			}
//			
//			raf.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return datacube;
//	}
//	
	public double[][] generateTICImage() {
		double[][] image = new double[getHeight()][getWidth()];

//		RandomAccessFile raf = null;
		
		for(Spectrum spectrum : getRun().getSpectrumList()) {
			int x = spectrum.getScanList().getScan(0).getCVParam(Scan.positionXID).getValueAsInteger() - 1;
			int y = spectrum.getScanList().getScan(0).getCVParam(Scan.positionYID).getValueAsInteger() - 1;				
			
			try {
				double tic = spectrum.getCVParam(Spectrum.totalIonCurrentID).getValueAsDouble();
//				System.out.println(spectrum.getID());
//				System.out.println(image.length + " - " + y);
//				System.out.println("x -> " + image[y].length + " - " + x);
				image[y][x] = tic;
			} catch(NullPointerException ex) {
				try {
//					if(raf == null)
//						raf = new RandomAccessFile(ibdFile, "r");
					
					double[] intensityArray = spectrum.getIntensityArray();				
					
					try {
						for(int i = 0; i < intensityArray.length; i++)
							image[y][x] += intensityArray[i];
					} catch(NullPointerException exception) {
						// Do nothing - no data
					}
				}catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
                            } catch (IOException ex1) {
                                Logger.getLogger(ImzML.class.getName()).log(Level.SEVERE, null, ex1);
                            }
			}
		}
		
//		if(raf != null) {
//			try {
//				raf.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		return image;
	}
//	
//	public double[][] generateBasePeakMZImage() {
//		double[][] image = new double[getHeight()][getWidth()];
//
//		RandomAccessFile raf = null;
//		
//		for(Spectrum spectrum : getRun().getSpectrumList()) {
//			int x = spectrum.getScanList().getScan(0).getCVParam(Scan.positionXID).getValueAsInteger() - 1;
//			int y = spectrum.getScanList().getScan(0).getCVParam(Scan.positionYID).getValueAsInteger() - 1;				
//			
//			try {
//				double tic = spectrum.getCVParam(Spectrum.basePeakMZID).getValueAsDouble();
//				
//				image[y][x] = tic;
//			} catch(NullPointerException ex) {
//				try {
//					if(raf == null)
//						raf = new RandomAccessFile(ibdFile, "r");
//					
//					double[] mzArray = spectrum.getmzArray(raf);
//					double[] intensityArray = spectrum.getIntensityArray(raf);
//					
//					double maxIntensity = 0;					
//					
//					try {
//						for(int i = 0; i < intensityArray.length; i++) {
//							if(intensityArray[i] > maxIntensity) {
//								image[y][x] = mzArray[i];
//								
//								maxIntensity = intensityArray[i];
//							}
//						}
//					} catch(NullPointerException exception) {
//						// Do nothing - no data
//					}
//				}catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		if(raf != null) {
//			try {
//				raf.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return image;
//	}
//	
//	public double[][] generateBasePeakIntensityImage() {
//		double[][] image = new double[getHeight()][getWidth()];
//
//		RandomAccessFile raf = null;
//		
//		for(Spectrum spectrum : getRun().getSpectrumList()) {
//			int x = spectrum.getScanList().getScan(0).getCVParam(Scan.positionXID).getValueAsInteger() - 1;
//			int y = spectrum.getScanList().getScan(0).getCVParam(Scan.positionYID).getValueAsInteger() - 1;				
//			
//			try {
//				double tic = spectrum.getCVParam(Spectrum.basePeakIntensityID).getValueAsDouble();
//				
//				image[y][x] = tic;
//			} catch(NullPointerException ex) {
//				try {
//					if(raf == null)
//						raf = new RandomAccessFile(ibdFile, "r");
//					
//					double[] intensityArray = spectrum.getIntensityArray(raf);				
//					
//					double maxIntensity = 0;
//					
//					try {
//						for(int i = 0; i < intensityArray.length; i++) {
//							if(intensityArray[i] > maxIntensity) {
//								image[y][x] = intensityArray[i];
//								
//								maxIntensity = intensityArray[i];
//							}
//						}
//					} catch(NullPointerException exception) {
//						// Do nothing - no data
//					}
//				}catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		if(raf != null) {
//			try {
//				raf.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return image;
//	}
//	
//	public double[][] generatemzImage(double mz, double width) {
//		double[][] image = new double[getHeight()][getWidth()];
//		
//		try {
//			RandomAccessFile raf = new RandomAccessFile(ibdFile, "r");
//			
//			double lower = mz - width;
//			double upper = mz + width;
//			
//			double[] mzArray = null;
//			
//			for(Spectrum spectrum : getRun().getSpectrumList()) {
//				// If the data is processed then we need to get the new m/z list
//				if(isProcessed() || mzArray == null)
//					mzArray = spectrum.getmzArray(raf);
//				
//				if(mzArray == null)
//					continue;
//				
//				ArrayList<Integer> indicies = new ArrayList<Integer>(); 
//				
//				int x = spectrum.getScanList().getScan(0).getCVParam(Scan.positionXID).getValueAsInteger() - 1;
//				int y = spectrum.getScanList().getScan(0).getCVParam(Scan.positionYID).getValueAsInteger() - 1;				
//				
//				for(int i = 0; i < mzArray.length; i++) {
//				//	System.out.println(mzArray[i] + " >= " + lower + " && " + mzArray[i] + " <= " + upper);
//					if(mzArray[i] >= lower && mzArray[i] <= upper)
//						indicies.add(i);
//				}
//				//System.out.println("x = " + x + ", y = " + y + ", " + mzArray.length + ", " + indicies.size());
//				if(indicies.size() > 0) {
//					double[] intensityArray = spectrum.getIntensityArray(raf);
//					
//					for(int i = 0; i < indicies.size(); i++)
//						image[y][x] += intensityArray[indicies.get(i)];
//				}					
//			}
//			
//			raf.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return image;
//	}
	
	public void setibdFile(File ibdFile) {
		this.ibdFile = ibdFile;
	}
	
	public void write() throws ImzMLWriteException {
		if(ibdFile == null)
			throw new ImzMLWriteException("No ibd file, can't write imzML file.");
		
		write(ibdFile.getAbsolutePath().substring(0, ibdFile.getAbsolutePath().length()-".ibd".length()) + ".imzML");
	}
	
        @Override
	public void write(String filename) throws ImzMLWriteException {
            String encoding = "ISO-8859-1";

            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename), encoding);
                    BufferedWriter output = new BufferedWriter(out)) {
                output.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
                outputXML(output, 0);

                output.flush();
            } catch (IOException e1) {
                throw new ImzMLWriteException("Error writing imzML file " + filename + ". " + e1.getLocalizedMessage());
            }
	}
}