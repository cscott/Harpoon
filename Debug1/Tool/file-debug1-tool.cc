#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/mman.h>
#include "file-debug1-tool.h"
extern "C" {
#include "test.h"
}
#include "Hashtable.h"
#include "model.h"
#include "element.h"

char *dstring="d\0";
struct filedesc files[MAXFILES];
struct InodeBitmap ib;
struct BlockBitmap bb;

int bbbptr;
int ibbptr;
int itbptr;
int rdiptr;


void printfile(char *filename, struct block *ptr);

int main(int argc, char **argv) 
{
  for(int i=0;i<MAXFILES;i++)
    files[i].used=false;


  switch(argv[1][0]) {

  case '0': 
    {
      createdisk(); 
      return 1;
    }


  case '1': 
    { 
      struct block * ptr=mountdisk("disk");
      
      for(int i=0; i<NUMFILES; i++) 
	{
	  char filename[10];
	  sprintf(filename,"file_%d",i);
	  openfile(ptr,filename);
	}    

      for(int j=0; j<5; j++) {
	for(int i=0; i<NUMFILES; i++) 
	  {
	    char buf[100];
	    sprintf(buf,"Contents of file_%d.", i);
	    writefile(ptr,i,buf,strlen(buf));
	  }
      }
      

      for(int i=0; i<NUMFILES; i++) 
	{
	  char filename[10], linkname[10];
	  sprintf(filename,"file_%d", i);
	  sprintf(linkname, "link_%d", NUMFILES-i);
	  createlink(ptr, filename, linkname);
	}

      
      for(int i=0; i<NUMFILES; i++) 
	closefile(ptr,i);
      

      unmountdisk(ptr);
      break;
    }


  case '2': 
    {
      struct block * ptr=mountdisk("disk");
      initializeanalysis();
      Hashtable *env=exportmodel->gethashtable();
      alloc(ptr,LENGTH);
      addmapping(dstring,ptr,"Disk");

      // use the tool
      doanalysis();

      dealloc(ptr);
      unmountdisk(ptr);
      break;
    }
  }
}



struct block * chmountdisk(char *filename) 
{
  int fd=open(filename,O_CREAT|O_RDWR);
  struct block *ptr=(struct block *) mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  return ptr;
}



void chunmountdisk(struct block *vptr) 
{
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");
}



struct block * mountdisk(char *filename) 
{
  int fd=open(filename,O_CREAT|O_RDWR);
  struct block *ptr=(struct block *) mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  struct SuperBlock *sb=(struct SuperBlock *) &ptr[0];
  struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
  bbbptr=gb->BlockBitmapBlock;
  ibbptr=gb->InodeBitmapBlock;
  itbptr=gb->InodeTableBlock;
  rdiptr=sb->RootDirectoryInode;
  
  struct InodeBitmap *ibb=(struct InodeBitmap *) &ptr[ibbptr];
  for(int i=0;i<(NUMINODES/8+1);i++)
    ib.inode[i]=ibb->inode[i];
  
  struct BlockBitmap *bbb=(struct BlockBitmap *) &ptr[bbbptr];
  for(int i=0;i<(NUMBLOCK/8+1);i++)
    bb.blocks[i]=bbb->blocks[i];
  
  printf("Disk mounted successfully from the file %s\n", filename);
  fflush(NULL);
  
  return ptr;
}


void unmountdisk(struct block *vptr) 
{
  struct InodeBitmap *ibb=(struct InodeBitmap *) &vptr[ibbptr];
  for(int i=0;i<(NUMINODES/8+1);i++)
    ibb->inode[i]=ib.inode[i];

  struct BlockBitmap *bbb=(struct BlockBitmap *) &vptr[bbbptr];
  for(int i=0;i<(NUMBLOCK/8+1);i++)
    bbb->blocks[i]=bb.blocks[i];
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");
}


void removefile(char *filename, struct block *ptr) 
{
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/128;j++) 
	if (db->entries[j].name[0]!=0)
	  if(strcmp(filename,db->entries[j].name)==0) 
	    {
	      db->entries[j].name[0]=0; 
	      int inode=db->entries[j].inodenumber;
	      
	      struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
	      itb->entries[inode].referencecount--;
	      
	      if (itb->entries[inode].referencecount==0) 
		{
		  for(int i=0;i<((itb->entries[inode].filesize+BLOCKSIZE-1)/BLOCKSIZE);i++) 
		    {
		      int blocknum=itb->entries[inode].Blockptr[i];
		      bb.blocks[blocknum/8]^=(1<<(blocknum%8));
		    }
		  ib.inode[inode/8]^=(1<<(inode%8));
		}
	    }	
    }
}


