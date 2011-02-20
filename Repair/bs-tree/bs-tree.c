#include<stdlib.h>
#include "test.h"
#include "cmemory.h"

#define TREESIZE 100

struct treeentry {
  int inuse;
  int leftindex;
  int rightindex;
  int value;
};
void addvalue(struct treeentry * tree, int *root,int value);
void printtree(struct treeentry *tree, int root);

char *sstring="treesize\0";
char* rstring="root\0";
char* tstring="tree\0";

struct treeentry *tree;
int root=-1;


void consistent_use_of_inuse();
void single_parent();
void legal_values();


void RepOk() {
  // should be checked in this order!
  consistent_use_of_inuse();
  single_parent();
  legal_values();
}


void legal_values() {
  int i;
  for (i=0; i<TREESIZE; i++)
    if (tree[i].inuse) {
      int l = tree[i].leftindex;
      int r = tree[i].rightindex;
      
      if ( (l<-1) || (l>=TREESIZE) ) {
	printf("Invalid value for tree[%i].leftindex (%d)\n", i, tree[i].leftindex);
	_exit(0);
      }

      if ( (r<-1) || (r>=TREESIZE) ) {
	printf("Invalid value for tree[%i].rightindex (%d)\n", i, tree[i].rightindex);
	_exit(0);
      }
    }
}


void single_parent() {
  int parent[TREESIZE];
  int i;
  for (i=0; i<TREESIZE; i++) 
    parent[i]=-1;

  for (i=0; i<TREESIZE; i++) 
    if (tree[i].inuse) {      
      int l = tree[i].leftindex;
      int r = tree[i].rightindex;
      
      if (l != -1)
	if (parent[l] != -1) {
	  // already has a parent!
	  printf("Multiple parents for node %d: %d and %d\n", l, parent[l], i);
	  _exit(0);
	}
	else parent[l] = i;     
      
      if (r != -1)
	if (parent[r] != -1) {
	// already has a parent!
	  printf("Multiple parents for node %d: %d and %d\n", r, parent[r], i);
	  _exit(0);
	}
	else parent[r] = i;    
    }
}


void fill(int node, int* referenced);

void consistent_use_of_inuse() {
  int* referenced = malloc(TREESIZE*sizeof(int));
  memset(referenced, 0, TREESIZE*sizeof(int));

  if (root != -1)
    fill(root, referenced);
  
  int i;
  for (i=0; i<TREESIZE; i++)
    if ( (tree[i].inuse == 1) && (referenced[i] == 0) || 
	 (tree[i].inuse == 0) && (referenced[i] == 1) ) {
      printf("Inconsistent use of inuse: tree[%d].inuse=%d, referenced[%d]=%d\n", i, tree[i].inuse, i, referenced[i]);
      _exit(0);
    }
}


void fill(int node, int* referenced) {
  if (referenced[node] == 1)
    return;

  referenced[node]=1;
  
  if (tree[node].leftindex != -1)
    fill(tree[node].leftindex, referenced);
  if (tree[node].rightindex != -1)
    fill(tree[node].rightindex, referenced);
}


int main(int argc, char **argv) {
  initializeanalysis();

  tree = malloc(TREESIZE*sizeof(struct treeentry));
  int i=0;  

  for (i=0;i<TREESIZE;i++) {
    tree[i].inuse=0;
  }

  addvalue(tree,&root,3);

  // RepOk call
  RepOk();

  /* tool call */
  addintmapping(sstring, TREESIZE);
  addintmapping(rstring, root);
  addmapping(tstring, tree, "treenodes");
  doanalysisfordebugging("Invocation");
  /* --------- */

  addvalue(tree,&root,1);

  //tree[root].inuse=0; // Error insertion

  // RepOk call
  RepOk();  
  
  /* tool call */
  addintmapping(sstring, TREESIZE);
  addintmapping(rstring, root);
  addmapping(tstring, tree, "treenodes");
  doanalysisfordebugging("Invocation");
  /* --------- */ 

  addvalue(tree,&root,5);
  addvalue(tree,&root,4);

  
  // Error insertion -> node w/ value 4 has two parents
  
  tree[root].rightindex = 3; 
  tree[3].leftindex = 2;

  // RepOk call
  RepOk();

  /* tool call */
  addintmapping(sstring, TREESIZE);
  addintmapping(rstring, root);
  addmapping(tstring, tree, "treenodes");
  doanalysisfordebugging("Invocation");
  /* --------- */ 
  
  printf("root=%ld\n", tree[root].value);
  printtree(tree,root);
}

void printtree(struct treeentry *tree, int root) {
  if (root==-1)
    return;
  printtree(tree,tree[root].leftindex);

  int leftson = -1;
  if (tree[root].leftindex != -1)
    leftson = tree[tree[root].leftindex].value;
  int rightson = -1;
  if (tree[root].rightindex != -1)
    rightson = tree[tree[root].rightindex].value;    

  printf("(node=%ld index=%d left=%ld right=%ld)\n",tree[root].value, root, leftson, rightson);
  printtree(tree,tree[root].rightindex);
}

void addvalue(struct treeentry *tree, int *root,int value) {
  int freenode;
  int *ptr=root;
  for(freenode=0;freenode<TREESIZE;freenode++) {
    if (tree[freenode].inuse==0)
      break;
  }
  if (freenode==TREESIZE)
    return;
  tree[freenode].value=value;
  tree[freenode].inuse=1;
  tree[freenode].leftindex=-1;
  tree[freenode].rightindex=-1;
  while(*ptr!=-1) {
    if (value<tree[*ptr].value)
      ptr=&tree[*ptr].leftindex;
      else
      ptr=&tree[*ptr].rightindex;
  }
  *ptr=freenode;
}
