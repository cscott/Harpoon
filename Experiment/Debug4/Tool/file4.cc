#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/mman.h>
#include "file.h"

#ifdef TOOL
extern "C" {
#include "test.h"
}

#include "Hashtable.h"
#include "model.h"
#include "element.h"
#include "classlist.h"

char *dstring="d\0";  // this constant is used by the tool; please ignore it
#endif


struct block *ptr;

struct filedesc files[MAXFILES];
struct InodeBitmap *ibb;
struct BlockBitmap *bbb;

int bbbptr; 
int ibbptr; 
int itbptr; 
int rdiptr; 

int callnumber = 0;


int main(int argc, char **argv) 
{
  ptr=createdisk();


  for(int i=0;i<MAXFILES;i++)
    files[i].used=false;

  
  int fd1 = openfile(ptr, "file1");

  char buf[100];
  sprintf(buf, "ONE");
  writefile(ptr, fd1, buf, strlen(buf));

  createlink(ptr, "file1", "link1");

  closefile(ptr, fd1);

  removefile("file1", ptr);
  
  int fd2 = openfile(ptr, "file2");
  
  sprintf(buf, "TWO");
  writefile(ptr, fd1, buf, strlen(buf));

  createlink(ptr, "file2", "link2");

  printfile("link2", ptr);
}




struct block* createdisk()
{
  int blocksize=BLOCKSIZE;
  int numblocks=NUMBLOCK;

  block *ptr=(struct block*) calloc(numblocks, blocksize);
  
  struct SuperBlock *sb=(struct SuperBlock*) &ptr[0];
  sb->FreeBlockCount=NUMBLOCK-5;
  sb->FreeInodeCount=NUMINODES-1;
  sb->NumberofInodes=NUMINODES;
  sb->NumberofBlocks=NUMBLOCK;
  sb->RootDirectoryInode=0;
  sb->blocksize=BLOCKSIZE;
    
  struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
  gb->BlockBitmapBlock=2;
  gb->InodeBitmapBlock=3;
  gb->InodeTableBlock=4;
  gb->GroupFreeBlockCount=NUMBLOCK-5;
  gb->GroupFreeInodeCount=NUMINODES-1;

  struct BlockBitmap *bb=(struct BlockBitmap *) &ptr[2];
  for(int i=0;i<(5+12);i++)
    bb->blocks[i/8]=bb->blocks[i/8]|(1<<(i%8));


  struct InodeBitmap *ib=(struct InodeBitmap *) &ptr[3];
  ib->inode[0]=1;


  struct InodeTable *itb=(struct InodeTable *) &ptr[4];

  itb->entries[0].referencecount=0;    
  itb->entries[0].filesize=12*BLOCKSIZE;
  for(int i=0;i<12;i++)
    itb->entries[0].Blockptr[i]=i+5;

  
  bbbptr=gb->BlockBitmapBlock;
  ibbptr=gb->InodeBitmapBlock;
  itbptr=gb->InodeTableBlock;
  rdiptr=sb->RootDirectoryInode;

  
  ibb=(struct InodeBitmap *) &ptr[ibbptr];
  bbb=(struct BlockBitmap *) &ptr[bbbptr];

  printf("Disk created successfully!\n");

  return ptr;
}




int openfile(struct block *ptr, char *filename) 
{
  int fd=-1;
  for(int k=0;k<MAXFILES;k++) {
    if(!files[k].used) 
      {	
	fd=k;
	files[fd].used=true;
	break;
      }
  }

  if (fd==-1) 
    return fd;


  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) 	
	if (db->entries[j].name[0]!=0)
	  if(strcmp(filename, db->entries[j].name)==0) 
	    {
	      files[fd].inode=db->entries[j].inodenumber;
	      files[fd].offset=0;
	      return fd;
	    }	  	
    }
  

  int inode=getinode(ptr);  
  if (inode==-1) 
    {
      files[fd].used=false;
      return -1;
    }
  itb->entries[inode].filesize=0;
  itb->entries[inode].referencecount=1;
  for (int i=0;i<12;i++)
    itb->entries[inode].Blockptr[i]=0;
  
  addtode(ptr, inode, filename);
  files[fd].inode=inode;
  files[fd].offset=0;


  return fd;
}


void createfile(struct block *ptr,char *filename, char *buf,int buflen) 
{
  int fd=openfile(ptr,filename);
  writefile(ptr,fd,buf,buflen);
  closefile(ptr,fd);
}



void removefile(char *filename, struct block *ptr) 
{
  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) 
	if (db->entries[j].name[0]!=0)
	  if(strcmp(filename,db->entries[j].name)==0) 
	    {
	      db->entries[j].name[0]=0; 
	      int inode=db->entries[j].inodenumber;
	      db->entries[j].inodenumber=0;
	      
	      struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
	      itb->entries[inode].referencecount--;

	      if (itb->entries[inode].referencecount==0) {
		for(int i=0;i<((itb->entries[inode].filesize+BLOCKSIZE-1)/BLOCKSIZE);i++) {
		  int blocknum=itb->entries[inode].Blockptr[i];
		  bbb->blocks[blocknum/8]^=(1<<(blocknum%8));
		  
		  struct SuperBlock *sb=(struct SuperBlock*) &ptr[0];
		  sb->FreeBlockCount++;
    
		  struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
		  gb->GroupFreeBlockCount++;
		}

		ibb->inode[inode/8]^=(1<<(inode%8));

		struct SuperBlock *sb=(struct SuperBlock*) &ptr[0];
		sb->FreeInodeCount++;
    
		struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
		gb->GroupFreeInodeCount++;
	      }
	    }	
    }
}