void createlink(struct block *ptr,char *filename, char *linkname) 
{
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/128;j++) 	
	if (db->entries[j].name[0]!=0) 
	  if(strcmp(filename,db->entries[j].name)==0) 
	    {
	      int inode=db->entries[j].inodenumber;
	      struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
	      itb->entries[inode].referencecount++;
	      addtode(ptr, inode, linkname);
	    }
    }
}


void closefile(struct block *ptr, int fd) 
{
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];  
  msync(&itb->entries[fd],sizeof(DirectoryEntry),MS_SYNC);
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

  struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
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
      msync(&fchar[noffset],tocopy,MS_SYNC);
      i+=tocopy;
      tfd->offset+=tocopy;
    }
  if (itb->entries[files[fd].inode].filesize<files[fd].offset)
    itb->entries[files[fd].inode].filesize=files[fd].offset;
  return len;
}


char readfile(struct block *ptr, int fd) 
{
  char array[1];
  if (readfile(ptr,fd,array,1)==1)
    return array[0];
  else
    return EOF;
}


int readfile(struct block *ptr, int fd, char *buf, int len) 
{
  struct filedesc *tfd=&files[fd];
  if (tfd->used==false)
    return -1;
  
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int filelen=itb->entries[tfd->inode].filesize;
  
  if ((filelen-tfd->offset)<len)
    len=filelen-tfd->offset;

  for(int i=0;i<len;) 
    {
      int nbuffer=tfd->offset/BLOCKSIZE;
      int noffset=tfd->offset%BLOCKSIZE;
      int block=itb->entries[tfd->inode].Blockptr[nbuffer];
      char *fchar=(char *)&ptr[block];
      int tocopy=len-i;
      if (tocopy>(BLOCKSIZE-noffset))
	tocopy=BLOCKSIZE-noffset;
      memcpy(&buf[i],&fchar[noffset],tocopy);
      i+=tocopy;
      tfd->offset+=tocopy;
    }
  return len;
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


  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
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


void addtode(struct block *ptr, int inode, char *filename) 
{
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) 
	if (db->entries[j].name[0]==0) 
	  {
	    strncpy(db->entries[j].name,filename,124);
	    db->entries[j].inodenumber=inode;
	    msync(&db->entries[j],sizeof(DirectoryEntry),MS_SYNC);
	    return;
	  }    
    }
}



int getinode(struct block *ptr) 
{
  for(int i=0;i<NUMINODES;i++)
    if (!(ib.inode[i/8]&(1<<(i%8))))      
      return i;        

  return -1;
}
  

int getblock(struct block * ptr) 
{
  for(int i=0;i<NUMBLOCK;i++)
    if (!(bb.blocks[i/8]&(1<<(i%8))))
      return i;
  
  return -1;
}



void createdisk() 
{
  int blocksize=BLOCKSIZE;
  int numblocks=NUMBLOCK;

  int fd=open("disk",O_CREAT|O_RDWR|O_TRUNC, S_IREAD|S_IWRITE);
  
  char *buf=(char *)calloc(1,blocksize);
  for(int i=0;i<numblocks;i++) 
    write(fd,buf,blocksize);
  
  free(buf);
  
  void *vptr=mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  struct block *ptr=(struct block *)vptr;
  {
    struct SuperBlock * sb=(struct SuperBlock*) &ptr[0];
    sb->FreeBlockCount=NUMBLOCK-5;
    sb->FreeInodeCount=NUMINODES-1;
    sb->NumberofInodes=NUMINODES;
    sb->NumberofBlocks=NUMBLOCK;
    sb->RootDirectoryInode=0;
    sb->blocksize=BLOCKSIZE;
  }
  {
    struct GroupBlock * gb=(struct GroupBlock *) &ptr[1];
    gb->BlockBitmapBlock=2;
    gb->InodeBitmapBlock=3;
    gb->InodeTableBlock=4;
    gb->GroupFreeBlockCount=NUMBLOCK-5;
    gb->GroupFreeInodeCount=NUMINODES-1;
  }
  {
    struct BlockBitmap * bb=(struct BlockBitmap *) &ptr[2];
    for(int i=0;i<(5+12);i++)
      bb->blocks[i/8]=bb->blocks[i/8]|(1<<(i%8));
  }
  {
    struct InodeBitmap * ib=(struct InodeBitmap *) &ptr[3];
    ib->inode[0]=1;
  }
  {
    struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
    
    itb->entries[0].filesize=12*BLOCKSIZE;
    for(int i=0;i<12;i++)
      itb->entries[0].Blockptr[i]=i+5;
    itb->entries[0].referencecount=0;
  }
  
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");
  
  printf("Disk created successfully!\n");
}

