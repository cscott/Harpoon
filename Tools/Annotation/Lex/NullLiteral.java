package harpoon.Tools.Annotation.Lex;

import harpoon.Tools.Annotation.Sym;
import java_cup.runtime.Symbol;

class NullLiteral extends Literal {
  NullLiteral() { }

  Symbol token() { return new Symbol(Sym.NULL_LITERAL); }

  public String toString() { return "NullLiteral <null>"; }
}