// ReadWorker.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>ReadWorker</code>
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: ReadWorker.java,v 1.1 2000-03-24 02:09:35 govereau Exp $
 */
class ReadWorker extends Thread {
	
	public void run() {
		int result;
		AsyncRequest req;
		IntDoneContinuation ic;
		try {
			while (true) {
				synchronized (Scheduler.readRequests) {
					Scheduler.readRequests.wait();
				}
				req = Scheduler.readRequests.nextRequest();
				if (req == null) {
					throw new RuntimeException("assert: ReadWorker: req == null");
				}

				ic = (IntDoneContinuation)req.thread.cc;
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
				Scheduler.addReadyThread(req.thread);
			}
		}
		catch (Exception ex) {
			System.err.println(ex);
		}
		
	}
}
