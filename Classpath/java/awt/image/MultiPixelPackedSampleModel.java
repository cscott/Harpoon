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

import gnu.java.awt.Buffers;
/**
 * @author Wes Beebee <wbeebee@mit.edu>
 */
public class MultiPixelPackedSampleModel extends SampleModel {
    private int scanlineStride;
    private int dataBitOffset;
    private int numberOfBits;

    public MultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits) {
	super(dataType, w, h, numberOfBits);
	this.numberOfBits = numberOfBits;
    }

    public MultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits, int scanlineStride, int dataBitOffset) {
	this(dataType, w, h, numberOfBits);
	
    }

    public SampleModel createCompatibleSampleModel(int w, int h) {
	return new MultiPixelPackedSampleModel(dataType, w, h, numberOfBits, 
					       scanlineStride, dataBitOffset);
    }

    public DataBuffer createDataBuffer() {
	int size = scanlineStride*height;
	return Buffers.createBuffer(getDataType(), size);
    }

    public SampleModel createSubsetSampleModel(int[] bands) {
	throw new Error("unimplemented");
    }

    public boolean equals(Object o) {
	throw new Error("unimplemented");
    }

    public int getBitOffset(int x) {
	throw new Error("unimplemented");
    }

    public int getDataBitOffset() {
	throw new Error("unimplemented");
    }

    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
	int offset = scanlineStride*y + x + data.getOffset();

	return Buffers.getData(data, offset, obj, 0, 1);
    }

    public int getNumDataElements() {
	throw new Error("unimplemented");
    }
    
    public int getOffset(int x, int y) {
	throw new Error("unimplemented");
    }

    public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
	throw new Error("unimplemented");
    }

    public int getPixelBitStride() {
	throw new Error("unimplemented");
    }

    public int getSample(int x, int y, int b, DataBuffer data) {
	throw new Error("unimplemented");
    }

    public int[] getSampleSize() {
	throw new Error("unimplemented");
    }

    public int getSampleSize(int band) {
	throw new Error("unimplemented");
    }

    public int getScanlineStride() {
	throw new Error("unimplemented");
    }

    public int getTransferType() {
	throw new Error("unimplemented");
    }

    public int hashCode() {
	throw new Error("unimplemented");
    }

    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
	throw new Error("unimplemented");
    }

    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
	throw new Error("unimplemented");
    }

    public void setSample(int x, int y, int b, int s, DataBuffer data) {
	throw new Error("unimplemented");
    }

}

