/* TitledBorder.java -- 
   Copyright (C) 2002 Free Software Foundation, Inc.

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

package javax.swing.border;

import java.awt.*;

public class TitledBorder extends AbstractBorder
{
    public static int ABOVE_BOTTOM = 0;
    public static int ABOVE_TOP = 1;
    public static int BELOW_BOTTOM = 2;
    public static int BELOW_TOP = 3;
    public static int BOTTOM = 4;
    public static int CENTER = 5;
    public static int DEFAULT_JUSTIFICATION = 6;
    public static int DEFAULT_POSITION = 7;
    protected static int EDGE_SPACING = 8;
    public static int LEADING = 9;
    public static int LEFT = 10;
    public static int RIGHT = 11;
    protected static int TEXT_INSET_H = 12;
    protected static int TEXT_SPACING = 13;
    protected String title;
    protected Color titleColor;
    protected Border border;
    protected Font titleFont;
    protected int titleJustification;
    protected int titlePosition;
    public static int TOP = 14;
    public static int TRAILING = 15;

    public TitledBorder() {
	super();
    }

    public TitledBorder(Border b) {
	this(b, "");
    }

    public TitledBorder(Border b, String title) {
	this(b, title, 0, 0);
    }

    public TitledBorder(Border b, String title, int titleJustification, int titlePosition) {
	this(b, title, titleJustification, titlePosition, null);
    }

    public TitledBorder(Border b, String title, int titleJustification, int titlePosition, Font titleFont) {
	this(b, title, titleJustification, titlePosition, titleFont, null);
    }

    public TitledBorder(Border b, String title, int titleJustification, int titlePosition, Font titleFont, Color titleColor) {
	setBorder(b);
	setTitle(title);
	setTitleJustification(titleJustification);
	setTitlePosition(titlePosition);
	setTitleFont(titleFont);
	setTitleColor(titleColor);
    }

    public TitledBorder(String title) {
	this(null, title);
    }

    public Border getBorder() {
	return border;
    }

    public Insets getBorderInsets(Component c) {
	return null;
    }

    public Insets getBorderInsets(Component  c,
				  Insets s)
    {
	s.left = s.right = s.top = s.bottom = 5;
	return s;
    }
    
    protected Font getFont(Component c) {
	throw new Error("unimplemented");
    }

    public Dimension getMinimumSize(Component c) {
	throw new Error("unimplemented");
    }
    
    public String getTitle() {
	return title;
    }

    public Color getTitleColor() {
	return titleColor;
    }

    public Font getTitleFont() {
	return titleFont;
    }

    public int getTitleJustification() {
	return titleJustification;
    }

    public int getTitlePosition() {
	return titlePosition;
    }

    public boolean isBorderOpaque()
    {
	return false;
    }
    
    public void paintBorder(Component c,
			    Graphics  g, 
			    int  x,
			    int  y, 
			    int  width, 
			    int  height)
    {
    }
   
    public void setBorder(Border border) {
	this.border = border;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public void setTitleColor(Color titleColor) {
	this.titleColor = titleColor;
    }

    public void setTitleFont(Font titleFont) {
	this.titleFont = titleFont;
    }

    public void setTitleJustification(int titleJustification) {
	this.titleJustification = titleJustification;
    }

    public void setTitlePosition(int titlePosition) {
	this.titlePosition = titlePosition;
    }

}

