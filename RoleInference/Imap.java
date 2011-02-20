import java.net.*;
import java.io.*;
import java.util.*;

class Imap {
    private Rectangle[] rectangles;
    private Point[] points;
    long THRESHOLD=400;

    public Imap(String filename) {
	FileReader fr=null;
	try {
	    fr=new FileReader(filename);
	    parseFile(fr);
	    fr.close();
	} catch (IOException e) {
	    System.out.println(e);
	    System.exit(-1);
	}
    }
    static class Rectangle {
	String label;
	int x1,y1,x2,y2;
	public Rectangle(String label, int x1,int y1, int x2, int y2) {
	    this.label=label;
	    this.x1=x1;
	    this.y1=y1;
	    this.x2=x2;
	    this.y2=y2;
	}
    }

    String parseclick(int x,int y) {
	System.out.println(x+","+y);
	for(int i=0;i<rectangles.length;i++) {
	    Rectangle r=rectangles[i];
	    if ((r.x1<=x)&&(r.y1>=y)&&
		(r.x2>=x)&&(r.y2<=y))
		return r.label;
	}
	long mindistance=Long.MAX_VALUE;
	int minindex=-1;
	for(int i=0;i<points.length;i++) {
	    Point p=points[i];
	    long dx=p.x-x;
	    long dy=p.y-y;
	    if ((dx*dx+dy*dy)<mindistance) {
		mindistance=dx*dx+dy*dy;
		minindex=i;
	    }
	}
	if (mindistance>THRESHOLD)
	    return null;
	else
	    return points[minindex].label;
    }

    static class Point {
	String label;
	int x,y;
	public Point(String label, int x,int y) {
	    this.label=label;
	    this.x=x;
	    this.y=y;
	}
    }

    void parseFile(FileReader fr) {
	int firstchar=0;
	ArrayList rectangles=new ArrayList();
	ArrayList points=new ArrayList();
	while(true) {
	    try {
		firstchar=fr.read();
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    /* EOF?*/
	    if (firstchar==-1)
		break;
	    switch(firstchar) {
		case'b':
		    case'#':
		    while(firstchar!='\n') {
			try {
			    firstchar=fr.read();
			} catch (IOException e) {
			    e.printStackTrace();
			    System.exit(-1);
			}
		    }
		break;
		case'r':
		    {
			nexttoken(fr,false);
			String label=nexttoken(fr,false);
			String x1=nexttoken(fr,true);
			String y1=nexttoken(fr,true);
			String x2=nexttoken(fr,true);
			String y2=nexttoken(fr,true);
			Rectangle r=new Rectangle(label,Integer.parseInt(x1),Integer.parseInt(y1),
						  Integer.parseInt(x2),Integer.parseInt(y2));
			rectangles.add(r);
		    }
		break;
		case'p':
		    {
			nexttoken(fr,false);
			String label=nexttoken(fr,false);
			String x=nexttoken(fr,true);
			String y=nexttoken(fr,true);
			Point p=new Point(label,Integer.parseInt(x),Integer.parseInt(y));
			points.add(p);
		    }
		break;
	    }
	}
	this.rectangles=(Rectangle[]) rectangles.toArray(new Rectangle[rectangles.size()]);
	this.points=(Point[]) points.toArray(new Point[points.size()]);
    }

    String nexttoken(java.io.InputStreamReader isr,boolean commas) {
	String string="";
	int c=0;
	boolean looped=false;
	while(true) {
	    try {
		c=isr.read();
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    if ((c==' ')||(c=='\n')||(commas&&c==',')) {
		if (!looped) {
		    looped=true;
		    continue;
		}
		return string;
	    }
	    string=string+new String(new char[]{(char)c});
	    looped=true;
	}
    }

}

