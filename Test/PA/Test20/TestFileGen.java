// TestFileGen.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test20;

import java.util.Random;

/**
 * <code>TestFileGen</code> generate a test file for the client
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: TestFileGen.java,v 1.1.2.2 2000-04-03 20:22:56 salcianu Exp $
 */
class TestFileGen {
    public static void main(String args[]) {
	if (args.length != 1) {
	    System.out.println("Usage:\n\tTestFileGen <n>\n" + 
			       "\tn = number of request to put in file");
	    System.exit(0);
	}
	
	int num  = Integer.parseInt(args[0]);
	Random r = new Random();
	for (; num >= 0; num--) {
	    System.out.println(r.nextLong() + "#" + r.nextLong() + "#");
	}
    }
}

