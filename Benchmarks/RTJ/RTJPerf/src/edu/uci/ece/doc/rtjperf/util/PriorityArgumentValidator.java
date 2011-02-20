// ************************************************************************
//    $Id: PriorityArgumentValidator.java,v 1.1 2002-07-02 15:55:07 wbeebee Exp $
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
package edu.uci.ece.doc.rtjperf.util;

// -- jTools Imports --
import edu.uci.ece.ac.jargo.Validator;
import edu.uci.ece.ac.jargo.InvalidArgumentValueException;

// -- RTJava Import --
import javax.realtime.PriorityScheduler;

/**
 * Describe class <code>MemoryTypeArgumentValidator</code> here.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class PriorityArgumentValidator implements Validator {
    
    public Object validate(String argValue) throws InvalidArgumentValueException {
        int prioValue = -1;
        try {
            prioValue = Integer.parseInt(argValue);
        }
        catch (Exception e) {
            throw new InvalidArgumentValueException(argValue + " is not an Integer!");
        }
        
        if ((prioValue < PriorityScheduler.MIN_PRIORITY) ||
            (prioValue > PriorityScheduler.MAX_PRIORITY))
            throw new InvalidArgumentValueException(argValue + " is not a valid priority. It should be in the range" +
                                                    PriorityScheduler.MIN_PRIORITY + " - " + PriorityScheduler.MAX_PRIORITY);
        return new Integer(prioValue);
    }
}
