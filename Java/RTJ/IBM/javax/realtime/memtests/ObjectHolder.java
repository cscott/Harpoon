package javax.realtime.memtests;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1999, 2000  All Rights Reserved
 */

public class ObjectHolder 
{
  public Object data;
  public ObjectHolder() {
    this(null);
  }   
  public ObjectHolder(Object obj) {
    this.data = obj;
  }  
  
  public String toString() {
    return super.toString() + " w/ " + (data == null ? "null" : data.toString());
  }
}
