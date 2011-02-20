// TestPhi.java, created Tue Sep 25 17:42:01 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

/**
 * <code>TestPhi</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TestPhi.java,v 1.1 2001-09-25 21:58:26 cananian Exp $
 */
public class TestPhi {
    public static void main(String[] args) {
	int x = 0, y = 1;
	for (int i=0; i<5; i++) {
	    System.out.println(x+" "+y);
	    int t = x;
	    x = y;
	    y = t;
	}
    }
}
