package com.alanmrace.jimzmlparser.mzML;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;

public class SampleList extends MzMLContent implements Iterable<Sample>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private ArrayList<Sample> sampleList;
	
	public SampleList(int count) {
		sampleList = new ArrayList<Sample>(count);
	}
	
	public SampleList(SampleList sampleList, ReferenceableParamGroupList rpgList) {
		this.sampleList = new ArrayList<Sample>(sampleList.size());
		
		for(Sample sample : sampleList)
			this.sampleList.add(new Sample(sample, rpgList));
	}
	
	public int size() {
		return sampleList.size();
	}
	
	public Sample getSample(String id) {
		for(Sample sample : sampleList)
			if(sample.getID().equals(id))
				return sample;
				
		return null;
	}
	
	public Sample getSample(int index) {
		return sampleList.get(index);
	}
	
	public void addSample(Sample sample) {
		sample.setParent(this);
		
		sampleList.add(sample);
	}
	
	public void removeSample(int index) {
		Sample removed = sampleList.remove(index);
		removed.setParent(null);
	}
	
	public void outputXML(BufferedWriter output, int indent) throws IOException {
		MzMLContent.indent(output, indent);
		output.write("<sampleList");
		output.write(" count=\"" + sampleList.size() + "\"");
		output.write(">\n");
		
		for(Sample sample : sampleList)
			sample.outputXML(output, indent+1);
		
		MzMLContent.indent(output, indent);
		output.write("</sampleList>\n");
	}

	@Override
	public Iterator<Sample> iterator() {
		return sampleList.iterator();
	}
	
	public String toString() {
		return "sampleList";
	}
	
//	@Override
//	public int getChildCount() {
//		// 
//		return size();
//	}
//	
//	@Override
//	public int getIndex(TreeNode childNode) {
//		return sampleList.indexOf(childNode);
//	}
//	
//	@Override
//	public TreeNode getChildAt(int index) {
//		return sampleList.get(index);
//	}
//	
//	@Override
//	public Enumeration<TreeNode> children() {
//		Vector<TreeNode> children = new Vector<TreeNode>();
//		children.addAll(sampleList);
//		
//		return children.elements();
//	}
}