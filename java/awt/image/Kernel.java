/* Copyright (C) 2000, 2002  Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.awt.image;

public class Kernel implements Cloneable {
    int width;
    int height;
    float[] data;

    public Kernel(int width, int height, float[] data) {
	this.width = width;
	this.height = height;
	this.data = new float[width*height];
	try {
	    System.arraycopy(data,0,this.data,0,
			     (this.width=width)*(this.height=height));
	} catch (ArrayStoreException e) {
	    throw new IllegalArgumentException("data.length = "+data.length+
					       ", width ="+width+
					       ", height ="+height);
	} catch (IndexOutOfBoundsException e) {
	    throw new IllegalArgumentException("data.length = "+data.length+
					       ", width ="+width+
					       ", height ="+height);
	}
    }

    public Object clone() {
	return new Kernel(width, height, data);
    }

    public final int getHeight() {
	return height;
    }

    public final float[] getKernelData(float[] data) {
	if (data == null) {
	    return (float[])this.data.clone();
	}
	try {
	    System.arraycopy(this.data,0,data,0,this.data.length);
	} catch (Exception e) {
	    throw new IllegalArgumentException(e.toString());
	}
	return data;
    }

    public final int getWidth() {
	return width;
    }

    public final int getXOrigin() {
	return (width-1)/2;
    }

    public final int getYOrigin() {
	return (height-1)/2;
    }
}
