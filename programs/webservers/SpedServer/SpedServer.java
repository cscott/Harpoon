// SpedServer.java, created Sat Mar 7 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

import java.net.Socket;
import java.net.ServerSocket;

import harpoon.Analysis.EnvBuilder.Environment;
import harpoon.Analysis.ContBuilder.VoidContinuation;
import harpoon.Analysis.ContBuilder.VoidResultContinuation;
import harpoon.Analysis.ContBuilder.ObjectContinuation;
import harpoon.Analysis.ContBuilder.ObjectResultContinuation;

import harpoon.Analysis.ContBuilder.Scheduler;

/**
 * Runs a Single Process Event Driven web server
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: SpedServer.java,v 1.3 2000-03-24 02:03:28 govereau Exp $
 */
public class SpedServer
    extends VoidContinuation
    implements VoidResultContinuation, ObjectResultContinuation
{
		/**
		 * <code>SpedEnv</code> holds the environment for
		 * instances of <code>SpedServer</code> class
		 */
    public class SpedEnv implements Environment {
		int port;
		String args[];
		ServerSocket listenSocket;
		
		public SpedEnv(String args[]) {
			this.args = args;
			port = 0;
			listenSocket = null;
		}
    }
    
		//
		// Constructors
		//
    private SpedEnv env;
    public SpedServer(String args[]) {
		this.env = new SpedEnv(args);
    }
    
    public SpedServer(SpedEnv env) {
		this.env = env;
    }
    
		//
		// VoidResultContinuation interface
		//
    public void resume() {
		try {
			if (env == null)
				throw new Exception("enviornment is null");
			
			if ((env.args == null) || (env.args.length != 1))
				throw new Exception("Usage: Sped <port>");
			
			env.port = Integer.parseInt(env.args[0]);			
			System.out.println("Using port: " + env.port);
			
			env.listenSocket = new ServerSocket(env.port);
			env.listenSocket.makeAsync();
			
			ObjectContinuation oc = env.listenSocket.acceptAsync();			
			oc.setNext(this);
		}
		catch (Throwable ex) {
			exception(ex);
			return;
		}
    }
	
		//
		// ObjectResultContinuation interface
		//
	public void resume(Object result) {
		try {
			Response res = new Response((Socket)result);
			res.start_Async();			

			ObjectContinuation oc = env.listenSocket.acceptAsync();
			oc.setNext(this);
		}
		catch (Throwable ex) {
			exception(ex);
			return;
		}	
	}
	
    public void exception(Throwable t) {
		t.printStackTrace(System.out);
	}
	
    public static void main(String args[]) {
		Scheduler.addReady((VoidResultContinuation)new SpedServer(args));
		Scheduler.loop();
    }
}
