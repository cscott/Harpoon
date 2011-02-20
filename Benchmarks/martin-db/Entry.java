/*
 * @(#)Entry.java	1.4 06/17/98
 *
 * Entry.java   Version 1.0 03/03/97 rrh
 * Randy Heisch       IBM Corp. - Austin, TX
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1996,1997,1998 IBM Corporation, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * IBM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.util.*;


public class Entry
   {
   Vector items;


   Entry()
      {
      items = new Vector();
      }


   public boolean equals(Object o)
      {
      Entry entry;
      Object o1, o2;

      if ( !(o instanceof Entry) )
         return false;

      entry = (Entry)o;

      if ( entry.items.size() != items.size() )
         return false;

      Enumeration e1 = items.elements();
      Enumeration e2 = entry.items.elements();

      while ( e1.hasMoreElements() )
         {
         o1 = e1.nextElement();
         o2 = e2.nextElement();

         if ( !((String)o1).equals(((String)o2)) )
            return false;
         }

      return true;
      }



   public int hashCode()
      {
      int hc = 0;
      String s;

      Enumeration e1 = items.elements();

      while ( e1.hasMoreElements() )
         {
         s = (String)e1.nextElement();

         hc += s.hashCode();
         }

      return hc;
      }



   }


