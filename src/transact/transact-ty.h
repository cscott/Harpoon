/* Definitions of 'safe' types related to transactions. */
#ifndef INCLUDED_TRANSACT_TY_H
#define INCLUDED_TRANSACT_TY_H

/* a pair of versions; returned by findVersion. */
struct versionPair {
  struct vinfo *committed;
  struct vinfo *waiting;
};

/* enumerated types */
enum opstatus { SUCCESS=-1, SAW_FALSE_FLAG=0, SAW_RACE=1, SAW_RACE_CLEANUP };
enum killer { KILL_WRITERS=-1, KILL_ALL=0 };

#endif /* INCLUDED_TRANSACT_TY_H */
