// Main.java, created Wed Feb 17 22:05:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;

/* Main entry point for the instruction selection tool.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.1.2.3 1999-02-18 11:46:38 cananian Exp $
 */
public class Main {
  public static void main(String args[]) throws Exception {
    Reader r = new BufferedReader(new FileReader(args[0]));
    ErrorMsg e = new ErrorMsg(args[0]);
    Lexer l = new Lexer(r, e);
    Parser p = new Parser(l, e);
    Spec s = (Spec) p./*debug_*/parse().value;
    System.out.println(s);
  }
}
