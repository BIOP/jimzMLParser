package com.alanmrace.jimzmlparser.mzML;

import com.alanmrace.jimzmlparser.util.XMLHelper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

public class Precursor extends MzMLContent  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// Attributes
	private String externalSpectrumID; 	// Optional
	private SourceFile sourceFileRef; 	// Optional
	private String spectrumRef;		// Optional
	
	// Sub-elements
	private IsolationWindow isolationWindow;
	private SelectedIonList selectedIonList;
	private Activation activation;
	
	public Precursor() {
		super();
	}
	
	public Precursor(Precursor precursor, ReferenceableParamGroupList rpgList, SourceFileList sourceFileList) {
		this.externalSpectrumID = precursor.externalSpectrumID;
		this.spectrumRef = precursor.spectrumRef;
		
		if(precursor.sourceFileRef != null && sourceFileList != null) {
			for(SourceFile sourceFile : sourceFileList) {
				if(precursor.sourceFileRef.getID().equals(sourceFile.getID())) {
					this.sourceFileRef = sourceFile;
					
					break;
				}
			}
		}
		
		if(precursor.isolationWindow != null)
			isolationWindow = new IsolationWindow(precursor.isolationWindow, rpgList);
		if(precursor.selectedIonList != null)
			selectedIonList = new SelectedIonList(precursor.selectedIonList, rpgList);
		if(precursor.activation != null)
			activation = new Activation(precursor.activation, rpgList);
	}
	
	public void setExternalSpectrumID(String externalSpectrumID) {
		this.externalSpectrumID = externalSpectrumID;
	}
	
	public void setSourceFileRef(SourceFile sourceFileRef) {
		this.sourceFileRef = sourceFileRef;
	}
	
	public void setSpectrumRef(String spectrumRef) {
		this.spectrumRef = spectrumRef;
	}
	
	public void setIsolationWindow(IsolationWindow isolationWindow) {
		isolationWindow.setParent(this);
		
		this.isolationWindow = isolationWindow;
	}
	
	public IsolationWindow getIsolationWindow() {
		return isolationWindow;
	}
	
	public void setSelectedIonList(SelectedIonList selectedIonList) {
		selectedIonList.setParent(this);
		
		this.selectedIonList = selectedIonList;
	}
	
	public SelectedIonList getSelectedIonList() {
		return selectedIonList;
	}
	
	public void setActivation(Activation activation) {
		activation.setParent(this);
		
		this.activation = activation;
	}
	
	public Activation getActivation() {
		return activation;
	}
	
        @Override
	public void outputXML(BufferedWriter output, int indent) throws IOException {
		MzMLContent.indent(output, indent);
		output.write("<precursor");
		if(externalSpectrumID != null)
			output.write(" externalSpectrumID=\"" + XMLHelper.ensureSafeXML(externalSpectrumID) + "\"");
		if(sourceFileRef != null)
			output.write(" sourceFileRef=\"" + XMLHelper.ensureSafeXML(sourceFileRef.getID()) + "\"");
		if(spectrumRef != null)
			output.write(" spectrumRef=\"" + XMLHelper.ensureSafeXML(spectrumRef) + "\"");
		output.write(">\n");
		
		if(isolationWindow != null)
			isolationWindow.outputXML(output, indent+1);
		
		if(selectedIonList != null && selectedIonList.size() > 0)
			selectedIonList.outputXML(output, indent+1);
		
		activation.outputXML(output, indent+1);
		
		MzMLContent.indent(output, indent);
		output.write("</precursor>\n");
	}
	
        @Override
	public String toString() {
		return "precursor: " + 
			((spectrumRef != null && !spectrumRef.isEmpty())? " spectrumRef=\"" + spectrumRef + "\"" : "") + 
			((externalSpectrumID != null && !externalSpectrumID.isEmpty())? " externalSpectrumID=\"" + externalSpectrumID + "\"" : "") + 
			((sourceFileRef != null)? " sourceFileRef=\"" + sourceFileRef.getID() + "\"" : "");
	}
	
	private int getAdditionalChildrenCount() {
		int additionalChildren = ((isolationWindow != null)? 1 : 0) + 
				((selectedIonList != null)? 1 : 0) + 
				((activation != null)? 1 : 0);
		
		return additionalChildren;
	}
	
//	@Override
//	public int getChildCount() {
//		return super.getChildCount() + getAdditionalChildrenCount();
//	}
//	
//	@Override
//	public Enumeration<TreeNode> children() {
//		Vector<TreeNode> children = new Vector<TreeNode>();		
//		Enumeration<TreeNode> superChildren = super.children();
//		
//		while(superChildren.hasMoreElements())
//			children.add(superChildren.nextElement());
//		
//		if(isolationWindow != null)
//			children.add(isolationWindow);
//		if(selectedIonList != null)
//			children.add(selectedIonList);
//		if(activation != null)
//			children.add(activation);
//		
//		return children.elements();
//	}
//	
//	@Override
//	public TreeNode getChildAt(int index) {
//		if(index < super.getChildCount()) {
//			return super.getChildAt(index);
//		} else if(index < getChildCount()) {			
//			int counter = super.getChildCount();
//			
//			if(isolationWindow != null) {
//				if(counter == index)
//					return isolationWindow;
//				
//				counter++;
//			}
//			if(selectedIonList != null) {
//				if(counter == index)
//					return selectedIonList;
//				
//				counter++;
//			}
//			if(activation != null) {
//				if(counter == index)
//					return activation;
//				
//				counter++;
//			}
//		}
//		
//		return null;
//	}
//	
//	@Override
//	public int getIndex(TreeNode childNode) {
//		int counter = super.getChildCount();
//			
//		if(childNode instanceof IsolationWindow)
//			return counter;
//		
//		if(isolationWindow != null)
//			counter++;
//		
//		if(childNode instanceof SelectedIonList)
//			return counter;
//		
//		if(selectedIonList != null)
//			counter++;
//		
//		if(childNode instanceof Activation)
//			return counter;
//		
//		if(activation != null)
//			counter++;
//		
//		return super.getIndex(childNode);
//	}
		
}