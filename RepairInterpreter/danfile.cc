#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <assert.h>

#include <errno.h>
extern int errno;

#include "file.h"
extern "C" {
#include "test.h"
}
#include "Hashtable.h"
#include "model.h"
#include "element.h"
#include "tmap.h"

char *dstring="d\0";
struct filedesc files[MAXFILES];
struct InodeBitmap ib;
struct BlockBitmap bb;

int bbbptr;  // pointer to the BlockBitmap block
int ibbptr;  // pointer to the InodeBlock block
int itbptr;  // pointer to the InodeTable block
int rdiptr;  // pointer to the RootDirectoryInode block

struct InodeBitmap* sc_ib;
struct BlockBitmap* sc_bb;
struct InodeBlock* sc_it;
int sc_bbbptr;
int sc_ibbptr;
int sc_itbptr;
int sc_rdiptr;

#include "SimpleHash.h"

int testinode(int i) {
    char temp;
    assert(sc_ib);
    temp = sc_ib->inode[i/8]&(1<<(i%8));
    return temp == 0 ? 0 : 1;
}

int testblock(int i) {
    char temp;
    assert(sc_bb);
    temp = sc_bb->blocks[i/8]&(1<<(i%8));
    return temp == 0 ? 0 : 1;
}

void assertvalidmemory(int low, int high) {
  typemap *tm=exportmodel->gettypemap();
  assert(tm->assertvalidmemory((void*) low, (void*) high));
}

unsigned long selfcheck2(struct block* d) {

    struct timeval begin,end;
    unsigned long t;
    gettimeofday(&begin,NULL);

#include "RepairCompiler/MCC/test2.cc"

    gettimeofday(&end,NULL);
    t=(end.tv_sec-begin.tv_sec)*1000000+end.tv_usec-begin.tv_usec;
    return t;
}

