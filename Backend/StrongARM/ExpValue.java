// ExpValue.java, created Mon May 17 16:02:07 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

/**
 * <code>ExpValue</code> is a data type to represent the return value
 * from an expression in a tree on a 32-bit architecture.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: ExpValue.java,v 1.1.2.2 1999-08-04 06:30:56 cananian Exp $
 */
public class ExpValue {
	private Temp low, high;
	private boolean dword;

	public ExpValue(Temp val) {
		dword = false;
		low = val;
	}

	public ExpValue(Temp lowval, Temp highval) {
		dword = true;
		low = lowval; high = highval;
	}

	public Temp low() { 
		Util.assert(dword);
		return low;
	}

	public Temp high() {
		Util.assert(dword);
		return high;
	}

	public Temp temp() {
		Util.assert(!dword);
		return low;
	}

	public boolean isDouble() {
		return dword;
	}
}
