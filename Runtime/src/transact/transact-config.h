// configuration information for the transactions implementation.
#ifndef INCLUDED_TRANSACT_CONFIG_H
#define INCLUDED_TRANSACT_CONFIG_H

// OBJ_CHUNK_SIZE must be larger than zero and divisible by eight
#define DO_HASH 0 // set to 1 to enable object chunking at all
#ifndef OBJ_CHUNK_SIZE
# define OBJ_CHUNK_SIZE (0x80000000/*24*/)
#endif
#define INITIAL_CACHE_SIZE 24

#endif /* INCLUDED_TRANSACT_CONFIG_H */
