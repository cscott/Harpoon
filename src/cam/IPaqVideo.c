/*
 * Command line test software for the iPAQ H3600 Backpaq camera
 * Driver for the HP iPAQ Mercury Backpaq camera
 * Video4Linux interface
 * Java interface
 *
 * Copyright 2001 Compaq Computer Corporation.
 *
 * Use consistent with the GNU GPL is permitted,
 * provided that this copyright notice is
 * preserved in its entirety in all copies and derived works.
 *
 * COMPAQ COMPUTER CORPORATION MAKES NO WARRANTIES, EXPRESSED OR IMPLIED,
 * AS TO THE USEFULNESS OR CORRECTNESS OF THIS CODE OR ITS
 * FITNESS FOR ANY PARTICULAR PURPOSE.
 *
 * Author: Andrew Christian 
 *         <andrew.christian@compaq.com>
 *         4 June 2001
 *
 * Author: Andrew Christian (driver)
 *         <andyc@handhelds.org>
 *         4 May 2001
 *
 * Changed: Wim de Haan
 *          Added items for i2c control of Philips imagers
 *          02/25/02
 *
 * Changed: Wes Beebee
 *          Created java library interface
 *          05/08/03
 *
 * Issues to be addressed:
 *    1. Writing to the FPGA when we need to do a functionality change
 *    2. Sampling the pixels correctly and building a pixel array
 *    3. Handling different pixel formats correctly
 *    4. Changing the contrast, brightness, white balance, and so forth.
 *    5. Specifying a subregion (i.e., setting "top, left" and SUBCAPTURE)
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <linux/videodev.h>
#include <string.h>
#include "IPaqVideo.h"

/* Philips camera defaults */
#define H3600_BACKPAQ_CAMERA_PHILIPS_CLOCK_DIVISOR      3     /* 15 fps */
#define H3600_BACKPAQ_CAMERA_PHILIPS_INTERRUPT_FIFO     0x20  /* 32 deep */
#define H3600_BACKPAQ_CAMERA_PHILIPS_ELECTRONIC_SHUTTER 255  
#define H3600_BACKPAQ_CAMERA_PHILIPS_SUBROW             0
#define H3600_BACKPAQ_CAMERA_PHILIPS_PROG_GAIN_AMP      128
#define H3600_BACKPAQ_CAMERA_PHILIPS_X1			24
#define H3600_BACKPAQ_CAMERA_PHILIPS_Y1			14
#define H3600_BACKPAQ_CAMERA_PHILIPS_WIDTH		644  
#define H3600_BACKPAQ_CAMERA_PHILIPS_HEIGHT		484
#define H3600_BACKPAQ_CAMERA_PHILIPS_BLACK_OFFSET	0x14
#define H3600_BACKPAQ_CAMERA_PHILIPS_GOOB		0x20
#define H3600_BACKPAQ_CAMERA_PHILIPS_GEOB		0x20
#define H3600_BACKPAQ_CAMERA_PHILIPS_GOEB		0x20
#define H3600_BACKPAQ_CAMERA_PHILIPS_GEEB		0x20

/* Philips camera register meanings */
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_REFEQ		(1<<6)
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_EQGAIN		(1<<5)
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_HANDV		(1<<4)  
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_MULTISEN	(1<<3)
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_RST		(1<<2)
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_PSAVE		(1<<1)
#define H3600_BACKPAQ_CAMERA_PHILIPS_CR1_PDOWN		(1<<0)  

struct h3600_backpaq_camera_philips {
    unsigned short clock_divisor;      /* 9 = 5 fps */
    unsigned short interrupt_fifo;
    unsigned char  read_polling_mode;  /* Force "read" to use polling mode */
    unsigned char  flip;               /* Set to TRUE to invert image */
    
    unsigned short electronic_shutter;
    unsigned char  subrow;
};


struct h3600_backpaq_camera_philips_winsize {
    unsigned short x1;      /* 1st row */
    unsigned short y1;      /* 1st column */
    unsigned short  width;  
    unsigned short height;  
};

struct h3600_backpaq_camera_philips_gains {
    unsigned short  goob;
    unsigned short  geob;
    unsigned short  goeb;  
    unsigned short  geeb;  
};

enum h3600_camera_type {
    H3600_SMAL,
    H3600_PHILIPS
};

