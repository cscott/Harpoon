// Purity.java, created Tue Sep 20 10:30:44 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.List;
import java.util.LinkedList;

import java.net.URL;


/**
 * <code>Purity</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: Purity.java,v 1.5 2005-11-06 21:16:11 salcianu Exp $
 */
public class Purity {

    /** Version number for the Purity tool. */
    public static String VERSION = "0.03";
    
    /** Convenient entry point for the purity analysis.
        <code>args[0]</code> should be the main class of the
        application you want to analyze.  <code>args[1]</code> should
        be the path where your application resides.  The analysis will
        load the analyzed classes from this path and from the GNU
        classpath implementation of the Java standard library (if your
        application invokes the standard library); you do NOT need to
        pass the path to GNU classpath: we use the
        <code>glibj.zip</code> included in the purity analysis kit.

	<p>
	The rest of the arguments are passed verbatim to
	<code>SAMain.main</code>

	<p>If you encounter problems, try using */
    public static void main(String[] args) {
	System.out.println("Purity Analysis Kit version " + VERSION);

	if(args.length < 2) {
	    usageExit();
	}

	String mainClassName = args[0];
	String appPath = args[1];
	
	StringBuffer harpoonCP = new StringBuffer();
	String pathSep = System.getProperty("path.separator");
	harpoonCP.append(appPath);
	
	String libDir = findFlexRoot() + "/lib";
	for(String stdLib : stdLibs) {
	    harpoonCP.append(pathSep);
	    harpoonCP.append(libDir + "/" + stdLib);
	}

	String oldHarpoonCP = System.getProperty("harpoon.class.path");
	if((oldHarpoonCP != null) && (oldHarpoonCP.length() > 0)) {
	    harpoonCP.append(pathSep);
	    harpoonCP.append(oldHarpoonCP);
	}

	System.out.println("harpoon.class.path set to " + harpoonCP);
	System.setProperty("harpoon.class.path", harpoonCP.toString());

	LinkedList<String> sa_args = new LinkedList<String>();
	for(int i = 2; i < args.length; i++) {
	    sa_args.addLast(args[i]);
	}
	sa_args.add("--wp-mutation");
	sa_args.add("--wp-mutation-save");
	sa_args.add("--no-code-gen");
	sa_args.add("--pa2:time-pre");
	sa_args.add("--pa2:stats");

	sa_args.add("-c");
	sa_args.add(mainClassName);

	sa_args.add("-r");
	sa_args.add("Support/locale-root-set-classpath");
	
	SAMain.main(sa_args.toArray(new String[sa_args.size()]));
    }


    private static String[] stdLibs = new String[] { 
	"reflect-thunk.jar",
	"cpvm.jar",
	"glibj-0.08-extra.jar",
	"glibj-0.08.zip"
    };


    private static final String cannotFindRoot = "FATAL: cannot find the Flex root";

    private static String findFlexRoot() {
	// file.separator should really be called dir.separator ..
	String fileSep = System.getProperty("file.separator");
	if(fileSep == null || fileSep.equals("")) {
	    System.out.println("WARNING sys property file.separator = \"" + fileSep + "\"; assume it is /");
	    fileSep = "/";	    
	}
	String thisClassName = Purity.class.getName().replace('.', fileSep.charAt(0)) + ".class";

	URL url = ClassLoader.getSystemResource(thisClassName);
	assert (url != null) : cannotFindRoot;
	String urlStr = url.toString();
	System.out.println("Purity entry point url = " + urlStr);

	int lastCol = urlStr.lastIndexOf(':');
	String fileName = lastCol == -1 ? urlStr : urlStr.substring(lastCol+1);
	
	String root = chop(chop(fileName, thisClassName), fileSep);

	if(root.endsWith(".jar") || root.endsWith(".jar!") || 
	   root.endsWith(".zip") || root.endsWith(".zip!")) {
	    int index = root.lastIndexOf(fileSep);
	    assert index != -1 : cannotFindRoot;
	    root = root.substring(0, index);
	}

	System.out.println("Flex Root = " + root);

	return root;
    }


    private static void usageExit() {
	System.out.println("Usage:\n\tjava harpoon.Main.Purity <mainClassName> <appPath> [ <optionalArgs> ]\n\nThe optional arguments <optionalArgs> are passed verbatim to <code>SAMain.main</code>.  Execute\n\tjava harpoon.Main.SAMain -h\nto see the possible parameters.  You should make sure that purity.jar, jpaul.jar, and jutil.jar are all on your JVM's CLASSPATH.");
	System.exit(1);
    }


    private static String chop(String s, String suffix) {
	if(s.endsWith(suffix)) {
	    return s.substring(0, s.length() - suffix.length());
	}
	return s;
    }

}
