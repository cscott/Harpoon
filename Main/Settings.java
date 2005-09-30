// Settings.java, created Wed Sep 14 19:08:36 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

import harpoon.ClassFile.Loader;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;

import harpoon.Util.Options.Option;

import jpaul.DataStructs.DSUtil;

/**
 * <code>Settings</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: Settings.java,v 1.5 2005-09-30 02:31:11 salcianu Exp $
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


    /** Checks that the path from the property
        <code>harpoon.class.path</code> contains a supported
        implementation of the standard library.  */
    public static void checkStdLibVersion() {
	switch(STD_LIB) {
	case SUNJDK: 
	    SAMain.messageln("Assume sunjdk 1.1.x is on the path " + classpaths() + "\ncurrently, no way to check");
	    break;
	case CLASSPATH:
	    try {
		HClass gnuConfig = Loader.systemLinker.forName("gnu.classpath.Configuration");
		HField hf = gnuConfig.getField("CLASSPATH_VERSION");
		if(!hf.isConstant()) {
		    System.err.println("Cannot determine GNU classpath version;\n\tgnu.classpath.Configuration.CLASSPATH_VERSION is not a constant field\nASSUME CLASSPATH VERSION " + STD_LIB_VER + " IS CORRECT");
		    return;
		}
		String gnuVer = (String) hf.getConstant();
		if(!STD_LIB_VER.equals(gnuVer)) {		    
		    throw new RuntimeException
			("User/default settings demanded classpath " + STD_LIB_VER + ", but " + gnuVer + 
			 " found instead in paths " + classpaths() +
			 "\nPlease check harpoon.class.path");
		}
		SAMain.messageln("Found GNU classpath-" + STD_LIB_VER + " on the path");
		// TODO: it would be nice to also print where on the path the GNU classpath resides
	    }
	    catch(harpoon.ClassFile.NoSuchClassException ex) {
		throw new RuntimeException
		    ("Cannot find gnu.classpath.Configuration class in paths " + classpaths() + 
		     "\nPlease check harpoon.class.path",
		     ex);
	    }
	    catch(NoSuchFieldError ex) {
		throw new RuntimeException
		    ("Class gnu.classpath.Configuration exists, but it does not have a CLASSPATH_VERSION field", ex);
	    }
	    break;
	}
    }

    private static String classpaths() {
	return
	    DSUtil.iterableToString(new Iterable() {
		public Iterator iterator() { return Loader.classpaths(); }
	    });
    }

}
