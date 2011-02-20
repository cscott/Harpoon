// ObjectDataPoint.java, created by benster 5/8/2003
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import imagerec.graph.ImageData;


/**
 * Class that holds information about a
 * snapshot of an object's state.
 * Used by the {@link ObjectInfo} class and
 * probably by any class that that uses
 * the {@link ObjectInfo} class.
 *
 * @see ObjectInfo
 */
public class ObjectDataPoint {
    
    private ImageData data;

    ObjectDataPoint(int x, int y, int width,
		    int height, float angle,
		    int imageDataID, long time,
		    float c1, float c2, float c3, float scaleFactor) {
	data = new ImageData();
	data.x = x;
	data.y = y;
	data.width = width;
	data.height = height;
	data.angle = angle;
	data.id = imageDataID;
	data.time = time;
	data.c1 = c1;
	data.c2 = c2;
	data.c3 = c3;
	data.scaleFactor = scaleFactor;
    }

    ObjectDataPoint(ImageData id) {
	data = ImageDataManip.clone(id);
    }

    public int getX() {return data.x;}
    public int getY() {return data.y;}
    public int getWidth() {return data.width;}
    public int getHeight() {return data.height;}
    public float getAngle() {return data.angle;}
    public int getID() {return data.id;}
    public long getTime() {return data.time;}
    public float getC1() {return data.c1;}
    public float getC2() {return data.c2;}
    public float getC3() {return data.c3;}
    public float getScaleFactor() {return data.scaleFactor;}

    public void setX(int x) {data.x = x;}
    public void setY(int y) {data.y = y;}
    public void setWidth(int width) {data.width = width;}
    public void setHeight(int height) {data.height = height;}
    public void setAngle(float angle) {data.angle = angle;}
    public void setID(int id) {data.id = id;}
    public void setTime(long time) {data.time = time;}
    public void setC1(float c1) {data.c1 = c1;}
    public void setC2(float c2) {data.c2 = c2;}
    public void setC3(float c3) {data.c3 = c3;}
    public void setScaleFactor(float scaleFactor) {data.scaleFactor = scaleFactor;}
}
