#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/mman.h>
#include "file.h"
#include "test3_aux.h"
#include "memory.h"


#define false 0
#define true 1
struct filedesc files[MAXFILES];
struct InodeBitmap ib;
struct BlockBitmap bb;
int bbbptr,ibbptr,itbptr,rdiptr;
int main(int argc, char **argv) {
  int i;
  initializemmap();

  for(i=0;i<MAXFILES;i++)
    files[i].used=false;
  switch(argv[1][0]) {
  case '0': createdisk();
    return 1;
  case '1': {
    int i,j;
    struct block * ptr=mountdisk("disk");
    for(i=0;i<145;i++) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      openfile(ptr,filename);
    }
    for(j=0;j<90;j++) {
      int i;
      for(i=0;i<145;i++) {
	char *buf="01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123";
	writefile2(ptr,i,buf,122);
      }
    }
    for(i=0;i<145;i++) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
    break;
  }
  case '2': {
    struct block * ptr=chmountdisk("disk");
    chunmountdisk(ptr);
    break;
  }
  case '3': {
    int i;
    struct block * ptr=mountdisk("disk");
    printdirectory(ptr);
    for(i=1;i<145;i++) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      printfile(filename,ptr);
    }
    unmountdisk(ptr);
    break;
  }
  case '4': {
    int i,j;
    struct block * ptr=mountdisk("disk");
    for(i=145;i>1;i--) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      openfile(ptr,filename);
    }
    for(j=0;j<90;j++) {
      int i;
      for(i=145;i>1;i--) {
	char *buf="01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123";
	writefile2(ptr,i,buf,122);
      }
    }
    for(i=145;i>1;i--) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
    break;
  }
  case '5': {
    int i,j;
    struct block * ptr=mountdisk("disk");
    for(i=15;i>=0;i--) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      openfile(ptr,filename);
    }
    for(j=0;j<60;j++) {
      int i;
      for(i=15;i>=0;i--) {
	char name[10];
	int len=sprintf(name, "%d ",i);
	writefile2(ptr,i,name,len);
      }
    }
    for(i=15;i>=0;i--) {
      closefile(ptr,i);
    }
    for(i=15;i>=0;i--) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      openfile(ptr,filename);
    }
    for(j=0;j<60;j++) {
      int i;
      for(i=15;i>=0;i--) {
	int l=0;
	char name[10];
	int len=sprintf(name, "%d ",i);
	readfile2(ptr,i,name,len);
	sscanf(name, "%d ", &l);
	if (l!=i) {
	  printf("ERROR in benchmark\n");
	}
      }
    }
    for(i=15;i>=0;i--) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
  }
  break;
  case '6': {
    int i,j;
    /*    {
      struct block * ptr=chmountdisk("disk");
      chunmountdisk(ptr);
      }*/
    struct block * ptr=mountdisk("disk");
    for(i=145;i>=0;i--) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      openfile(ptr,filename);
    }
    for(j=0;j<6000;j++) {
      int i;
      for(i=145;i>=0;i--) {
	char name[10];
	int len=sprintf(name, "%d ",i);
	writefile2(ptr,i,name,len);
      }
    }
    for(i=145;i>=0;i--) {
      closefile(ptr,i);
    }
    for(i=145;i>=0;i--) {
      char filename[10];
      sprintf(filename,"fil%d",i);
      openfile(ptr,filename);
    }
    for(j=0;j<400;j++) {
      int i;
      for(i=145;i>=0;i--) {
	int l=0;
	char name[10];
	int len=sprintf(name, "%d ",i);
	readfile2(ptr,i,name,len);
	sscanf(name, "%d ", &l);
	if (l!=i) {
	  printf("ERROR in benchmark\n");
	}
      }
    }
    for(i=145;i>=0;i--) {
      closefile(ptr,i);
    }
    unmountdisk(ptr);
  }
  break;
  case 'x': {
    struct block * ptr=mountdisk("disk");

    printdirectory(ptr);

    // check the DSs
    // check the DSs
    unsigned long time = 0;

    time += selfcheck2(ptr);

    printf("\ncompiled: %u us\n", (time));

    break;
  }


  }


}


unsigned long selfcheck2(struct block* d) {

    struct timeval begin,end;
    unsigned long t;
    gettimeofday(&begin,NULL);

#include "test3.c"

    gettimeofday(&end,NULL);
    t=(end.tv_sec-begin.tv_sec)*1000000+end.tv_usec-begin.tv_usec;
    return t;
}


