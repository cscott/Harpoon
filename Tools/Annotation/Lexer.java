package harpoon.Tools.Annotation;

import harpoon.Tools.Annotation.Lex.LinePos;

/* Lexer.java.  Copyright (C) 1998 C. Scott Ananian.
 * This program is free software; see the file COPYING for more details.
 */

public interface Lexer {
    public java_cup.runtime.Symbol nextToken() throws java.io.IOException;
    public LinePos linepos(int character_offset);
    /** report an error */
    public void errorMsg(String msg, java_cup.runtime.Symbol info);
    /** return the number of errors reported */
    public int numErrors();
}
