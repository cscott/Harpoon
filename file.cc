#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/mman.h>
#include "file.h"
extern "C" {
#include "test.h"
}
#include "Hashtable.h"
#include "model.h"
#include "element.h"
#include "classlist.h"

char *dstring="d\0";
struct filedesc files[MAXFILES];
struct InodeBitmap ib;
struct BlockBitmap bb;

int bbbptr;  // pointer to the BlockBitmap block
int ibbptr;  // pointer to the InodeBlock block
int itbptr;  // pointer to the InodeTable block
int rdiptr;  // pointer to the RootDirectoryInode block


int main(int argc, char **argv) 
{

  for(int i=0;i<MAXFILES;i++)
    files[i].used=false;


  switch(argv[1][0]) {

  case '0': 
    //creates a disk
    createdisk(); 
    return 1;


  case '1': { 
    /* mounts the disk, creates NUMFILES files, and writes "buf" in each file 
       for 90 times */ 
    struct block * ptr=mountdisk("disk");

    for(int i=0; i<NUMFILES; i++) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      openfile(ptr,filename);
    }    

    for(int j=0; j<90; j++) {
      for(int i=0; i<NUMFILES; i++) {
	char *buf="01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123";
	writefile(ptr,i,buf,122);
      }
    }

    for(int i=0; i<NUMFILES; i++) {
      closefile(ptr,i);
    }

    printdirectory(ptr);
    printinodeblock(ptr);

    unmountdisk(ptr);
    break;
  }


  case '2': {
    struct block * ptr=mountdisk("disk");
    initializeanalysis();
    Hashtable *env=exportmodel->gethashtable();
    alloc(ptr,LENGTH);
    addmapping(dstring,ptr,"Disk");
    //    env->put(dstring,new Element(ptr,exportmodel->getstructure("Disk")));//should be of badstruct

    printdirectory(ptr);
    printinodeblock(ptr);

    printf(".");
    // check the DSs
    doanalysis();
    printf(".");

    dealloc(ptr);
    unmountdisk(ptr);
    break;
  }


  // insert errors that break the specs
  case '3': {
    struct block * ptr=mountdisk("disk");
    initializeanalysis();
    Hashtable *env=exportmodel->gethashtable();
    alloc(ptr,LENGTH);
    addmapping(dstring,ptr,"Disk");

    // insert errors that break the specs
    doanalysis2();
    dealloc(ptr);
    unmountdisk(ptr);
    break;
  }


  // insert errors that do not break the specs
  case '4': {
    struct block * ptr=mountdisk("disk");
    initializeanalysis();
    Hashtable *env=exportmodel->gethashtable();
    alloc(ptr,LENGTH);
    addmapping(dstring,ptr,"Disk");

    // insert errors that do not break the specs
    doanalysis3();
    dealloc(ptr);
    unmountdisk(ptr);
    break;
  }

  
  case '5': {
  // prints the directory structure, and prints the contents of each file
    struct block * ptr=mountdisk("disk");
    printdirectory(ptr);
    for(int i=1; i<NUMFILES; i++) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      printfile(filename,ptr);
    }
    unmountdisk(ptr);
    break;
  }
 
  case '6': {
  // the same as "case '1'" only that the files are accessed in reversed order
    struct block * ptr=mountdisk("disk");
    for(int i=NUMFILES; i>1; i--) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      openfile(ptr,filename);
    }
    for(int j=0; j<90; j++) {
      for(int i=NUMFILES; i>1; i--) {
	char *buf="01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123";
	writefile(ptr,i,buf,122);
      }
    }
    for(int i=NUMFILES; i>1; i--) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
    break;
  }

  case '7': {
    struct block * ptr=mountdisk("disk");
    for(int i=NUMFILES; i>=0; i--) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      openfile(ptr,filename);
    }

    for(int j=0;j<6000;j++) {
      for(int i=NUMFILES; i>=0; i--) {
	char name[10];
	int len=sprintf(name, "%d ",i);
	writefile(ptr,i,name,len);
      }
    }
    for(int i=NUMFILES; i>=0; i--) {
      closefile(ptr,i);
    }
    for(int i=NUMFILES; i>=0; i--) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      openfile(ptr,filename);
    }

    for(int j=0;j<400;j++) {
      for(int i=NUMFILES; i>=0; i--) {
	int l=0;
	char name[10];
	int len=sprintf(name, "%d ",i);
	readfile(ptr,i,name,len);
	sscanf(name, "%d ", &l);
	if (l!=i) {
	  printf("ERROR in benchmark\n");
	}
      }
    }
    for(int i=NUMFILES; i>=0; i--) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
  }
  break;


  case '8': {
    {
      struct block * ptr=chmountdisk("disk");
      initializeanalysis();
      Hashtable *env=exportmodel->gethashtable();
      alloc(ptr,LENGTH);
      addmapping(dstring,ptr,"Disk");
      doanalysis();
      dealloc(ptr);
      chunmountdisk(ptr);
    }
    struct block * ptr=mountdisk("disk");
    for(int i=NUMFILES; i>=0; i--) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      openfile(ptr,filename);
    }
    for(int j=0; j<6000; j++) {
      for(int i=NUMFILES; i>=0; i--) {
	char name[10];
	int len=sprintf(name, "%d ",i);
	writefile(ptr,i,name,len);
      }
    }
    for(int i=NUMFILES; i>=0; i--) {
      closefile(ptr,i);
    }
    for(int i=NUMFILES; i>=0; i--) {
      char filename[10];
      sprintf(filename,"file_%d",i);
      openfile(ptr,filename);
    }
    for(int j=0;j<400;j++) {
      for(int i=NUMFILES; i>=0; i--) {
	int l=0;
	char name[10];
	int len=sprintf(name, "%d ",i);
	readfile(ptr,i,name,len);
	sscanf(name, "%d ", &l);
	if (l!=i) {
	  printf("ERROR in benchmark\n");
	}
      }
    }
    for(int i=NUMFILES; i>=0; i--) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
  }

  case '9': {
    for(int i=0;i<MAXFILES;i++)
      files[i].used=false;
    
    
    struct block * ptr=mountdisk("disk");
    
    for(int i=0; i<NUMFILES; i++) 
      {
	char filename[10];
	sprintf(filename,"file_%d", i);
	openfile(ptr,filename);
      }
    
    for(int i=0; i<NUMFILES; i++) 
      {	    
	char buf[100];
	sprintf(buf,"This is file_%d.", i);
	writefile(ptr,i,buf,strlen(buf));
      }    
    
    
    createlink(ptr, "file_1", "link_1");
    createlink(ptr, "file_1", "link_2");

    removefile("file_1", ptr);

    int fd = openfile(ptr, "new");
    writefile(ptr, fd, "new", 3);
    
    printfile("file_1", ptr);
    printfile("link_1", ptr);
    printfile("link_2", ptr);
  
    for(int i=0; i<NUMFILES; i++)
      closefile(ptr,i);
    
    
    unmountdisk(ptr);    
  }
  
  }
}



