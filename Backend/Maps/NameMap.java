// NameMap.java, created Fri Aug  6 17:41:55 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.Util.Util;

/**
 * <code>NameMap</code> has the standard munging routines for turning
 * our methods into C-legal methods. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: NameMap.java,v 1.1.2.3 1999-08-06 22:46:32 pnkfelix Exp $
 */
public class NameMap {

    public String mangle(HMethod hm) {
	return NameMap.munge(hm);
    }
    
    public static String munge(HMethod hm) {
	return "_" + "Java_" +
	    mangle(hm.getDeclaringClass().getName()) +
	    "_" + mangle(hm.getName()) + "__" +
	    mangleArgs(hm.getParameterTypes());
    }

    private static String mangle(String s) {
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<s.length(); i++) {
	    switch(s.charAt(i)) {
	    case '.':
	    case '/':
		sb.append("_");
		break;
	    case '_':
		sb.append("_1");
		break;
	    case ';':
		sb.append("_2");
		break;
	    case '[':
		sb.append("_3");
	    default:
		if ((s.charAt(i) >= 'a' &&
		     s.charAt(i) <= 'z') ||
		    (s.charAt(i) >= 'A' &&
		     s.charAt(i) <= 'Z') ||
		    (s.charAt(i) >= '0' &&
		     s.charAt(i) <= '9')) {
		    sb.append(s.charAt(i));
		} else {
		    //Util.assert(false, "Ack, "+s.charAt(i)+" is
		    // probably Unicode!  " + s);
		    String hexval=Integer.toHexString((int)s.charAt(i));
		    while(hexval.length()<4) hexval="0"+hexval;
		    sb.append("_0" + hexval);
		}
	    }
	}
	return sb.toString();
    }

    private static String mangleArgs(HClass[] types) {
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<types.length; i++) {
	    sb.append(mangle(types[i].getDescriptor()));
	}
	return sb.toString();
    }

    /** Creates a <code>NameMap</code>. */
    private NameMap() {
        
    }
    
}
