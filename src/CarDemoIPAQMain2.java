// CarDemoIPAQMain2.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;
//import imagerec.corba.CORBA;
import imagerec.corba.Sockets;

public class CarDemoIPAQMain2 {
    public static void main(String args[]) {
	//if (args.length == 0) {
	//    System.out.println("Usage: java -jar carDemoIPAQ2.jar");
	//    System.out.println("       <CORBA name for Results-from-Ground Server>");
	//    System.out.println("       <CORBA name for Send-to-Ground Client (for each analyzed object)>");
	//    System.out.println("       [CORBA options]");
	//    System.exit(-1);
	//}

	if (args.length == 0) {
	    System.out.println("Usage: java -jar carDemoIPAQ2.jar");
	    System.out.println("       <server port #>");
	    System.out.println("       <client port #>");
	    System.out.println("       <camera control server port #>");
	    System.out.println("       <label control server port #>");
	    System.out.println("       <car control server port #>");
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
	Node confirm = new LabelConfirm(label);
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
	Node goLeftCmd = new Command(Command.GO_LEFT, null);
	Node n = new Node();
	Node n2 = new Node();
	Node branch = new Switch();
	Node branch2 = new Switch();
	Strip strip = new Strip();
	strip.copyImage(true);
	
	//Server resultsFromGround = new Server(new CORBA(args), args[0], null);
	Server resultsFromGround = new Server(new Sockets(), args[0], null);
	
	//runs the ground results server
	Node pipe2;
	/*
	pipe2 =
	    resultsFromGround.linkL(branch.link(getLabelSmCmd.link(labelSmCache,
								   set),
						set));
	*/
	pipe2 = 
	    resultsFromGround.linkL(confirm);
	try {
	    Thread t = new Thread(pipe2);
	    t.start(); //calls pipe2.run();
	}
	catch (Exception e) {
	    System.out.println("Results from ground thread died");
	    System.out.println(e.getMessage());
	    System.out.println(e.getStackTrace());
	    
	}
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


	//Node embedToGroundClient = new Client(new CORBA(args), args[1]);
	Node embedToGroundClient = new Client(new Sockets(), args[1]);

	Node setOrigDims = new SetOrigDimensions();

	Node async = new Async(null);

	//previously in receiverstub main
	Node heartbeat = new Regulator("heartbeat");
	Node atrClient = new Node();
	//Load imageSource;
	//imageSource = new Load(null, "/home/benster/ImageRec/images/uav_ppm_norm/uav_ppm", 100, null);
	//imageSource = new Load("woodgrain.jar", "tank.gz", 533, null);
	//imageSource = new Load("movie/tank.jar", "tank.gz", 533, null);
	Camera imageSource = new Camera(null);
	imageSource.flip();

	/*
	imageSource.linkL(heartbeat.linkL(atrClient.linkL(atr)));
	atr.link(setOrigDims.linkL(pause.link(timer1,
					      n.link(calibCmd.linkL(labelBlue),
						     noneCmd.linkL(cleanCache.link(robCross.linkL(thresh.linkL(hyst.linkL(label.link(null,
																     regulate.linkL(labelSmCache.link(branch2.link(getCropCmd.linkL(cleanCache),
																						   goLeftCmd.linkL(strip.linkL(embedToGroundClient))),
																				      noneCmd3.linkL(thin.linkL(debugDisp.linkL(range.linkL(alert.linkL(alertServer))))))))))),
										   n2.linkL(embedToGroundClient)))))),
		 feedbackClient.linkL(timer2.linkL(feedbackServer)));
	*/

	imageSource.linkL(timer1.linkL(setOrigDims.linkL(cleanCache.link(robCross.linkL(thresh.linkL(hyst.linkL(label.link(null,
													      branch2.link(async.linkL(getCropCmd.linkL(cleanCache)),
															   thin.linkL(range.linkL(alert.linkL(alertServer)))))))),
							    embedToGroundClient))));

	//previously in trackerstub main
	Node alertDisp = new AlertDisplay();
	CarController car = new CarController();
	Node servo = new Servo();
	
	alertServer.linkL(alertDisp.linkL(car.linkL(servo)));
	Thread t2 = new Thread(imageSource);
	try {
	    t2.start(); // calls imageSource.run();
	}
	catch (Exception e) {
	    System.out.println("Embedded ATR thread died");
	    System.out.println(e.getMessage());
	    System.out.println(e.getStackTrace());
	}
  
	
	CameraControl cc = new CameraControl(imageSource);
	System.out.println("Starting camera control server");
	try {
	    Server cameraControlServer = new Server(new Sockets(), args[2], cc);
	    Thread t3 = new Thread(cameraControlServer);
	    t3.start();
	}
	catch (Exception e) {
	    System.out.println("Camera control thread died");
	    System.out.println(e.getMessage());
	    System.out.println(e.getStackTrace());
	}

	LabelControl lc = new LabelControl(label);
	System.out.println("*** Starting label control server");
	try {
	    Server labelControlServer = new Server(new Sockets(), args[3], lc);
	    Thread t4 = new Thread(labelControlServer);
	    t4.start();
	}
	catch (Exception e) {
	    System.out.println("Label control thread died");
	    System.out.println(e.getMessage());
	    System.out.println(e.getStackTrace());
	}
	
	CarControllerControl ccc = new CarControllerControl(car);
	System.out.println("****Starting car control server");
	try {
	    Server carControllerControlServer = new Server(new Sockets(), args[4], ccc);
	    Thread t5 = new Thread(carControllerControlServer);
	    t5.start();
	}
	catch (Exception e) {
	    System.out.println("Car control thread died");
	    System.out.println(e.getMessage());
	    System.out.println(e.getStackTrace());
	}
	
    }
}
