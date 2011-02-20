// ************************************************************************
//    $Id: MemoryTypeArgumentValidator.java,v 1.1 2002-07-02 15:55:07 wbeebee Exp $
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

/**
 * Describe class <code>MemoryTypeArgumentValidator</code> here.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class MemoryTypeArgumentValidator implements Validator {

    private static final String[] MEM_AREA_TYPE = {"immortal", "heap", "scoped"};
    
    public Object validate(String argValue) throws InvalidArgumentValueException {
        boolean notValid = true;
        
        for (int i = 0; i < MEM_AREA_TYPE.length; ++i) {
            if (argValue.equals(MEM_AREA_TYPE[i])) {
                notValid = false;
                break;
            }
        }
        
        if (notValid)
            throw new InvalidArgumentValueException(argValue + " Is not a valid memory type!");

        return argValue;
    }
}
