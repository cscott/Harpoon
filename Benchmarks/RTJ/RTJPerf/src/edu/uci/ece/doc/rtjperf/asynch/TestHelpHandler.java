// ************************************************************************
//    $Id: TestHelpHandler.java,v 1.1 2002-07-02 15:53:04 wbeebee Exp $
// ************************************************************************
//
//                               RTJPerf
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.doc.rtjperf.asynch;

// -- jTools Import --
import edu.uci.ece.ac.jargo.HelpHandler;


public class TestHelpHandler extends HelpHandler {
    
    static String msg = "\nUSAGE: The following option are available for this test (option marked \n" +
        "with a (*) are compulsory: \n\n\n" +
        "--threadBoundHandler         If this option is present the BoundAsyncEvent-\n" +
        "                             Handler is used.\n\n" +
        "--noHeap                     If this option is present no heap memory is used.\n\n" +
        
        "--handlerPriority <priority>  Defines the priority at which the handler is\n" +
        "                              run (the default is PriorityScheduler.MAX_PRIORITY.\n\n" +
        
        "--fireCount <fire-count> (*)  Sets the number of times that the event has to be\n" +
        "                              fired during the test\n\n" +
        
        "--memoryArea <mem-area-type>  Sets the memory area type to be used; available\n" +
        "                               option are \"immortal\" and \"heap\"\n\n" +
        
        "--memProfiling <profile-step> Sets the memory profile mode.\n\n" +
        

        "--outDir <dir-path>      (*)  Sets directory under which the result of the\n" +
        "                              test have to be saved.\n\n"  +
        
        "--lpAsyncHandlerNumber <num>  Sets the number of secondary handler that\n"+
        "                              are registered before the main handler\n\n" +

        "--lpAsyncHandlerPriority <p>  Sets the priority for the of secondary handler\n"+
        "                              that are registered before the main handler\n\n" ;
       
    public TestHelpHandler() {
        super(msg);
    }
}