void createlink(struct block *ptr, char *filename, char *linkname) 
{
  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) 	
	if (db->entries[j].name[0]==0) 
	  if(strcmp(filename,db->entries[j].name)==0) 
	    {
	      int inode=db->entries[j].inodenumber;
	      struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
	      itb->entries[inode].referencecount++;
	      addtode(ptr, inode, linkname);
	    }
    }
}


void closefile(struct block *ptr, int fd) 
{
  files[fd].used=false;
}


bool writefile(struct block *ptr, int fd, char *s) 
{
  return (writefile(ptr,fd,s,1)==1);
}


int writefile(struct block *ptr, int fd, char *s, int len) 
{
  struct filedesc *tfd=&files[fd];
  if (tfd->used==false)
    return -1;

  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
  int filelen=itb->entries[tfd->inode].filesize;
  if ((12*BLOCKSIZE-tfd->offset)<len)
    len=12*BLOCKSIZE-tfd->offset;

  for(int i=0;i<len;i++) 
    {
      int nbuffer=tfd->offset/BLOCKSIZE;
      int noffset=tfd->offset%BLOCKSIZE;
      if (tfd->offset>=filelen) 
	if (noffset==0) 
	  {
	    int bptr=getblock(ptr);
	    if (bptr==-1) 
	      {
		if (itb->entries[files[fd].inode].filesize<files[fd].offset)
		  itb->entries[files[fd].inode].filesize=files[fd].offset; 
		return i;
	      }
	    itb->entries[tfd->inode].Blockptr[nbuffer]=bptr;
	  }

      int block=itb->entries[tfd->inode].Blockptr[nbuffer];
      char *fchar=(char *)&ptr[block];
      int tocopy=len-i;
      if (tocopy>(BLOCKSIZE-noffset))
	tocopy=BLOCKSIZE-noffset;
      memcpy(&fchar[noffset],&s[i],tocopy);
      i+=tocopy;
      tfd->offset+=tocopy;
    }
  if (itb->entries[files[fd].inode].filesize<files[fd].offset)
    itb->entries[files[fd].inode].filesize=files[fd].offset;

  return len;
}


void addtode(struct block *ptr, int inode, char *filename) 
{
  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) 
	if (db->entries[j].name[0]==0) 
	  {
	    strncpy(db->entries[j].name,filename,124);
	    db->entries[j].inodenumber=inode;
	    return;
	  }    
    }
}



int getinode(struct block *ptr) 
{
  for(int i=0;i<NUMINODES;i++)
    if (!(ibb->inode[i/8]&(1<<(i%8))))
      {
	ibb->inode[i/8]=ibb->inode[i/8]|(1<<(i%8));

	struct SuperBlock *sb=(struct SuperBlock*) &ptr[0];
	sb->FreeInodeCount--;
    
	struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
	gb->GroupFreeInodeCount--;

	return i;
      }
  
  return -1;
}
  

int getblock(struct block * ptr) 
{
  for(int i=0;i<NUMBLOCK;i++)
    if (!(bbb->blocks[i/8]&(1<<(i%8))))
      {
	bbb->blocks[i/8]=bbb->blocks[i/8]|(1<<(i%8));

	struct SuperBlock *sb=(struct SuperBlock*) &ptr[0];
	sb->FreeBlockCount--;
    
	struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
	gb->GroupFreeBlockCount--;
	
	return i;
      }
  
  return -1;
}





void printfile(char *filename, struct block *ptr) 
{
  printf("=== BEGIN of %s ===\n", filename);
  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
  for(int i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  /* Found file */
	  int inode=db->entries[j].inodenumber;

	  struct InodeTable * itb=(struct InodeTable *) &ptr[itbptr];
	  for(int i=0;i<((itb->entries[inode].filesize+BLOCKSIZE-1)/BLOCKSIZE);i++) {
	    struct block *b=&ptr[itb->entries[inode].Blockptr[i]];
	    write(0,b,BLOCKSIZE);
	  }
	}
      }
    }
  }
  printf("\n=== END of %s ===\n", filename);
}




void calltool(char *text)
{
#ifdef TOOL
  // initialize the tool 
  initializeanalysis();
  Hashtable *env=exportmodel->gethashtable();
  alloc(ptr,LENGTH);
  addmapping(dstring,ptr,"Disk");

  callnumber++;
  printf("\n%s - Call %d\n", text, callnumber);
  bool errors = doanalysis();

  dealloc(ptr);

  if (errors)
    {
      fflush(NULL);
      exit(0);
    }
#endif
}
