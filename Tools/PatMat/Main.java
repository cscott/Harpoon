package harpoon.Tools.PatMat;

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
    Reader r = new BufferedReader(new FileReader(args[0]));
    ErrorMsg e = new ErrorMsg(args[0]);
    Lexer l = new Lexer(r, e);
    Parser p = new Parser(l, e);
    Spec s = (Spec) p./*debug_*/parse().value;
    System.out.println(s);
  }
}
