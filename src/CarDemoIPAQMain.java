// CarDemoIPAQMain.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;


public class CarDemoIPAQMain {
    public static void main(String args[]) {

	(new Thread() {
		public void run() {
		    String args[] = {"1", "ATR Alert",
				     "-ORBInitRef",
				     "NameService=file://root/.jacorb"};
		    CarDemoTrackerStubMain.main(args);
		}
	    }).run();

	try {
	    Thread.currentThread().sleep(2000);
	}
	catch (InterruptedException e) {
	}


	(new Thread() {
		public void run() {
		    String args[] = {"camera", "1",
				     "LMCO ATR", "ATR Feedback",
				     "-ORBInitRef",
				     "NameService=file://root/.jacorb"};
		    CarDemoReceiverStubMain.main(args);
		}
	    }).run();

	try {
	    Thread.currentThread().sleep(2000);
	}
	catch (InterruptedException e) {
	}

	(new Thread() {
		public void run() {
		    String args[] = {"1", "nocompress",
				     "LMCO ATR", "embedToGround",
				     "groundToEmbed", "ATR Alert",
				     "ATR Feedback", "-ORBInitRef",
				     "NameService=file://root/.jacorb"};
		    CarDemoEmbeddedMain.main(args);
		}
	    }).run();
    }
}
