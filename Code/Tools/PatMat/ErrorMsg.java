// ErrorMsg.java, created Wed Feb 17  5:58:23 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

/** Record the character positions of line endings, in order to
 *  generate intelligible error messages.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *         <i>Modern Compiler Implementation in Java</i>, by Andrew Appel.
 * @version $Id: ErrorMsg.java,v 1.2 2002-02-25 21:08:30 cananian Exp $
 */
class ErrorMsg {
  private LineList linePos = new LineList(-1,null);
  private int lineNum=1;
  private String filename;
  public boolean anyErrors=false;

  public ErrorMsg(String f) {
      filename=f;
  }

  public void newline(int pos) {
     lineNum++;
     linePos = new LineList(pos,linePos);
  }
  public void error(int pos, String msg) {
    System.err.println(errorMsg(pos, msg));
    anyErrors = true;
  }
  public String errorMsg(int pos, String msg) {
    //if (1!=0)throw new RuntimeException("got stack trace?");
    int n = lineNum;
    LineList p = linePos;
    String sayPos = "0.0";
    while (p!=null) {
      if (p.head<pos) {
	sayPos = ":" + String.valueOf(n) + "." + String.valueOf(pos-p.head);
	break;
      }
      p=p.tail; n--;
    }
    return filename+":"+sayPos+":"+msg;
  }
}

class LineList {
  int head;
  LineList tail;
  LineList(int h, LineList t) {head=h; tail=t;}
}
