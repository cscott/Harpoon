// CarDemoIPAQMain2.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;
import imagerec.corba.CORBA;

public class CarDemoIPAQMain2 {
    public static void main(String args[]) {
	if (args.length == 0) {
	    System.out.println("Usage: java -jar carDemoEmbeddedATR.jar");
	    System.out.println("       <CORBA name for Ground Server>");
	    System.out.println("       <CORBA name for Ground Client (for each analyzed object)>");
	    System.out.println("       [CORBA options]");
	    System.exit(-1);
	}


	//previously in embedded main
	Node timer1 = new Timer(true, false, null);
	Node timer2 = new Timer(false, false, null);
	Node pause = new Node();
	//Node pause = new Pause(-1, 1, null);
	
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

	Server resultsFromGround = new Server(new CORBA(args), args[0], null);

	
	//runs the ground results server
	Node pipe2;
	pipe2 =
	    resultsFromGround.linkL(branch.link(getLabelSmCmd.link(labelSmCache,
								   set),
						set));
	Thread t = new Thread(pipe2);
	t.start(); //calls pipe2.run();
	
	Node pipe1;
	//Display debugDisp = new Display("debug", false, false, true);
	//debugDisp.displayColorAs(Display.BLUE, Display.GREEN);
	Node debugDisp = new Node();

	Node atr = new Node();
	Node alert = new Node();
	Node alertServer = new Node();
	Node feedbackClient = new Node();

	Node feedbackServer = new Node();	
	Node set2 = new SetCommonValue("heartbeat", new Boolean(true));
	
	feedbackServer.linkL(set2);


	Node embedToGroundClient = new Client(new CORBA(args), args[1]);


	//previously in receiverstub main
	Node heartbeat = new Regulator("heartbeat");
	Node imageSource;
	Node atrClient = new Node();
	imageSource = new Camera(null);
	imageSource.linkL(heartbeat.linkL(atrClient.linkL(atr)));

	atr.link(pause.link(timer1,
			    n.link(calibCmd.linkL(labelBlue),
				   noneCmd.linkL(cleanCache.link(robCross.linkL(thresh.linkL(hyst.linkL(label.link(null,
														   regulate.linkL(labelSmCache.link(getCropCmd.linkL(cleanCache),
																		    noneCmd3.linkL(thin.linkL(debugDisp.linkL(range.linkL(alert.linkL(alertServer))))))))))),
								 n2.linkL(embedToGroundClient))))),
		 feedbackClient.linkL(timer2.linkL(feedbackServer)));
	



	//previously in trackerstub main
	Node alertDisp = new AlertDisplay();
	Node car = new CarController();
	Node servo = new Servo();
	
	alertServer.linkL(alertDisp.linkL(car.linkL(servo)));

	imageSource.run();

    }
}
