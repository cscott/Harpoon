// VersionInfo.java, created Sun Nov  5 17:54:21 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime.Transactions;

/**
 * The <code>VersionInfo</code> structure identifies the transactions
 * associated with a particular version of an object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: VersionInfo.java,v 1.1.2.1 2000-11-06 21:28:17 cananian Exp $
 */
public class VersionInfo {
    /** Transaction identifier. */
    CommitRecord transid;
    /** List of readers. (First node should be inlined). */
    CommitRecord.List readers;
    /** Next version to look at if transid is aborted. */
    VersionInfo anext;
    /** Next version to look at if transid is waiting. */
    VersionInfo wnext;
}
