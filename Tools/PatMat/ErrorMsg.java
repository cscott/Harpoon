package harpoon.Tools.PatMat;

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
    System.out.println(errorMsg(pos, msg));
    anyErrors = true;
  }
  public String errorMsg(int pos, String msg) {
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
