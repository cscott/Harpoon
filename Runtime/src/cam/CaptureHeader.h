/*
 * $Id: CaptureHeader.h,v 1.1 2000-08-22 20:19:26 bdemsky Exp $
 * Purpose: native method for Video4Linux
 * Author: Kazushi Mukaiyama <kazu@arizona.ne.jp>
 */

#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>

#include <linux/types.h>
#include <linux/videodev.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

extern int fd;
extern int status;
extern int WIDTH, HEIGHT;
extern int n;
extern char *map, *p;
extern char tmp;
extern struct video_capability vd;
extern struct video_channel vc[10];
extern struct video_picture vp;
extern struct video_mbuf vm;
extern struct video_mmap vmm;
extern struct video_buffer vb;
extern struct video_window vw;

extern void getCapInfo();
extern void showCapInfo();
extern void getChanInfo();
extern void showChanInfo();
extern void getMmapInfo();
extern void showMmapInfo();
extern void getWinInfo();
extern void showWinInfo();
extern void getColorInfo();
extern void showColorInfo();

extern void openDev(const char *);
extern void closeDev();
extern void setChannel(int, int);
extern void setColors(int, int, int, int, int, int, int);
extern void setMmap();
extern void releaseMmap();
extern void setCapSize(int, int);
