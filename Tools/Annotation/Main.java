package harpoon.Tools.Annotation;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;

/* Test skeleton for java parser/lexer.
 * Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
 * This is released under the terms of the GPL with NO WARRANTY.
 * See the file COPYING for more details.
 */

public class Main {
  public static void main(String args[]) throws Exception {
    Reader fr = new BufferedReader(new FileReader(args[0]));
    // Change the boolean in the next line to 'false' for a
    // pre-java 1.2 lexer.
    Lexer l = new harpoon.Tools.Annotation.Lex.Lexer(fr, true);
    Java12 g = new Java12(l, true);
    g./*debug_*/parse();
  }
}
