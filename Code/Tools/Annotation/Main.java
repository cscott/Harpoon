package harpoon.Tools.Annotation;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;

/* Test skeleton for java parser/lexer.
 * Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
 * This is released under the terms of the GPL with NO WARRANTY.
 * See the file COPYING for more details.
 */

/** Main routine for the Java source-code annotation tool.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 */
public class Main {
  public static void main(String args[]) throws Exception {
    Reader fr = new BufferedReader(new FileReader(args[0]));
    Lexer l = new harpoon.Tools.Annotation.Lex.Lexer(fr);
    Java12 g = new Java12(l);
    g./*debug_*/parse();
    System.exit(l.numErrors());
  }
}
