// CarDemoIPAQMain.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;


public class CarDemoIPAQMain {
    public static void main(String args[]) {

	System.out.println("**** Running the Tracker ***");
	(new Thread() {
		public void run() {
		    String args[] = {"1", "ATR Alert",
				     "-ORBInitRef"};
		    CarDemoTrackerStubMain.main(args);
		}
	    }).start();

	try {
	    Thread.currentThread().sleep(2000);
	}
	catch (InterruptedException e) {
	}


	System.out.println("**** Running the Receiver ***");
	(new Thread() {
		public void run() {
		    String args[] = {"camera", "1",
				     "LMCO ATR", "ATR Feedback",
				     "-ORBInitRef"};
		    CarDemoReceiverStubMain.main(args);
		}
	    }).start();

	try {
	    Thread.currentThread().sleep(2000);
	}
	catch (InterruptedException e) {
	}
	
	System.out.println("***** Running the Embedded ATR *****");
	(new Thread() {
		public void run() {
		    String args[] = {"1", "nocompress",
				     "LMCO ATR", "embedToGround",
				     "groundToEmbed", "ATR Alert",
				     "ATR Feedback", "-ORBInitRef"};
		    CarDemoEmbeddedMain.main(args);
		}
	    }).start();
    }
}
