/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlparser.mzml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Abstract class implementing the basic functionality for a tag in MzML which has
 * child tags.
 *  
 * @author Alan Race
 */
public abstract class MzMLContentWithChildren extends MzMLContent implements HasChildren {

    protected void outputXMLContent(RandomAccessFile raf, BufferedWriter output, int indent) throws IOException {
        ArrayList<MzMLTag> children = new ArrayList<MzMLTag>();
        
        addChildrenToCollection(children);
        
        for(MzMLTag child : children) {
            child.outputXML(raf, output, indent);
        }
    }
       
    @Override
    public void outputXML(RandomAccessFile raf, BufferedWriter output, int indent) throws IOException {
        String attributeText = getXMLAttributeText();
        
        MzMLContent.indent(output, indent);
        output.write("<" + getTagName());
        
        if(attributeText != null && !attributeText.isEmpty())
            output.write(" " + attributeText);
        
        output.write(">\n");

        outputXMLContent(raf, output, indent + 1);

        MzMLContent.indent(output, indent);
        output.write("</" + getTagName() + ">\n");
    }
}