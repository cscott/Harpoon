#include "ole.h"


unsigned char MAGIC[8] = { 0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1 };
 char buffer[OUR_BLK_SIZE];
char *extract_name = NULL;
int extract = 0;
int dir_count = 0;
int *FAT;
int verbose = 0;
int FATblk;
int currFATblk;
int block_list[OUR_BLK_SIZE / sizeof (int)];
extern int errno;

int get_dir_block (int fd, int blknum, char *dest);
void get_dir_info (char *src);
void extract_stream (int fd, int blknum, int size);
void dump_header ();
void dump_dirent (int which_one);
void get_block (int fd, int blknum, char *dest);
void get_FAT_block (int fd, int blknum, int *dest);
int reorder_dirlist (struct DIRECTORY *dir, int level);


int
get_dir_block (int fd, int blknum, char *dest)
{
  int i;
  struct OLE_DIR *dir;

  get_block (fd, blknum, dest);
  for (i = 0; i < DIRS_PER_BLK; i++)
    {
      dir = (struct OLE_DIR *) &buffer[sizeof (struct OLE_DIR) * i];
      if (dir->type == NO_ENTRY)
	break;
    }
  return (i == DIRS_PER_BLK);
}

void
get_dir_info (char *src)
{
  int i, j;
  char *p, *q;
  struct OLE_DIR *dir;

  for (i = 0; i < DIRS_PER_BLK; i++)
    {
      dir = (struct OLE_DIR *) &src[sizeof (struct OLE_DIR) * i];
      if (dir->type == NO_ENTRY)
	break;
      if (verbose)
	dump_dirent (i);
      dl = &dirlist[dir_count++];
      q = dl->name;
      p = dir->name;
      if (*p < ' ')
	p += 2;			/* skip leading short */                                                           
      for (j = 0; j < dir->namsiz; j++, p++)
	{
	  if (*p)
	    *q++ = *p;
	}
      *q = 0;
      dl->type = dir->type;
      dl->size = dir->size;
      dl->start_block = dir->start_block;
      dl->next = dir->next_dirent;
      dl->prev = dir->prev_dirent;
      dl->dir = dir->dir_dirent;
      if (dir->type != STREAM)
	{
	  dl->s1 = dir->secs1;
	  dl->s2 = dir->secs2;
	  dl->d1 = dir->days1;
	  dl->d2 = dir->days2;
	}
    }
}

void
extract_stream (int fd, int blknum, int size)
{
  int outfd;
  char data_block[OUR_BLK_SIZE];

  outfd = open (extract_name, O_RDWR | O_CREAT | O_TRUNC, 0644);
  if (outfd < 0)
    die ("Can't create '%s'.\n", extract_name);

  if (verbose)
    fprintf (stderr, "Block list (%d): ", size);
  get_FAT_block (fd, blknum, block_list);
  while (size > 0)
    {
      get_block (fd, blknum, data_block);
      Write (outfd, data_block, MIN (size, OUR_BLK_SIZE));
      if (verbose)
	fprintf (stderr, "%d(%d),", blknum, size);
      size -= OUR_BLK_SIZE;
      get_FAT_block (fd, blknum, block_list);
      blknum = block_list[blknum % 128];	/* get next block number */                                            
      if (blknum == END_OF_CHAIN)	/* end of chain? */                                                       
	break;
    }
  if (verbose)
    fprintf (stderr, "\n");
  close (outfd);
}

static int *lnlv;		/* last next link visited ! */                                                             
int
reorder_dirlist (struct DIRECTORY *dir, int level)
{
  dir->level = level;
  if (dir->dir != -1 || dir->dir > dir_count)
  {
      return 0;
  }
    else if (!reorder_dirlist (&dirlist[dir->dir], level + 1))
      return 0;
  /* reorder next-link subtree, saving the most next link visited */
  if (dir->next != -1)
    {
      if (dir->next > dir_count)
	return 0;
      else if (!reorder_dirlist (&dirlist[dir->next], level))
	return 0;
    }
  else
    lnlv = &dir->next;
  /* move the prev child to the next link and reorder it, if any exist                                            
   */
  if (dir->prev != -1)
    {
      if (dir->prev > dir_count)
	return 0;
      else
	{
	  *lnlv = dir->prev;
	  dir->prev = -1;
	  if (!reorder_dirlist (&dirlist[*lnlv], level))
	    return 0;
	}
    }
  return 1;
}

