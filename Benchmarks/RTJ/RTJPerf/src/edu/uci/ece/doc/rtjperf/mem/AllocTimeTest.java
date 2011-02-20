// ************************************************************************
//    $Id: AllocTimeTest.java,v 1.1 2002-07-02 15:53:25 wbeebee Exp $
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
package edu.uci.ece.doc.rtjperf.mem;

// -- RTJava Import --
import javax.realtime.MemoryArea;
import javax.realtime.LTMemory;
import javax.realtime.VTMemory;
import javax.realtime.RealtimeThread;
import javax.realtime.CTMemoryArea;
import javax.realtime.PriorityParameters;

// -- jTools Import --
import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResClock;
import edu.uci.ece.ac.time.PerformanceReport;
import edu.uci.ece.ac.jargo.*;

// -- RTJPerf Import --
import edu.uci.ece.doc.rtjperf.util.RTJPerfArgs;

/**
 * This test takes care of measuring the time necessary to allocated
 * different sizes of object.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class AllocTimeTest {

    private static String outDir;
    
    static class MemAllocatorLogic implements Runnable {

        public static String ALLOC_TIME;

        private int count;
        private int allocSize;
        private String reportName;
        private MemoryArea memArea;
        private int memType;
        
        public MemAllocatorLogic(int count, int allocSize,
                                 MemoryArea memArea,
                                 String reportName,
                                 String varName,
                                 int memType) {
 
            this.count = count;
            this.allocSize = allocSize;
            this.reportName = reportName;
            this.memArea = memArea;
            this.ALLOC_TIME = varName;
            this.memType = memType;
        }
           
        public void run() {
            System.out.println("---------------------> Test Started <------------------ ");
            HighResTimer timer = new HighResTimer();
            byte[] vec;
            long start, stop;
            timer.start();
            timer.stop();
            timer.reset();
            PerformanceReport report = new PerformanceReport("AllocTime");
            for (int i = 0; i < this.count; ++i) {
                timer.start();
                vec = new byte[allocSize];
                timer.stop();
                vec = null;
                report.addMeasuredVariable(ALLOC_TIME, timer.getElapsedTime());
            }
            try {
                report.generateDataFile(AllocTimeTest.outDir + "/AllocTime" + this.memType);
            }
            catch (java.io.IOException e) {
                e.printStackTrace();
            }
            System.out.println("---------------------> Test Completed <------------------ ");
        }
    }

    static ArgParser parseArgs(String[] args) throws Exception {

        CommandLineSpec cls = new CommandLineSpec();
        cls.addRequiredArg(RTJPerfArgs.COUNT_OPT);
        cls.addRequiredArg(RTJPerfArgs.SCOPED_MEMORY_TYPE_OPT);
        cls.addRequiredArg(RTJPerfArgs.MEM_SIZE_OPT);
        cls.addRequiredArg(RTJPerfArgs.OUT_DIR_OPT);
        cls.addRequiredArg(RTJPerfArgs.ALLOC_SIZE_OPT);
        
        ArgParser argParser = new ArgParser(cls, new AllocTimeTestHelpHandler());
        argParser.parse(args);
        return argParser;
    }
    
    public static void main(String[] args) throws Exception {
        ArgParser argParser = parseArgs(args);

        Integer value = (Integer)argParser.getArg(RTJPerfArgs.COUNT_OPT).getValue();
        final int count = value.intValue();

        value = (Integer)argParser.getArg(RTJPerfArgs.ALLOC_SIZE_OPT).getValue();
        final int allocSize = value.intValue();

        value = (Integer)argParser.getArg(RTJPerfArgs.SCOPED_MEMORY_TYPE_OPT).getValue();
        final int memType = value.intValue();

        value = (Integer)argParser.getArg(RTJPerfArgs.MEM_SIZE_OPT).getValue();
        long memSize = value.intValue();

        System.out.println("Allocating Memory area of: " + memSize + " bytes");
        final MemoryArea memArea = MemoryAreaFactory.createMemoryArea(memSize, memSize, memType);

        AllocTimeTest.outDir = (String)argParser.getArg(RTJPerfArgs.OUT_DIR_OPT).getValue();
        
        RealtimeThread rt = new RealtimeThread() {
                public void run() {
                    Runnable logic = new MemAllocatorLogic(count,
                                                           allocSize,
                                                           memArea,
                                                           "AllocTime",
                                                           "AllocTime" + allocSize,
                                                            memType);
                    memArea.enter(logic);
                }
            };
        rt.setSchedulingParameters(new PriorityParameters(30));
        rt.start();
    }
}            
    
