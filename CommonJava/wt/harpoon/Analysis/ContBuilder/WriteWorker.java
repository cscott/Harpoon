// WriteWorker.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>WriteWorker</code>
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: WriteWorker.java,v 1.1 2000-03-24 02:32:25 govereau Exp $
 */
class WriteWorker extends Thread {

	public void run() {
		int result;
		AsyncRequest req;
		VoidDoneContinuation cc;
		try {
			while (true) {
				synchronized (Scheduler.writeRequests) {
					Scheduler.writeRequests.wait();
				}
				req = Scheduler.writeRequests.nextRequest();
				if (req == null) {
					throw new RuntimeException("assert: WriteWorker: req == null");
				}

				if (req.b == null) {
					req.out.write(req.off); // see OutputStream.writeAsync(int)
				} else {
					req.out.write(req.b, req.off, req.len);
				}
				Scheduler.addReadyThread(req.thread);
			}
		}
		catch (Exception ex) {
			System.err.println(ex);
		}
		
	}
}
