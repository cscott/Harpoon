// ************************************************************************
//    $Id: Assert.java,v 1.1 2002-07-02 15:35:46 wbeebee Exp $
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
package edu.uci.ece.ac.util;

/**
 * This class provides some basic assertion checking functionalities.
 *
 * @author Angelo Corsaro
 * @version 1.0
 */
public class Assert {

    // Disallow constructors
    private Assert() {
    }

    /**
     * Asserts that a precondition represented by a boolean
     * expression is preserved.
     *
     * @param expression the expression representing the precondition
     * @throws PreConditionException if the precondition being tested
     *         is violated
     */
    public static void preCondition(boolean expression) throws PreConditionException {
        if (expression == false)
            throw new PreConditionException();
    }


    /**
     * Asserts that a pre-condition represented by a boolean
     * expression is preserved.
     *
     * @param expression the expression representing the precondition
     * @param msg the message that will be associated with the
     * @param obj the object on which the precondition is being evaluated
     * @throws PreConditionException if the precondition being tested
     *         is violated
     */
    public static void preCondition(boolean expression, String msg) throws PreConditionException {
        if (expression == false)
            throw new PreConditionException(msg);
    }

    /**
     * Asserts that a post-condition represented by a boolean
     * expression is preserved.
     *
     * @param expression the expression representing the post-condition
     * @throws PostConditionException if the postcondition being tested
     *         is violated
     */
    public static void postCondition(boolean expression) throws PostConditionException {
        if (expression == false)
            throw new PostConditionException();
    }


    /**
     * Asserts that a post-condition represented by a boolean
     * expression is preserved.
     *
     * @param expression the expression representing the post-condition
     * @param msg the message that will be associated with the exception thrown
     * @throws PostConditionException if the post-condition being tested
     *         is violated
     */
    public static void postCondition(boolean expression, String msg) throws PostConditionException {
        if (expression == false)
            throw new PostConditionException(msg);
    }


    /**
     * Asserts that an invatiant represented by a boolean
     * expression is preserved.
     *
     * @param expression the expression representing the invariant
     * @throws InvariantConditionException if the postcondition being tested
     *         is violated
     */
    public static void invariant(boolean expression) throws InvariantConditionException {
        if (expression == false)
            throw new InvariantConditionException();
    }
}