void selfcheck(struct block* diskptr) {

    /* get time information for statistics */
    struct timeval begin,end;
    unsigned long t;
    gettimeofday(&begin,NULL);

    
    /* hand written data structure consistency */

    struct SuperBlock* sb = (struct SuperBlock*)&diskptr[0];
    struct GroupBlock* gb = (struct GroupBlock*)&diskptr[1];
    
    int numblocks = sb->NumberofBlocks;
    int numinodes = sb->NumberofInodes;

    SimpleHash* hash_inodeof = new SimpleHash(1000); // estimation of the number of files!
    SimpleHash* hash_contents = new SimpleHash(1000); // contents
    SimpleList* list_inodes = new SimpleList();
    SimpleList* list_blocks = new SimpleList();

    // simple test

    // check bitmap consistency with superblock, groupblock, inotetableblock
    // inodebitmapblock, blockbitmapblock, rootidrectoryinode

    sc_bbbptr = gb->BlockBitmapBlock;
    sc_ibbptr = gb->InodeBitmapBlock;
    sc_itbptr = gb->InodeTableBlock;
    sc_rdiptr = sb->RootDirectoryInode;

    // constraint 8: automatic...
    // constraint 9: automatic...

    // constraint 10:
    if (sc_itbptr < numblocks) {
        sc_it = (InodeBlock*)&diskptr[sc_itbptr];        
    } else {
        sc_it = NULL;
    }

    // constraint 11:
    if (sc_ibbptr < numblocks) {
        sc_ib = (InodeBitmap*)&diskptr[sc_ibbptr];
    } else {
        sc_ib = NULL;
    }

    // constraint 12:
    if (sc_bbbptr < numblocks) {
        sc_bb = (BlockBitmap*)&diskptr[sc_bbbptr];
    } else {
        sc_bb = NULL;
    }

    // rule 1
    if (sc_bb) {
        // constraint 3
        assert(testblock(0)); // superblock
        
        // building blocks
        list_blocks->add(0);
    }

    // rule 2
    if (sc_bb) {
        // constraint 3
        assert(testblock(1)); // groupblock

        // building list_blocks
        list_blocks->add(1);
    }

    // rule 3
    if (sc_bb) {
        // constraint 3
        assert(testblock(sc_itbptr));

        // building list_blocks
        list_blocks->add(sc_itbptr);
    }

    // rule 4
    if (sc_bb) {
        // constraint 3
        assert(testblock(sc_ibbptr));

        // building list_blocks
        list_blocks->add(sc_ibbptr);
    }

    // rule 5
    if (sc_bb) {
        // constraint 3
        assert(testblock(sc_bbbptr));

        // building list_blocks
        list_blocks->add(sc_bbbptr);
    }

    // build inodeof and contents
    if (sb->RootDirectoryInode < numinodes) {
        int dinode = sb->RootDirectoryInode;

        // building list_inodes
        list_inodes->add(dinode);

        for (int k = 0 ; k <= 11 ; k++) {

            int block = sc_it->entries[dinode].Blockptr[k];

            if (block != 0) {
                hash_contents->add(dinode, block);
                list_blocks->add(block);
            }
            
            if (block < numblocks) {

                DirectoryBlock* db = (DirectoryBlock*)&diskptr[block];

                for (int j = 0; j < sb->blocksize/128 ; j++) {

                    DirectoryEntry* de = (DirectoryEntry*)&db->entries[j];

                    if (de->inodenumber < numinodes) {
                        // add <de, de.inodenumber> to inodeof                    
                        hash_inodeof->add((int)de, de->inodenumber);
                    }
                    
                    if (de->inodenumber < numinodes && de->inodenumber != 0) {
                        
                        // build list_inodes
                        list_inodes->add(de->inodenumber);                        
                        
                        for (int j2 = 0 ; j2 <= 11 ; j2++) {
                            int block2 = sc_it->entries[de->inodenumber].Blockptr[j2];
                            if (block2 != 0) {
                                hash_contents->add(de->inodenumber, block2);
                                if (block2 < numblocks) {
                                    list_blocks->add(block2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //printf("\n");

    // rule 6 and rule 11: rootdirectoryinode
    if (sb->RootDirectoryInode < numinodes) {
        int inode = sb->RootDirectoryInode;

        // constraint 1
        assert(testinode(inode));

        int filesize = sc_it->entries[inode].filesize;
        int contents = 0;
        for (int j = 0; j <= 11; j++) {
            int block2 = sc_it->entries[inode].Blockptr[j];
            if (block2 != 0) {
                // TBD: needs to actual store state because
                // there could be duplicate numbers and they
                // shouldn't be double counted
                contents++;

                // rule 11
                if (block2 < numblocks) {
                    // constraint 3
                    assert(testblock(block2));
                    
                    // constraint 7
                    //printf("%d - %d %d %d\n", inode, j, block2, hash_contents->countdata(block2));
                    assert(hash_contents->countdata(block2)==1);
                }
            }
        }

        // constraint 6
        assert(filesize <= (contents*8192));

        // constraint 5:
        assert(sc_it->entries[inode].referencecount == hash_inodeof->countdata(inode));
    }

    // rule 14
    if (sb->RootDirectoryInode < numinodes) {
        int dinode = sb->RootDirectoryInode;

        for (int j = 0; j < sb->blocksize/128 ; j++) {
            for (int k = 0 ; k <= 11 ; k++) {
                int block = sc_it->entries[dinode].Blockptr[k];
                if (block < numblocks) {
                    DirectoryBlock* db = (DirectoryBlock*)&diskptr[block];
                    DirectoryEntry* de = (DirectoryEntry*)&db->entries[j];
                    
                    int inode = de->inodenumber;
                    if (inode < numinodes && inode != 0) {

                        // constraint 1
                        assert(testinode(inode));
                        
                        // constraint 6
                        int filesize = sc_it->entries[inode].filesize;
                        int contents = 0;
                        for (int j2 = 0; j2 <= 11; j2++) {
                            int block2 = sc_it->entries[inode].Blockptr[j2];
                            if (block2 != 0) {
                                // TBD 
                                contents++;

                                // rule 11
                                if (block2 < numblocks) {
                                    // constraint 3
                                    assert(testblock(block2));
                                    
                                    // constraint 7
                                    assert(hash_contents->countdata(block2)==1);
                                }
                            }
                        }
                        assert(filesize <= (contents*8192));

                        // constraint 5:
                        assert(sc_it->entries[inode].referencecount == hash_inodeof->countdata(inode));                                                
                    }
                }
            }
        }
    }

    // to go, [7, 8 ]
    // interesting question is going to be how to deal with 7 and 8
    // actually it turns out that the constraints bound to rules 7 and 8 are
    // easy... its just that creating the lists for 7 and 8 is a little tricky...
    // 7 can easily piggyback on the creation of inodeof/contents... it fits quite 
    // nicely into that traversal... same goes for 8

    // rule 7
    for (int i = 0 ; i < numinodes ; i++) {    
        if (!list_inodes->contains(i)) {
            // constraint 2
            if (testinode(i)) {
                printf("<bad inode,%d>", i);
                assert(testinode(i)==0);
            } 
        } 
    } 

    // rule 8
    for (int i = 0 ; i < numblocks ; i++) {    
        if (!list_blocks->contains(i)) {
            // constraint 4
            if (testblock(i)) {
                printf("<bad block,%d>", i);
                assert(testblock(i)==0);
            }

        }
    } 


    gettimeofday(&end,NULL);
    t=(end.tv_sec-begin.tv_sec)*1000000+end.tv_usec-begin.tv_usec;

    printf("\npassed tests in %ld u-seconds!\n", t);


}


int main(int argc, char **argv) 
{

  for(int i=0;i<MAXFILES;i++)
    files[i].used=false;


  if (argc <= 1) {
      printf("Filesystem Repair:\n\tusage: main [0..9]\n\n");
      printf("\t 0 : creates disk\n");
      printf("\t 1 : mount disk, creates files and writes test data\n");
      printf("\t 2 : \n");
      printf("\t 3 : inserts errors to break specs\n");
      printf("\t 4 : \n");
      printf("\t 5 : \n");
      printf("\t 6 : \n");
      printf("\t 7 : \n");
      printf("\t 8 : \n");
      printf("\t 9 : \n");
      exit(-1);
  }

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


  case 'r': {
    struct block * ptr=mountdisk("disk");

    initializeanalysis();

    alloc(ptr,LENGTH);
    addmapping(dstring,ptr,"Disk");

    printdirectory(ptr);
    printinodeblock(ptr);

    // check the DSs
    unsigned long time = 0;
    for (int i = 0; i < 50; i++) {
        time += benchmark();
    }

    printf("\ninterpreted: %u us\n", (time/50)); 
    
    dealloc(ptr);
    unmountdisk(ptr);
    break;
  }      

  case 's': {
    struct block * ptr=mountdisk("disk");

    initializeanalysis();

    alloc(ptr,LENGTH);
    addmapping(dstring,ptr,"Disk");

    printdirectory(ptr);
    printinodeblock(ptr);

    // check the DSs
    selfcheck(ptr);
    

    dealloc(ptr);
    unmountdisk(ptr);
    break;
  }

  case 'x': {
    struct block * ptr=mountdisk("disk");

    initializeanalysis();

    alloc(ptr,LENGTH);
    addmapping(dstring,ptr,"Disk");

    printdirectory(ptr);
    printinodeblock(ptr);

    // check the DSs
    // check the DSs
    unsigned long time = 0;
    for (int i = 0; i < 50; i++) {
        time += selfcheck2(ptr);
    }

    printf("\ncompiled: %u us\n", (time/50));    

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
  int val=munmap(vptr,LENGTH);
  if (val!=0)
    printf("Error!\n");
}



// mounts the disk from the file "filename"
struct block * mountdisk(char *filename) {
  int fd=open(filename,O_CREAT|O_RDWR);
  struct block *ptr=(struct block *) mmap(NULL,LENGTH,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);

  // droy: debugging
  if ((int)ptr == -1) {
      perror("mountdisk\0");
      exit(-1);
  }


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

  // added by dan roy for debugging
  if ((int)vptr == -1) {
      perror("createdisk()\0");
      exit(-1);
  }
  // end dan roy

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
    //memset(bb, 0, sizeof(BlockBitmap));
    for(int i=0;i<(5+12);i++) {
        bb->blocks[i/8]=bb->blocks[i/8]|(1<<(i%8));
    }
  }
  {
    struct InodeBitmap * ib=(struct InodeBitmap *) &ptr[3];
    //memset(ib, 0, sizeof(InodeBitmap));
    ib->inode[0]=1;
  }
  {
    struct InodeBlock * itb=(struct InodeBlock *) &ptr[4];

    itb->entries[0].filesize=12*BLOCKSIZE;
    for(int i=0;i<12;i++)
      itb->entries[0].Blockptr[i]=i+5;  // blocks 5 to 16 are RootDirectory entries
    itb->entries[0].referencecount=0;
  }

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


