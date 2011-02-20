package demo.stringgrid;

import org.omg.CosNaming.*;

public class Client
{
    public static void main(String args[]) 
    { 
	try
	{
	    MyServer grid;

	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

	    if( args.length == 1 )
	    {
		// args[0] is an IOR-string 
		grid = MyServerHelper.narrow(orb.string_to_object(args[0]));
	    } 
	    else
	    {
		NamingContextExt nc = 
                    NamingContextExtHelper.narrow(
      		        orb.resolve_initial_references( "NameService" ));

                org.omg.CORBA.Object o = 
                    nc.resolve(nc.to_name("grid"));

		grid = MyServerHelper.narrow(o);

	    }

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
       
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	}
    }
}


