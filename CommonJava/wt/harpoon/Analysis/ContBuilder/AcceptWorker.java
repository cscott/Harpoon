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
 * @version $Id: AcceptWorker.java,v 1.1 2000-03-24 02:32:25 govereau Exp $
 */
class AcceptWorker extends Thread {
	public void run() {
		Socket clientSocket;
		AsyncRequest req;
		ObjectDoneContinuation oc;
		try {
			while (true) {
				synchronized (Scheduler.acceptRequests) {
					Scheduler.acceptRequests.wait();
				}
				req = Scheduler.acceptRequests.nextRequest();
				if (req == null) {
					throw new RuntimeException("assert: AcceptWorker: req == null");
				}

				oc = (ObjectDoneContinuation)req.thread.cc;
				try {
					clientSocket = req.s.accept();
					oc.setResult((Object)clientSocket);
				}
				catch (IOException ex) {
					oc.setException((Throwable)ex);
					System.out.println("AcceptWorker: Accept error");
				}
				Scheduler.addReadyThread(req.thread);
			}
		}
		catch (Exception ex) {
			System.err.println(ex);
		}		
	}
}
