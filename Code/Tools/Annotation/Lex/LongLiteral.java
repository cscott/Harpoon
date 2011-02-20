package harpoon.Tools.Annotation.Lex;

import harpoon.Tools.Annotation.Sym;
import java_cup.runtime.Symbol;

class LongLiteral extends NumericLiteral {
  LongLiteral(long l) { this.val = new Long(l); }

  Symbol token() { return new Symbol(Sym.INTEGER_LITERAL, val); }
}
