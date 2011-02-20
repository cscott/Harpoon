package imagerec;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class DisplayImage extends ClientServer {
    private Frame frame = new Frame("Display movie");
    private BufferedImage image = 
	new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    class ImageCanvas extends Canvas {
	ImageCanvas() { super(); }
	public void paint(Graphics g) {
	    g.drawImage(DisplayImage.this.image, 0, 0, getWidth(), getHeight(), this);
	}
	public void update(Graphics g) {
	    paint(g);
	}
    }

    private ImageCanvas canvas = new ImageCanvas();

    public DisplayImage(String args[]) {
	super(args);
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	frame.setLayout(new BorderLayout());
	frame.add(canvas, BorderLayout.CENTER);
	frame.setSize(new Dimension(100, 100));
	frame.setVisible(true);
    }

    public synchronized void process(ImageData id) {
	BufferedImage newImage = new BufferedImage(id.width, id.height, 
						   BufferedImage.TYPE_INT_RGB);
	WritableRaster raster = newImage.getRaster();
	raster.setSamples(0,0,id.width,id.height,0,id.rvals);
	raster.setSamples(0,0,id.width,id.height,1,id.gvals);
	raster.setSamples(0,0,id.width,id.height,2,id.bvals);
	image = newImage;
	canvas.repaint();
    }

    public static void main(String args[]) {
	DisplayImage di = new DisplayImage(args);
	di.server(args[0]);
    }
}
