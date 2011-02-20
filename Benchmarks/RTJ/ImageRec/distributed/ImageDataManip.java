package imagerec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ImageDataManip {
    public static ImageData readPPM(String fileName) {
	try {
	    ImageData id = new ImageData();
	    GZIPInputStream gz = new GZIPInputStream(new FileInputStream(fileName));
	    StreamTokenizer s = new StreamTokenizer(new InputStreamReader(gz));
	    s.eolIsSignificant(false);
	    s.parseNumbers();
	    s.commentChar('#');
	    s.nextToken();
	    if (s.ttype!=s.TT_WORD) throw new IOException("Not a GZIP'ed PPM!");
	    if (!s.sval.startsWith("P3")) throw new IOException("Not a GZIP'ed PPM!");
	    s.nextToken();
	    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
	    id.width=(int)s.nval;
	    s.nextToken();
	    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
	    id.height=(int)s.nval;
	    s.nextToken();
	    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
	    id.rvals = new int[id.width*id.height];
	    id.gvals = new int[id.width*id.height];
	    id.bvals = new int[id.width*id.height];
	    for (int i=0; i<id.width*id.height; i++) {
		s.nextToken();
		if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
		id.rvals[i] = (int)s.nval;
		s.nextToken();
		if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
		id.gvals[i] = (int)s.nval;
		s.nextToken();
		if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
		id.bvals[i] = (int)s.nval;
	    }
	    return id;
	} catch (IOException e) {
	    System.out.println(e.toString());
	    System.exit(-1);
	    return null;
	}
    }

    public static void writePPM(ImageData id, String fileName) {
	try {
	    PrintWriter out = new PrintWriter(new GZIPOutputStream(new FileOutputStream(fileName)));
	    out.println("P3 "+id.width+" "+id.height+" 255");
	    for (int i=0; i<id.width*id.height; i++) {
		out.println(id.rvals[i]+" "+id.gvals[i]+" "+id.bvals[i]);
	    }
	    out.close();
	} catch (FileNotFoundException e) {
	    System.out.println(e.toString());
	    System.exit(-1);
	} catch (IOException e) {
	    System.out.println(e.toString());
	    System.exit(-1);
	}
    }

    public static void main(String args[]) {
	if (args.length<2) {
	    System.out.println("Usage: java ImageData <in.ppm.gz> <out.ppm.gz>");
	}
	ImageData id = readPPM(args[0]);
	System.out.println("Width: "+id.width+" Height: "+id.height);
	writePPM(id, args[1]);
    }
}
