// HCLibrary.java, created Mon Dec 28 21:01:12 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;

/**
 * <code>HCLibrary</code> is a simple superclass designed to bring the
 * <code>HClass</code> objects for common classes into class scope.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCLibrary.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
abstract class HCLibrary  {
    final HClass HCbyteA, HCcharA, HCclass, HCclassA, HCcloneable, HCdouble;
    final HClass HCfloat, HCobject, HCruntime, HCsmanager, HCstring;
    final HClass HCstringA, HCsystem, HCthrowable, HCfile, HCfiledesc;
    final HClass HCfistream, HCfostream, HCrafile, HCproperties;
    final HClass HCarraystoreE, HCarrayindexE, HCclassnotfoundE;
    final HClass HCillegalaccessE, HCinstantiationE, HCnullpointerE;
    final HClass HCnegativearrayE, HCarithmeticE, HCclasscastE;
    final HClass HCclonenotsupportedE, HCioE, HCsecurityE;
    final HClass HCillegalaccessErr, HCnosuchmethodErr;
    final HClass HCunsatisfiedlinkErr;

    HCLibrary(Linker linker) {
	HCbyteA = linker.forDescriptor("[B");
	HCcharA = linker.forDescriptor("[C");
	HCclass = linker.forName("java.lang.Class");
	HCclassA = linker.forDescriptor("[Ljava/lang/Class;");
        HCcloneable = linker.forName("java.lang.Cloneable");
	HCdouble = linker.forName("java.lang.Double");
	HCfloat = linker.forName("java.lang.Float");
	HCobject = linker.forName("java.lang.Object");
	HCruntime = linker.forName("java.lang.Runtime");
	HCsmanager = linker.forName("java.lang.SecurityManager");
	HCstring = linker.forName("java.lang.String");
	HCstringA = linker.forDescriptor("[Ljava/lang/String;");
	HCsystem = linker.forName("java.lang.System");
	HCthrowable = linker.forName("java.lang.Throwable");
	HCfile = linker.forName("java.io.File");
	HCfiledesc = linker.forName("java.io.FileDescriptor");
	HCfistream = linker.forName("java.io.FileInputStream");
	HCfostream = linker.forName("java.io.FileOutputStream");
	HCrafile = linker.forName("java.io.RandomAccessFile");
	HCproperties = linker.forName("java.util.Properties");
	HCarraystoreE = linker.forName("java.lang.ArrayStoreException");
	HCarrayindexE = linker.forName("java.lang.ArrayIndexOutOfBounds"+
				       "Exception");
	HCclassnotfoundE = linker.forName("java.lang.ClassNotFoundException");
	HCillegalaccessE = linker.forName("java.lang.IllegalAccessException");
	HCinstantiationE = linker.forName("java.lang.InstantiationException");
	HCnullpointerE = linker.forName("java.lang.NullPointerException");
	HCnegativearrayE = linker.forName("java.lang.NegativeArraySize"+
					  "Exception");
	HCarithmeticE = linker.forName("java.lang.ArithmeticException");
	HCclasscastE = linker.forName("java.lang.ClassCastException");
        HCclonenotsupportedE = linker.forName("java.lang.CloneNotSupported"+
					      "Exception");
	HCioE = linker.forName("java.io.IOException");
	HCsecurityE = linker.forName("java.lang.SecurityException");
	HCillegalaccessErr = linker.forName("java.lang.IllegalAccessError");
	HCnosuchmethodErr = linker.forName("java.lang.NoSuchMethodError");
	HCunsatisfiedlinkErr=linker.forName("java.lang.UnsatisfiedLinkError");
    }
}
