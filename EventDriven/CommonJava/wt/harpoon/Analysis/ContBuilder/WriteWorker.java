// WriteWorker.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>WriteWorker</code>
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: WriteWorker.java,v 1.2 2000-04-01 21:50:40 bdemsky Exp $
 */
class WriteWorker extends Thread {
    public void run() {
	int result;
	AsyncRequest req;
	VoidDoneContinuation cc;
	try {
	    while (true) {
		synchronized (Scheduler.writeRequests) {
		    while ((req=Scheduler.writeRequests.nextRequest())==null)
			Scheduler.writeRequests.wait();
		}
		//System.out.println("Trying write");
		if (req.b == null) {
		    req.out.write(req.off); // see OutputStream.writeAsync(int)
		} else {
		    req.out.write(req.b, req.off, req.len);
		}
		//System.out.println("Completing write");
		Scheduler.addReadyThread(req.thread);
	    }
	}
	catch (Exception ex) {
	    System.err.println("WW"+ex);
	}
    }
}
