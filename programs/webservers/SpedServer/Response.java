
import java.io.*;
import java.net.*;

import harpoon.Analysis.ContBuilder.*;

/**
 * handles http responses
 *
 * @author P.Govereau govereau@mit.edu
 */
public class Response extends Thread {

		/**
		 * Env for response
		 */
    class ResponseEnv {
	OutputStream out;
	InputStream in;
	ByteArrayOutputStream req;
	String filename;
	public ResponseEnv() {
	    out = null;
	    in = null;
	    req = null;
	    filename = null;
	}
    }

    //
    // Constructor
    //
    private Socket clientSocket;
    private final int bufferLength = 2048; // max http request size
    Response(Socket s) {
	clientSocket = s;
    }

    // SPED Server
    public VoidContinuation run_Async() {
	return new ReadRequest(new ResponseEnv());
    }
    
    /**
     * Reads HTTP request and starts async file io
     *
     */
    class ReadRequest
	extends VoidContinuation
	implements VoidResultContinuation, IntResultContinuation
    {
	//
	// Constructor
	//
	int length;
	byte[] buffer;
	ResponseEnv env;
	public ReadRequest(ResponseEnv env) {
	    this.env = env;
	    Scheduler.addReady(this);
	}
	
	public void exception(Throwable t) {}
	//
	// VoidResultContinuation interface
	//
	public void resume() {
	    try {
		env.out = clientSocket.getAsyncOutputStream();
		env.in = clientSocket.getAsyncInputStream();
		env.req = new ByteArrayOutputStream(bufferLength);
		buffer = new byte[bufferLength];
		
		IntContinuation ic = env.in.readAsync(buffer, 0, bufferLength);
		ic.setNext(this);
	    } catch (Throwable ex) {
		exception(ex);
	    }
	}

	//
	// IntResultContinuation interface
	//
	public void resume(int result) {
	    try {
		length = result;
		if (length <= 2) { // this must be a crlf
		    JhttpWorker w = new JhttpWorker();
		    w.method(new BufferedReader(new StringReader(env.req.toString())));
		    env.filename = "." + w.fileName;
		    new ReadFile(env);
		} else {
		    env.req.write(buffer, env.req.size(), length);
		    
		    JhttpWorker w = new JhttpWorker();
		    w.method(new BufferedReader(new StringReader(env.req.toString())));
		    env.filename = "." + w.fileName;
		    
		    String header = new String("HTTP/1.0 200 OK\n\n");
		    env.out.write(header.getBytes(), 0, header.length());
		    
		    new ReadFile(env);
		    
		    if (true) return;
		    IntContinuation ic = env.in.readAsync(buffer, 0, bufferLength);
		    ic.setNext(this);
		} 
	    } catch (Throwable ex) {
		exception(ex);
	    }
	}		
    }
    
    /**
     * Reads a file and writes it to env.out
     *
     */
    class ReadFile
	extends VoidContinuation
	implements VoidResultContinuation, IntResultContinuation
    {
	//
	// Constructor
	//
	int length;
	byte[] buffer;
	FileInputStream in;
	ResponseEnv env;
	public ReadFile(ResponseEnv env) {
	    this.env = env;
	    Scheduler.addReady(this);
	}

	public void exception(Throwable t) {}
	
	//
	// VoidResultContinuation interface
	//
	public void resume() {
	    try {
		buffer = new byte[bufferLength];
		in = new FileInputStream(env.filename);
		IntContinuation ic = in.readAsync(buffer, 0, bufferLength);
		ic.setNext(this);
	    } catch (Throwable ex) {
		System.out.println(ex);
	    }
	}
	
	//
	// IntResultContinuation interface
	//
	public void resume(int result) {
	    try {
		length = result;
		if (length == -1) {
		    clientSocket.close();
		    env.in.close();
		    env.out.close();
		    in.close();
		} else {
		    env.out.write(buffer, 0, length);
		    IntContinuation ic = in.readAsync(buffer, 0, bufferLength);
		    ic.setNext(this);
		} 
	    } catch (Throwable ex) {
		System.out.println(ex);
	    }
	}		
    }
}








