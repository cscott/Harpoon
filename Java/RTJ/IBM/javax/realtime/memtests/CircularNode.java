package javax.realtime.memtests;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1999, 2000  All Rights Reserved
 */

class CircularNode 
{
  CircularNode next;
  CircularNode prev;
  Object data;
  CircularNode() {
    this(null);
  }
  CircularNode(Object dat) {
    next = prev = this;
    this.data = dat;
  }    
  void insertNext(CircularNode cn) {
    cn.next = this.next;
    next = cn;
    cn.prev = this;
  }
  void insertPrev(CircularNode cn) {
    cn.prev = this.prev;
    prev = cn;
    cn.next = this;
  }
}