struct block * chmountdisk(char *filename) {
  int fd=open(filename,O_CREAT|O_RDWR);
  struct block *ptr=(struct block *) mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  return ptr;
}



void chunmountdisk(struct block *vptr) {
  msync(vptr,LENGTH,MS_SYNC);
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");
}



// mounts the disk from the file "filename"
struct block * mountdisk(char *filename) {
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



void unmountdisk(struct block *vptr) {
  struct InodeBitmap *ibb=(struct InodeBitmap *) &vptr[ibbptr];
  for(int i=0;i<(NUMINODES/8+1);i++)
    ibb->inode[i]=ib.inode[i];

  struct BlockBitmap *bbb=(struct BlockBitmap *) &vptr[bbbptr];
  for(int i=0;i<(NUMBLOCK/8+1);i++)
    bbb->blocks[i]=bb.blocks[i];
  msync(vptr,LENGTH,MS_SYNC);
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");
}


void removefile(char *filename, struct block *ptr) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    for(int j=0;j<BLOCKSIZE/128;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  /* Found file */
	  db->entries[j].name[0]=0; //Delete entry
	  int inode=db->entries[j].inodenumber;
	  db->entries[j].inodenumber=0;
	  
	  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
	  itb->entries[inode].referencecount--;

	  if (itb->entries[inode].referencecount==0) {
	    for(int i=0;i<((itb->entries[inode].filesize+BLOCKSIZE-1)/BLOCKSIZE);i++) {
	      int blocknum=itb->entries[inode].Blockptr[i];
	      bb.blocks[blocknum/8]^=(1<<(blocknum%8));
	    }
	    ib.inode[inode/8]^=(1<<(inode%8));
	  }
	}
      }
    }
  }
}


void createlink(struct block *ptr,char *filename, char *linkname) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  /* Found file */
	  int inode=db->entries[j].inodenumber;
  	  struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
	  itb->entries[inode].referencecount++;
	  addtode(ptr, inode, linkname);
	}
      }
    }
  }
}


void closefile(struct block *ptr, int fd) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
  

  msync(&itb->entries[fd],sizeof(DirectoryEntry),MS_SYNC);
  files[fd].used=false;
}


bool writefile(struct block *ptr, int fd, char *s) {
  return (writefile(ptr,fd,s,1)==1);
}


