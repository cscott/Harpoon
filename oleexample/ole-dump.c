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
  initializeanalysis();
  {
    void *ptr=(struct block *) mmap(NULL,filesize,PROT_READ|PROT_WRITE|PROT_EXEC,MAP_SHARED,fd,0);
    alloc(ptr,filesize);
    addmapping("d",ptr,"ole");
    addintmapping("size",filesize);
    doanalysis();
    msync(ptr,filesize,MS_SYNC);
    munmap(ptr,filesize);
  }
  /* get the header block */

  get_block (fd, -1, buffer);
  if (memcmp (buffer, MAGIC, sizeof (MAGIC)) != 0)
    die ("%s is not an OLE2 Structured Storage File\n", filename);
  fprintf (stdout, "Table of Contents for %s:\n", filename);
  if (verbose)
    dump_header ();

  /* build the FAT */

  h = (struct OLE_HDR *) buffer;
  FAT = (int *) Malloc (OUR_BLK_SIZE * (h->num_extra_FAT_blocks + 1));
  p = (char *) FAT;
  memcpy (p, &h[1], OUR_BLK_SIZE - FAT_START);
  if (h->FAT_next_block > 0)
    {
      p += (OUR_BLK_SIZE - FAT_START);
      blknum = h->FAT_next_block;
      for (i = 0; i < h->num_extra_FAT_blocks; i++)
	{
	  get_block (fd, blknum, p);
	  p += OUR_BLK_SIZE - sizeof (int);	/* point at link */

	  blknum = *((int *) p);
	}
    }
  /* get directory list */
  /* first get size  */

  extra_dir_blocks = 0;
  blknum = h->root_start_block;
  get_dir_block (fd, blknum, buffer);
  while (blknum != END_OF_CHAIN) {
    get_FAT_block (fd, blknum, block_list);
    blknum = block_list[blknum % 128];
    if (blknum == END_OF_CHAIN)
      break;
    extra_dir_blocks++;
    if (!get_dir_block (fd, blknum, buffer))	/* short block? */
      break;
  }
  /* now get entries */

  get_block (fd, -1, buffer);
  blknum = h->root_start_block;
  size = OUR_BLK_SIZE * (extra_dir_blocks + 1);
  dirlist = (struct DIRECTORY *) Malloc (size);
  memset (dirlist, 0, size);
  get_dir_block (fd, blknum, buffer);
  get_dir_info (buffer);
  for (i = 0; i < extra_dir_blocks; i++)
    {
      /* get rest of directories */                                                                              
      get_FAT_block (fd, blknum, block_list);
      blknum = block_list[blknum % 128];
      if (blknum == END_OF_CHAIN)
	break;
      get_dir_block (fd, blknum, buffer);
      get_dir_info (buffer);
    }
  reorder_dirlist (dirlist, 0);
  /* display entries */                                                                                              
  for (dl = dirlist, i = 0; i < dir_count; i++, dl++)
    {
      memset (buffer, ' ', 75);
      j = dl->level * 4;
      sprintf (&buffer[j], "%-s", dl->name);
      j = strlen (buffer);
      if (dl->type == STREAM)
	{
	  buffer[j] = ' ';	/* remove null  */                                                
	  sprintf (&buffer[60], "%8d\n", dl->size);
	}
      else
	sprintf (&buffer[j], "\n");
      fprintf (stdout, buffer);
    }
  for (dl = dirlist, i = 0; i < dir_count; i++, dl++)
    {
      if (strncasecmp (dl->name, "DocumentSu", 10) == 0)
	{
	  get_FAT_block (fd, dl->start_block, block_list);
	  get_block (fd, dl->start_block, buffer);
	  p = &buffer[0x159];
	  if (strncmp (p, "GUID", 40) == 0)
	    {
	      p = &buffer[0x1a0];
	      fprintf (stdout, "Creator MAC address: ");
	      for (j = 0; j < 12; j++)
		{
		  fprintf (stdout, "%c", *p);
		  p += 2;
		  if ((j < 11) && (j % 2))
		    fprintf (stdout, ":");
		}
	      fprintf (stdout, "\n");
	    }
	  break;
	}
    }
  if (extract)
    {
      for (dl = dirlist, i = 0; i < dir_count; i++, dl++)
	{
	  if (strncasecmp (dl->name, extract_name,
			   strlen (extract_name)) == 0)
	    break;
	}
      if (i == dir_count)
	{
	  sprintf (buffer, "Can't find '%s' in %s\n", extract_name, filename);
	  die (buffer, 0);
	}
      extract_stream (fd, dl->start_block, dl->size);
      fprintf (stdout, "\n'%s' extracted to %s\n", dl->name, extract_name);
    }
  close (fd);
  free (FAT);
  return 0;
}