void
get_block (int fd, int blknum, char *dest)
{
  lseek (fd, OUR_BLK_SIZE * (blknum + 1), SEEK_SET);
  Read (fd, dest, OUR_BLK_SIZE);
}

void
get_FAT_block (int fd, int blknum, int *dest)
{
  static int FATblk;
  static int currFATblk = -1;

  FATblk = FAT[blknum / (OUR_BLK_SIZE / sizeof (int))];
  if (currFATblk != FATblk)
    {
      get_block (fd, FATblk, (char *) dest);
      currFATblk = FATblk;
    }
}


void
dump_header ()
{
  int i, *x;
  struct OLE_HDR *h = (struct OLE_HDR *) buffer;

  fprintf (stderr, "unk1  = %x\t", h->unk1);
  fprintf (stderr, "unk2  = %x\t", h->unk2);
  fprintf (stderr, "unk3  = %x\t", h->unk3);
  fprintf (stderr, "unk4  = %x\n", h->unk4);
  fprintf (stderr, "unk5  = %x\t", h->unk5);
  fprintf (stderr, "unk6  = %x\t", h->unk6);
  fprintf (stderr, "unk7  = %x\t", h->unk7);
  fprintf (stderr, "unk8  = %x\n", h->unk8);
  fprintf (stderr, "unk9  = %x\t", h->unk9);
  fprintf (stderr, "unk10 = %x\t", h->unk10);
  fprintf (stderr, "unk11 = %x\t", h->unk11);
  fprintf (stderr, "unk12 = %x\n", h->unk12);
  fprintf (stderr, "root  = %x\n", h->root_start_block);
  fprintf (stderr, "dir flag = %x\n", h->dir_flag);
  fprintf (stderr, "# FAT blocks = %x\n", h->num_FAT_blocks);
  fprintf (stderr, "FAT_next_block = %x\n", h->FAT_next_block);
  fprintf (stderr, "# extra FAT blocks = %x\n", h->num_extra_FAT_blocks);
  x = (int *) &h[1];
  fprintf (stderr, "bbd list:");
  for (i = 0; i < 109; i++, x++)
    {
      if ((i % 10) == 0)
	fprintf (stderr, "\n");
      fprintf (stderr, "%x ", *x);
    }
  fprintf (stderr, "\n");
}

void
dump_dirent (int which_one)
{
  int i;
  char *p;
  short unknown;
  struct OLE_DIR *dir;

  dir = (struct OLE_DIR *) &buffer[which_one * sizeof (struct OLE_DIR)];
  if (dir->type == NO_ENTRY)
    return;
  fprintf (stderr, "DIRENT_%d :\t", dir_count);
  fprintf (stderr, "%s\t", (dir->type == ROOT) ? "root directory" :
	   (dir->type == STORAGE) ? "directory" : "file");

  /* get UNICODE name */                                                                                             
  p = dir->name;
  if (*p < ' ')
    {
      unknown = *((short *) p);
      fprintf (stderr, "%04x\t", unknown);
      p += 2;			/* step over unknown short */                                                              
    }
  for (i = 0; i < dir->namsiz; i++, p++)
    {
      if (*p && (*p > 0x1f))
	fprintf (stderr, "%c", *p);
    }
  fprintf (stderr, "\n");
  fprintf (stderr, "prev dirent = %x\t", dir->prev_dirent);
  fprintf (stderr, "next dirent = %x\t", dir->next_dirent);
  fprintf (stderr, "dir  block  = %x\n", dir->dir_dirent);
  fprintf (stderr, "unk1  = %x\t", dir->unk1);
  fprintf (stderr, "unk2  = %x\t", dir->unk2);
  fprintf (stderr, "unk3  = %x\n", dir->unk3);
  fprintf (stderr, "unk4  = %x\t", dir->unk4);
  fprintf (stderr, "unk5  = %x\t", dir->unk5);
  fprintf (stderr, "unk6  = %x\n", dir->unk6);
  if (dir->type != STREAM)
    {
      fprintf (stderr, "secs1  = %u\t", dir->secs1);
      fprintf (stderr, "secs2  = %u\n", dir->secs2);
      fprintf (stderr, "days1  = %u\t", dir->days1);
      fprintf (stderr, "days2  = %u\n", dir->days2);
    }
  fprintf (stderr, "start block  = %x\n", dir->start_block);
  fprintf (stderr, "size  = %x\n", dir->size);
}

