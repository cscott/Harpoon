package harpoon.Tools.Annotation.Lex;

abstract class InputElement {
  /** Create an annotation for this input element. */
  void annotate(LinePos left, LinePos right) { /* default, no annotation */ }
}
