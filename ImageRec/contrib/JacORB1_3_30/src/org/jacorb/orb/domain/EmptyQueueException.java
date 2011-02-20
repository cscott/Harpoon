package org.jacorb.orb.domain;
/**
 * Raised by operations front and dequeue  if a DomainQueue is empty.
 * @author Herbert Kiefer
 * @version $Revision: 1.1 $
 */

public class EmptyQueueException extends java.lang.RuntimeException {
  
  public EmptyQueueException() 
  {
    super("This queue is empty and cannot provide an element.");
  }
  
} // EmptyQueue






