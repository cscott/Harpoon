// ************************************************************************
//    $Id: TimeoutHandler.java,v 1.1 2002-07-02 15:54:55 wbeebee Exp $
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
package edu.uci.ece.doc.rtjperf.timer;

import edu.uci.ece.ac.time.HighResTimer;
import edu.uci.ece.ac.time.HighResTime;
import edu.uci.ece.ac.time.PerformanceReport;
import edu.uci.ece.ac.concurrent.EventVariable;
import javax.realtime.*;


public class TimeoutHandler extends javax.realtime.BoundAsyncEventHandler {

    TimeoutHandler(SchedulingParameters scheduling,
                   ReleaseParameters release,
                   MemoryParameters memory,
                   MemoryArea area,
                   ProcessingGroupParameters group,
                   boolean nonheap,
                   Runnable logic)
    {
        super(scheduling,
              release,
              memory,
              area,
              group,
              nonheap,
              logic);
    }
}

