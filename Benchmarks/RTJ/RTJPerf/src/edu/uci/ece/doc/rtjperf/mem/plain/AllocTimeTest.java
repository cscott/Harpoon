// ************************************************************************
//    $Id: AllocTimeTest.java,v 1.1 2002-07-02 15:53:35 wbeebee Exp $
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
package edu.uci.ece.doc.rtjperf.mem.plain;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.PerformanceReport;

public class AllocTimeTest {

    public static String ALLOC_TIME = "AllocTime";
    public static void main(String args[]) {
        final int count = Integer.parseInt(args[0]);
        final int chunckSize = Integer.parseInt(args[1]);
        String path = args[2];
        HighResTimer timer = new HighResTimer();
        byte[] vec;
        PerformanceReport report = new PerformanceReport("AllocTime");
        long start, stop;
        for (int i = 0; i < count; i++) {
            timer.start();
            vec = new byte[chunckSize];
            vec = null;
            timer.stop();
            report.addMeasuredVariable(ALLOC_TIME + chunckSize, timer.getElapsedTime());
            timer.reset();
        }

        try {
            report.generateDataFile(path);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }
}
