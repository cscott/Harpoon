package harpoon.Tools.Annotation.Lex;

import harpoon.Tools.Annotation.Sym;
import java_cup.runtime.Symbol;

class EOF extends Token {
  EOF() {}
  Symbol token() { return new Symbol(Sym.EOF); }
  public String toString() { return "EOF"; }
}
