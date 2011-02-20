package harpoon.Tools.Annotation.Lex;

abstract class Comment extends InputElement {
  private StringBuffer comment = new StringBuffer();

  void annotate(LinePos left, LinePos right) {
    System.out.println(left.line+" "+left.pos+"\t"+right.line+" "+right.pos);
    System.out.println("<font color=red>");
    System.out.println("</font>");
  }

  String getComment() { return comment.toString(); }

  void appendLine(String more) { // 'more' is '\n' terminated.
    int i=0;

    // skip leading white space.
    for (; i<more.length(); i++)
      if (!Character.isSpaceChar(more.charAt(i))) 
	break;

    // skip any leading stars.
    for (; i<more.length(); i++)
      if (more.charAt(i)!='*')
	break;

    // the rest of the string belongs to the comment.
    if (i<more.length())
      comment.append(more.substring(i));
  }

}
