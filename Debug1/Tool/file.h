#ifndef FILE_H
#define FILE_H

#define BLOCKSIZE 8192
#define NUMBLOCK 1024
#define LENGTH BLOCKSIZE*NUMBLOCK
#define NUMINODES BLOCKSIZE/56

struct block {
  char array[BLOCKSIZE];
};


struct SuperBlock {
  int FreeBlockCount;
  int FreeInodeCount;
  int NumberofBlocks;
  int NumberofInodes;
  int RootDirectoryInode;
  int blocksize;
};


struct GroupBlock {
  int BlockBitmapBlock;
  int InodeBitmapBlock;
  int InodeTableBlock;
  int GroupFreeBlockCount;
  int GroupFreeInodeCount;
};


struct BlockBitmap {
  char blocks[NUMBLOCK/8+1];
};


struct InodeBitmap {
  char inode[NUMINODES/8+1];
};


struct Inode {
  int filesize;
  int Blockptr[12];
  int referencecount;
};


struct InodeTable {
  struct Inode entries[NUMINODES];
};


#define DIRECTORYENTRYSIZE 128
struct DirectoryEntry {
  char name[124];
  int inodenumber;
};



struct DirectoryBlock {
  struct DirectoryEntry entries[BLOCKSIZE/128];
};





struct block* createdisk();
void createfile(struct block *ptr,char *filename, char *buf,int buflen);
void addtode(struct block *ptr, int inode, char * filename);
int getinode(struct block *ptr);
int getblock(struct block * ptr);

void removefile(char *filename, struct block *ptr);
void createlink(struct block *ptr,char *filename, char *linkname);

void closefile(struct block *ptr, int fd);
bool writefile(struct block *ptr, int fd, char *s);
int writefile(struct block *ptr, int fd, char *s, int len);
char readfile(struct block *ptr, int fd);
int readfile(struct block *ptr, int fd, char *buf, int len);
int openfile(struct block *ptr, char *filename);
void printfile(char *filename, struct block *ptr);

void calltool(char *text);


#define MAXFILES 300
struct filedesc {
  int inode;
  int offset;
  bool used;
};
#endif

