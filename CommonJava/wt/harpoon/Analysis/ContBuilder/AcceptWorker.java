// AcceptWorker.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <code>AcceptWorker</code>
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: AcceptWorker.java,v 1.2 2000-04-01 21:50:40 bdemsky Exp $
 */
class AcceptWorker extends Thread {
    public void run() {
	Socket clientSocket;
	AsyncRequest req;
	ObjectDoneContinuation oc;
	try {
	    while (true) {
		synchronized (Scheduler.acceptRequests) {
		    while ((req=Scheduler.acceptRequests.nextRequest())==null)
			Scheduler.acceptRequests.wait();
		}
		
		oc = (ObjectDoneContinuation)req.thread.cc;
		//System.out.println("Trying accept request");
		try {
		    clientSocket = req.s.accept();
		    oc.setResult((Object)clientSocket);
		}
		catch (IOException ex) {
		    oc.setException((Throwable)ex);
		    System.out.println("AcceptWorker: Accept error");
		}
		//System.out.println("Completing accept request");
		Scheduler.addReadyThread(req.thread);
	    }
	}
	catch (Exception ex) {
	    System.err.println("AW"+ex);
	}		
    }
}
