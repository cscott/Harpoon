// CarDemoEmbeddedMain.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec;

import imagerec.graph.*;

import imagerec.util.RunLength;

import imagerec.corba.CORBA;

public class CarDemoEmbeddedMain {
    public static void main(String args[]) {
	if (args.length == 0) {
	    System.out.println("Usage: java -jar carDemoEmbeddedATR.jar <pipeline #>");
	    System.out.println("       <compress|nocompress>");
	    System.out.println("       <CORBA name for image source>");
	    System.out.println("       <CORBA name for Ground Server>");
	    System.out.println("       <CORBA name for Ground Client (for each analyzed object)>");
	    System.out.println("       <CORBA name for Alert> <CORBA name for Feedback Client> [CORBA options]");
	    System.out.println("");
	    System.out.println("  The compress option must match that of carDemoGroundATR.jar");
	    System.out.println("  If compression is turned on, the embedded ATR will send");
	    System.out.println("  outlines of objects instead of full color images.");
	    System.exit(-1);
	}
	
	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[0]);
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
	    if (pipelineNumber == 1) {

		Node timer1 = new Timer(true, false, null);
		Node timer2 = new Timer(false, false, null);
		//Node pause = new Node();
		Node pause = new Pause(-1, 1, null);
		
		Cache cleanCache = new Cache(1, null, null);
		cleanCache.saveCopies(true);
		Node labelSmCache = new Cache(1, null, null);

		Node robCross = new RobertsCross(null);
		Node thresh = new Thresholding(null);
		Node hyst = new Hysteresis(null);
		Label label = new Label(null, null);
		label.setObjectTracker("objectTracker");
		Node thin = new Thinning(Thinning.BLUE, null);
		Node range = new RangeFind(null);
		
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
		Server resultsFromGround = new Server(new CORBA(args), args[4], null);

		//runs the ground results server
		Node pipe2;
		pipe2 =
		    resultsFromGround.linkL(branch.link(getLabelSmCmd.link(labelSmCache,
									   set),
							set));
		Thread t = new Thread(pipe2);
		t.start(); //calls pipe2.run();

		//server must be started before any of the following clients try to connect, otherwise there is deadlock
		ATR atr = new ATR(new CORBA(args), args[2], null);

		//client that sends image datas to the ground system
		Node embedToGroundClient = new Client(new CORBA(args), args[3]);
		//client that sends alerts to the alert server
		Node alert = new Alert(new CORBA(args), args[5]);
		//client that sends the heartbeat to 
		Node feedbackClient = new Alert(new CORBA(args), args[6]);
		
		//System.out.println("imagedata server: #"+atr.getUniqueID());
		//System.out.println("resultsfromGoundServer: #"+resultsFromGround.getUniqueID());
		//System.out.println("embed to ground client: #"+embedToGroundClient.getUniqueID());
		//System.out.println("alert client: #"+alert.getUniqueID());
		//System.out.println("feedback client: #"+feedbackClient.getUniqueID());
		
		Node pipe1;
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
		
		//if compression is selected,
		//modify cleanCache so that it stores outlines instead of full color images
		boolean willCompress = args[1].equalsIgnoreCase("compress");
		if (willCompress) {
		    Node compress = new Compress(new RunLength(), null);
		    noneCmd.linkL(robCross);//remove cleanCache from previous location
		    hyst.linkL(cleanCache.linkL(label)); //insert cleanCache into new location
		    n2.linkL(compress.linkL(embedToGroundClient));
		}
	
		pipe1.run();

	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    
	    }
	}//else [if(pipelineNumber <= 0)]
    }//public static void main()
}//public class CarDemoEmbeddedMain
