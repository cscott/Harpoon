package harpoon.IR.QuadNoSSA;

import java.io.*;
import java.util.*;

class NLabel extends NInsn {

    String myLabel;

    NLabel (String label){
	myLabel = label;
    }

    public String toString (){
	return myLabel;
    }

    void writeInsn (PrintWriter out, Hashtable indexTable) throws IOException{
	out.print (myLabel + ":\n");
    }
}
