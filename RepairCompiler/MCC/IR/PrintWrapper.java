package MCC.IR;
import java.util.*;

public class PrintWrapper {
  java.io.PrintWriter output;
  String buffered="";
  boolean buffer=false;
  Hashtable vartable=new Hashtable();

  public PrintWrapper(java.io.PrintWriter output) {
    this.output=output;
  }
  void print(String s) {
    if (buffer)
      buffered+=s;
    else
      output.print(s);
  }
  void flush() {
    if (!buffer)
      output.flush();
  }
  void println(String s) {
    if (buffer)
      buffered+=s+"\n";
    else
      output.println(s);
  }
  void startBuffer() {
    buffer=true;
  }
  void emptyBuffer() {
    //Print out declarations
    for(Iterator it=vartable.keySet().iterator();it.hasNext();) {
      String var=(String)it.next();
      output.println(((String)vartable.get(var))+" "+var+";");
    }
    output.print(buffered);
    buffered="";
    vartable=new Hashtable();
    buffer=false;
  }
  void addDeclaration(String type, String varname) {
    if (buffer) {
      if (vartable.containsKey(varname)) {
        String oldtype=(String)vartable.get(varname);
        if (!oldtype.equals(type)) {
          throw new Error("Internal error: Inconsistent declarations for:"+varname);
        }
      } else {
        vartable.put(varname,type);
      }
    } else
      output.println(type+" "+varname+";");
  }
}
