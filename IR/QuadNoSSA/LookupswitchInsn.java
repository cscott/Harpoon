package harpoon.IR.QuadNoSSA;

import java.util.*;
import java.io.*;


class LookupswitchInsn extends NInsn{

    NLabel myLabels[];
    int myKeys[];
    NLabel myDefault;

    LookupswitchInsn (NLabel def, int[] keys, NLabel[] labels){
	myLabels = labels; 
	myKeys = keys;
	myDefault = def;
    }

    void writeInsn (PrintWriter out, Hashtable indexTable) throws IOException{
	out.println (";before things start");
	out.print ("lookupswitch\n");

	int numberKeys = myKeys.length;
	out.println (";before switch");
	for (int i= 0; i < numberKeys; i++){
	    out.println ("; printing key " + i);
	    out.print (myKeys[i] + " : " + myLabels[i].toString() + "\n");
	}
	out.println (";before default");
	out.println ("default : " + myDefault.toString());
	out.println (";after default");
	out.println (";after things end");
    }

}
