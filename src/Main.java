// Main.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Main {
    public static void main(String args[]) {
	Node n1;
	if (false) {
	    Node[] d = new Node[] { new Display("original"), new Display("robertsCross"),
				    new Display("threshold"), new Display("hysteresis"),
				    new Display("thinning"), new Display("pruning"),
				    new Display("label"), new Display("bound")};
	    Node n7 = new Label(d[6]);
	    Node n6 = new Pruning(new Node(d[5], n7));
	    Node n5 = new Thinning(new Node(d[4],n6));
	    Node n4 = new Hysteresis(new Node(d[3],n5));
	    Node n3 = new SearchThreshold(new Node(d[2], n4));
	    Node n2 = new RobertsCross(new Node(d[1], n3));
	    n1 = new Load("../movie/mov3.gz", 139, new Node(d[0], n2));
	} else {
	    n1 = new Load("../movie/mov3.gz", 139, new Node(new Display("original"),
		 new RobertsCross(new SearchThreshold(new Hysteresis(
		 new Thinning(new Pruning(new Label(new Display("label"),new Display("bound")))))))));
	}
	while (true) {
	    n1.run();
	}
    }
}
