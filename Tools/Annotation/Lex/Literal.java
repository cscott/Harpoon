package harpoon.Tools.Annotation.Lex;

abstract class Literal extends Token { 
  /** highlight literals in 'dark cyan' */
  void annotate(LinePos left, LinePos right) {
    System.out.println(left.line+" "+left.pos+"\t"+right.line+" "+right.pos);
    System.out.println("<font color=darkcyan>");
    System.out.println("</font>");
  }
}
