package harpoon.Tools.Annotation.Lex;

/** FIFO class.  This helps implement the lookahead we need for JSR-14.
 * Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
 * This program is released under the terms of the GPL; see the file
 * COPYING for more details.  There is NO WARRANTY on this code.
 */

class FIFO {
  java_cup.runtime.Symbol[] backing = new java_cup.runtime.Symbol[10];
  int start=0, end=0;
  final Getter getter;
  FIFO(Getter getter) { this.getter = getter; }
  public boolean isEmpty() { return start==end; }
  private boolean isFull() {
    return start==end+1 || (start==0 && end==backing.length-1);
  }
  private int size() {
    return ((end<start)?end+backing.length:end)-start;
  }
  public void put(java_cup.runtime.Symbol o) {
    if (isFull()) {
      java_cup.runtime.Symbol[] nbacking =
	new java_cup.runtime.Symbol[backing.length*2];
      System.arraycopy(backing, start, nbacking, 0, backing.length-start);
      System.arraycopy(backing, 0, nbacking, backing.length-start, start);
      start = 0;
      end = backing.length-1;
      backing = nbacking;
    }
    assert !isFull();
    backing[end++] = o;
    if (end == backing.length)
      end = 0;
    assert !isEmpty();
  }
  public java_cup.runtime.Symbol get() throws java.io.IOException {
    if (isEmpty())
      put(getter.next());
    assert !isEmpty();
    java_cup.runtime.Symbol o = backing[start++];
    if (start == backing.length)
      start = 0;
    assert !isFull();
    return o;
  }
  public java_cup.runtime.Symbol peek(int i) throws java.io.IOException {
    while (i >= size())
      put(getter.next());
    int index = start+i;
    if (index >= backing.length) index -= backing.length;
    assert 0<= index && index < backing.length;
    return backing[index];
  }
  abstract static class Getter {
    abstract java_cup.runtime.Symbol next()
      throws java.io.IOException;
  }
}
	
    
