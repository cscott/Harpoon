package org.jacorb.security.level2;

import org.omg.SecurityLevel2.*;
import org.omg.Security.*;
import org.omg.CORBA.*;
/**
 * DelegationDirectivePolicyImpl.java
 *
 *
 * Created: Tue Jun 13 17:02:48 2000
 *
 * $Id: DelegationDirectivePolicyImpl.java,v 1.1 2003-04-03 16:56:46 wbeebee Exp $
 */

public class DelegationDirectivePolicyImpl 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements DelegationDirectivePolicy
{
  
    public DelegationDirectivePolicyImpl() 
    {
    
    }

    public DelegationDirective delegation_directive()
    {
        return null;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
        return null;
    }

    /**
     *
     */
    public void destroy() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
        return -1;
    }
} // DelegationDirectivePolicyImpl






