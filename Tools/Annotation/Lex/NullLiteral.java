package harpoon.Tools.Annotation.Lex;

import harpoon.Tools.Annotation.Sym;
import java_cup.runtime.Symbol;

class NullLiteral extends Literal {
  NullLiteral() { }

  Symbol token() { return new Symbol(Sym.NULL_LITERAL); }

  void annotate(LinePos left, LinePos right) {
    System.out.println(left.line+" "+left.pos+"\t"+right.line+" "+right.pos);
    System.out.println("<font color=green>");
    System.out.println("</font>");
  }

  public String toString() { return "NullLiteral <null>"; }
}
