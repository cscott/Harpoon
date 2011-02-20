/*
 * %W% %G%
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1996,1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

public class Main {


    static long runBenchmark( String[] args ) {
    
        if( args.length == 0 ) {
   	    args = new String[3];	    
	    args[0] = "" + (200*Context.getSpeed())/100;
	    args[1] = "200";		    
	    args[2] = "input/time-test.model";
	}
	
	return new RayTracer().inst_main( args );
    }


    public static void main( String[] args ) {  	 
        runBenchmark( args );
    }

    
    public long harnessMain( String[] args ) {
        return runBenchmark( args );
    }

  
}
