package MCC.IR;

class AbstractInterferes {
    static public boolean interferes(AbstractRepair ar, DNFPredicate dp) {
	if ((ar.getDescriptor()!=dp.getPredicate().getDescriptor()) &&
	    ((ar.getDescriptor() instanceof SetDescriptor)||
	     !dp.getPredicate().usesDescriptor((RelationDescriptor)ar.getDescriptor())))
	    return false;

	/* This if handles all the c comparisons in the paper */
	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.ADDTOSET||ar.getType()==AbstractRepair.ADDTORELATION)&&
	    (ar.getPredicate().getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate().inverted()==ar.getPredicate().getPredicate().inverted())&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg1=ar.getPredicate().isNegated();
	    Opcode op1=((ExprPredicate)ar.getPredicate().getPredicate()).getOp();
	    int size1=((ExprPredicate)ar.getPredicate().getPredicate()).leftsize();
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).leftsize();
	    if ((!neg1&&((op1==Opcode.EQ)||(op1==Opcode.NE)||(op1==Opcode.GT)||op1==Opcode.GE))||
		(neg1&&((op1==Opcode.EQ)||(op1==Opcode.NE)||(op1==Opcode.LT)||op1==Opcode.LE))) {
		int size1a=0;
		if (!neg1) {
		    if((op1==Opcode.EQ)||(op1==Opcode.GE))
			size1a=size1;
		    if((op1==Opcode.GT)||(op1==Opcode.NE))
			size1a=size1+1;
		}
		if (neg1) {
		    if((op1==Opcode.EQ)||(op1==Opcode.LE))
			size1a=size1+1;
		    if((op1==Opcode.LT)||(op1==Opcode.NE))
			size1a=size1;
		}
		if ((!neg2&&(op2==Opcode.EQ)&&(size1a==size2))||
		    (neg2&&(op2==Opcode.EQ)&&(size1a!=size2))||
		    (!neg2&&(op2==Opcode.NE)&&(size1a!=size2))||
		    (neg2&&(op2==Opcode.NE)&&(size1a==size2))||
		    (!neg2&&(op2==Opcode.GE))||
		    (!neg2&&(op2==Opcode.GT))||
		    (neg2&&(op2==Opcode.LE))||
		    (neg2&&(op2==Opcode.LT))||
   		    (neg2&&(op2==Opcode.GE)&&(size1a<size2))||
		    (neg2&&(op2==Opcode.GT)&&(size1a<=size2))||
		    (!neg2&&(op2==Opcode.LE)&&(size1a<=size2))||
		    (!neg2&&(op2==Opcode.LT)&&(size1a<size2)))
		    return false;
	    } 
	}
	/* This if handles all the c comparisons in the paper */
	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.REMOVEFROMSET||ar.getType()==AbstractRepair.REMOVEFROMRELATION)&&
	    (ar.getPredicate().getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate().inverted()==ar.getPredicate().getPredicate().inverted())&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg1=ar.getPredicate().isNegated();
	    Opcode op1=((ExprPredicate)ar.getPredicate().getPredicate()).getOp();
	    int size1=((ExprPredicate)ar.getPredicate().getPredicate()).leftsize();
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).leftsize();
	    if ((neg1&&((op1==Opcode.EQ)||(op1==Opcode.LT)||op1==Opcode.LE)||(op1==Opcode.NE))||
		(!neg1&&((op1==Opcode.EQ)||(op1==Opcode.GT)||op1==Opcode.GE)||(op1==Opcode.NE))) {
		int size1a=0;
		if (neg1) {
		    if((op1==Opcode.EQ)||(op1==Opcode.GE))
			size1a=size1-1;
		    if((op1==Opcode.GT)||(op1==Opcode.NE))
			size1a=size1;
		}
		if (!neg1) {
		    if((op1==Opcode.EQ)||(op1==Opcode.LE))
			size1a=size1;
		    if((op1==Opcode.LT)||(op1==Opcode.NE))
			size1a=size1-1;
		}
		if ((!neg2&&(op2==Opcode.EQ)&&(size1a==size2))||
		    (neg2&&(op2==Opcode.EQ)&&(size1a!=size2))||
		    (!neg2&&(op2==Opcode.NE)&&(size1a!=size2))||
		    (neg2&&(op2==Opcode.NE)&&(size1a==size2))||
		    (neg2&&(op2==Opcode.GE))||
		    (neg2&&(op2==Opcode.GT))||
		    (!neg2&&(op2==Opcode.LE))||
		    (!neg2&&(op2==Opcode.LT))||
   		    (!neg2&&(op2==Opcode.GE)&&(size1a>=size2))||
		    (!neg2&&(op2==Opcode.GT)&&(size1a>size2))||
		    (neg2&&(op2==Opcode.LE)&&(size1a>size2))||
		    (neg2&&(op2==Opcode.LT)&&(size1a>=size2)))
		    return false;
	    } 


	}
	return true;
    }
}
