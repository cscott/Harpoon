// ************************************************************************
//    $Id: ArgValue.java,v 1.1 2002-07-02 15:35:26 wbeebee Exp $
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
package edu.uci.ece.ac.jargo;

public class ArgValue {

    Object[] val;
    String name;
    
    ArgValue(ArgSpec spec, String[] val, int offset, int elems)
        throws Exception {
        this.name = spec.getName();
        ArgSpec.ValidatorsIterator iterator = spec.validatorIterator();
        this.val = new Object[elems];

        int i = 0;
        
        while (i < elems) {
            this.val[i] = iterator.next().validate(val[offset + i]);
            ++i;
        }

    }

    public int getArgNum() {
        return this.val.length;
    }

    public Object getValue() {
        return this.val[0];
    }

    public Object getValue(int i) {
        return this.val[i];
    }

    public String getName() {
        return this.name;
    }
    
    public String toString() {
        String str = "Name: " + this.name + " {";
        for (int i = 0; i < this.val.length - 1; i++) 
            str += val[i]+ ", ";
        if (this.val.length != 0)
            str += val[this.val.length - 1];
        str += "}";
        return str;
    }
}
