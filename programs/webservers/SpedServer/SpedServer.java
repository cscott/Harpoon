
import java.io.*;
import java.net.*;

import harpoon.Analysis.ContBuilder.*;

/**
 * Runs a S.P.E.D web server
 *
 * @author P.Govereau govereau@mit.edu
 */
public class SpedServer
    extends VoidContinuation
    implements VoidResultContinuation
{
		/**
		 * This class holds the env. for SpedServer class
		 *
		 */
    public class SpedEnv {
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
    
    public void exception(Throwable t) {}
    
    //
    // resume (void)
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
	    Acceptor a = new Acceptor(env);
	    oc.setNext(a);
	    a.setNext(next);
	}
	catch (Throwable ex) {
	    exception(ex);
	    return;
	}
    }

    /**
     * Accepts request and starts new async "threads"
     * to handle them
     */
    class Acceptor
	extends VoidContinuation
	implements ObjectResultContinuation
    {
	//
	// Constructor
	//
	private SpedEnv env;
	public Acceptor(SpedEnv env) {
	    this.env = env;
	}
	public void exception(Throwable t) {}
	//
	// ObjectResultContinuation interface
	// this one is scheduled when connections come in
	//
	public void resume(Object result) {
	    try {
		Response res = new Response((Socket)result);
		res.start_Async();
		
		ObjectContinuation oc = env.listenSocket.acceptAsync();			
		Acceptor a = new Acceptor(env);
		oc.setNext(a);
		a.setNext(next);
	    }
	    catch (Throwable ex) {
		exception(ex);
		return;
	    }	
	}
    }

    public static void main(String args[]) {
	Scheduler.addReady((VoidResultContinuation)new SpedServer(args));
	Scheduler.loop();
    }
}
