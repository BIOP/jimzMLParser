package com.alanmrace.jimzmlparser.mzML;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;


public class ReferenceableParamGroupList extends MzMLContent  implements  Iterable<ReferenceableParamGroup>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<ReferenceableParamGroup> referenceableParamGroupList;
	
	public ReferenceableParamGroupList(int count) {
		referenceableParamGroupList = new ArrayList<ReferenceableParamGroup>(count);
	}
	
	public ReferenceableParamGroupList(ReferenceableParamGroupList rpgList) {
		referenceableParamGroupList = new ArrayList<ReferenceableParamGroup>(rpgList.size());
		
		for(ReferenceableParamGroup rpg : rpgList)
			referenceableParamGroupList.add(new ReferenceableParamGroup(rpg));
	}
	
	public void addReferenceableParamGroup(ReferenceableParamGroup rpg) {
		boolean exists = false;
		
		for(ReferenceableParamGroup currentRPG : referenceableParamGroupList)
			if(currentRPG.getID().equals(rpg.getID())) {
				exists = true;
				break;
			}	
		
		if(!exists) {
			rpg.setParent(this);
			referenceableParamGroupList.add(rpg);
		}
	}
	
	public void remove(int index) {
		ReferenceableParamGroup removed = referenceableParamGroupList.remove(index);
		removed.setParent(null);
	}
	
	public int size() {
		return referenceableParamGroupList.size();
	}
	
	public ReferenceableParamGroup getReferenceableParamGroup(String id) {
		for(ReferenceableParamGroup rpg : referenceableParamGroupList)
			if(rpg.getID().equals(id))
				return rpg;
		
		return null;
	}
	
	public ReferenceableParamGroup getReferenceableParamGroup(int index) {
		return referenceableParamGroupList.get(index);
	}
	
	public void outputXML(BufferedWriter output, int indent) throws IOException {
		MzMLContent.indent(output, indent);
		output.write("<referenceableParamGroupList");
		output.write(" count=\"" + referenceableParamGroupList.size() + "\"");
		output.write(">\n");
		
		for(ReferenceableParamGroup rpg : referenceableParamGroupList)
			rpg.outputXML(output, indent+1);
		
		MzMLContent.indent(output, indent);
		output.write("</referenceableParamGroupList>\n");
	}

	@Override
	public Iterator<ReferenceableParamGroup> iterator() {
		return referenceableParamGroupList.iterator();
	}
	
//	@Override
//	public int getChildCount() {
//		// 
//		return size();
//	}
//	
//	@Override
//	public int getIndex(TreeNode childNode) {
//		return referenceableParamGroupList.indexOf(childNode);
//	}
//	
//	@Override
//	public TreeNode getChildAt(int index) {
//		return referenceableParamGroupList.get(index);
//	}
//	
//	@Override
//	public Enumeration<TreeNode> children() {
//		Vector<TreeNode> children = new Vector<TreeNode>();
//		children.addAll(referenceableParamGroupList);
//		
//		return children.elements();
//	}
	
	public String toString() {
		return "referenceableParamGroupList";
	}
}