import java.io.*;
import java.net.*;

import harpoon.Analysis.ContBuilder.*;

public class SWorker extends Thread{
    Socket clientSocket;
    int bufferLength;

    SWorker(Socket s, int l) {
	clientSocket= s;
	bufferLength= l;
    }

    public VoidContinuation run_Async() {
	return new RunC1( new RunE () );
    }

    class RunE {
	OutputStream out;
	InputStream in;
	byte[] buffer;
	int length;
    }

    class RunC1 extends VoidContinuation implements VoidResultContinuation
    {
    private Continuation link;

    public void setLink(Continuation newLink) { 
	link= newLink;
    }

    public Continuation getLink() { 
	return link;
    }

	RunE env;

        RunC1(RunE env) {
	    this.env= env;
	    Scheduler.addReady(this);
	}

	public void resume() {
	    try {
	    env.out= clientSocket.getAsyncOutputStream();
	    env.in= clientSocket.getAsyncInputStream();
	    env.buffer= new byte[bufferLength];
	    IntContinuation ic= env.in.readAsync(env.buffer, 0, bufferLength);
	    RunC2 thisC= new RunC2(env);
	    ic.setNext(thisC);
	    thisC.setNext(next);
	    } catch (Throwable t) {
		exception(t);
	    }
	}

	public void exception(Throwable ex)
	{
	    if (next!=null) next.exception(ex);
	    else ex.printStackTrace(System.err);
	}

    }

    class RunC2 extends VoidContinuation implements IntResultContinuation
    {

    private Continuation link;

    public void setLink(Continuation newLink) { 
	link= newLink;
    }

    public Continuation getLink() { 
	return link;
    }	RunE env;

        RunC2(RunE env) {
	    this.env= env;
	}

	public void resume(int result) {
	    try {
		// goto trick
		do {
		    env.length= result;
		    //System.out.println(result);
		    if (env.length==-1) break;
		    env.out.write(env.buffer, 0, env.length);
		    IntContinuation ic= env.in.readAsync(env.buffer, 0, bufferLength);
		    RunC2 thisC= new RunC2(env);
		    ic.setNext(thisC);
		    thisC.setNext(next);
		    return;
		} while (false);
		//System.out.println("Closing...");
		clientSocket.close();
	    } catch (Throwable t) {
		exception(t);
	    }
	}

	public void exception(Throwable ex)
	{
	    if (next!=null) next.exception(ex);
	    else ex.printStackTrace(System.err);
	}

    }    
}

