import java.io.*;
import java.util.*;

class Process {
  static HashSet set=null;
  static Hashtable currtable=new Hashtable();
  static String declaration;

  static void debug(String str) {
    System.out.println(str);
  }

  static public void main(String[] args) {
    debug("Opening file:"+args[0]);
    BufferedReader br=null;
    BufferedWriter bw=null;
    try {
      br=new BufferedReader(new FileReader(args[0]));
      int count=0;
      boolean start=false;
      while(true) {
        String line=br.readLine();
        if (line==null)
          break;
        String replacewith=line+".elem[";
        if (replacewith==null)
          break;
        currtable.put(line+"[",replacewith);
      }

      /* Built table */
      br=new BufferedReader(new FileReader(args[1]));
      bw=new BufferedWriter(new FileWriter("fixed-"+args[1]));
      count=0;
      start=false;
      while(true) {
        String line=br.readLine();
        if (line==null)
          break;
        if (line.equals("")) {
          bw.newLine();
          continue;
        }
        if (line.equals("DECLARE")) {
          bw.write(line);
          bw.newLine();
          start=true;
          count=0;
          declaration=br.readLine();
          bw.write(declaration);
          bw.newLine();
          continue;
        }
        if (start) {
          if ((count%4)==0) {
            for(Iterator it=currtable.keySet().iterator();it.hasNext();) {
              String str=(String)it.next();
              String replace=(String)currtable.get(str);
              line=replace(line,str,replace);
            }
          }
          bw.write(line);
          bw.newLine();
          count++;
        } else {
          bw.write(line);
          bw.newLine();
        }
      }
      bw.close();


      /* Built table */
      br=new BufferedReader(new FileReader(args[2]));
      bw=new BufferedWriter(new FileWriter("fixed-"+args[2]));
      count=0;
      start=false;
      while(true) {
        String line=br.readLine();
        if (line==null)
          break;
        if (line.equals("")) {
          bw.write(line);
          bw.newLine();
          start=true;
          count=0;
          declaration=br.readLine();
          bw.write(declaration);
          bw.newLine();
          continue;
        }
        if (start) {
          if ((count%3)==0) {
            for(Iterator it=currtable.keySet().iterator();it.hasNext();) {
              String str=(String)it.next();
              String replace=(String)currtable.get(str);
              line=replace(line,str,replace);
            }
          }
          bw.write(line);
          bw.newLine();
          count++;
        } else {
          bw.write(line);
          bw.newLine();
        }
      }
      bw.close();

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  static String generatepostfix(String str, int numderefs) {
    if (numderefs==0)
      return "";
    int start=str.length();
    for(int i=0;i<numderefs;i++) {
      start=str.lastIndexOf("[",start-1);
    }
    if (start==-1)
      throw new Error();
    return str.substring(start);
  }


  static String generateprefix(String str,int numdots) {
    int offset=0;
    for(int i=0;i<numdots;i++) {
      offset=str.indexOf(".",offset)+1;
    }
    int nextdot=str.indexOf(".",offset);
    if (nextdot==-1) {
      nextdot=str.indexOf("[",offset);
      if (nextdot==-1)
        nextdot=str.length();
    }
    return str.substring(0,nextdot);
  }

  static String nextfield(String str,int numdots) {
    int offset=0;
    for(int i=0;i<=numdots;i++) {
      offset=str.indexOf(".",offset)+1;
      if (offset==0)
        return "";
    }
    int nextdot=str.indexOf(".",offset);
    if (nextdot==-1) {
      nextdot=str.indexOf("[",offset);
      if (nextdot==-1)
        nextdot=str.length();
    }
    return "."+str.substring(offset,nextdot);
  }

  static int count(String str, String tofind) {
    int offset=0;
    int total=0;
    while(true) {
      int newoffset=str.indexOf(tofind,offset);
      if (newoffset==-1)
        break;
      total++;
      offset=newoffset+1;
    }
    return total;
  }

  static String replace(String str, String tofind, String toreplace) {
    if (str.indexOf(tofind)!=-1) {
      str=str.substring(0,str.indexOf(tofind))+str.substring(str.indexOf(tofind)+tofind.length());
    }
    return str;
  }
}
