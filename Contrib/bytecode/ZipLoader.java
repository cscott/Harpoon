// Copyright (c) 1997  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;

/** Load classes from a Zip archive.
 * @author	Per Bothner
 */

public class ZipLoader extends ClassLoader
{
  /** The zip archive from which we will load the classes.
   * The format of the archive is the same as classes.zip. */
  java.util.zip.ZipFile zar;

  /** Number of classes managed by this loader. */
  int size;

  /* A list of pairs of (name, class) of already loaded classes. */
  private java.util.Vector loadedClasses;

  public ZipLoader (String name) throws java.io.IOException
  {
    this.zar = new java.util.zip.ZipFile(name);
    size = 0;
    java.util.Enumeration e = this.zar.entries();
    while (e.hasMoreElements())
      {
	java.util.zip.ZipEntry ent = (java.util.zip.ZipEntry) e.nextElement();
	if (! ent.isDirectory())
	  size++;
      }
    loadedClasses = new java.util.Vector(size);
  }

  public Class loadClass (String name, boolean resolve)
       throws ClassNotFoundException
  {
    Class clas;
    int index = loadedClasses.indexOf(name);
    if (index >= 0)
      clas = (Class) loadedClasses.elementAt(index+1);
    else
      {
	String member_name = name.replace ('.', '/') + ".class";
	java.util.zip.ZipEntry member = zar.getEntry(member_name);
	if (member == null)
	  clas = findSystemClass (name);
	else
	  {
	    try
	      {
		int size = (int) member.getSize();
		java.io.InputStream strm = zar.getInputStream(member);
		byte[] bytes = new byte[size];
		strm.read(bytes);
		clas = defineClass (name, bytes, 0, size);
		loadedClasses.addElement(name);
		loadedClasses.addElement(clas);
		if (2 * size == loadedClasses.size())
		  {
		    zar.close ();
		    zar = null;
		  }
	      }
	    catch (java.io.IOException ex)
	      {
		throw new
		  Error ("IOException while loading from ziparchive \""
			 + name + "\": " + ex.toString ());
	      }
	  }
      }

    if (resolve)
      resolveClass (clas);
    return clas;
  }
}
