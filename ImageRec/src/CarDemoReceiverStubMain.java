// CarDemoReceiverStubMain.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec;

import imagerec.graph.*;

import imagerec.corba.CORBA;

public class CarDemoReceiverStubMain {
    public static void main(String args[]) {
	if (args.length == 0) {
	    System.out.println("Usage #1: java -jar carDemoReceiverStub.jar camera");
	    System.out.println("               <pipeline #> <CORBA name for ATR>");
	    System.out.println("               <CORBA name for feedback server>");
	    System.out.println("               (CORBA options)");
	    System.out.println("");
	    System.out.println("Usage #2: java -jar carDemoReceiverStub.jar nocamera");
	    System.out.println("               <jarFile|'none'> <filePrefix> <# of images>");
	    System.out.println("               <pipeline #> <CORBA name for ATR> <CORBA name for feedback server> ");
	    System.out.println("               (CORBA options)");
	    System.exit(-1);
	}

	boolean useCamera;
	int pipelineIndex;
	int corbaATRIndex;
	int corbaFeedbackIndex;
	if (args[0].equalsIgnoreCase("camera")) {
	    useCamera = true;
	    pipelineIndex = 1;
	    corbaATRIndex = 2;
	    corbaFeedbackIndex = 3;
	}
	else {
	    useCamera = false;
	    pipelineIndex = 4;
	    corbaATRIndex = 5;
	    corbaFeedbackIndex = 6;
	}


	

	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[pipelineIndex]);
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
		AlertServer feedbackServer = new AlertServer(new CORBA(args), args[corbaFeedbackIndex], null);
		
		Node set = new SetCommonValue("heartbeat", new Boolean(true));
		
		feedbackServer.linkL(set);
		Thread t = new Thread(feedbackServer);
		t.start(); //calls feedbackServer.run();
		
		
		Node atrClient = new ATRClient(new CORBA(args), args[corbaATRIndex]);
		Node regulate = new Regulator("heartbeat");
		Node imageSource;
		if (useCamera) {
		    imageSource = new Camera(null);
		}
		else {
		    if (args[1].equalsIgnoreCase("none")) {
			imageSource = new Load(args[2], Integer.parseInt(args[3]), null);
		    } else {
			imageSource = new Load(args[1], args[2], Integer.parseInt(args[3]), null);
		    }
		}
		imageSource.linkL(regulate.linkL(atrClient));
		while (true) {
		    imageSource.run();
		}
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    
	    }
	}//else [if(pipelineNumber <= 0)]
    }//public static void main()
}
