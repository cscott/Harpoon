// Display.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * {@link Display} displays the images from the in-node on the screen.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Display extends Node {
    private Frame frame;
    private BufferedImage image = 
	new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    /** Inner class that represents the actual canvas that the image is painted on. */
    class ImageCanvas extends Canvas {
	ImageCanvas() { super(); }
	public void paint(Graphics g) {
	    g.drawImage(Display.this.image, 0, 0, getWidth(), getHeight(), this);
	}
	public void update(Graphics g) {
	    paint(g);
	}
    }

    private ImageCanvas canvas = new ImageCanvas();

    /** Construct a {@link Display} node, given a <code>title</code> for the window. 
     *  Note that this actually opens a frame visible on the screen.
     */
    public Display(String title) {
	super();
	frame = new Frame(title);
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		frame.setVisible(false);
	    }
	});
	frame.setLayout(new BorderLayout());
	frame.add(canvas, BorderLayout.CENTER);
	frame.setSize(new Dimension(100, 100));
	frame.setVisible(true);
    }

    /** Display the image represented by <code>id</code>, usually called by the in-node. */
    public synchronized void process(ImageData id) {
	if (frame.isVisible()) {
	    BufferedImage newImage = new BufferedImage(id.width, id.height, 
						       BufferedImage.TYPE_INT_RGB);
	    WritableRaster raster = newImage.getRaster();
	    
	    /* Horribly inefficient - find a better way... */
	    int[] vals = new int[id.rvals.length];
	    for (int i=0; i<id.rvals.length; i++) vals[i]=(id.rvals[i]|256)&255;       
	    raster.setSamples(0,0,id.width,id.height,0,vals);
	    for (int i=0; i<id.gvals.length; i++) vals[i]=(id.gvals[i]|256)&255;       
	    raster.setSamples(0,0,id.width,id.height,1,vals);
	    for (int i=0; i<id.bvals.length; i++) vals[i]=(id.bvals[i]|256)&255;       
	    raster.setSamples(0,0,id.width,id.height,2,vals);
	    image = newImage;
	    canvas.repaint();
	}
    }
}
