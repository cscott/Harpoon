package harpoon.IR.QuadNoSSA;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;

import java.io.*;
import java.util.*;

class MultiarrayInsn extends NInsn {

    HClass myClass;
    int myDimensions;

    MultiarrayInsn (HClass hclass, int dimensions){
	myClass = hclass;
	myDimensions = dimensions;
    }

    void writeInsn(PrintWriter out, Hashtable indexTable) throws IOException{
	out.print ("multianewarray " + myClass.getDescriptor() + " " + 
			myDimensions + "\n");
    }
}
