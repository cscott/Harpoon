package harpoon.Tools.Annotation.Lex;

import harpoon.Tools.Annotation.Sym;
import java_cup.runtime.Symbol;

class StringLiteral extends Literal {
  String val;
  StringLiteral(String s) { this.val = s; }

  Symbol token() { return new Symbol(Sym.STRING_LITERAL, val); }

  void annotate(LinePos left, LinePos right) {
    System.out.println(left.line+" "+left.pos+"\t"+right.line+" "+right.pos);
    System.out.println("<font color=goldenrod>");
    System.out.println("</font>");
  }

  public String toString() { 
    return "StringLiteral <"+Token.escape(val)+">"; 
  }
}