struct block * chmountdisk(char *filename) {
  int fd=open(filename,O_CREAT|O_RDWR);
  struct block *ptr=(struct block *) mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  alloc(ptr,LENGTH);
  return ptr;
}

void chunmountdisk(struct block *vptr) {
  int val=munmap(vptr,LENGTH);
  dealloc(vptr);
}

struct block * mountdisk(char *filename) {
  int fd=open(filename,O_CREAT|O_RDWR);
  int i;
  struct block *ptr=(struct block *) mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  alloc(ptr,LENGTH);
  struct SuperBlock *sb=(struct SuperBlock *) &ptr[0];
  struct GroupBlock *gb=(struct GroupBlock *) &ptr[1];
  bbbptr=gb->BlockBitmapBlock;
  ibbptr=gb->InodeBitmapBlock;
  itbptr=gb->InodeTableBlock;
  rdiptr=sb->RootDirectoryInode;

  {
    struct InodeBitmap *ibb=(struct InodeBitmap *) &ptr[ibbptr];
    struct BlockBitmap *bbb=(struct BlockBitmap *) &ptr[bbbptr];

    for(i=0;i<(NUMINODES/8+1);i++)
      ib.inode[i]=ibb->inode[i];
    for(i=0;i<(NUMBLOCK/8+1);i++)
      bb.blocks[i]=bbb->blocks[i];
    return ptr;
  }
}

void unmountdisk(struct block *vptr) {
  struct InodeBitmap *ibb=(struct InodeBitmap *) &vptr[ibbptr];
  int i,val;
  for(i=0;i<(NUMINODES/8+1);i++)
    ibb->inode[i]=ib.inode[i];

  {
    struct BlockBitmap *bbb=(struct BlockBitmap *) &vptr[bbbptr];
    for(i=0;i<(NUMBLOCK/8+1);i++)
      bbb->blocks[i]=bb.blocks[i];
    val=munmap(vptr,LENGTH);
    dealloc(vptr);
  }
}

void printdirectory(struct block *ptr) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int i;
  for(i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    int j;
    for(j=0;j<BLOCKSIZE/128;j++) {
      if (db->entries[j].name[0]!=0) {
	/* lets finish */
	printf("%s %d inode %d bytes\n",db->entries[j].name, db->entries[j].inodenumber,itb->entries[db->entries[j].inodenumber].filesize);
      }
    }
  }
}

void printfile(char *filename, struct block *ptr) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int i;
  for(i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    int j;
    for(j=0;j<BLOCKSIZE/128;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  /* Found file */
	  int inode=db->entries[j].inodenumber;

	  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
	  int i;
	  for(i=0;i<((itb->entries[inode].filesize+BLOCKSIZE-1)/BLOCKSIZE);i++) {
	    struct block *b=&ptr[itb->entries[inode].Blockptr[i]];
	    write(0,b,BLOCKSIZE);
	  }
	}
      }
    }
  }
}

