package com.alanmrace.jimzmlparser.mzML;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;

public class ScanSettingsList extends MzMLContent implements Iterable<ScanSettings>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private ArrayList<ScanSettings> scanSettingsList;
	
	public ScanSettingsList(int count) {
		scanSettingsList = new ArrayList<ScanSettings>(count);
	}
	
	public ScanSettingsList(ScanSettingsList scanSettingsList, ReferenceableParamGroupList rpgList, SourceFileList sourceFileList) {
		this.scanSettingsList = new ArrayList<ScanSettings>(scanSettingsList.size());
		
		for(ScanSettings scanSettings : scanSettingsList)
			this.scanSettingsList.add(new ScanSettings(scanSettings, rpgList, sourceFileList));
	}
	
	public int size() {
		return scanSettingsList.size();
	}
	
	public void addScanSettings(ScanSettings scanSettings) {
		scanSettings.setParent(this);
		
		scanSettingsList.add(scanSettings);
	}
	
	public ScanSettings getScanSettings(String id) {
		for(ScanSettings scanSettings : scanSettingsList)
			if(scanSettings.getID().equals(id))
				return scanSettings;
				
		return null;
	}
	
	public ScanSettings getScanSettings(int index) {
		if(index >= scanSettingsList.size())
			return null;
					
		return scanSettingsList.get(index);
	}
	
	public void outputXML(BufferedWriter output, int indent) throws IOException {
		MzMLContent.indent(output, indent);
		output.write("<scanSettingsList");
		output.write(" count=\"" + scanSettingsList.size() + "\"");
		output.write(">\n");
	
		for(ScanSettings scanSettings : scanSettingsList)
			scanSettings.outputXML(output, indent+1);
		
		MzMLContent.indent(output, indent);
		output.write("</scanSettingsList>\n");
	}

	@Override
	public Iterator<ScanSettings> iterator() {
		return scanSettingsList.iterator();
	}
	
	public String toString() {
		return "scanSettingsList";
	}
	
//	@Override
//	public int getChildCount() {
//		// 
//		return size();
//	}
//	
//	@Override
//	public int getIndex(TreeNode childNode) {
//		return scanSettingsList.indexOf(childNode);
//	}
//	
//	@Override
//	public TreeNode getChildAt(int index) {
//		return scanSettingsList.get(index);
//	}
//	
//	@Override
//	public Enumeration<TreeNode> children() {
//		Vector<TreeNode> children = new Vector<TreeNode>();
//		children.addAll(scanSettingsList);
//		
//		return children.elements();
//	}
}