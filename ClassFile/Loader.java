package harpoon.ClassFile;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.io.IOException;
import java.io.FileNotFoundException;

/** Quick and dirty class file loader.
 *  Looks through CLASSPATH to find the class.  Understands .jar and .zip
 *  files.
 *  @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 *  @version $Id: Loader.java,v 1.1 1998-07-31 08:33:28 cananian Exp $
 */
class Loader {
  public static String classToResource(String classname) {
    String filesep   = System.getProperty("file.separator");
    // Swap all '.' for '/' & append ".class"
    return classname.replace('.', filesep.charAt(0)) + ".class";
  }

  /** Open an InputStream on a resource found somewhere in the CLASSPATH.
   * @param name The filename of the resource to locate.
   */
  public static InputStream getResourceAsStream(String name) {
    String classpath = System.getProperty("java.class.path");
    String pathsep   = System.getProperty("path.separator");

    // For convenience, make sure classpath begins with and ends with pathsep.
    if (!classpath.startsWith(pathsep)) classpath = pathsep + classpath;
    if (!classpath.endsWith(pathsep)) classpath = classpath + pathsep;

    // Separate classpath at pathsep
    while(classpath.length() > pathsep.length()) {
      String path = classpath.substring(pathsep.length(), 
					classpath.indexOf(pathsep,
							  pathsep.length()));
      
      // System.out.println("Looking for "+name+" in "+path); // debug.
      InputStream is = getResourceAsStream(path, name);
      if (is!=null) return is; // return stream if found.

      classpath = classpath.substring(path.length()+pathsep.length());
    }
    // Couldn't find resource.
    return null;
  }

  /** Attempt to open resource given section of CLASSPATH and resource name. */
  static InputStream getResourceAsStream(String path, String name) {
    // special case .zip and .jar files.
    if (path.toLowerCase().endsWith(".zip") ||
	path.toLowerCase().endsWith(".jar")) {
      try {
	return getResourceAsStream(new ZipFile(path), name);
      } catch (IOException e) {
	return null;
      }
    } else return getResourceAsStream(new File(path, name));
  }

  /** Open a resource in a zipfile. */
  static InputStream getResourceAsStream(ZipFile zf, String name) {
    try {
      ZipEntry ze = zf.getEntry(name);
      if (ze==null) return null;
      return zf.getInputStream(ze);
    } catch (IOException e) {
      return null;
    }
  }
  
  /** Open a resource in a file. */
  static InputStream getResourceAsStream(File name) {
    try {
      return new FileInputStream(name);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
