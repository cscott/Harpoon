// GUIMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * This is the main program for displaying the results of the 
 * image recognition pipeline on a test suite.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class GUIMain {
    /** The entry point to the GUI display of the image recognition pipeline.
     *
     *  @param args foo
     */
    public static void main(String args[]) {
	if (args.length<3) {
	    System.out.println("Usage: java -jar GUI.jar <'pause'|'nopause'> <'corba'|'nocorba'> <pipeline #> [CORBA options]");
	    System.exit(-1);
	}

	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[2]);
	}
	catch (NumberFormatException nfe) {
	    System.out.println("Error: Pipeline argument was not a valid integer.");
	    System.exit(-1);
	}

	if (pipelineNumber <= 0) {
	    System.out.println("Error: Pipeline # must be > 0 (value was '"+pipelineNumber+")");
	    System.exit(-1);
	}
	else {
	    boolean usePause = args[0].equalsIgnoreCase("pause");
	    boolean useCorba = args[1].equalsIgnoreCase("corba");
	    Node pause;
	    //if pause is requested, then 'pause' is made to be a Pause node,
	    //otherwise, it becomes a dummy Node, and ImageDatas will just be passed through
	    //with minimal performance impact
	    if (usePause) {
		//'-1.0' says to wait for user input
		//'1' says to pause before every image
		pause = new Pause(-1.0, 1, null);
	    }
	    else {
		pause = new Node();
	    }
	    Node pipe = null;
	    if (pipelineNumber == 1) {
		Node origDisp = new Display("Original");
		Node endDisp = new Display("final");
		Node circleCache = new Cache(1, null, null);
		Node copy = new Copy(null);
		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Label label = new Label(Label.DEFAULT1, null, null);
		label.findOneObject(true);
		Node circle = new Circle(null, null);
		Node retrCmd = new Command(Command.RETRIEVED_IMAGE, null);
		Node getCmd = new Command(Command.GET_IMAGE, null);
		Node n = new Node();
		Node n2 = new Node();

		pipe = 
		    n.link(pause.linkL(origDisp),
			   circleCache.link(copy.linkL(robCross.linkL(thresh.linkL(label.link(null,
											      circle.link(endDisp,
													  getCmd.linkL(circleCache)))))),
					    retrCmd.linkL(circle)));
		//if we are using corba,
		//insert range tracking and alert nodes between label and circle nodes.
		//and use ATR node instead of Load node
		Node head;
		if (useCorba) {
		    Node range = new RangeFind(null);
		    Node alert = new Alert(args);
		    label.linkR(n2.link(range.linkL(alert),
				       circle));
		    head = new ATR(args, pipe);
		}
		else {
		    head = new Load("GUI.jar", "tank.gz", 533, pipe);
		}
		pipe = head.linkL(n);
	    }
	    else if (pipelineNumber == 2) {
		Node origDisp = new Display("Original");
		Node endDisp = new Display("final");
		Node labelBlue = new LabelBlue(null, null);
		Node circleCache = new Cache(1, null, null);
		Node circle = new Circle(null, null);
		Node retrCmd = new Command(Command.RETRIEVED_IMAGE, null);
		Node getCmd = new Command(Command.GET_IMAGE, null);
		Node n = new Node();
		Node n2 = new Node();
		Node copy = new Copy(null);
		pipe =
		    n.link(origDisp,
			   pause.linkL(circleCache.link(copy.linkL(labelBlue.link(null,
										  circle.link(endDisp,
											      getCmd.linkL(circleCache)))),
							retrCmd.linkL(circle))));
		//if we are using corba,
		//insert range tracking and alert nodes between labelBlue and circle nodes.
		//and use ATR node instead of Load node
		Node head;
		if (useCorba) {
		    Node range = new RangeFind(null);
		    Node alert = new Alert(args);
		    Node n3 = new Node();
		    labelBlue.linkR(n3.link(range.linkL(alert),
				       circle));
		    head = new ATR(args, pipe);
		}
		else {
		    head = new Load("GUI.jar", "tank.gz", 533, pipe);
		}
		pipe = head.linkL(n);
		
	    }
	    else if (pipelineNumber == 3) {
		Node origDisp = new Display("Original");
		Node endDisp = new Display("final");

		Node circleCache = new Cache(1, null, null);
		Node cleanCache = new Cache(1, null, null);
		Node labelSmCache = new Cache(1, null, null);

		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Node hyst = new Hysteresis(null);
		Label label = new Label(null, null);

		LabelBlue labelBlue = new LabelBlue(null, null);
		labelBlue.calibrateAndProcessSeparately(true);

		Node circle = new Circle(null, null);
		Node copy = new Copy(null);

		Node calibCmd = new Command(Command.CALIBRATION_IMAGE, null);
		Node noneCmd = new Command(Command.NONE, null);
		Node noneCmd2 = new Command(Command.NONE, null);
		Node retrCmd = new Command(Command.RETRIEVED_IMAGE, null);
		Node getCmd = new Command(Command.GET_IMAGE, null);
		Node getCropCmd = new Command(Command.GET_CROPPED_IMAGE, null);
		Node getLabelSmCmd = new Command(Command.GET_IMAGE, null);

		
		Node n = new Node();
		Node n2 = new Node();
		Node n3 = new Node();
		pipe = 
		    n.link(pause,
			   origDisp.linkL(circleCache.link(cleanCache.link(n2.link(calibCmd.linkL(labelBlue),
										   noneCmd.linkL(copy.linkL(robCross.link(null,
															  thresh.link(null,
																      hyst.link(null,
																		label.link(null,
																			   labelSmCache.linkL(getCropCmd.linkL(cleanCache))))))))),
									   n3.link(null,
										   labelBlue.link(null,
												  getLabelSmCmd.link(labelSmCache,
														     noneCmd2.linkL(circle.linkR(getCmd.linkL(circleCache))))))),
							   retrCmd.linkL(circle.linkL(endDisp)))));
		//if we are using corba,
		//add thinning, range finding, and alert to LabelSmCache's right-hand branch
		//and use ATR node instead of Load node
		Node head;
		if (useCorba) {
		    Node thin = new Thinning(Thinning.BLUE, null);
		    Node range = new RangeFind(null);
		    Node alert = new Alert(args);
		    Node n4 = new Node();
		    labelSmCache.linkR(thin.linkL(n4.link(range.linkL(alert),
							  circle)));
		    head = new ATR(args, pipe);
		}
		else {
		    head = new Load("GUI.jar", "tank.gz", 533, pipe);
		}
		pipe = head.linkL(n);
		
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    }
	    while (true) {
		pipe.run();
	    }
	    
	    
	}//else [if(pipelineNumber <= 0)]
	

    }//public static void main()

}//public class GUIMain
