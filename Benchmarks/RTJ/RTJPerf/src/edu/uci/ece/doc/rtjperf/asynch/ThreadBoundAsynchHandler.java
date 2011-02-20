// ************************************************************************
//    $Id: ThreadBoundAsynchHandler.java,v 1.1 2002-07-02 15:53:04 wbeebee Exp $
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


// -- RTJava Import --
import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.SchedulingParameters;
import javax.realtime.ReleaseParameters;
import javax.realtime.MemoryParameters;
import javax.realtime.MemoryArea;
import javax.realtime.ProcessingGroupParameters;


// Angelo 31 dec 2001> This class has to be created because the
// BoundAsyncEventHandler is defined as an abstract class, and cannot
// be instantiated.
class ThreadBoundAsynchHandler extends  BoundAsyncEventHandler {

    ThreadBoundAsynchHandler(SchedulingParameters scheduling,
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
