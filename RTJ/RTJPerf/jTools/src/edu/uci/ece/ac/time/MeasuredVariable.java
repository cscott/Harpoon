// ************************************************************************
//    $Id: MeasuredVariable.java,v 1.1 2002-07-02 15:35:35 wbeebee Exp $
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
 * The class <code>MeasuredVariable</code> represent a variable
 * measured by some performance test.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class MeasuredVariable {

    private String name;
    private Object value;
    
    public MeasuredVariable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    

    public String toString() {
        return this.name + " = " + this.value.toString();
    }
}