int writefile(struct block *ptr, int fd, char *s, int len) {
  struct filedesc *tfd=&files[fd];
  if (tfd->used==false)
    return -1;
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
  int filelen=itb->entries[tfd->inode].filesize;
  if ((12*BLOCKSIZE-tfd->offset)<len)
    len=12*BLOCKSIZE-tfd->offset;
  for(int i=0;i<len;i++) {
    int nbuffer=tfd->offset/BLOCKSIZE;
    int noffset=tfd->offset%BLOCKSIZE;
    if (tfd->offset>=filelen) {
      if (noffset==0) {
	int bptr=getblock(ptr);
	if (bptr==-1) {
	  if (itb->entries[files[fd].inode].filesize<files[fd].offset)
	    itb->entries[files[fd].inode].filesize=files[fd].offset; 
	  return i;
	}
	itb->entries[tfd->inode].Blockptr[nbuffer]=bptr;
      }
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


// reads one char from the file fd and returns it
char readfile(struct block *ptr, int fd) {
  char array[1];
  if (readfile(ptr,fd,array,1)==1)
    return array[0];
  else
    return EOF;
}

// reads len chars from file fd (file system *ptr) and returns them in buf
int readfile(struct block *ptr, int fd, char *buf, int len) {
  struct filedesc *tfd=&files[fd];
  if (tfd->used==false)
    return -1;

  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int filelen=itb->entries[tfd->inode].filesize;

  // if there are fewer than len chars left, read until the end
  if ((filelen-tfd->offset)<len)
    len=filelen-tfd->offset;

  for(int i=0;i<len;) {
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



int openfile(struct block *ptr, char *filename) {
  /* Locate fd */
  int fd=-1;
  for(int k=0;k<MAXFILES;k++) {
    if(!files[k].used) {
      /* Found file */
      fd=k;
      files[fd].used=true;
      break;
    }
  }
  if (fd==-1) return fd;

  /* Check to see if file exists*/
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) 
	{
	  if (db->entries[j].name[0]!=0) {
	    if(strcmp(filename, db->entries[j].name)==0) 
	      {
		files[fd].inode=db->entries[j].inodenumber;
		files[fd].offset=0;
		return fd;
	      }
	  }
	}
    }
  
  /* If file doesn't exist, create it */
  int inode=getinode(ptr);
  if (inode==-1) {
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


void createfile(struct block *ptr,char *filename, char *buf,int buflen) {
  int fd=openfile(ptr,filename);
  writefile(ptr,fd,buf,buflen);
  closefile(ptr,fd);
}



// adds a file to the directory entry
void addtode(struct block *ptr, int inode, char *filename) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) {
      if (db->entries[j].name[0]==0) {
	/* lets finish */
	strncpy(db->entries[j].name,filename,124);
	db->entries[j].inodenumber=inode;
	msync(&db->entries[j],sizeof(DirectoryEntry),MS_SYNC);
	return;
      }
    }
  }
}


// return the first free node in the InodeTable.  Marks that inode as used.
int getinode(struct block *ptr) {
  for(int i=0;i<NUMINODES;i++) {
    if (!(ib.inode[i/8]&(1<<(i%8)))) {
      ib.inode[i/8]=ib.inode[i/8]|(1<<(i%8));
      return i;
    }
  }
  return -1;
}


int getblock(struct block * ptr) {
  for(int i=0;i<NUMBLOCK;i++) {
    if (!(bb.blocks[i/8]&(1<<(i%8)))) {
      bb.blocks[i/8]=bb.blocks[i/8]|(1<<(i%8));
      return i;
    }
  }
  return -1;
}



void createdisk() 
{
  int blocksize=BLOCKSIZE;
  int numblocks=NUMBLOCK;

  int fd=open("disk",O_CREAT|O_RDWR|O_TRUNC, S_IREAD|S_IWRITE);

  // creates numblocks and initializes them with 0
  char *buf=(char *)calloc(1,blocksize);
  for(int i=0;i<numblocks;i++) {
    write(fd,buf,blocksize);
  }
  free(buf);

  // maps the file 'disk' into memory
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
      itb->entries[0].Blockptr[i]=i+5;  // blocks 5 to 16 are RootDirectory entries
    itb->entries[0].referencecount=0;
  }
  msync(vptr,LENGTH,MS_SYNC);
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");

  printf("Disk created successfully!\n");
}


void printdirectory(struct block *ptr) 
{
  struct InodeBlock *itb=(struct InodeBlock *) &ptr[itbptr];
  
  for(int i=0;i<12;i++) 
    {
      struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];

      for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) {
	if (db->entries[j].name[0]!=0) 
	  {
	    /* lets finish */
	    //printf("%s %d\n",db->entries[j].name, db->entries[j].inodenumber);
	    printf("%s (inode %d) (%d bytes)\n",db->entries[j].name, db->entries[j].inodenumber, itb->entries[db->entries[j].inodenumber].filesize);
	  }
      }
    }

  //printf("end of printdirectory\n");
}

// prints the contents of the file with filename "filename"
void printfile(char *filename, struct block *ptr) 
{
  printf("=== BEGIN of %s ===\n", filename);
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  for(int i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    for(int j=0;j<BLOCKSIZE/DIRECTORYENTRYSIZE;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  /* Found file */
	  int inode=db->entries[j].inodenumber;

	  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
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


void printinodeblock(struct block *ptr)
{
  struct InodeBlock *itb=(struct InodeBlock *) &ptr[itbptr];  

  for (int i=0; i<NUMINODES; i++)
    {
      Inode inode = itb->entries[i];
      printf("inode %d: (filesize %d), (referencecount %d)\n", i, inode.filesize, inode.referencecount);
    }
}


