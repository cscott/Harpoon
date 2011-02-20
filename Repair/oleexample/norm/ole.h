
#define SPECIAL_BLOCK           -3
#define END_OF_CHAIN            -2
#define UNUSED                  -1

#define NO_ENTRY                0
#define STORAGE                 1
#define STREAM                  2
#define ROOT                    5

#define FAT_START               0x4c
#define OUR_BLK_SIZE            512
#define DIRS_PER_BLK            4
#define MIN(x,y)                ((x) < (y) ? (x) : (y))

struct OLE_HDR
{
  char magic[8];	/*0*/         
  int unk1;			/*8*/                                               
  int unk2;			/*c*/                                               
  int unk3;			/*10*/                                              
  int unk4;			/*14*/                                                  
  int unk5;			/*18*/                                                 
  int unk6;			/*1c*/                                               
  int unk7;			/*20*/                                                 
  int unk8;			/*24*/                                                   
  int unk9;			/*28*/                                                    
  int num_FAT_blocks;	/*2c*/                                          
  int root_start_block;	/*30*/                                     
  int unk10;			/*34*/                                          
  int unk11;			/*38*/                                           
  int dir_flag;			/*3c*/                                                
  int unk12;			/*40*/                                          
  int FAT_next_block;	/*44*/                                     
  int num_extra_FAT_blocks;	/*48*/                                                                          
  /* FAT block list starts here !! first 109 entries  */                                                            
};

struct OLE_DIR
{
  char name[64];
  short namsiz;
  char type;
  char filler1;
  int prev_dirent;
  int next_dirent;
  int dir_dirent;
  int unk1;
  int unk2;
  int unk3;
  int unk4;
  int unk5;
  int secs1;
  int days1;
  int secs2;
  int days2;
  int start_block;
  int size;
  int unk6;
};

struct DIRECTORY
{
  char name[64];
  int type;
  int level;
  int start_block;
  int size;
  int next;
  int prev;
  int dir;
  int s1;
  int s2;
  int d1;
  int d2;
}
 *dirlist, *dl;

#include <stdarg.h>
#include <string.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/stat.h>
