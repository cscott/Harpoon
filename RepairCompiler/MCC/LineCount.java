package MCC;

import java.util.*;

public class LineCount {

    private static Vector lineBreaks = new Vector();

    public static void reset() {
        lineBreaks = new Vector();
    }

    public static void addLineBreak (int pos) {
	lineBreaks.addElement(new Integer(pos));
    }

    public static int getLine (int pos) {
	int i;
	int a;

	for (i = 0; i < lineBreaks.size(); i++) {
	    a = ((Integer) lineBreaks.elementAt(i)).intValue();
	    if (pos < a) {
		return (i + 1);
	    }
	}

	return (i + 1);
    }

    public static int getColumn (int pos) {
	int i = 0;
	int a = 0;

	for (i = lineBreaks.size() - 1; i >= 0 ; i--) {
	    a = ((Integer) lineBreaks.elementAt(i)).intValue();
	    if (pos > a) {
		return pos - a;
	    }
	}

	return pos - a ;
    }

}
