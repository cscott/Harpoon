// Main.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;
import imagerec.util.*;

/**
 * This is the main program.  Look at this code to get an idea of 
 * how the image recognition pipeline is put together.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Main {

    /** The entry point to the main program.
     *  
     *  @param args Should include parameters for the servers and clients, if necessary.
     */
    public static void main(String args[]) {
	Node n13 = (args.length>1)?new Alert(args):null;
	Node n12 = new RangeFind(n13);
	Node n11 = new Command(Command.RETRIEVED_IMAGE, null);
	Node n10 = new Cache(5, null, n11);
	Node n9 = new Circle(new Display("out"), new Command(Command.GET_IMAGE, n10));
	n11.setLeft(n9);
	Node n8 = new Match(new Node(n12, new Command(Command.NONE, n9)));
	Node n7 = new Label(null, n8);
	Node n6 = new Pruning(null);
	Node n5 = new Thinning(n6);
	Node n4 = new Hysteresis(n5);
	Node n3 = new Thresholding(n4);
	Node n2 = new RobertsCross(n3);
	n10.setLeft(n2);

	if (args.length>1) {
	    (new ATR(args, n10)).run();
	} else {
	    Node n1 = new Load("movie/movie.jar", "mov3.gz", 139, null);
	    while (true) {
		n1.run();
	    }
	}

//  	(new Load("dbase/db.gz", 237, 
//  		  new RobertsCross(new RangeThreshold(new Node(new Display("orig"),
//  							       new Hysteresis(new Thinning(new Pruning(new Display("threshold"))))))))).run();
//  	(new Load("dbase/db.gz", 237,
//  		  new RobertsCross(new Node(new Hough(new Node(new Display("r vs. t"),
//  							       new HoughThreshold(new Node(new Display("thresh"),
//  											   new HoughThin(new Node(new Display("thin"),
//  														  new DeHough(new Display("mapped")))))))),
//  					    new Display("original"))))).run();


    }
}