void removefile(char *filename, struct block *ptr) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int i;
  for(i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    int j;
    for(j=0;j<BLOCKSIZE/128;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  /* Found file */
	  db->entries[j].name[0]=0; //Delete entry
	  {
	    int inode=db->entries[j].inodenumber;

	    struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
	    itb->entries[inode].referencecount--;

	    if (itb->entries[inode].referencecount==0) {
	      int i;
	      for(i=0;i<((itb->entries[inode].filesize+BLOCKSIZE-1)/BLOCKSIZE);i++) {
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
}

void createlink(struct block *ptr,char *filename, char *linkname) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int i;
  for(i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    int j;
    for(j=0;j<BLOCKSIZE/128;j++) {
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
  msync(&itb->entries[fd],sizeof(struct DirectoryEntry),MS_SYNC);
  files[fd].used=false;
}

bool writefile(struct block *ptr, int fd, char *s) {
  return (writefile2(ptr,fd,s,1)==1);
}

int writefile2(struct block *ptr, int fd, char *s, int len) {
  struct filedesc *tfd=&files[fd];
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
  int filelen=itb->entries[tfd->inode].filesize;
  int i;

  if (tfd->used==false)
    return -1;

  if ((12*BLOCKSIZE-tfd->offset)<len)
    len=12*BLOCKSIZE-tfd->offset;
  for(i=0;i<len;i++) {
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
    {
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
  }
  if (itb->entries[files[fd].inode].filesize<files[fd].offset)
    itb->entries[files[fd].inode].filesize=files[fd].offset;
  return len;
}


char readfile(struct block *ptr, int fd) {
  char array[1];
  if (readfile2(ptr,fd,array,1)==1)
    return array[0];
  else
    return EOF;
}

int readfile2(struct block *ptr, int fd, char *buf, int len) {
  struct filedesc *tfd=&files[fd];
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int filelen=itb->entries[tfd->inode].filesize;
  int i;

  if (tfd->used==false)
    return -1;
  if ((filelen-tfd->offset)<len)
    len=filelen-tfd->offset;
  for(i=0;i<len;) {
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
  int k;
  struct InodeBlock * itb;
  int inode;
  int i;

  for(k=0;k<MAXFILES;k++) {
    /* Found file */
    if(!files[k].used) {
      fd=k;
      files[fd].used=true;
      break;
    }
  }
  if (fd==-1) return fd;

  /* Check to see if file exists*/
  itb=(struct InodeBlock *) &ptr[itbptr];
  for(i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    int j;
    for(j=0;j<BLOCKSIZE/128;j++) {
      if (db->entries[j].name[0]!=0) {
	if(strcmp(filename,db->entries[j].name)==0) {
	  files[fd].inode=db->entries[j].inodenumber;
	  files[fd].offset=0;
	  return fd;
	}
      }
    }
  }
  /* Create file */

  inode=getinode(ptr);
  if (inode==-1) {
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

void createfile(struct block *ptr,char *filename, char *buf,int buflen) {
  int fd=openfile(ptr,filename);
  writefile2(ptr,fd,buf,buflen);
  closefile(ptr,fd);
}

void addtode(struct block *ptr, int inode, char * filename) {
  struct InodeBlock * itb=(struct InodeBlock *) &ptr[itbptr];
  int i;
  for(i=0;i<12;i++) {
    struct DirectoryBlock *db=(struct DirectoryBlock *) &ptr[itb->entries[rdiptr].Blockptr[i]];
    int j;
    for(j=0;j<BLOCKSIZE/128;j++) {
      if (db->entries[j].name[0]==0) {
	/* lets finish */
	strncpy(db->entries[j].name,filename,124);
	db->entries[j].inodenumber=inode;
	msync(&db->entries[j],sizeof(struct DirectoryEntry),MS_SYNC);
	return;
      }
    }
  }
}

int getinode(struct block *ptr) {
  int i;
  for(i=0;i<NUMINODES;i++) {
    if (!(ib.inode[i/8]&(1<<(i%8)))) {
      ib.inode[i/8]=ib.inode[i/8]|(1<<(i%8));
      return i;
    }
  }
  return -1;
}

int getblock(struct block * ptr) {
  int i;
  for(i=0;i<NUMBLOCK;i++) {
    if (!(bb.blocks[i/8]&(1<<(i%8)))) {
      bb.blocks[i/8]=bb.blocks[i/8]|(1<<(i%8));
      return i;
    }
  }
  return -1;
}



void createdisk() {
  int blocksize=BLOCKSIZE;
  int numblocks=NUMBLOCK;
  int fd=open("disk",O_CREAT|O_RDWR|O_TRUNC);
  char *buf=(char *)calloc(1,blocksize);
  int i;
  void *vptr;
  struct block * ptr;
  for(i=0;i<numblocks;i++) {
    write(fd,buf,blocksize);
  }
  free(buf);
  vptr=mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
  alloc(vptr,LENGTH);
  ptr=(struct block *)vptr;
  {
    struct SuperBlock * sb=(struct SuperBlock*) &ptr[0];
    sb->FreeBlockCount=BLOCKSIZE-5;
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
    gb->GroupFreeBlockCount=BLOCKSIZE-5;
    gb->GroupFreeInodeCount=NUMINODES-1;
  }
  {
    struct BlockBitmap * bb=(struct BlockBitmap *) &ptr[2];
    int i;
    for(i=0;i<(5+12);i++)
      bb->blocks[i/8]=bb->blocks[i/8]|(1<<(i%8));
  }
  {
    struct InodeBitmap * ib=(struct InodeBitmap *) &ptr[3];
    ib->inode[0]=1;
  }
  {
    int i;
    struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];
    itb->entries[0].filesize=12*BLOCKSIZE;
    for(i=0;i<12;i++)
      itb->entries[0].Blockptr[i]=i+5;
    itb->entries[0].referencecount=1;
  }
  {
    int val=munmap(vptr,LENGTH);
    dealloc(vptr);
    if (val!=0)
      printf("Error!\n");
  }
}
