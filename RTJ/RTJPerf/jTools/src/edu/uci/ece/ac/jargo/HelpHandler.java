// ************************************************************************
//    $Id: HelpHandler.java,v 1.1 2002-07-02 15:35:26 wbeebee Exp $
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

public class HelpHandler {

    private String helpMessage;
    
    public HelpHandler(String helpMessage) {
        this.helpMessage = helpMessage;
    }
    
    public void handleHelp() {
        System.out.println(helpMessage);
        System.exit(0);
    }
}
