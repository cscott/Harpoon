// ATRMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;
//import imagerec.corba.EventChannel;

import imagerec.graph.*;

/**
 * This is the main program for the MIT ATR one-component system
 * for use in the BBN UAV OEP.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ATRMain {

    /** The entry point to the ATR main program.
     *
     *  @param args Should include parameters for contacting 
     *              the CORBA nameservice.
     */
    public static void main(String args[]) {
	/*
	 * New development of pipelines can be found in {@link Main} 
	 * (under imagerec.jar).  When pipelines are finished development,
	 * they will be placed in this file.
	 */

	if (args.length<4) {
	    System.out.println("Usage: java -jar ATR.jar");
	    System.out.println("       <'timer'|'notimer'>");
	    System.out.println("       <pipeline #>");
	    System.out.println("       <CORBA name for ATR>");
	    System.out.println("       <CORBA name for Alert>");
	    System.out.println("       [CORBA options]");
	    System.out.println("--or-- ");
	    System.out.println("       java -jar ATR.jar");
	    System.out.println("       <'timer'|'notimer'>");
	    System.out.println("       <pipeline #>");
	    System.out.println("       <Event Channel Name>");
	    System.out.println("       <CORBA name for ATR>");
	    System.out.println("       <CORBA name for Alert>");
	    System.out.println("       [CORBA options]");
	    System.out.println("");
	    System.out.println("For pipelines 1-3, use format #1.");
	    System.out.println("For pipeline 4, use format #2.");
	    System.exit(-1);
	}
	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[1]);
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
	    boolean timer = args[0].equalsIgnoreCase("timer");
	    Node timer1;
	    Node timer2;
	    Node receivingTimer;
	    //if timer is requested, then timer1/timer2 are made to be Timer nodes,
	    //otherwise, they become dummy Nodes, and ImageDatas will just be passed through
	    //with minimal performance impact
	    if (timer) {
		timer1 = new Timer(true, false, null);
		timer2 = new Timer(false, true, "Processing", null);
		receivingTimer = new Timer(false, true, "Receiving Frame", null);
	    }
	    else {
		timer1 = new Node();
		timer2 = new Node();
		receivingTimer = new Node();
	    }
	    Node pipe = null;
	    if (pipelineNumber == 1) {
		Node alert = new Alert(new CORBA(args), args[3]);
		
		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Label label = new Label(Label.DEFAULT1, null, null);
		label.findOneObject(true);
		Node range = new RangeFind(null);
		pipe = timer1.linkL(robCross.linkL(thresh.linkL(label.link(null,
									   range.linkL(timer2.linkL(alert))))));
		(new ATR(new CORBA(args), args[2], pipe)).run();
	    }
	    else if (pipelineNumber == 2) {
		Node alert = new Alert(new CORBA(args), args[3]);
		Node labelBlue = new LabelBlue(null, null);
		Node range = new RangeFind(null);
		pipe = timer1.linkL(labelBlue.link(null,
						   range.linkL(timer2.linkL(alert))));
		(new ATR(new CORBA(args), args[2], pipe)).run();
	    }
	    else if (pipelineNumber == 3) {
		Node alert = new Alert(new CORBA(args), args[3]);
		Node cleanCache = new Cache(1, null, null);
		Node n = new Node();
		LabelBlue labelBlue = new LabelBlue(null, null);
		labelBlue.calibrateAndProcessSeparately(true);
		Node calibCmd = new Command(Command.CALIBRATION_IMAGE, null);
		Node noneCmd = new Command(Command.NONE, null);
		Node copy = new Copy(null);
		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Node hyst = new Hysteresis(null);
		Node label = new Label(null, null);
		Node labelSmCache = new Cache(1, null, null);
		Node getCropCmd = new Command(Command.GET_CROPPED_IMAGE, null);
		Node thin = new Thinning(Thinning.BLUE, null);
		Node range = new RangeFind(null);
		Node getLabelSmCmd = new Command(Command.GET_IMAGE, null);
		pipe = 
		receivingTimer.linkL(timer1.linkL(cleanCache.link(n.link(calibCmd.linkL(labelBlue),
						    noneCmd.linkL(copy.linkL(robCross.link(null,
											   thresh.link(null,
												       hyst.link(null,
														 label.link(null,
															    labelSmCache.link(getCropCmd.linkL(cleanCache),
																	      thin.link(null,
																			range.linkL(timer2.linkL(alert))))))))))),
					    
					     labelBlue.link(null,
							    getLabelSmCmd.linkL(labelSmCache)))));
    
		(new ATR(new CORBA(args), args[2], pipe)).run();
	    }
	    else if (pipelineNumber == 4) {
		/*
		Node alert = new Alert(new EventChannel(args), args[4]);
		Node cleanCache = new Cache(1, null, null);
		Node n = new Node();
		LabelBlue labelBlue = new LabelBlue(null, null);
		labelBlue.calibrateAndProcessSeparately(true);
		Node calibCmd = new Command(Command.CALIBRATION_IMAGE, null);
		Node noneCmd = new Command(Command.NONE, null);
		Node copy = new Copy(null);
		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Node hyst = new Hysteresis(null);
		Node label = new Label(null, null);
		Node labelSmCache = new Cache(1, null, null);
		Node getCropCmd = new Command(Command.GET_CROPPED_IMAGE, null);
		Node thin = new Thinning(Thinning.BLUE, null);
		Node range = new RangeFind(null);
		Node getLabelSmCmd = new Command(Command.GET_IMAGE, null);
		pipe = 
		receivingTimer.linkL(timer1.linkL(cleanCache.link(n.link(calibCmd.linkL(labelBlue),
						    noneCmd.linkL(copy.linkL(robCross.link(null,
											   thresh.link(null,
												       hyst.link(null,
														 label.link(null,
															    labelSmCache.link(getCropCmd.linkL(cleanCache),
																	      thin.link(null,
																			range.linkL(timer2.linkL(alert))))))))))),
					    
					     labelBlue.link(null,
							    getLabelSmCmd.linkL(labelSmCache)))));
    
		(new ATR(new CORBA(args), args[3], pipe)).run();
		*/
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    }

	}//else [if(pipelineNumber <= 0)]
    }//public static void main()
}//public class ATRMain
