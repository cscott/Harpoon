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
	boolean headless = true;
	if (true) {
	    Node alert = (args.length>2)?(new Alert(args)):null;
	    Node n2;
	    if (headless) {
		n2 = new Timer(true, false, new RobertsCross(new Thresholding(new Label(null, 
					       new RangeFind(new Timer(false, true, alert))))));
	    } else {
		Node n7 = new Command(Command.GET_IMAGE, null);
		Node n6 = new Circle(new Display("identified"), n7);
		Node n5 = new Node(new RangeFind(alert), n6);
		Node n4 = new RobertsCross(new Thresholding(new Label(null, n5)));
		Node n3 = new Cache(2, new Copy(n4), new Command(Command.RETRIEVED_IMAGE, n6));
		n2 = new Node(new Display("original"), n3);
		n7.setLeft(n3);
	    }
	    Node n1 = (args.length>2)?(Node)(new ATR(args, n2)):
		(Node)(new Load("movie/tank.jar", "tank.gz", 533, n2));
	    while (true) {
	       n1.run();
	    }
	}

	if (false) {
	    Node n27 = new Display("possible");
	    Node n26 = new Circle(n27, null);
	    Node n25 = new Command(Command.RETRIEVED_IMAGE, n26);
	    Node n24 = new Command(Command.GET_IMAGE, null);
	    Node n23 = new Command(Command.RETRIEVED_IMAGE, null);
	    Node n22 = new Command(Command.GET_IMAGE, null);
	    Node n21 = (args.length>2)?new Alert(args):null;
	    Node n20 = new Display("matched");
	    Node n19 = new Node(n20, n21);
	    Node n18 = new RangeFind(n19);
	    Node n17 = new Circle(n18, n22);
//  	    Node n16 = new Match(n17);
	    Node n15 = new Copy(n26, n17);
	    Node n14 = new Display("labelled");
	    Node n13 = new Label(n14, n15);
//  	    Node n12 = new Pruning(new Node(new Display("Pruning"), n13));
//  	    Node n11 = new Thinning(new Node(new Display("Thinning"), n12));
//  	    Node n10 = new Hysteresis(new Node(new Display("Hysteresis"), n13));
	    Node n9 = new Thresholding(new Node(new Display("Thresholding"), n13));
	    Node n8 = new RobertsCross(new Node(new Display("Robert's Cross"), n9));
	    Node n7 = new Cache(5, n23);
	    Node n6 = new Cache(5, new Copy(n8), n25);
	    Node n5 = new Copy(n7, n6);
	    Node n4 = new Command(Command.NONE,n5);
	    Node n3 = new Display("original");
	    Node n2 = new Node(n3, n4);
	    n26.setRight(n24);
	    n24.setLeft(n6);
	    n23.setLeft(n17);
	    n22.setLeft(n7);
	    if (args.length>2) {
		(new ATR(args, n2)).run();
	    } else {
		Node n1 = new Load("movie/tank.jar", "tank.gz", 600, n2);
		while (true) {
		    n1.run();
		}
	    }
	}

	if (false) {
	    Node n4 = new RangeFind(new Alert(args));
	    Node n3 = new Thinning(new Pruning(new Label(null, n4 /* new Match(n4) */)));
	    Node n2 = new RobertsCross(new Thresholding(new Hysteresis(n3)));
	    Node n1 = new Load("movie/movie.jar", "mov3.gz", 139, n2);
//  	    Node n1 = new ATR(args, n2);
	    n1.run();
	}

//  	(new Load("dbase/db.gz", 237, 
//  	   new RobertsCross(new RangeThreshold(new Node(new Display("orig"),
//  	    new Hysteresis(new Thinning(new Pruning(new Display("threshold"))))))))).run();
//  	(new Load("dbase/db.gz", 237,
//  	  new RobertsCross(new Node(new Hough(new Node(new Display("r vs. t"),
//  	   new HoughThreshold(new Node(new Display("thresh"),
//  		new HoughThin(new Node(new Display("thin"),
//  		 new DeHough(new Display("mapped")))))))),
//  				  new Display("original"))))).run();


    }
}
