// Settings.java, created Wed Sep 14 19:08:36 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.List;
import java.util.Arrays;

import harpoon.Util.Options.Option;

/**
 * <code>Settings</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: Settings.java,v 1.1 2005-09-15 03:40:00 salcianu Exp $
 */
public abstract class Settings {

    public enum StdLib { SUNJDK , CLASSPATH };

    /** The standard library implementation that we use.  Default: CLASSPATH. */
    public static StdLib STD_LIB = StdLib.CLASSPATH;

    /** The version of the STD_LIB that we use.  Default: 0.08
	@see STD_LIB */
    public static String STD_LIB_VER = "0.08";


    public static List<Option> getOptions() {
	return Arrays.<Option>asList
	    (new Option("use-sun-jdk") {
		public void action() {
		    STD_LIB = StdLib.SUNJDK;
		    System.out.println("Use sunjdk standard library");
		}
	    },
	    new Option("use-classpath", "<version>", "Use GNU Classpath implementation of the Java standard library") {
		public void action() {
		    STD_LIB = StdLib.CLASSPATH;
		    STD_LIB_VER = getArg(0);
		    System.out.println("Use GNU Classpath " + STD_LIB_VER + " standard library");
		}
	    });
    }


    /** Returns a string that describes the standard library
        implementation that we use.  E.g., "classpath-0.08".  */
    public static String getStdLibVerName() {
	switch(STD_LIB) {
	case SUNJDK:
	    return "sunjdk";
	case CLASSPATH:
	    return "classpath-" + STD_LIB_VER;
	}
	// should not happen
	throw new Error("unknown STD_LIB " + STD_LIB);
    }

}
