// ************************************************************************
//    $Id: PerformanceReport.java,v 1.1 2002-07-02 15:35:35 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.ac.time;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.File;
import java.io.FileOutputStream;

/**
 * This class provide a way of encapsulating performances results. 
 * Performance results are represented as a collection of set of
 * <code>MeasuredVariable</code>s that represent measure of the same
 * variable.
 * 
 * <b>NOTE:</b> This class is purposly not thread safe.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class PerformanceReport {

    private String reportName;
    private String description;
    
    private Hashtable measuredVariableList = new Hashtable(); 
    
    public PerformanceReport(String reportName) {
        this.reportName = reportName;
    }
    
    public PerformanceReport(String reportName, String description) {
        this.reportName = reportName;
        this.description = description;
    }
    
    
    public void addMeasuredVariable(String name, Object value) {
        MeasuredVariable mv = new MeasuredVariable(name, value);
        this.getMeasuredVariableList(name).addElement(mv);
        mv = null;
    }
    
    public void addMeasuredVariable(MeasuredVariable mv) {
        this.getMeasuredVariableList(mv.getName()).addElement(mv);
    }

    public Vector getMeasuredVariable(String name) {
        return this.getMeasuredVariableList(name);
    }

    public Enumeration getMeasuredVariables() {
        return this.measuredVariableList.elements();
    }

    public Enumeration getMeasuredVariableNames() {
        Vector v = new Vector();
        Enumeration variables = this.getMeasuredVariables();
        String name;
        MeasuredVariable mv;
        while (variables.hasMoreElements()) {
            mv = (MeasuredVariable)((Vector)variables.nextElement()).firstElement();
            name = mv.getName();
            v.addElement(name);
        }
        
        return v.elements();
    }            

    public void preallocateMeasuredVariableStorage(String name, int size) {
        Vector v = this.getMeasuredVariableList(name);
        if (v == null) {
            v = new Vector(size);
            this.measuredVariableList.put(name, v);
        }
    }
    
    public String toString() {
        Enumeration variables = this.getMeasuredVariables();
        String str = "";
        Vector vec;
        MeasuredVariable mv;
        while (variables.hasMoreElements()) {
            vec = (Vector)variables.nextElement();
            for (int i = 0; i < vec.size(); ++i) {
                mv = (MeasuredVariable)vec.elementAt(i);
                str += mv.toString() +"\n";
            }
        }
        return str;
    }
    
    public void generateDataFile(String path) throws java.io.IOException {
        Enumeration variables = this.getMeasuredVariables();
        Vector vec;
        MeasuredVariable mv;
        String testResultPath = path + "/" + this.reportName;
        File testResultDir = new File(testResultPath);
        testResultDir.mkdirs();
        
        while (variables.hasMoreElements()) {
            vec = (Vector)variables.nextElement();
            mv = (MeasuredVariable)vec.firstElement();
            File file = new File(testResultPath + "/" + mv.getName());
            FileOutputStream fostream = new FileOutputStream(file);
            
            fostream.write(mv.getValue().toString().getBytes());
            fostream.write('\n');
            
            for (int i = 1; i < vec.size(); ++i) {
                mv = (MeasuredVariable)vec.elementAt(i);
                fostream.write(mv.getValue().toString().getBytes());
                fostream.write('\n');
            }

            fostream.flush();
            fostream.close();
            file = null;
            fostream = null;
        }
    }


    private Vector getMeasuredVariableList(String name) {
        Vector vec = (Vector)this.measuredVariableList.get(name);

        if (vec == null) {
            vec = new Vector();
            this.measuredVariableList.put(name, vec);
        }

        return vec;
    }
}
