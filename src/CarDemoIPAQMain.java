// CarDemoIPAQMain.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;


public class CarDemoIPAQMain {
    public static void main(String args[]) {

	if (args.length == 0) {
	    System.out.println("Usage: java -jar carDemoIPAQ.jar <CORBA name service>");
	    System.exit(-1);
	}


	final String nameService = args[0];
	System.out.println("**** Running the Tracker ***");
	(new Thread() {
		public void run() {
		    String args[] = {"1", "ATR Alert",
				     "-ORBInitRef", nameService};
		    CarDemoTrackerStubMain.main(args);
		}
	    }).start();

	try {
	    Thread.currentThread().sleep(1750);
	}
	catch (InterruptedException e) {
	}


	System.out.println("**** Running the Receiver ***");
	(new Thread() {
		public void run() {
		    String args[] = {"camera", "1",
				     "LMCO ATR", "ATR Feedback",
				     "-ORBInitRef", nameService};
		    CarDemoReceiverStubMain.main(args);
		}
	    }).start();

	try {
	    Thread.currentThread().sleep(1750);
	}
	catch (InterruptedException e) {
	}
	
	System.out.println("***** Running the Embedded ATR *****");
	(new Thread() {
		public void run() {
		    String args[] = {"1", "nocompress",
				     "LMCO ATR", "embedToGround",
				     "groundToEmbed", "ATR Alert",
				     "ATR Feedback", "-ORBInitRef", nameService};
		    CarDemoEmbeddedMain.main(args);
		}
	    }).start();
    }
}
