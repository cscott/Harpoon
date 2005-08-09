// Loader.java, created Fri Jul 31  4:33:28 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import harpoon.Util.ArrayIterator;
import net.cscott.jutil.CombineIterator;
import net.cscott.jutil.Default;
import harpoon.Util.EnumerationIterator;
import net.cscott.jutil.FilterIterator;
import harpoon.Util.Util;

import net.cscott.jutil.UnmodifiableIterator;

/** 
 * Class file loader.
 * Looks through CLASSPATH to find resources.  Understands .jar and .zip
 * files.  Platform-independent (hopefully).
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Loader.java,v 1.18 2005-08-09 21:06:49 salcianu Exp $
 */
public abstract class Loader {
  public static boolean VERBOSE = false;
  static abstract class ClasspathElement {
    /** Open a stream to read the given resource, or return 
     *  <code>null</code> if resource cannot be found. */
    abstract InputStream getResourceAsStream(String resourcename);
    /** Iterate over all classes in the given package. */
    abstract Iterator listPackage(String packagename);
  }
  /** A .zip or .jar file in the CLASSPATH. */
  static class ZipFileElement extends ClasspathElement {
    ZipFile zf;
    ZipFileElement(ZipFile zf) { this.zf = zf; }
    public String toString() { return zf.getName(); }
    InputStream getResourceAsStream(String name) {
      name = name.replace('\\','/'); // work around bug in windows java ports
      try { // look for name in zipfile, return null if something goes wrong.
	ZipEntry ze = zf.getEntry(name);
	return (ze==null)?null:zf.getInputStream(ze);
      } catch (UnsatisfiedLinkError e) {
	System.err.println("UNSATISFIED LINK ERROR: "+name);
	return null;
      } catch (IOException e) { return null; }
    }
    Iterator listPackage(final String pathname) {
      // look for directory name first
      final String filesep   = System.getProperty("file.separator");
      /* not all .JAR files have entries for directories, unfortunately.
      ZipEntry ze = zf.getEntry(pathname);
      if (ze==null) return Default.nullIterator;
      */
      return new FilterIterator(new EnumerationIterator(zf.entries()),
				new FilterIterator.Filter() {
	public boolean isElement(Object o) { ZipEntry zze=(ZipEntry) o;
	String name = zze.getName();
	return (!zze.isDirectory()) && name.startsWith(pathname) &&
	  name.lastIndexOf(filesep)==(pathname.length()-1);
	}
	public Object map(Object o) {
	  return ((ZipEntry)o).getName();
	}
      });
    }
    /** Close the zipfile when this object is garbage-collected. */
    protected void finalize() throws Throwable {
        // yes, it is possible to finalize an uninitialized object.
        try { if (zf!=null) zf.close(); } finally { super.finalize(); }
    }
  }
  /** A regular path string in the CLASSPATH. */
  static class PathElement extends ClasspathElement {
    String path;
    PathElement(String path) { this.path = path; }
    public String toString() { return path; }
    InputStream getResourceAsStream(String name) {
      try { // try to open the file, starting from path.
	File f = new File(path, name);
	return new FileInputStream(f);
      } catch (FileNotFoundException e) {
	return null; // if anything goes wrong, return null.
      }
    }
    Iterator listPackage(final String pathname) {
      File f = new File(path,pathname);
      if (!f.exists() || !f.isDirectory()) return Default.nullIterator;
      return new FilterIterator(new ArrayIterator(f.list()),
				new FilterIterator.Filter() {
	public Object map(Object o) { return pathname + ((String)o); }
      });
    }
  }

  /** Static vector of ClasspathElements corresponding to CLASSPATH entries. */
  static final List classpathList = new ArrayList();
  static { // initialize classpathVector.
    Set duplicates = new HashSet(); // don't add duplicates.
    for (Iterator it = classpaths(); it.hasNext(); ) {
      String path = (String) it.next();
      if (duplicates.contains(path)) continue; // skip duplicate.
      else duplicates.add(path);
      if (path.toLowerCase().endsWith(".zip") ||
	  path.toLowerCase().endsWith(".jar"))
	try {
	  classpathList.add(new ZipFileElement(new ZipFile(path)));
	} catch (IOException ex) { /* skip this zip file, then. */ }
      else
	classpathList.add(new PathElement(path));
    }
    ((ArrayList) classpathList).trimToSize(); // save memory.
  }

