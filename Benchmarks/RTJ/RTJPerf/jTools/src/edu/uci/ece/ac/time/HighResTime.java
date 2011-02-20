// ************************************************************************
//    $Id: HighResTime.java,v 1.1 2002-07-02 15:35:35 wbeebee Exp $
// ************************************************************************
//
//                               jTools
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
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.ac.time;

/**
 * This class provide a representation for high resolution time.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class HighResTime {

    private long msec;
    private long usec;
    private long nsec;
    
    public HighResTime() { }


    public HighResTime(long msec, long usec, long nsec) {
        this.msec = msec;
        this.usec = usec;
        this.nsec = nsec;
    }

    public HighResTime(long msec, long usec) {
        this(msec, usec, 0L);
    }

    public long getMilliSec() {
        return this.msec;
    }

    public void setMilliSec(long msec) {
        this.msec = msec;
    }

    public long getMicroSec() {
        return this.usec;
    }

    public void setMicroSec(long usec) {
        this.usec = usec;
    }

    public long getNanoSec() {
        return this.nsec;
    }

    public void setNanoSec(long nsec) {
        this.nsec = nsec;
    }
    
    public void setTime(long msec, long usec, long nsec) {
        this.msec = msec;
        this.usec = usec;
        this.nsec = nsec;
    }        

    public void incrementBy(long msec, long usec, long nsec) {
        this.msec += msec;
        this.usec += usec;
        this.nsec += nsec;
    }

    public HighResTime add(HighResTime time) {
        return new HighResTime(this.msec + time.msec, this.usec + time.usec);
    }
    
    public void reset() {
        this.msec = 0;
        this.usec = 0;
        this.nsec = 0;
    }
    
    public String toString() {
        float totMillis = this.msec + (float)this.usec/1000 + (float)this.nsec/1000000;
        return Float.toString(totMillis);
    }

    public void printTo(java.io.PrintStream ostream) {
        ostream.println(this.msec + (float)this.usec/1000 + (float)this.nsec/1000000);
    }
}
