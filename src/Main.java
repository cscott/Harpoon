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
	boolean headless = true;  /* Configure to run without display */
	boolean timer = false;     /* Time the processing rate */




	//CreateHistogram2D test
	//way way way too slow
	//almost a 1/4 second just to calculate a histogram from a whole image
	if (false) {
	    Node n1 = new Load("/share/tank.jar", "tank.gz", 1,
			  new Node (new Display("Original"),
				    new Timer(true, false, new CreateHistogram2D("H", new Timer(false, true, null)))));
	    //while (true) {
		n1.run();
		//}
	}


	//test pipeline using LabelBlue
	if (true) {
	    //added by benji, hack for improved memory usage and speed
	    //Unclear whether this helps.  It depends on whether it is running
	    //headless or not.
	    //ImageDataManip.useSameArrays(true);

	    //set the window size for the Diplay node windows
	    //that pop up
	    Display.setDefaultSize(260, 200);
	   						      
	    //Node load = new Load ("/share/tank3.jar", "tank3.gz", 74, null);
	    //Node load = new Load ("/share/tank2.jar", "tank2.gz", 77, null);
	    //Node load = new Load("/share/tank1.jar", "tank1.gz", 125, null);
	    //Node load = new Load("/share/tank.jar", "tank.gz", 554, null);
	    Node load = new Load("/share/woodgrain.jar", "tank.gz", 533, null);
	    Node circle = new Circle(null, null);
	    Node copy = new Copy(null);
	    Node copy2 = new Copy(null);
	    Node circleCache = new Cache(1, null, null);
	    Node cleanCache = new Cache(1, null, null);
	    Node labelSmCache = new Cache(1, null, null);
	    Node timer1 = new Timer(true, false, null);
	    Node timer2 = new Timer(false, true, null);
	    Node robCross = new RobertsCross(null);
	    Node thresh = new Thresholding(null);
	    Node hyst = new Hysteresis(null);
	    //Node hyst = new Node();
	    Label label = new Label(null, null);
	    Node thin = new Thinning(Thinning.BLUE, null);
	    Node range = new RangeFind(null);

	    /*
	    Node origDisp = new Display("Original");
	    Node robDisp = new Display("RobCross");
	    Node threshDisp = new Display("Thresh");
	    Node hystDisp = new Display("Hyst");
	    Node labelSmDisp = new Display("Label Small", false, false, true);
	    Node labelDisp = new Display("Labeled", false, false, true);
	    Node labelBlueDisp = new Display("Label Blue");
	    Node thinDisp = new Display("Thinned", false, false, true);
	    Node endDisp = new Display("Final");
	    */

	    ///*
	    //Node thinDisp = new Node();
	    Node thinDisp = new Display("Thinned", false, false, true);
	    Node endDisp = new Display("final");;
	    //Node endDisp = new Node();
	    Node origDisp = new Node();
	    Node robDisp = new Node();
	    Node labelBlueDisp = new Node();
	    Node labelDisp = new Node();
	    Node labelSmDisp = new Node();
	    Node threshDisp = new Node();
	    Node hystDisp = new Node();
	    //*/

	    Node pause = new Pause(-1.0, 1, null);
	    Node pause2 = new Pause(-1.0, 0, null);
	    //Node pause = new Node();
	    //Node pause2 = new Node();
	    LabelBlue labelBlue = new LabelBlue(null, null);
	    labelBlue.calibrateAndProcessSeparately(true);
	    Node calibCmd = new Command(Command.CALIBRATION_IMAGE, null);
	    Node checkBlueCmd = new Command(Command.CHECK_FOR_BLUE, null);
	    Node noneCmd = new Command(Command.NONE, null);
	    Node noneCmd2 = new Command(Command.NONE, null);
	    Node retrCmd = new Command(Command.RETRIEVED_IMAGE, null);
	    Node getCmd = new Command(Command.GET_IMAGE, null);
	    Node getCropCmd = new Command(Command.GET_CROPPED_IMAGE, null);
	    Node getLabelSmCmd = new Command(Command.GET_IMAGE, null);
	    Node n = new Node();
	    Node n2 = new Node();
	    Node n3 = new Node();
	    /*
	    //Finds a single blue object in the loaded frames. Circles it.
	    load.link(origDisp,
		      pause.linkL(circleCache.link(copy.linkL(timer1.linkL(n.link(calibCmd.linkL(labelBlue),
									    noneCmd.linkL((labelBlue.link(labelBlueDisp,
													  timer2.linkL(circle.link(endDisp,
																   getCmd.linkL(circleCache))))))))),
					     retrCmd.linkL(circle))));
	    */
	    ///*
	    load.link(pause,
		      origDisp.linkL(circleCache.link(timer1.linkL(cleanCache.link(n.link(calibCmd.linkL(labelBlue),
											  noneCmd.linkL(copy.linkL(robCross.link(robDisp,
																 thresh.link(threshDisp,
																	     hyst.link(hystDisp,
																		       label.link(labelDisp,
																				  labelSmDisp.linkL(labelSmCache.link(getCropCmd.linkL(cleanCache),
																								      thin.link(thinDisp,
																										range.linkL(timer2))))))))))),
										   n2.link(null,
											   labelBlue.link(labelBlueDisp,
													  getLabelSmCmd.link(labelSmCache,
															     noneCmd2.linkL(circle.linkR(getCmd.linkL(circleCache)))))))),
						      retrCmd.linkL(circle.linkL(endDisp.linkL(pause2))))));
	    //*/
	    /*
	      //implements Wes's simple single-object tank finder
	    label.findOneObject(true);
	    load.link(origDisp,
		      timer1.linkL(circleCache.link(copy.linkL(robCross.linkL(thresh.linkL(label.linkR(timer2.linkL(circle.link(endDisp,
																getCmd.linkL(circleCache))))))),
					     retrCmd.linkL(circle))));
	    */
	    load.run();
	    System.out.println("Done running pipeline.");
	}



	//toGrayscale test
	//MAXIMUM_VALUE seems to work better
	if (false) {
	    /*
	    Node n1 = (new Load(null, "movie/tank.nozip", 11,
				new Pause(new Node(new Display("Original"),
						   new Node(new ToGrayscale(new Display("Average")),
							    new ToGrayscale(ToGrayscale.MAXIMUM_VALUE,new Display("Maximum")))))));
	    */
	    Node n1 = (new Load(null, "movie/tank.nozip", 11,
				new Pause(new Node(new Display("Original"),
						   new ToGrayscale(ToGrayscale.GREEN,
								   ToGrayscale.MAXIMUM_VALUE,
								   new Display("Max"))))));
	    while (true) {
		n1.run();
	    }
	}

	//GaussianSmoothing test
	if (false) {
	    Node n1 = new Load(null, "movie/tank.nozip", 35,
			       new Pause(new Node(new Display("Original"),
						  new Timer(true, false, new GaussianSmoothing(true, true, true, new Timer(false, true, new Display("Smooth")))))));
	    while (true) {
		n1.run();
	    }
	}

	//Wes' original pipeline
	if (false) {
	    Node n1 = (new Load("movie/tank-test.jar", "tank.gz", 35, new Display("tank-test")));
	    while (true) {
		n1.run();
	    }
	}

	if (false) {
	    Node alert = (args.length>1)?(new Alert(args)):null;
	    Node n2;
	    if (headless) {
		if (timer) {
		    alert = new Timer(false, true, alert);
		}
		n2 = new RobertsCross(new Thresholding(new Label(null, new RangeFind(alert))));
		if (timer) {
		    n2 = new Timer(true, false, n2);
		}
	    } else {
		Node n7 = new Command(Command.GET_IMAGE, null);
		Node n6 = new Circle(new Display("identified"), n7);
		Node n5 = new Node(new RangeFind(alert), n6);

		///*		
		Node n4 = new RobertsCross(new Node(new Display("Robert's Cross"),
						    new Thresholding(new Node(new Display("Thresholding"),
									      new Label(new Display("Labeled"),
											n5)))));
		//*/
		/*
		Node n4 = new Canny(new Node(new Display("Canny"),
						    new Thresholding(new Node(new Display("Thresholding"),
									      new Label(new Display("Labeled"),
											n5)))));
		*/
		Node n3 = new Cache(2, new Copy(n4), new Command(Command.RETRIEVED_IMAGE, n6));
		n2 = new Node(new Display("original"), n3);
		n7.setLeft(n3);
	    }

//  	    Node n1 = new Load("movie/tank.jar", "tank.gz", 533, n2);
	    Node n1 = (args.length>1)?(Node)(new ATR(args, n2)):
		//(new Load("movie/tank-test.jar", "tank.gz", 35, new Pause(n2)));
		(new Load(null, "movie/tank.nozip", 35, new Pause(n2)));
		//(new Load("movie/tank-test.jar", "tank.gz", 35, new Node(new Save("myTank", true),
		//							 new Pause(n2))));
		//		(Node)(new Load("movie/tank.jar", "tank.gz", 533, n2));
		//(new Load(null, "tank", 1, new Node(new Save("myTankFromUnZipped", true, false), new Pause(n2))));
	    while (true) {
	       n1.run();
	    }
	}

	//Stephen's original pipeline
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
 

	//not sure what this is
	if (false) {
	    Node n4 = new RangeFind(new Alert(args));
	    Node n3 = new Thinning(new Pruning(new Label(null, n4 /* new Match(n4) */)));
	    Node n2 = new RobertsCross(new Thresholding(new Hysteresis(n3)));
	    Node n1 = new Load("movie/movie.jar", "mov3.gz", 139, n2);
//  	    Node n1 = new ATR(args, n2);
	    n1.run();
	}


	//Wes' hough transform test
	if (false) {
	    /*
	    (new Load("dbase/plane/db.gz", 237,
		      new Node(
			  new RobertsCross(
			      new RangeThreshold(
				  new Node(new Display("orig"),
					   new Hysteresis(new Thinning(new Pruning(new Display("threshold"))))))),
			  new Copy(new Display("True original"))))).run();
	    */
	    
	    (new Load("dbase/plane/plane.jar", "db.gz", 236,
		      new Node(
			       new Display("Original Image"),
			       new RobertsCross(
						new Node(
							 new Display("Robert's Cross"),
							 new Thresholding(
									  new Node(new Hough(
											     new Node(new Display("r vs. theta"),
												      new HoughThreshold(
															 new Node(new Display("Hough Threshold"),
																  new HoughThin(
																		 new Node(new Display("thin"),
																			 new DeHough(new Display("mapped"))))))))))))))).run();
	    
	}//if (false)
	
    }//public static void main()
} //class Main
