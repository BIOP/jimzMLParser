package com.alanmrace.jimzmlparser.mzML;

import com.alanmrace.jimzmlparser.exceptions.InvalidXPathException;
import com.alanmrace.jimzmlparser.exceptions.UnfollowableXPathException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class FileContent extends MzMLContent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static String dataFileContentID = "MS:1000524";		// Required child (1+)
    public static String spectrumRepresentationID = "MS:1000525";		// Optional child (1)

    public static String binaryTypeID = "IMS:1000003";
    public static String binaryTypeContinuousID = "IMS:1000030";
    public static String binaryTypeProcessedID = "IMS:1000031";

    public static String ibdIdentificationID = "IMS:1000008";
    public static String uuidIdntificationID = "IMS:1000080";

    public static String ibdChecksumID = "IMS:1000009";
    public static String md5ChecksumID = "IMS:1000090";
    public static String sha1ChecksumID = "IMS:1000091";

    public static String ibdFileID = "IMS:1000007";

    public FileContent() {
        super();
    }

    public FileContent(FileContent fileContent, ReferenceableParamGroupList rpgList) {
        super(fileContent, rpgList);
    }

    @Override
    public ArrayList<OBOTermInclusion> getListOfRequiredCVParams() {
        ArrayList<OBOTermInclusion> required = new ArrayList<OBOTermInclusion>();
        required.add(new OBOTermInclusion(dataFileContentID, false, true, false));
        required.add(new OBOTermInclusion(binaryTypeID, true, true, false));
        required.add(new OBOTermInclusion(ibdChecksumID, true, true, false));
        required.add(new OBOTermInclusion(ibdIdentificationID, true, true, false));

        return required;
    }

    @Override
    public ArrayList<OBOTermInclusion> getListOfOptionalCVParams() {
        ArrayList<OBOTermInclusion> optional = new ArrayList<OBOTermInclusion>();
        optional.add(new OBOTermInclusion(spectrumRepresentationID, true, true, false));
        optional.add(new OBOTermInclusion(ibdFileID, true, true, false));

        return optional;
    }
    
    @Override
    public void outputXML(BufferedWriter output, int indent) throws IOException {
        MzMLContent.indent(output, indent);
        output.write("<fileContent");
        output.write(">\n");

        super.outputXML(output, indent + 1);

        MzMLContent.indent(output, indent);
        output.write("</fileContent>\n");
    }

    public void addDataFileContent(CVParam dataFileContent) {
        this.addCVParam(dataFileContent);
    }

    public ArrayList<CVParam> getDataFileContents() {
        return getChildrenOf(dataFileContentID);
    }

    @Override
    public String toString() {
        return "fileContent";
    }

    @Override
    public String getTagName() {
        return "fileContent";
    }
}
