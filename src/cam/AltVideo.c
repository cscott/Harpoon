/* stripped down simplified camera interface -- CSA */
/* much code copied from 'vidcat' in the w3cam package. license==GPL. */
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <linux/videodev.h>

#include "AltVideo.h"

JNIEXPORT jboolean JNICALL Java_AltVideo_capture
    (JNIEnv *env, jclass cls, jint width, jint height, jintArray buffer) {
    jint *bf; jsize bf_length;
    struct video_capability vid_caps;
    struct video_mbuf vid_buf;
    struct video_mmap vid_mmap;
    struct video_channel vid_chnl;
    jboolean result = JNI_FALSE; /* no success yet */
    int mmapped;
    char *map;
    int len;
    int fd;
    int i;

    /* default width/height */
    if (width==0) width=320;
    if (height==0) height=240;
    
    fd = open("/dev/video", O_RDWR);
    if (fd<0) goto out0;

    if (ioctl (fd, VIDIOCGCAP, &vid_caps) == -1) {
	perror ("ioctl (VIDIOCGCAP)");
	goto out1;
    }
    if (ioctl (fd, VIDIOCGMBUF, &vid_buf) == -1) {
	/* to do a normal read()
	 */
	struct video_window vid_win;
	fprintf (stderr, "using read()\n");

	if (ioctl (fd, VIDIOCGWIN, &vid_win) != -1) {
	    vid_win.width  = width;
	    vid_win.height = height;
	    if (ioctl (fd, VIDIOCSWIN, &vid_win) == -1)
		goto out1;
	}
	    
	mmapped=0;
	map = malloc (width * height * 3);
	len = read (fd, map, width * height * 3);
	if (len <=  0) goto out2;
    } else {
	mmapped=1;
	map = mmap (0, len=vid_buf.size, PROT_READ|PROT_WRITE,MAP_SHARED,fd,0);
	if ((unsigned char *)-1 == (unsigned char *)map) {
	    perror ("mmap()");
	    goto out1;
	}

	vid_mmap.format = VIDEO_PALETTE_RGB24; /* potentially adjustable */
	vid_mmap.frame = 0;
	vid_mmap.width = width;
	vid_mmap.height =height;
	if (ioctl (fd, VIDIOCMCAPTURE, &vid_mmap) == -1) {
		perror ("VIDIOCMCAPTURE");
		goto out2;
	}
	if (ioctl (fd, VIDIOCSYNC, &vid_mmap) == -1) {
		perror ("VIDIOCSYNC");
		goto out2;
	}
    }
    /* okay, we have the map.  Copy it into our java buffer */

    bf = (*env)->GetIntArrayElements(env, buffer, NULL);
    if (bf==NULL) goto out2;
    bf_length = (*env)->GetArrayLength(env, buffer);

    /* assume that data is always in memory little-endian.  Convert
     * to host order as we copy it into the java int array. */
    for (i=0; i<bf_length; i++) {
	jint val=0; int j;
	for (j=0; j<3; j++)
	    if (i*3+j < len)
		val |= (0xFF & map[i*3+j]) << (j*8);
	bf[i] = val;
    }

    /* success! */
    result = JNI_TRUE;

    out3:
    (*env)->ReleaseIntArrayElements(env, buffer, bf, result ? 0 : JNI_ABORT);
    out2:
    if (!mmapped) free(map); else munmap (map, len);
    out1:
    close(fd);
    out0:
    return result; /* failure */
}


