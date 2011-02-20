// AsyncRequest.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

/**
 * <code>AsyncRequest</code> encapsulates a request to a worker thread
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: AsyncRequest.java,v 1.1 2000-03-24 02:32:25 govereau Exp $
 */
public class AsyncRequest {
	public Thread thread;
	public AsyncRequest next;
	public ServerSocket s;
	public InputStream in;
	public OutputStream out;
	public byte[] b;
	public int off;
	public int len;

	public AsyncRequest() {
		this.thread = Scheduler.currentThread;
		this.next = null;
	}

	public AsyncRequest(ServerSocket socket) {
		this();
		this.s = socket;
	}

	public AsyncRequest(InputStream in, byte[] b, int off, int len) {
		this();
		this.in = in;
		this.b = b;
		this.off = off;
		this.len = len;
	}	

	public AsyncRequest(OutputStream out, byte[] b, int off, int len) {
		this();
		this.out = out;
		this.b = b;
		this.off = off;
		this.len = len;
	}	
}
