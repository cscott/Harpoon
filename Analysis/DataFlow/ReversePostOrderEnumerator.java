// ReversePostOrderEnumerator.java, created Wed Mar 10  9:00:54 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

/**
 * ReversePostOrderEnumerator
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ReversePostOrderEnumerator.java,v 1.1.2.8 2001-06-17 23:06:45 cananian Exp $
 */

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import harpoon.Analysis.BasicBlock;
import harpoon.Util.ArrayEnumerator;
import harpoon.Util.Util;
import harpoon.IR.Quads.Quad;

public class ReversePostOrderEnumerator implements Enumeration {

  public static boolean DEBUG = false;
  public static void db(String s) { System.out.println(s); }

  Stack order;

  public ReversePostOrderEnumerator(BasicBlock start) {
    Stack enum_stack = new Stack();
    Stack bb_stack = new Stack();
    order = new Stack();
    Set done = new HashSet();
    done.add(start); bb_stack.push(start); enum_stack.push(start.next());
    while (!bb_stack.isEmpty()) {
      Util.assert(bb_stack.size() == enum_stack.size());
      for (Enumeration e = (Enumeration)enum_stack.pop();
	   e.hasMoreElements(); ) {
	BasicBlock bb2 = (BasicBlock) e.nextElement();
	if (!done.contains(bb2)) {
	  if (DEBUG) db("visiting "+bb2+" for the first time");
	  done.add(bb2);
	  bb_stack.push(bb2);
	  enum_stack.push(e);
	  e = bb2.next();
	}
      }
      Object o = bb_stack.pop();
      if (DEBUG) db("leaving "+o);
      order.push(o);
    }
  }

  public ReversePostOrderEnumerator(Quad start) {
    Stack enum_stack = new Stack();
    Stack bb_stack = new Stack();
    order = new Stack();
    Set done = new HashSet();
    done.add(start); bb_stack.push(start);
    enum_stack.push(new ArrayEnumerator(start.next()));
    while (!bb_stack.isEmpty()) {
      Util.assert(bb_stack.size() == enum_stack.size());
      for (Enumeration e = (Enumeration)enum_stack.pop();
	   e.hasMoreElements(); ) {
	Quad bb2 = (Quad) e.nextElement();
	if (!done.contains(bb2)) {
	  if (DEBUG) db("visiting "+bb2+" for the first time");
	  done.add(bb2);
	  bb_stack.push(bb2);
	  enum_stack.push(e);
	  e = new ArrayEnumerator(bb2.next());
	}
      }
      Object o = bb_stack.pop();
      if (DEBUG) db("leaving "+o);
      order.push(o);
    }
  }

  public boolean hasMoreElements() {
    return !order.empty();
  }

  public Object nextElement() {
    return order.pop();
  }
  public Object next() {
    return order.pop();
  }

  public ReversePostOrderEnumerator copy() {
    ReversePostOrderEnumerator r = new ReversePostOrderEnumerator();
    r.order = (Stack)order.clone();
    return r;
  }
  private ReversePostOrderEnumerator() {}

}