struct h3600_backpaq_camera_type {
    unsigned char  model;              /* See "backpaq.h" for a list of the camera types */
    unsigned char  orientation;        /* 0 = portrait, 1 = landscape */
    enum h3600_camera_type type;       /* General type */
};

/*
   Private IOCTL to control camera parameters and image flipping
 */

#define H3600CAM_G_TYPE         _IOR ('v', BASE_VIDIOCPRIVATE+6, struct h3600_backpaq_camera_type)
#define H3600CAM_RESET		_IO  ('v', BASE_VIDIOCPRIVATE+2 )

/* Ioctl's specific to the Philips imager */
#define H3600CAM_PH_G_PARAMS    _IOR ('v', BASE_VIDIOCPRIVATE+7, struct h3600_backpaq_camera_philips)
#define H3600CAM_PH_S_PARAMS    _IOW ('v', BASE_VIDIOCPRIVATE+8, struct h3600_backpaq_camera_philips)
#define H3600CAM_PH_SET_ESHUT	_IOW ('v', BASE_VIDIOCPRIVATE+3, int )
#define H3600CAM_PH_SET_SUBROW	_IOW ('v', BASE_VIDIOCPRIVATE+4, int )
#define H3600CAM_PH_SET_PGA	_IOW ('v', BASE_VIDIOCPRIVATE+5, int )

#define H3600CAM_PH_G_WINSIZE   _IOR ('v', BASE_VIDIOCPRIVATE+11, struct h3600_backpaq_camera_philips_winsize)
#define H3600CAM_PH_S_WINSIZE   _IOW ('v', BASE_VIDIOCPRIVATE+12, struct h3600_backpaq_camera_philips_winsize)

#define H3600CAM_PH_G_GAINS   _IOR ('v', BASE_VIDIOCPRIVATE+13, struct h3600_backpaq_camera_philips_gains)
#define H3600CAM_PH_S_GAINS   _IOW ('v', BASE_VIDIOCPRIVATE+14, struct h3600_backpaq_camera_philips_gains)

#define H3600CAM_PH_G_BLACK_OFFSET   _IOR ('v', BASE_VIDIOCPRIVATE+15, int)
#define H3600CAM_PH_S_BLACK_OFFSET   _IOW ('v', BASE_VIDIOCPRIVATE+16, int)

/* Ioctl's for debugging  these set/get the value of a 32 bit register on the  */
/* FPGA.  this can be very useful for timing if you bring some of those signals */
/* out to a scope :) */
#define H3600CAM_GET_TEST32	_IOR ('v', BASE_VIDIOCPRIVATE+9, int )
#define H3600CAM_SET_TEST32	_IOW ('v', BASE_VIDIOCPRIVATE+10, int )

#define VIDEO_DEVICE "/dev/v4l/video0"

struct video_picture    vpic;
struct video_window     vwin;
struct h3600_backpaq_camera_type    ctype;
struct h3600_backpaq_camera_philips philips;

int size = 0;
int fd = 0;
unsigned char* buf = NULL;

int setup() {
    /* Read the camera type */
    fd = open(VIDEO_DEVICE, O_RDONLY | O_NOCTTY);
    
    ioctl(fd, H3600CAM_G_TYPE, &ctype);
    
    if (ctype.type == H3600_SMAL) {
	fprintf(stderr, "SMaL Camera not supported!\n");
	fflush(stderr);
	return -1;
    }
    
    return camera_properties(128, 128, 5, 0, 0, 0, 640, 480);
}
  
