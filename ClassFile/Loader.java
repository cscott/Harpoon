// Loader.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.io.IOException;
import java.io.FileNotFoundException;

import harpoon.Util.Util;
/** 
 * Class file loader.
 * Looks through CLASSPATH to find resources.  Understands .jar and .zip
 * files.  Platform-independent (hopefully).
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Loader.java,v 1.10.2.5 1999-01-03 03:01:41 cananian Exp $
 */
public abstract class Loader {
  static abstract class ClasspathElement {
    /** Open a stream to read the given resource, or return 
     *  <code>null</code> if resource cannot be found. */
    abstract InputStream getResourceAsStream(String resourcename);
  }
  /** A .zip or .jar file in the CLASSPATH. */
  static class ZipFileElement extends ClasspathElement {
    ZipFile zf;
    ZipFileElement(ZipFile zf) { this.zf = zf; }
    InputStream getResourceAsStream(String name) {
      try { // look for name in zipfile, return null if something goes wrong.
	ZipEntry ze = zf.getEntry(name);
	return (ze==null)?null:zf.getInputStream(ze);
      } catch (IOException e) { return null; }
    }
    /** Close the zipfile when this object is garbage-collected. */
    protected void finalize() throws Throwable {
        try { zf.close(); } finally { super.finalize(); }
    }
  }
  /** A regular path string in the CLASSPATH. */
  static class PathElement extends ClasspathElement {
    String path;
    PathElement(String path) { this.path = path; }
    InputStream getResourceAsStream(String name) {
      try { // try to open the file, starting from path.
	File f = new File(path, name);
	return new FileInputStream(f);
      } catch (FileNotFoundException e) {
	return null; // if anything goes wrong, return null.
      }
    }
  }

  /** Static vector of ClasspathElements corresponding to CLASSPATH entries. */
  static final Vector classpathVector = new Vector();
  static { // initialize classpathVector.
    Hashtable duplicates = new Hashtable(); // don't add duplicates.
    for (Enumeration e = classpaths(); e.hasMoreElements(); ) {
      String path = (String) e.nextElement();
      if (duplicates.containsKey(path)) continue; // skip duplicate.
      else duplicates.put(path, path);
      if (path.toLowerCase().endsWith(".zip") ||
	  path.toLowerCase().endsWith(".jar"))
	try {
	  classpathVector.addElement(new ZipFileElement(new ZipFile(path)));
	} catch (IOException ex) { /* skip this zip file, then. */ }
      else
	classpathVector.addElement(new PathElement(path));
    }
    classpathVector.trimToSize(); // save memory.
  }

  /** Enumerate the components of the system CLASSPATH. 
   *  Each element is a <code>String</code> naming one segment of the
   *  CLASSPATH. */
  public static final Enumeration classpaths() {
    String classpath = System.getProperty("java.class.path");
    final String pathsep = System.getProperty("path.separator");

    // For convenience, make sure classpath begins with and ends with pathsep.
    if (!classpath.startsWith(pathsep)) classpath = pathsep + classpath;
    if (!classpath.endsWith(pathsep)) classpath = classpath + pathsep;
    final String cp = classpath;

    return new Enumeration() {
      int i=0;
      public boolean hasMoreElements() { 
	return (cp.length() > (i+pathsep.length()));
      }
      public Object nextElement() {
	i+=pathsep.length(); // cp begins with pathsep.
	String path = cp.substring(i, cp.indexOf(pathsep, i));
	i+=path.length(); // skip over path.
	return path;
      }
    };
  }

  /** Translate a class name into a corresponding resource name. 
   * @param classname The class name to translate.
   */
  public static String classToResource(String classname) {
    Util.assert(classname.indexOf('/')==-1); // should have '.' separators.
    String filesep   = System.getProperty("file.separator");
    // Swap all '.' for '/' & append ".class"
    return classname.replace('.', filesep.charAt(0)) + ".class";
  }

  /** Open an <code>InputStream</code> on a resource found somewhere 
   *  in the CLASSPATH.
   * @param name The filename of the resource to locate.
   */
  public static InputStream getResourceAsStream(String name) {
    for (Enumeration e = classpathVector.elements(); e.hasMoreElements(); ) {
      ClasspathElement cpe = (ClasspathElement) e.nextElement();
      InputStream is = cpe.getResourceAsStream(name);
      if (is!=null) return is; // return stream if found.
    }
    // Couldn't find resource.
    return null;
  }
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
