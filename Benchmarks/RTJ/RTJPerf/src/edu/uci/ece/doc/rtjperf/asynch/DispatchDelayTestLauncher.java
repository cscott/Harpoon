// ************************************************************************
//    $Id: DispatchDelayTestLauncher.java,v 1.1 2002-07-02 15:53:04 wbeebee Exp $
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
import edu.uci.ece.ac.jargo.*;
import edu.uci.ece.ac.time.*;

// -- RTJPerf Utils Import --
import edu.uci.ece.doc.rtjperf.util.*;

// -- RTJava Import --
import javax.realtime.AsyncEventHandler;
import javax.realtime.AsyncEvent;
import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.SchedulingParameters;
import javax.realtime.ReleaseParameters;
import javax.realtime.MemoryParameters;
import javax.realtime.MemoryArea;
import javax.realtime.ImmortalMemory;
import javax.realtime.ProcessingGroupParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;
import javax.realtime.HeapMemory;
import javax.realtime.AperiodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.ThreadedAsyncEventHandler;
import javax.realtime.CTMemoryArea;
import javax.realtime.ScopedMemory;

import edu.uci.ece.doc.rtjperf.sys.*;

/**
 * This class takes care of acquiring and setting up all the
 * parameters needed to run the test.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class DispatchDelayTestLauncher {

    private AsyncEventHandler eventHandler;
    private EventHandlerLogic logic;
    
    private int fireCount;
    private int handlerPriority = PriorityScheduler.MAX_PRIORITY;
    private boolean noHeap;
    private boolean threadBound;
    private String outDir;
    private boolean memProfiling = false;
    private int profileStep;
    private SchedulingParameters schedParams;
    private ReleaseParameters releaseParams;
    private MemoryParameters memoryParams;
    private MemoryArea memoryArea;
    private ProcessingGroupParameters procGroupParams;
        
    private ArgParser argParser;
    protected PerformanceTestCase testCase;
    

    public DispatchDelayTestLauncher(final String[] args) throws Exception {
        this.argParser = parseArgs(args);
        this.init();
    }

    public void launchTest() throws Exception {
        this.testCase.run();
        PerformanceReport report = testCase.getPerformanceReport();
        report.generateDataFile(this.outDir);
    }


    private void init() throws Exception {
        ArgValue cla; 

        cla = argParser.getArg(RTJPerfArgs.OUT_DIR_OPT.getName());
        this.outDir = (String)cla.getValue();
        
        if (this.argParser.isArgDefined(RTJPerfArgs.THREAD_BOUND_OPT.getName()))
            this.threadBound = true;
        else
            this.threadBound = false;

        if (this.argParser.isArgDefined(RTJPerfArgs.NO_HEAP_OPT.getName()))
            this.noHeap = true;
        else
            this.noHeap = false;
        
        if (this.argParser.isArgDefined(RTJPerfArgs.HANDLER_PRIORITY_OPT.getName())) {
            cla = this.argParser.getArg(RTJPerfArgs.HANDLER_PRIORITY_OPT.getName());
            this.handlerPriority = ((Integer)cla.getValue()).intValue();
            cla = null;
        }

        this.schedParams = new PriorityParameters(this.handlerPriority);

        cla = this.argParser.getArg(RTJPerfArgs.FIRE_COUNT_OPT.getName());
        this.fireCount = ((Integer)cla.getValue()).intValue();
        cla = null;
        
        if (this.argParser.isArgDefined(RTJPerfArgs.MEMORY_AREA_OPT.getName())) {
            cla = this.argParser.getArg(RTJPerfArgs.MEMORY_AREA_OPT.getName());
            this.memoryArea =SingletonMemoryAreaAccessor.instance((String)cla.getValue());
        }
        else 
            memoryArea =
                SingletonMemoryAreaAccessor.instance(SingletonMemoryAreaAccessor.HEAP_MEMORY);
        

        cla = this.argParser.getArg(RTJPerfArgs.MEM_PROFILE_OPT.getName());
        if (cla != null) {
            this.memProfiling = true;
            this.profileStep = ((Integer)cla.getValue()).intValue();
        }
        // Set up remaining parameters...

        this.memoryParams = null;
        this.procGroupParams = null;
        this.releaseParams = new AperiodicParameters(new RelativeTime(10, 0), // cost
                                                     null,                    // deadline
                                                     null,                    // overrun handler
                                                     null);                   // miss handler
        
        this.logic = new EventHandlerLogic(this.memProfiling);
        if (this.threadBound) {
            this.eventHandler = new ThreadBoundAsynchHandler(this.schedParams,
                                                             this.releaseParams,
                                                             this.memoryParams,
                                                             this.memoryArea,
                                                             this.procGroupParams,
                                                             this.noHeap,
                                                             this.logic);
        }
        else {
            this.eventHandler = new ThreadedAsyncEventHandler(this.schedParams,
                                                              this.releaseParams,
                                                              this.memoryParams,
                                                              this.memoryArea,
                                                              this.procGroupParams,
                                                              this.noHeap,
                                                              this.logic);
        }

        int testThreadPriority;
        if (this.handlerPriority > PriorityScheduler.MIN_PRIORITY + 1)
            testThreadPriority = this.handlerPriority - 1;
        else
            testThreadPriority = PriorityScheduler.MIN_PRIORITY;

        AsyncEvent event = new AsyncEvent();
        cla = this.argParser.getArg(RTJPerfArgs.LP_ASYNC_HANDLER_NUMBER_OPT.getName());
        
        if (cla != null) {
            int handlerNum = ((Integer)cla.getValue()).intValue();
            cla = cla = this.argParser.getArg(RTJPerfArgs.LP_ASYNC_HANDLER_PRIORITY_OPT.getName());
            int handlerPrio;
            if (cla != null) {
                handlerPrio = ((Integer)cla.getValue()).intValue();
                cla = null;
            }
            else
                handlerPrio = testThreadPriority - 1;

            PriorityParameters pp = new PriorityParameters(handlerPrio);
            Runnable noOpLogic = new Runnable() {
                    public void run() {}
                };
            ThreadBoundAsynchHandler handler = new ThreadBoundAsynchHandler(pp,
                                                                            this.releaseParams,
                                                                            this.memoryParams,
                                                                            this.memoryArea,
                                                                            this.procGroupParams,
                                                                            this.noHeap,
                                                                            noOpLogic);

            for (int i = 0; i < handlerNum; ++i)
                event.addHandler(handler);
            
        }
        
        this.testCase = new AsyncEventHandlerDispatchDelayTest(this.eventHandler,
                                                               event,
                                                               this.logic,
                                                               this.fireCount,
                                                               new PriorityParameters(testThreadPriority),
                                                               this.noHeap,
                                                               this.memoryArea,
                                                               this.memProfiling,
                                                               this.profileStep);

        this.argParser = null;
    }

    public MemoryArea getMemoryArea() {
        return this.memoryArea;
    }

    private static ArgParser parseArgs(String[] args) throws Exception {
        CommandLineSpec spec = new CommandLineSpec();

        // -- Required Args --
        spec.addRequiredArg(RTJPerfArgs.OUT_DIR_OPT);
        spec.addRequiredArg(RTJPerfArgs.FIRE_COUNT_OPT);

        // -- Optional Args --
        spec.addArg(RTJPerfArgs.THREAD_BOUND_OPT);
        spec.addArg(RTJPerfArgs.MEMORY_AREA_OPT);
        spec.addArg(RTJPerfArgs.NO_HEAP_OPT);
        spec.addArg(RTJPerfArgs.HANDLER_PRIORITY_OPT);
        
        ArgParser argParser = new ArgParser(spec, new TestHelpHandler());
        argParser.parse(args);
        return argParser;
    }
    
    public static void main(String[] args)  throws Exception {
        final String[] fargs = args;
        ArgParser argParser = parseArgs(args);
        String memType = "heap";
        if (argParser.isArgDefined(RTJPerfArgs.MEMORY_AREA_OPT.getName()))
            memType =
                (String)argParser.getArg(RTJPerfArgs.MEMORY_AREA_OPT.getName()).getValue();
        
        final MemoryArea memoryArea = SingletonMemoryAreaAccessor.instance(memType);
        argParser = null;
        
        // This thread is used to guarantee that all the memory
        // allocated during the test is allocated out of the type of
        // memory specified by the command line argument --memoryArea
        final Runnable logic = new Runnable() {
                public void run() {
                    try {
                        final DispatchDelayTestLauncher testLauncher =
                            new DispatchDelayTestLauncher(fargs);
                        System.out.println("--- Test Started ---");
                        testLauncher.launchTest();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            };
        
        Runnable rtlogic = new Runnable() {
                public void run() {
                    memoryArea.enter(logic);
                }
            };
        
        // MemoryArea.enter can only be called by a RealtimeThread
        PriorityParameters pp =
            new PriorityParameters((PriorityScheduler.MAX_PRIORITY - PriorityScheduler.MIN_PRIORITY)/2);

        RealtimeThread rtThread = new RealtimeThread(pp,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     rtlogic);
        
        rtThread.start();
        rtThread.join();
        System.out.println("--- Test Completed ---");
    }
}


    