int camera_properties(int desired_brightness, /* 0-255 */
		      int desired_contrast, /* 0-255 */
		      int desired_fps, /* 1-45 */
		      int desired_gain, /* 0-4 */
		      int desired_poll, /* 0 1 */
		      int desired_flip, /* 0 1 */
		      int width, /* 640 352 320 176 160 */
		      int height /* 480 288 240 144 120 */)
{
    int oldsize = size;
    ioctl(fd, VIDIOCGPICT, &vpic); 
    
    /* Fix picture information */
    vpic.palette = VIDEO_PALETTE_RGB24;
    vpic.brightness = ((unsigned int)desired_brightness) * 256;
    vpic.contrast = (unsigned int)desired_contrast * 256;
    
    ioctl(fd, VIDIOCSPICT, &vpic);
    ioctl(fd, VIDIOCGPICT, &vpic); 
    ioctl(fd, VIDIOCGWIN, &vwin);
    
    /* Fix the capture window stuff */
    vwin.width = width;
    vwin.height = height;
    
    ioctl(fd, VIDIOCSWIN, &vwin);
    ioctl(fd, VIDIOCGWIN, &vwin);
    
    /* Set up the parameters */
    ioctl(fd, H3600CAM_PH_G_PARAMS, &philips);
    
    philips.clock_divisor     = 45 / desired_fps;
    philips.read_polling_mode = desired_poll;
    philips.flip              = desired_flip;
    
    ioctl(fd, H3600CAM_PH_S_PARAMS, &philips);
    ioctl(fd, H3600CAM_PH_G_PARAMS, &philips);
    
    size = vwin.height * vwin.width * vpic.depth / 8;
    if (!buf) {
	buf = (unsigned char *) malloc(size);
    } else if (size != oldsize) {
	buf = (unsigned char *) realloc(buf, size);
    }
    return 0;
}

int read_frame() {
    return read(fd, buf, size);
}

int read_frame_buf(unsigned char* buf) {
    return read(fd, buf, size);
}

int shutdown() {
    return close(fd);
}

/*
 * Class:     IPaqVideo
 * Method:    setup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_setup
(JNIEnv *env, jobject ipaq) {
    setup();
}

/*
 * Class:     IPaqVideo
 * Method:    capture
 * Signature: ([B[B[B)V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_capture___3B_3B_3B
(JNIEnv *env, jobject ipaq, 
 jbyteArray rvals, jbyteArray gvals, jbyteArray bvals) {
    int i, j;
    jbyte *rbuf = (*env)->GetByteArrayElements(env, rvals, NULL);
    jbyte *gbuf = (*env)->GetByteArrayElements(env, gvals, NULL);
    jbyte *bbuf = (*env)->GetByteArrayElements(env, bvals, NULL);
    jsize rbf_length = (*env)->GetArrayLength(env, rvals);
    jsize gbf_length = (*env)->GetArrayLength(env, gvals);
    jsize bbf_length = (*env)->GetArrayLength(env, bvals);
    jsize bf_length = rbf_length<gbf_length?rbf_length:gbf_length;
    bf_length = bbf_length<bf_length?bbf_length:bf_length;
    bf_length = (size/3)<bf_length?(size/3):bf_length;

    /* bf_length contains the minimum length */

    read_frame();

    j = 0;
    for (i=0; i<bf_length; i++) {
	j += 3;
	rbuf[i] = (jbyte)buf[j]; 
	gbuf[i] = (jbyte)buf[j+1];
	bbuf[i] = (jbyte)buf[j+2];
    }

    (*env)->ReleaseByteArrayElements(env, rvals, rbuf, 0);
    (*env)->ReleaseByteArrayElements(env, gvals, gbuf, 0);
    (*env)->ReleaseByteArrayElements(env, bvals, bbuf, 0);
}

/*
 * Class:     IPaqVideo
 * Method:    capture
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_capture___3B
(JNIEnv *env, jobject ipaq, jbyteArray vals) {
    jbyte *vbuf = (*env)->GetByteArrayElements(env, vals, NULL);
    jsize bf_length = (*env)->GetArrayLength(env, vals);
    int i;

    if (bf_length != size) {
	jclass excls = (*env)->FindClass(env, "java/lang/Error");
	char desc[200];
	snprintf(desc, 200, "Wrong size of array in capture: %d != %d.", 
		 bf_length, size);
	(*env)->ThrowNew(env, excls, desc);
    }
    
    read_frame_buf((unsigned char*)vbuf);

    (*env)->ReleaseByteArrayElements(env, vals, vbuf, 0);
}

/*
 * Class:     IPaqVideo
 * Method:    unsafeSetProperties
 * Signature: (BBBBZZII)V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_unsafeSetProperties
(JNIEnv *env, jobject ipaq, jbyte brightness, jbyte contrast, jbyte fps, jbyte gain, 
 jboolean poll, jboolean flip, jint width, jint height) {
    camera_properties((int)brightness, (int)contrast, (int)fps, (int)gain,
		      (poll==JNI_TRUE)?(int)1:(int)0, (flip==JNI_TRUE)?(int)1:(int)0, 
		      (int)width, (int)height);
}
