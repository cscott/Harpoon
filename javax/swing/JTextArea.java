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

package javax.swing;

import java.awt.*;
import javax.swing.text.*;
import javax.accessibility.*;

public class JTextArea extends JTextComponent {
    public JTextArea() {
	throw new Error("Unimplemented");
    }

    public JTextArea(Document doc) {
	throw new Error("Unimplemented");
    }

    public JTextArea(Document doc, String text, int rows, int columns) {
	throw new Error("Unimplemented");
    }

    public JTextArea(String text) {
	throw new Error("Unimplemented");
    }

    public JTextArea(String text, int rows, int columns) {
	throw new Error("Unimplemented");
    }

    public void append(String str) {
	throw new Error("Unimplemented");
    }

    protected Document createDefaultModel() {
	throw new Error("Unimplemented");
    }

    public AccessibleContext getAccessibleContext() {
	throw new Error("Unimplemented");
    }

    public int getColumns() {
	throw new Error("Unimplemented");
    }

    protected int getColumnWidth() {
	throw new Error("Unimplemented");
    }

    public int getLineCount() {
	throw new Error("Unimplemented");
    }

    public int getLineEndOffset(int line) {
	throw new Error("Unimplemented");
    }

    public int getLineOfOffset(int offset) {
	throw new Error("Unimplemented");
    }

    public int getLineStartOffset(int line) {
	throw new Error("Unimplemented");
    }

    public boolean getLineWrap() {
	throw new Error("Unimplemented");
    }

    public Dimension getPreferredScrollableViewportSize() {
	throw new Error("Unimplemented");
    }

    public Dimension getPreferredSize() {
	throw new Error("Unimplemented");
    }
    
    protected int getRowHeight() {
	throw new Error("Unimplemented");
    }

    public int getRows() {
	throw new Error("Unimplemented");
    }

    public boolean getScrollableTracksViewportWidth() {
	throw new Error("Unimplemented");
    }
    
    public boolean getScrollableTracksViewportHeight() {
	throw new Error("Unimplemented");
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
					  int orientation,
					  int direction) {
	throw new Error("Unimplemented");
    }

    public int getTabSize() {
	throw new Error("Unimplemented");
    }

    public String getUIClassID() {
	throw new Error("Unimplemented");
    }

    public boolean getWrapStyleWord() {
	throw new Error("Unimplemented");
    }

    public void insert(String str, int pos) {
	throw new Error("Unimplemented");
    }

    protected String paramString() {
	return "JTextArea";
    }
    
    public void replaceRange(String str, int start, int end) {
	throw new Error("Unimplemented");
    }

    public void setColumns(int columns) {
	throw new Error("Unimplemented");
    }

    public void setEditable(boolean editable) {
	throw new Error("Unimplemented");
    }

    public void setFont(Font f) {
	throw new Error("Unimplemented");
    }

    public void setLineWrap(boolean wrap) {
	throw new Error("Unimplemented");
    }

    public void setRows(int rows) {
	throw new Error("Unimplemented");
    }

    public void setTabSize(int size) {
	throw new Error("Unimplemented");
    }

    public void setWrapStyleWord(boolean word) {
	throw new Error("Unimplemented");
    }
}
