import java.io.*;
import java.net.*;

import harpoon.Analysis.ContBuilder.*;

public class SMain {
    public static void main(String args[] ) throws Exception
    {
	_main(args);
	Scheduler.loop();
    }

    public static VoidContinuation _main(String args[]) throws Exception
    {
	return new MainC1( new MainE(args) );
    }
}

class MainE {
    int p,l;
    String args[];
    ServerSocket s;

    public MainE(String args[]) {
	this.args= args;
    }
}

class MainC1 extends VoidContinuation implements VoidResultContinuation
{
    private Continuation link;

    public void setLink(Continuation newLink) { 
	link= newLink;
    }

    public Continuation getLink() { 
	return link;
    }
    MainE env;

    public MainC1(MainE env)
    {
	this.env= env;
	Scheduler.addReady(this);
    }

    public void resume()
    {
	try {
	    env.p= Integer.valueOf(env.args[0]).intValue();
	    env.l= Integer.valueOf(env.args[1]).intValue();
	    
	    env.s= new ServerSocket(env.p);
	    env.s.makeAsync();
	    System.out.println(env.s);
	    ObjectContinuation oc= env.s.acceptAsync();
	    MainC2 newC= new MainC2(env);
	    oc.setNext(newC);
	    newC.setNext(next);
	} catch (Throwable ex) { exception(ex); return; }
    }

    public void exception(Throwable ex) {
	if (next!=null) next.exception(ex);
	// can be top level
	else {
	    ex.printStackTrace(System.err);
	    System.exit(1);
	}
    }
}

class MainC2 extends VoidContinuation implements ObjectResultContinuation
{

    private Continuation link;

    public void setLink(Continuation newLink) { 
	link= newLink;
    }

    public Continuation getLink() { 
	return link;
    }
    MainE env;

    public MainC2(MainE env)
    {
	this.env= env;
    }

    public void resume(Object result)
    {
	try {
	    Socket c= (Socket) result;
	    SWorker w= new SWorker(c, env.l);
	    w.start_Async();
	    ObjectContinuation oc= env.s.acceptAsync();
	    MainC2 newC= new MainC2(env);
	    oc.setNext(newC);
	    newC.setNext(next);
	} catch (Throwable ex) { exception(ex); return; }
    }

    public void exception(Throwable ex) {
	if (next!=null) next.exception(ex);
	// can be top level
	else {
	    ex.printStackTrace(System.err);
	    System.exit(1);
	}
    }
}

	


