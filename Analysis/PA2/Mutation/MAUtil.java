// MAUtil.java, created Mon Sep  5 07:45:11 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Iterator;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;

import harpoon.Temp.Temp;

import harpoon.Analysis.PA2.PANode;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.PAUtil;

/**
 * <code>MAUtil</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MAUtil.java,v 1.1 2005-09-05 16:38:57 salcianu Exp $
 */
abstract class MAUtil {

    public static List<Temp> getParamTemps(HMethod hm, CachingCodeFactory ccf) {
	HCode hcode = ccf.convert(hm);
	METHOD method = ((HEADER) ccf.convert(hm).getRootElement()).method();
	return Arrays.asList(method.params());
    }


    public static List<String> getParamNames(HMethod hm) {
	List<String> paramNames = new LinkedList<String>();
	if(!hm.isStatic()) {
	    paramNames.add("this");
	}
	for(String paramName : hm.getParameterNames()) {
	    paramNames.add(paramName);
	}
	return paramNames;
    }


    public static List<ParamInfo> getParamInfo(HMethod hm, PointerAnalysis pa) {
	List<ParamInfo> paramInfo = new LinkedList<ParamInfo>();

	Iterator<HClass> itParamTypes = PAUtil.getParamTypes(hm).iterator();
	Iterator<Temp>   itParamTemps = MAUtil.getParamTemps(hm, pa.getCodeFactory()).iterator();
	Iterator<PANode> itParamNodes = pa.getNodeRep().getParamNodes(hm).iterator();
	Iterator<String> itParamNames = MAUtil.getParamNames(hm).iterator();

	while(itParamTypes.hasNext()) {
	    Temp   temp = itParamTemps.next();
	    HClass type = itParamTypes.next();
	    String declName = itParamNames.next();
	    PANode node = type.isPrimitive() ? null : itParamNodes.next();

	    paramInfo.add(new ParamInfo(temp, type, declName, node));
	}
  	
	return paramInfo;
    }

}
