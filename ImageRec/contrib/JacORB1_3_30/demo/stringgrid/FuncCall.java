package demo.stringgrid;

import java.io.*;
import org.omg.CosNaming.*;

public class FuncCall
{
    public static void mainClient(String args[]) 
    { 
	try
	{
	    MyServer grid2;
	    gridImpl grid;

	    synchronized (FuncCall.class) {
		FuncCall.class.wait();
	    }
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

	    if( args.length == 1 )
	    {
		// args[0] is an IOR-string 
		grid2 = MyServerHelper.narrow(orb.string_to_object(args[0]));
	    } 
	    else
	    {
		NamingContextExt nc = 
                    NamingContextExtHelper.narrow(
      		        orb.resolve_initial_references( "NameService" ));

                org.omg.CORBA.Object o = 
                    nc.resolve(nc.to_name("grid"));

		grid2 = MyServerHelper.narrow(o);

	    }

	    grid = new gridImpl();

	    short i = grid.height();
	    System.out.println("Height = " + i);

	    short j = grid.width();
	    System.out.println("Width = " + j);

	    java.io.LineNumberReader file = new java.io.LineNumberReader(new java.io.FileReader("client.times"));
	    String s = file.readLine();
	    boolean print = (file.readLine().equals("print"));
	    file.close();
	    int times = Integer.parseInt(s);
	    if (print)
		System.out.println("Repeating " + times + "times");
	    for(; times >= 0 ; times--){
		// setting the value in all the cell of the grid
		for(short x = 0; x < i; x++)
		    for(short y = 0; y < j; y++){
			java.lang.String value = ("" + (470 + times));
			
  			if (print)
  			    System.out.println("Old value at (" + x + "," + y +"): " + 
  					   grid.get( x,y));
  			else
  			    grid.get(x, y);
			
			if (print)
			    System.out.println("Setting (" + x + "," + y +") to " + value);
			
			grid.set( x, y, value);
			
  			if (print)
  			    System.out.println("New value at (" + x + "," + y +"): " + 
  					   grid.get( x,y));
  			else
  			    grid.get(x, y);
		    }
	    }

//  	    try 
//  	    {
//  		grid.opWithException();
//  	    }
//  	    catch (demo.stringgrid.MyServerPackage.MyException ex) 
//  	    {
//  		System.out.println("MyException, reason: " + ex.why);
//  	    }


            orb.shutdown(true);
            System.out.println("done. ");
	    System.exit(0);
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	}
    }

    public static void mainServer( String[] args )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
       	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();
	    
	    org.omg.CORBA.Object o = 
                poa.servant_to_reference( new gridImpl() );

	    if( args.length == 1 ) 
	    {
	       	// write the object reference to args[0]

		PrintWriter ps = 
                    new PrintWriter(new FileOutputStream(new File( args[0] )));
		ps.println( orb.object_to_string( o ) );
		ps.close();
	    } 
	    else
	    {
                // use the naming service

		NamingContextExt nc = 
                    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
		nc.bind( nc.to_name("grid"), o);
	    }
	    
	    synchronized (FuncCall.class) {
		FuncCall.class.notify();
	    }
            orb.run();
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}        
    }

    public static boolean block = false;
    public static boolean done = false;

    public static void main(final String[] args) {
	Thread t1 = new Thread() {
	    public void run() {
		FuncCall.mainServer(args);
	    }
	};
	Thread t2 = new Thread() {
	    public void run() {
		FuncCall.mainClient(args);
	    }
	};
	try {
	    t1.start();
	    t2.start();
	    t1.join();
	    t2.join();
	} catch (InterruptedException e) {
	    System.out.println("Interrupted: "+e);
	}
    }

}


