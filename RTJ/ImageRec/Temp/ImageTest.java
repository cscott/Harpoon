import java.awt.Container;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;
import java.io.File;

class ImageTest
{
    public static native void writePPM(String filename, int[] data,  int width, int height);

    static
    {
	System.loadLibrary("writePPM");
    }

    protected static BufferedImage bimg;
    protected static BufferedImage img1, img2, img3, img4, img5;
    protected static WritableRaster raster;
    protected static WritableRaster ras1, ras2, ras3, ras4, ras5;

    protected static long start;
    protected static int width, height;
    protected static int[] pix;

    public static void main(String[] args)
    {
	int T1 = Integer.valueOf(args[0]).intValue(); //definite edge threshold
	int T2 = Integer.valueOf(args[1]).intValue(); //possible edge threshold
	int imgcount = Integer.valueOf(args[2]).intValue();

	String number;
	Container c = new Container();
	for(int z=0;z<=imgcount;z++)
	{

	number = Integer.toString(z);
	while(number.length() < 6) number = "0" + number;
	try
        {
	    bimg = ImageIO.read(new File("movie3-"+number));
        } catch (Exception e)
	{
	    System.out.println("IOException while reading file");
	}

	width = bimg.getWidth(null);
	height = bimg.getHeight(null);

	//bimg = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
	img1 = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
	img2 = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
	img3 = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
	img4 = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
	img5 = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
	//Graphics2D context = bimg.createGraphics();
	//context.drawImage(img, 0, 0, null);

	raster = bimg.getRaster();
	ras1 = img1.getRaster();
	ras2 = img2.getRaster();
	ras3 = img3.getRaster();
	ras4 = img4.getRaster();
	ras5 = img5.getRaster();
	int[] rvals = new int[width*height], gvals = new int[width*height], bvals = new int[width*height];
	int[] outvals = new int[width*height];
	int[] outvals0 = new int[width*height];
	int dx0, dx1, dx2;
	int numpix;

	//System.out.println("Start");
	start = System.currentTimeMillis();
	raster.getSamples(0,0,width,height,0,rvals);
	raster.getSamples(0,0,width,height,1,gvals);
	raster.getSamples(0,0,width,height,2,bvals);
	//System.out.println("Retrieved samples - " + (System.currentTimeMillis()-start) + "ms");

	numpix=width*height-width-1;
	start = System.currentTimeMillis();
	for(int p=0;p<numpix;p++)
	{
	    dx0 = Math.abs(rvals[p] - rvals[p+width+1])+Math.abs(rvals[p+1]-rvals[p+width]);
	    dx1 = Math.abs(gvals[p] - gvals[p+width+1])+Math.abs(gvals[p+1]-gvals[p+width]);
	    dx2 = Math.abs(bvals[p] - bvals[p+width+1])+Math.abs(bvals[p+1]-bvals[p+width]);
	    dx0 = Math.max(Math.max(dx0, dx1), dx2);
	    outvals[p] = Math.min(dx0*2,255);
	}
	writePPM("roberts-" + number + ".ppm", outvals, width, height);
	//System.out.println("Roberts Cross - " + (System.currentTimeMillis()-start) + "ms");

	start = System.currentTimeMillis();
	numpix = width*height;
	for(int p=0;p<numpix;p++)
	{
	    if(outvals[p] >= T1) outvals0[p] = 255;
	    else if(outvals[p] >= T2) outvals0[p] = 1;
	    else outvals0[p] = 0;
	}
	//System.out.println("Thresholding - " + (System.currentTimeMillis()-start) + "ms");

	pix = outvals0;
	numpix = width*(height-2)-1;
	start = System.currentTimeMillis();
	for(int p=width+1;p<numpix;p++)
	{
	    if(pix[p]==255) 
	    {
		recursiveHysteresis(p);
	    }
	}
	writePPM("hysteresis-" + number + ".ppm", pix, width, height);
	//System.out.println("Recursive hysteresis - " + (System.currentTimeMillis()-start) + "ms");

	//thinning
	start=System.currentTimeMillis();
	int changed=1;
	for(int i=0;i<6 && changed==1;i++)
	{
	    changed=0;
	    for(int p=width+1;p<numpix;p++)
	    {
		if(pix[p]==255)
		{
		    int[] near  = new int[8];
		    /* 0 1 2
		       3 * 4
		       5 6 7 */
		    near[0] = pix[p-width-1];
		    near[1] = pix[p-width];
		    near[2] = pix[p-width+1];
		    near[3] = pix[p-1];
		    near[4] = pix[p+1];
		    near[5] = pix[p+width-1];
		    near[6] = pix[p+width];
		    near[7] = pix[p+width+1];
		    
		    if( ((near[0] | near[1] | near[2])<2 && (near[5] & near[6] & near[7])==255) ||
			((near[2] | near[4] | near[7])<2 && (near[0] & near[3] & near[5])==255) ||
			((near[5] | near[6] | near[7])<2 && (near[0] & near[1] & near[2])==255) ||
			((near[0] | near[3] | near[5])<2 && (near[2] & near[4] & near[7])==255) ||
			
			((near[1] | near[2] | near[4])<2 && (near[3] & near[6])==255) ||
			((near[4] | near[6] | near[7])<2 && (near[1] & near[3])==255) ||
			((near[3] | near[5] | near[6])<2 && (near[1] & near[4])==255) ||
			((near[0] | near[1] | near[3])<2 && (near[4] & near[6])==255) )
		    {
			changed=1;
			pix[p] = 0;
		    }
		}
	    }
	}
	//System.out.println("Thinning - " + (System.currentTimeMillis()-start) + "ms");

	for(int i=0;i<width*height;i++) outvals[i]=pix[i];

	//pruning
	start=System.currentTimeMillis();
	changed=1;
	for(int i=0;i<10 && changed==1;i++)
	{
	    changed=0;
	    for(int p=width+1;p<numpix;p++)
	    {
		if(pix[p]==255)
		{
		    int[] near  = new int[8];
		    /* 0 1 2
		       3 * 4
		       5 6 7 */
		    near[0] = pix[p-width-1];
		    near[1] = pix[p-width];
		    near[2] = pix[p-width+1];
		    near[3] = pix[p-1];
		    near[4] = pix[p+1];
		    near[5] = pix[p+width-1];
		    near[6] = pix[p+width];
		    near[7] = pix[p+width+1];
		    
		    if( ((near[0] | near[1] | near[2] | near[3] | near[4]) < 2 && (near[5] & near[7]) < 2) ||
			((near[1] | near[2] | near[4] | near[6] | near[7]) < 2 && (near[0] & near[5]) < 2) ||
			((near[3] | near[4] | near[5] | near[6] | near[7]) < 2 && (near[0] & near[2]) < 2) ||
			((near[0] | near[1] | near[3] | near[5] | near[6]) < 2 && (near[2] & near[7]) < 2) )
		    {
			changed=1;
			outvals[p] = 0;
		    }
		}
	    }
	    int a=width*height;
	    for(int p=0;p<a;p++)
		pix[p] = outvals[p];
	}
	//System.out.println("Pruning - " + (System.currentTimeMillis()-start) + "ms");
	
	/*	
	//Hough transformation
	start = System.currentTimeMillis();
	final int PRECISION=360;
	int[][] accumulator = new int[PRECISION][PRECISION];
	//clear accumulator
	for(int i=0;i<PRECISION;i++)
	    for(int j=0;j<PRECISION;j++)
		accumulator[i][j] = 0;
	//map each point in (r,theta) space
	int x,y,t,r, max=0;
	double radius, theta;
	for(y=-height/2;y<height/2;y++)
	{
	    for(x=-width/2;x<width/2;x++)
	    {
		if(pix[(y+height/2)*width+(x+width/2)]==255)
		{
		    for(t=0;t<PRECISION;t++)
		    {
			theta = ((double)t/PRECISION)*Math.PI;
			radius = x*Math.cos(theta) + y*Math.sin(theta);
			r = (int)Math.floor( ((radius+300)/600)*PRECISION );
			if(r>=0 && r<PRECISION) accumulator[r][t]++;
		    }
		}
	    }
	}
	for(r=0;r<PRECISION;r++)
	    for(t=0;t<PRECISION;t++)
		if(accumulator[r][t] > max) max = accumulator[r][t];
	for(r=0;r<PRECISION;r++)
	    for(t=0;t<PRECISION;t++)
		accumulator[r][t] = (int)(((double)accumulator[r][t])/max*255);
	System.out.println("Hough transform - " + (System.currentTimeMillis()-start) + "ms");
	//graph r vs t just for testing
	//for(int i=0;i<PRECISION*PRECISION;i++)
	//{
	    //outvals1[i]=accumulator[(int)Math.floor(i/PRECISION)][i%PRECISION];
	//}
	//writePPM("Hough.ppm", outvals1, PRECISION, PRECISION);

	start=System.currentTimeMillis();
	for(r=0;r<PRECISION;r++)
	    for(t=0;t<PRECISION;t++)
		if(accumulator[r][t] < T3)
		    accumulator[r][t] = 0;

	int[][] accum2 = new int[PRECISION][PRECISION];
	for(r=0;r<PRECISION;r++)
	{
	    for(t=0;t<PRECISION;t++)
	    {
		if(accumulator[r][t]>0)
		{
		    max=0;
		    for(int j=-3;j<=3;j++)
		    {
			for(int k=-3;k<=3;k++)
			{
			    try
			    {
				if( (j != 0 || k != 0) && accumulator[r+j][t+k] > max) max = accumulator[r+j][t+k];
			    } catch (Exception e) { }
			}
		    }

		    if( accumulator[r][t] >= max ) accum2[r][t] = 255;
		    if( accumulator[r][t] == max) accumulator[r][t]++;
		}
	    }
	}

	//graph r vs t just for testing
	for(int i=0;i<PRECISION*PRECISION;i++)
	{
	    outvals1[i]=accum2[(int)Math.floor(i/PRECISION)][i%PRECISION];
	}
	ras4.setSamples(0,0,PRECISION,PRECISION,1,outvals1);

	int misses, hits, maxhits;
	for(r=0;r<PRECISION;r++)
	{
	    for(t=0;t<PRECISION;t++)
	    {
		if(accum2[r][t] > 0)
		{
		    misses = hits = maxhits = 0;
		    radius = ((double)r/PRECISION)*600-300;
		    theta = ((double)t/PRECISION)*Math.PI;
		    if(t >= PRECISION/4 && t < 3*PRECISION/4)
		    {
			for(x=-width/2;x<width/2;x++)
			{
			    y = (int)( (radius - x*Math.cos(theta))/Math.sin(theta));
			    if(y >= -height/2 && y < height/2)
			    {
				int p = (y+height/2)*width+x+width/2;
				int test = pix[p];
				if(x>-width/2 && x<width/2-1 && y>-height/2 && y<height/2-1)
				{
				    test = test | pix[p-width] | pix[p-1] | pix[p+1] | pix[p+width];
				}

				if( test < 255 ) 
				{
				    misses++;
				    if(misses>T5)
				    {
					if(hits > maxhits) maxhits = hits;
					misses = hits = 0;
					continue;
				    }
				} else
				{
				    hits++;
				    misses=0;
				}
			    }
			}
		    } else
		    {
			for(y=-height/2;y<height/2;y++)
			{
			    x = (int)( (radius - y*Math.sin(theta))/Math.cos(theta));
			    if(x >= -width/2 && x < width/2)
			    {
				int p = (y+height/2)*width+x+width/2;
				int test = pix[p];
				if(x>-width/2 && x<width/2-1 && y>-height/2 && y<height/2-1)
				{
				    test = test | pix[p-width] | pix[p-1] | pix[p+1] | pix[p+width];
				}
				if(test < 255) 
				{
				    misses++;
				    if(misses>T5)
				    {
					if(hits > maxhits) maxhits = hits;
					misses = hits = 0;
					continue;
				    }
				} else
				{
				    hits++;
				    misses=0;
				}
			    }
			}
		    }
		    if(maxhits < T4) accum2[r][t] = 0;
		    else System.out.println("Max hits: " + maxhits);
		}
	    }
	}

	for(r=0;r<PRECISION;r++)
	{
	    for(t=0;t<PRECISION;t++)
	    {
		if(accum2[r][t] > 0)
		{
		    radius = ((double)r/PRECISION)*600-300;
		    theta = ((double)t/PRECISION)*Math.PI;
		    if(t >= PRECISION/4 && t < 3*PRECISION/4)
		    {
			for(x=-width/2;x<width/2;x++)
			{
			    y = (int)( (radius - x*Math.cos(theta))/Math.sin(theta));
			    if(y >= -height/2 && y < height/2)
				outvals2[(y+height/2)*width+(x+width/2)] = 255;
			}
		    } else
		    {
			for(y=-height/2;y<height/2;y++)
			{
			    x = (int)( (radius - y*Math.sin(theta))/Math.cos(theta));
			    if(x >= -width/2 && x < width/2)
				outvals2[(y+height/2)*width+(x+width/2)] = 255;
			}
		    }
		}
	    }
	}
	ras5.setSamples(0,0,width,height,0,outvals2);
	ras5.setSamples(0,0,width,height,1,pix);
	ras5.setSamples(0,0,width,height,2,outvals2);
	System.out.println("Dehoughed and mapped lines - " + (System.currentTimeMillis()-start) + "ms");
	*/

	writePPM("image-" + number + ".ppm", pix, width, height);
	System.out.println("Image " + z + " done");
	}

    }

    public static void recursiveHysteresis(int index)
    {
	if(index<width || index>width*(height-1) || (index%width)==0 || ((index+1)%width)==0) return;
	pix[index] = 255;
	if(pix[index-width-1]==1) recursiveHysteresis(index-width-1);
	if(pix[index-width]==1) recursiveHysteresis(index-width);
	if(pix[index-width+1]==1) recursiveHysteresis(index-width+1);
	if(pix[index-1]==1) recursiveHysteresis(index-1);
	if(pix[index+1]==1) recursiveHysteresis(index+1);
	if(pix[index+width-1]==1) recursiveHysteresis(index+width-1);
	if(pix[index+width]==1) recursiveHysteresis(index+width);
	if(pix[index+width+1]==1) recursiveHysteresis(index+width+1);
    }
}
