#include <stdio.h>
#include <stdlib.h>

void *Malloc (size_t bytes);
int Read (int fd, char *buf, int size);
int Write (int fd, char *buf, int size);
void die (char *fmt, void *arg);

void *
Malloc (size_t bytes)
{
  void *x;

  x = malloc (bytes);
  if (x)
    return x;
  die ("Can't malloc %d bytes.\n", (char *) bytes);
  return 0;
}

int
Read (int fd, char *buf, int size)
{
  if (read (fd, buf, size) != size)
    {
      fprintf (stderr, "Bad read of %d bytes\n", size);
      exit (1);
    }
  return size;
}

int
Write (int fd, char *buf, int size)
{
  if (write (fd, buf, size) != size)
    {
      fprintf (stderr, "Bad write of %d bytes\n", size);
      exit (1);
    }
  return size;
}

void
die (char *fmt, void *arg)
{
  fprintf (stderr, fmt, arg);
  exit (1);
}
