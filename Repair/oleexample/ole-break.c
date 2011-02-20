#include "ole.h"
#include "test.h"
#include <sys/mman.h>

extern char buffer[OUR_BLK_SIZE];
extern int verbose;
extern int dir_count;
extern int block_list[OUR_BLK_SIZE / sizeof (int)];
extern int *FAT;
extern unsigned char MAGIC[8];
extern char *extract_name;
extern int extract;

int
main (int argc, char *argv[])
{

char *prog;

  int i, j;
  int fd;
  int size;
  int blknum;
  int error = 0;
  int extra_dir_blocks;
  char *p;
  char *filename;
  int filesize;
  int shareblocks=0,nonexistblocks=0,badfatsectors=0,badendsectors=0;
  struct stat status;
  struct OLE_HDR *h = (struct OLE_HDR *) buffer;

  prog = *argv;
  argc--;
  argv++;
  if (argc < 1)
    error++;
  else
    {
      while (*argv[0] == '-')
	{
	  switch (argv[0][1])
	    {
	    case 'v':
	      verbose++;
	      break;
	    case 'x':
	      if (argc < 2)
		error++;
	      else
		{
		  extract++;
		  extract_name = argv[1];
		  argc--;
		  argv++;
		}
	      break;
	    case 'b':
	      shareblocks=1;
	      break;
	    case 'n':
	      nonexistblocks=1;
	      break;
	    case 'f':
	      badfatsectors=1;
	      break;
	    case 'e':
	      badendsectors=1;
	      break;
	    default:
	      error++;
	      break;
	    }
	  argc--;
	  argv++;
	}
    }
  if (error || (argc < 1))
    {
      fprintf (stderr, "USAGE: %s [-v] [-x section_name] OLE2_file\n", prog);
      exit (1);
    }
  filename = *argv;
  fd = open (filename, O_RDWR);

  
  if (fd < 0)
    die ("Can't open %s\n", filename);

  fstat(fd,&status);
  filesize=status.st_size;
  {
    void *ptr=(struct block *) mmap(NULL,filesize,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
    struct OLE_HDR *olehdr=ptr;
    if (shareblocks) {
      int lastval=-1;
      int clastval=-1;
      int i;
      for(i=0;i<109;i++) {
	int sectnum=olehdr->sects[i];
	struct fatblk *fatptr=&(((struct fatblk *)ptr)[sectnum+1]);
	int j=0;
	for(j=0;j<128;j++) {
	  if (fatptr->sects[j]==END_OF_CHAIN) {
	    /* merge with previous */
	    fatptr->sects[j]=clastval;
	    clastval=lastval;
	  } else {
	    lastval=fatptr->sects[j];
	  }
	}
      }
    }
    if (nonexistblocks) {
      int i=0;
      for(i=0;i<109;i++) {
	int sectnum=olehdr->sects[i];
	struct fatblk *fatptr=&((struct fatblk *)ptr)[sectnum+1];
	int j=0;
	for(j=0;j<128;j++) {
	  if (fatptr->sects[j]==END_OF_CHAIN) {
	    /* merge with previous */
	    fatptr->sects[j]=1000000;
	  }
	}
      }
    }
    if (badfatsectors) {
      int i=0;
      for(i=0;i<109;i++) {
	int sectnum=olehdr->sects[i];
	struct fatblk *fatptr=&((struct fatblk *)ptr)[sectnum+1];
	int j=0;
	for(j=0;j<128;j++) {
	  if (fatptr->sects[j]==SPECIAL_BLOCK) {
	    /* merge with previous */
	    fatptr->sects[j]=UNUSED;
	  }
	}
      }
    }
    if (badendsectors) {
      int i=0;
      for(i=0;i<109;i++) {
	int sectnum=olehdr->sects[i];
	struct fatblk *fatptr=&((struct fatblk *)ptr)[sectnum+1];
	int j=0;
	for(j=0;j<128;j++) {
	  if (fatptr->sects[j]==END_OF_CHAIN) {
	    /* merge with previous */
	    fatptr->sects[j]=UNUSED;
	  }
	}
      }
    }
    msync(ptr,filesize,MS_SYNC);
    munmap(ptr,filesize);
  }
  /* get the header block */
  close (fd);
  return 0;
}

