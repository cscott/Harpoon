// EmbeddedMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;
import imagerec.corba.CORBA;

/**
 * This is the main program that should run on the embedded processor in the UAV.
 * It has to compete with the hard real-time load of the mission computer.
 *
 * This is the main program for the embedded component of the two-part ATR.
 * It has been optimized for "bang/bunk" and does coarse image recognition 
 * with limited CPU.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class EmbeddedATRMain {

    /** The entry point to the embedded part of the two-component version of the ATR.
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	if (args.length<9) {
	    System.out.println("Usage: java -jar embeddedATR.jar <'timer'|'notimer'> <'pause'|'nopause'>");
	    System.out.println("       <'head'|'headless'> <pipeline #>"); 
	    System.out.println("       <CORBA name for ATR> <CORBA name for Ground Server>");
	    System.out.println("       <CORBA name for Ground Client (for each analyzed object)>");
	    System.out.println("       <CORBA name for Alert> <CORBA name for Feedback Client> [CORBA options]");
	    System.exit(-1);
	}
	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[3]);
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
	    boolean headless = args[2].equalsIgnoreCase("headless");
	    boolean timer = args[0].equalsIgnoreCase("timer");
	    Node timer1;
	    Node timer2;
	    //if timer is requested, then timer1/timer2 are made to be Timer nodes,
	    //otherwise, they become dummy Nodes, and ImageDatas will just be passed through
	    //with minimal performance impact
	    if (timer) {
		timer1 = new Timer(true, false, null);
		timer2 = new Timer(false, true, null);
	    }
	    else {
		timer1 = new Node();
		timer2 = new Node();
	    }
	    if (pipelineNumber == 1) {
		Node circle = new Circle(null, null);
		Node arrow = new DrawArrow(null, null);
		Node copy = new Copy(null);
		Node copy2 = new Copy(null);
		
		//these two store identical images, but
		//send them to different spots
		Node circleCache = new Cache(1, null, null);
		Node arrowCache = new Cache(1, null, null);
		
		Cache cleanCache = new Cache(1, null, null);
		cleanCache.saveCopies(true);
		Node labelSmCache = new Cache(1, null, null);

		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Node hyst = new Hysteresis(null);
		Label label = new Label(null, null);
		Node thin = new Thinning(Thinning.BLUE, null);
		Node range = new RangeFind(null);
		
		boolean insertPauses = args[1].equalsIgnoreCase("pause");
		Node pause;
		Node pause2;
		if (insertPauses) {
		    pause = new Pause(-1.0, 1, null);
		    pause2 = new Pause(-1.0, 0, null);
		}
		else {
		    pause = new Node();
		    pause2 = new Node();
		}

		Node regulate = new Regulator("nextSmallImage");
		Node set = new SetCommonValue("nextSmallImage", new Boolean(true));

		LabelBlue labelBlue = new LabelBlue(null, null);
		labelBlue.calibrateAndProcessSeparately(true);
		Node calibCmd = new Command(Command.CALIBRATION_IMAGE, null);
		Node checkBlueCmd = new Command(Command.CHECK_FOR_BLUE, null);
		Node noneCmd = new Command(Command.NONE, null);
		Node noneCmd2 = new Command(Command.NONE, null);
		Node noneCmd3 = new Command(Command.NONE, null);
		Node retrCmd = new Command(Command.RETRIEVED_IMAGE, null);
		Node retr2Cmd = new Command(Command.RETRIEVED_IMAGE, null);
		Node getCmd = new Command(Command.GET_IMAGE, null);
		Node getCmd2 = new Command(Command.GET_IMAGE, null);
		Node getCropCmd = new Command(Command.GET_CROPPED_IMAGE, null);
		Node getLabelSmCmd = new Command(Command.GET_IMAGE, null);
		Node n = new Node();
		Node n2 = new Node();
		Node branch = new Switch();

		//server must be started before any of the following clients try to connect, otherwise there is deadlock
		//server that waits for results from the ground system
		Server resultsFromGround = new Server(new CORBA(args), args[6], null);

		//runs the ground results server
		Node pipe2;
		if (!headless) {
		    pipe2 =
			resultsFromGround.linkL(branch.link(getLabelSmCmd.link(labelSmCache,
									       noneCmd2.linkL(circle.linkR(getCmd.link(circleCache,
														       set)))),
							    set));
		}
		//else if headless
		else {
		    pipe2 =
			resultsFromGround.linkL(branch.link(getLabelSmCmd.link(labelSmCache,
									       set),
							    set));
		}
		Thread t = new Thread(pipe2);
		t.start(); //calls pipe2.run();

		//server must be started before any of the following clients try to connect, otherwise there is deadlock
		ATR atr = new ATR(new CORBA(args), args[4], null);

		//client that sends image datas to the ground system
		Node embedToGroundClient = new Client(new CORBA(args), args[5]);
		//client that sends alerts to the alert server
		Node alert = new Alert(new CORBA(args), args[7]);
		//client that sends the heartbeat to 
		Node feedbackClient = new Alert(new CORBA(args), args[8]);
		
		//System.out.println("imagedata server: #"+atr.getUniqueID());
		//System.out.println("resultsfromGoundServer: #"+resultsFromGround.getUniqueID());
		//System.out.println("embed to ground client: #"+embedToGroundClient.getUniqueID());
		//System.out.println("alert client: #"+alert.getUniqueID());
		//System.out.println("feedback client: #"+feedbackClient.getUniqueID());
		
		Node pipe1;
		if (!headless) {
		    //Node origDisp = new Display("Original");
		    /*
		    Node robDisp = new Display("RobCross");
		    Node threshDisp = new Display("Thresh");
		    Node hystDisp = new Display("Hyst");
		    Node labelSmDisp = new Display("Label Small", false, false, true);
		    Node labelDisp = new Display("Labeled", false, false, true);
		    Node cropDisp = new Display("Cropped");
		    Node labelBlueDisp = new Display("Label Blue");
		    Node thinDisp = new Display("Thinned", false, false, true);
		    */
		    Node endDisp = new Display("Final");

		    Node origDisp = new Node();
		    Node thinDisp = new Node();
		    Node robDisp = new Node();
		    Node labelBlueDisp = new Node();
		    Node labelDisp = new Node();
		    Node labelSmDisp = new Node();
		    Node threshDisp = new Node();
		    Node hystDisp = new Node();
		    Node cropDisp = new Node();




		    pipe1 = 
			atr.link(pause.link(origDisp,
					    n.link(calibCmd.linkL(labelBlue),
						   noneCmd.linkL(circleCache.link(arrowCache.link(timer1.linkL(cleanCache.link(copy.linkL(robCross.link(robDisp,
																			thresh.link(threshDisp,
																				    hyst.link(hystDisp,
																					      label.link(labelDisp,
																							 regulate.linkL(labelSmDisp.linkL(labelSmCache.link(getCropCmd.linkL(cleanCache),
																													    noneCmd3.linkL(thin.link(thinDisp,
																																     range.linkL(timer2.linkL(arrow.linkR(getCmd2.linkL(arrowCache)))))))))))))),
															       //cropped, full color objects that were located by their outlines
															       n2.link(cropDisp,
																       embedToGroundClient))),
												  retr2Cmd.linkL(arrow.linkL(alert))),
										  retrCmd.linkL(circle.linkL(endDisp)))))),
				 feedbackClient);
		}
		//else if not headless
		else {
		    //Display debugDisp = new Display("debug", false, false, true);
		    //debugDisp.displayColorAs(Display.BLUE, Display.GREEN);
		    Node debugDisp = new Node();
		    pipe1 =
			atr.link(pause.link(timer1,
					    n.link(calibCmd.linkL(labelBlue),
						   noneCmd.linkL(cleanCache.link(robCross.linkL(thresh.linkL(hyst.linkL(label.link(null,
																   regulate.linkL(labelSmCache.link(getCropCmd.linkL(cleanCache),
																				     noneCmd3.linkL(thin.linkL(debugDisp.linkL(range.linkL(alert)))))))))),
										 n2.linkL(embedToGroundClient))))),
				 feedbackClient.linkL(timer2));
		}
		
		pipe1.run();


		
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    }
	}//else [if(pipelineNumber <= 0)]
    }//public static void main()

}//public class EmbeddedATRMain
