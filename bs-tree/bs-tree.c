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



int main(int argc, char **argv) {
  initializeanalysis();
  struct treeentry *tree=malloc(TREESIZE*sizeof(struct treeentry));


  
  int root=-1;
  int i=0;

  for (i=0;i<TREESIZE;i++) {
    tree[i].inuse=0;
  }
  addvalue(tree,&root,3);

  /* tool call */
  addintmapping(sstring, TREESIZE);
  addintmapping(rstring, root);
  addmapping(tstring, tree, "treenodes");
  doanalysisfordebugging("Invocation");
  /* --------- */

  addvalue(tree,&root,1);

  //tree[root].inuse=0; // Error insertion
  
  /* tool call */
  addintmapping(sstring, TREESIZE);
  addintmapping(rstring, root);
  addmapping(tstring, tree, "treenodes");
  doanalysisfordebugging("Invocation");
  /* --------- */ 

  addvalue(tree,&root,5);
  addvalue(tree,&root,4);

  
  //tree[root].rightindex = 3; // Error insertion -> node w/ value 4 has two parents

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
