/*
 * $Id: DeviceCtl.c,v 1.1 2000-08-22 20:19:26 bdemsky Exp $
 * Purpose: control functions for Video4Linux
 * Author: Kazushi Mukaiyama <kazu@arizona.ne.jp>
 */

#include "CaptureHeader.h"

int fd;
int status;
int WIDTH, HEIGHT;
int n;
char *map, *p;
char tmp;
struct video_capability vd;
struct video_channel vc[10];
struct video_picture vp;
struct video_mbuf vm;
struct video_mmap vmm;
struct video_buffer vb;
struct video_window vw;

void openDev(const char *);
void closeDev();
void setChannel(int, int);
void setColors(int, int, int, int, int, int, int);
void setMmap();
void releaseMmap();
void setCapSize(int, int);

/* open the device */
void openDev(const char *device) {
	status = fd = open(device, O_RDWR);
	if (status == -1) {
		perror("error open device");
		exit(1);
	}
}

/* close the device */
void closeDev() {
	status = close(fd);
	if (status == -1) {
		perror("error closing device");
		exit(1);
	}
}

/* set channel */
void setChannel(int channel, int signal) {
	vc[channel].norm = signal;	/* 0:PAL 1:NTSC 2:SECAM */
	status = ioctl(fd, VIDIOCSCHAN, &vc[channel]);
	if (status == -1) {
		perror("error ioctl(VIDIOCSCHAN)");
		exit(1);
	}
}

/* set colors */
void setColors(int b, int h, int cl, int ct, int w, int d, int p) {
		vp.brightness = b;
		vp.hue = h;
		vp.colour = cl;
		vp.contrast = ct;
		vp.whiteness = w;
		vp.depth = d;		/* color depth */
		vp.palette = p; 	/* palette style */
		status = ioctl(fd, VIDIOCSPICT, &vp);
		if (status == -1) {
			perror("error ioctl(VIDIOCSPICT)");
			exit(1);
		}
}

/* set mmap */
void setMmap() {
	if ((map = mmap(0, vm.size, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0)) == (char *)-1) {
		perror("error mmap");
		exit(1);
	}
}

/* release mmap */
void releaseMmap() {
	status = munmap(map, vm.size);
	if (status == -1) {
		perror("error munmap");
		exit(1);
	}
}

void setCapSize(int w, int h) {
	WIDTH = w;
	HEIGHT= h;

	/* define capture size */
	if (WIDTH>vd.maxwidth) WIDTH=vd.maxwidth;
	if (WIDTH<vd.minwidth) WIDTH=vd.minwidth;
	if (HEIGHT>vd.maxheight) HEIGHT=vd.maxheight;
	if (HEIGHT<vd.minheight) HEIGHT=vd.minheight;
	
	if (strncmp(vd.name,"OV511",5)==0){
		vw.width = WIDTH;
		vw.height = HEIGHT;
		status = ioctl(fd, VIDIOCSWIN, &vw);
		if (status<0){
			perror("ioctl(VIDIOCSWIN)");
			exit(1);
		}
	}
}

