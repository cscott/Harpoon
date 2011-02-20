// AsyncRequest.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

/**
 * <code>AsyncRequests</code> holds request queues for async worker threads
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: AsyncRequests.java,v 1.1 2000-03-24 02:32:25 govereau Exp $
 */
public class AsyncRequests {
	private AsyncRequest requestQueue;
	private AsyncRequest queueEnd;

	public AsyncRequests() {
		requestQueue = null;
		queueEnd = null;
	}
	
		/**
		 * Get the next accept request in the accept request queue
		 *
		 * @return an <code>AsyncRequest</code> object or <code>null</code>
		 * if no requests are in the queue
		 */
	public synchronized AsyncRequest nextRequest() {
		if (requestQueue == null) return null;
		AsyncRequest req = requestQueue;
		requestQueue = req.next;
		if (requestQueue == null) queueEnd = null;
		return req;
	}

		/**
		 * Add a new accept request to the end of the accept request queue
		 *
		 * @param req an <code>AsyncRequest</code> object
		 */
	public synchronized void addRequest(AsyncRequest req) {
		if (queueEnd == null) {
				//assert(requestQueue == null);
			if (requestQueue != null) {
				throw new RuntimeException("assert: requestQueue != null");
			}
			requestQueue = req;
			queueEnd = req;
			req.next = null;
		} else {
			queueEnd.next = req;
			queueEnd = req;
			req.next = null;
		}
	}
}
