// ReadWorker.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>ReadWorker</code>
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: ReadWorker.java,v 1.2 2000-04-01 21:50:40 bdemsky Exp $
 */
class ReadWorker extends Thread {
	
    public void run() {
	int result;
	AsyncRequest req;
	IntDoneContinuation ic;
	try {
	    while (true) {
		synchronized (Scheduler.readRequests) {
		    while ((req=Scheduler.readRequests.nextRequest())==null)
			Scheduler.readRequests.wait();
		}
		
		ic = (IntDoneContinuation)req.thread.cc;
		//System.out.println("Trying read request");
		try {
		    if (req.b == null) {
			result = req.in.read();
		    } else {
			result = req.in.read(req.b, req.off, req.len);
		    }
		    ic.setResult(result);
		}
		catch (IOException ex) {
		    ic.setException((Throwable)ex);
		}
		//System.out.println("Completeds read request");
		Scheduler.addReadyThread(req.thread);
	    }
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    System.err.println("RW"+ex);
	}
    }
}
