// ************************************************************************
//    $Id: TimerDemo.java,v 1.1 2002-07-02 15:34:51 wbeebee Exp $
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
package demo.time;

// -- jTools Import --
import edu.uci.ece.ac.time.*;
import edu.uci.ece.ac.jargo.*;

public class TimerDemo {

    private static ArgParser parseArgs(String[] args) throws Exception {
        CommandLineSpec cls = new CommandLineSpec();
        cls.addRequiredArg(new ArgSpec("iteration",
                                       new String[] {"edu.uci.ece.ac.jargo.NaturalValidator"}));
        
        ArgParser argParser = new ArgParser(cls);
        cls = null;
        argParser.parse(args);

        return argParser;
    }

    public static void main(String args[]) throws Exception {
            ArgParser argParser = parseArgs(args);
            ArgValue av = argParser.getArg("iteration");
            int count = ((Integer)av.getValue()).intValue();

            HighResTimer timer = new HighResTimer();
            timer.start();
            for (int i = 0; i < count; i++) { }
            timer.stop();

            System.out.println("Executed " + count + " iteration in: " + timer.getElapsedTime() + " msec");
    }
}
