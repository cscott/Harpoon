// ImageDataManip.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import imagerec.graph.ImageData;

/**
 *
 *
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

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
	    id.rvals = new byte[id.width*id.height];
	    id.gvals = new byte[id.width*id.height];
	    id.bvals = new byte[id.width*id.height];
	    for (int i=0; i<id.width*id.height; i++) {
		s.nextToken();
		if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
		id.rvals[i] = (byte)s.nval;
		s.nextToken();
		if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
		id.gvals[i] = (byte)s.nval;
		s.nextToken();
		if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a GZIP'ed PPM!");
		id.bvals[i] = (byte)s.nval;
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
		out.println(((id.rvals[i]|256)&255)+" "+((id.gvals[i]|256)&255)+" "+((id.bvals[i]|256)&255));
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

    public static ImageData crop(ImageData id, int x, int y, int width, int height) {
	ImageData newID = new ImageData(new byte[width*height+1],
					new byte[width*height+1],
					new byte[width*height+1],
					x, y, width, height, 
					id.time, id.id, id.command, id.receiverID);
	int newPos = 0;
	for (int j=y; j<=y+width; j++) {
	    int oldPos = y*id.width+x;
	    System.arraycopy(id.rvals, oldPos, newID.rvals, newPos, width);
	    System.arraycopy(id.gvals, oldPos, newID.gvals, newPos, width);
	    System.arraycopy(id.bvals, oldPos, newID.bvals, newPos, width);
	    newPos+=width;
	}
	return newID;
    }
}
