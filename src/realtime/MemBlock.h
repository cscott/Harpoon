/* MemBlock.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __MEMBLOCK_H__
#define __MEMBLOCK_H__

#include <jni.h>
#include <assert.h>
#include "jni-private.h"
#include "heapMemory.h"
#include "blockAllocator.h"
#include "listAllocator.h"
#include "RTJconfig.h"

typedef void* Allocator;

struct MemBlock;

struct BlockInfo {
  long refCount;
  jobject memoryArea;
  jobject realtimeThread;
  void* (*alloc) ( struct MemBlock* mem,  size_t size);
  void  (*free)  (struct MemBlock* mem);
  struct MemBlock* superBlock;
  Allocator allocator;
};

struct MemBlock {
  struct BlockInfo* block_info;
  struct Block* block;
};

struct MemBlock* MemBlock_new(JNIEnv* env,
			       jobject memoryArea, 
			       jobject realtimeThread,
			      struct MemBlock* superBlock);
inline struct MemBlock* HeapMemory_RThread_MemBlock_new();
inline void* MemBlock_alloc( struct MemBlock* memBlock, 
			     size_t size);
inline void MemBlock_free(struct MemBlock* memBlock);
inline struct MemBlock* MemBlock_prevMemBlock( struct MemBlock* memBlock);
inline long MemBlock_INCREF(struct MemBlock* memBlock);
inline long MemBlock_DECREF(struct MemBlock* memBlock);
inline struct inflated_oobj* getInflatedObject(JNIEnv* env, 
					        jobject obj);
inline int IsNoHeapRealtimeThread(JNIEnv *env, 
				   jobject realtimeThread);
inline void checkException(JNIEnv *env);
void* ScopedPhysical_RThread_MemBlock_alloc( struct MemBlock* mem,
					     size_t size);
void  ScopedPhysical_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator ScopedPhysical_RThread_MemBlock_allocator( jobject memoryArea);
void* ScopedPhysical_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem,
						   size_t size);
void  ScopedPhysical_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator ScopedPhysical_NoHeapRThread_MemBlock_allocator( jobject memoryArea);

void* ImmortalPhysical_RThread_MemBlock_alloc( struct MemBlock* mem,
					       size_t size);
void  ImmortalPhysical_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator ImmortalPhysical_RThread_MemBlock_allocator( jobject memoryArea);
void* ImmortalPhysical_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem,
						     size_t size);
void  ImmortalPhysical_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator ImmortalPhysical_NoHeapRThread_MemBlock_allocator( jobject memoryArea);

inline void* Scope_RThread_MemBlock_alloc( struct MemBlock* mem, 
					   size_t size);
inline void  Scope_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator Scope_RThread_MemBlock_allocator( jobject memoryArea);
void* CTScope_RThread_MemBlock_alloc( struct MemBlock* mem,  size_t size);
void  CTScope_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator CTScope_RThread_MemBlock_allocator( jobject memoryArea);
void* VTScope_RThread_MemBlock_alloc( struct MemBlock* mem, 
				      size_t size);
void  VTScope_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator VTScope_RThread_MemBlock_allocator( jobject memoryArea);
void* LTScope_RThread_MemBlock_alloc( struct MemBlock* mem, 
				      size_t size);
void  LTScope_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator LTScope_RThread_MemBlock_allocator( jobject memoryArea);

inline void* Scope_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem, 
						 size_t size);
inline void  Scope_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator Scope_NoHeapRThread_MemBlock_allocator( jobject memoryArea);
void* CTScope_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem, 
					    size_t size);
void  CTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator CTScope_NoHeapRThread_MemBlock_allocator( jobject memoryArea);
void* VTScope_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem, 
					    size_t size);
void  VTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator VTScope_NoHeapRThread_MemBlock_allocator( jobject memoryArea);
void* LTScope_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem, 
					    size_t size);
void  LTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator LTScope_NoHeapRThread_MemBlock_allocator( jobject memoryArea);

void* Heap_RThread_MemBlock_alloc( struct MemBlock* mem, 
				   size_t size);
void  Heap_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator Heap_RThread_MemBlock_allocator( jobject memoryArea);

void* Immortal_RThread_MemBlock_alloc( struct MemBlock* mem, 
					     size_t size);
void  Immortal_RThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator Immortal_RThread_MemBlock_allocator( jobject memoryArea);
void* Immortal_NoHeapRThread_MemBlock_alloc( struct MemBlock* mem, 
						   size_t size);
void  Immortal_NoHeapRThread_MemBlock_free(struct MemBlock* mem);
inline  Allocator Immortal_NoHeapRThread_MemBlock_allocator( jobject memoryArea);

#endif /* __MEMBLOCK_H__ */