  /** Iterate over the components of the system CLASSPATH. 
   *  Each element is a <code>String</code> naming one segment of the
   *  CLASSPATH. */
  public static final Iterator classpaths() {
    final String pathsep = System.getProperty("path.separator");
    String classpath = null;

    // allow overriding classpath.
    /*if (classpath==null) classpath = System.getenv("HCLASSPATH");*/
    if (classpath==null) classpath = System.getProperty("harpoon.class.path");
    if (classpath==null) classpath = System.getProperty("java.class.path");
    assert classpath!=null;

    // For convenience, make sure classpath begins with and ends with pathsep.
    if (!classpath.startsWith(pathsep)) classpath = pathsep + classpath;
    if (!classpath.endsWith(pathsep)) classpath = classpath + pathsep;
    final String cp = classpath;

    return new UnmodifiableIterator() {
      int i=0;
      public boolean hasNext() { 
	return (cp.length() > (i+pathsep.length()));
      }
      public Object next() {
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
    assert classname.indexOf('/')==-1; // should have '.' separators.
    String filesep   = System.getProperty("file.separator");
    // Swap all '.' for '/' & append ".class"
    return classname.replace('.', filesep.charAt(0)) + ".class";
  }

  /** Open an <code>InputStream</code> on a resource found somewhere 
   *  in the CLASSPATH.
   * @param name The filename of the resource to locate.
   */
  public static InputStream getResourceAsStream(String name) {
    for (Object cpeO : classpathList) {
      ClasspathElement cpe = (ClasspathElement) cpeO;
      InputStream is = cpe.getResourceAsStream(name);
      if (is!=null) {
	if(VERBOSE) {
	  System.err.println("[LOADING "+new File(cpe.toString(),name)+"]");
	}
	return is; // return stream if found.
      }
    }
    // Couldn't find resource.
    return null;
  }

  /** Returns an iterator of Strings naming the available classes in
   *  the given package which are on the classpath. */
  public static Iterator listClasses(String packagename) {
    final String filesep = System.getProperty("file.separator");
    final String pathname = (packagename.length()==0)?"":
      (packagename.replace('.',filesep.charAt(0))+filesep);
    FilterIterator.Filter cpe2sl = new FilterIterator.Filter() {
      public Object map(Object o) {
	return ((ClasspathElement) o).listPackage(pathname);
      }
    };
    FilterIterator.Filter sl2cl = new FilterIterator.Filter() {
      private Set nodups = new HashSet();
      public boolean isElement(Object o) {
	return ((String)o).toLowerCase().endsWith(".class") &&
	       !nodups.contains(o);
      }
      public Object map(Object o) {
	String name = (String) o; nodups.add(o);
	return name.substring(0,name.length()-6)
	           .replace(filesep.charAt(0),'.');
      }
    };
    return new FilterIterator(new CombineIterator(
           new FilterIterator(classpathList.iterator(), cpe2sl)), sl2cl);
  }

  /** System-linker: the class names resolved by this linker are always
   *  immutable and identical to those on disk.
   */
  public static final Linker systemLinker = new SystemLinker();
  private static class SystemLinker extends Linker implements Serializable {
    protected final HClass forDescriptor0(String descriptor) 
      throws NoSuchClassException {
      assert descriptor.startsWith("L") && descriptor.endsWith(";");
      // classname in descriptor is '/' delimited.
      String className = descriptor.substring(1, descriptor.indexOf(';'));
      className = className.replace('/','.'); // make proper class name.
      InputStream is = 
	  Loader.getResourceAsStream(Loader.classToResource(className));
      if (is == null) throw new NoSuchClassException(className);
      // OK, go ahead and load this.
      try {
	return /*ImplGNU*/ImplMagic.forStream(this, new BufferedInputStream(is));
      } catch (java.lang.ClassFormatError e) {
	throw new NoSuchClassException(className+" ["+e.toString()+"]");
      } catch (java.io.IOException e) {
	throw new NoSuchClassException(className);
      } finally {
	try { is.close(); } catch(java.io.IOException e) { }
      }
    }
    /* Serializable interface: system linker is unique. */
    public Object writeReplace() { return new Stub(); }
    private static final class Stub implements Serializable {
      public Object readResolve() {
	return Loader.replaceWithRelinker ?
	  Loader.systemRelinker : Loader.systemLinker;
      }
    }
  }
  // allow the user to reserialize a systemLinker (and associated classes)
  // as a *relinker*...  evil evil evil evil. [CSA 4-apr-2000]
  final static boolean replaceWithRelinker =
    System.getProperty("harpoon.relinker.hack", "no")
    .equalsIgnoreCase("yes");
  final static Linker systemRelinker = new Relinker(systemLinker);

  /** System code factory: this code factory will return bytecode
   *  representations for classes loaded via the system linker. */
  public static final HCodeFactory systemCodeFactory =
    /*ImplGNU*/ImplMagic.codeFactory;
}
// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
