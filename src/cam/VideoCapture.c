/*
 * $Id: VideoCapture.c,v 1.1 2000-08-22 20:19:26 bdemsky Exp $
 * Purpose: native method for Video4Linux
 * Author: Kazushi Mukaiyama <kazu@arizona.ne.jp>
 */

#include "CaptureHeader.h"
#include "VideoCapture.h"
#include <errno.h>

/*
 * Class:     VideoCapture
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_VideoCapture_open(
	JNIEnv *env, jobject obj, 
	jstring device, 
	jint width, jint height, 
	jint channel, jint signal, 
	jint b, jint h, jint cl, jint ct, jint w) {
		const char *str = (*env)->GetStringUTFChars(env, device, 0);		
		openDev(str);
		getCapInfo();
		if (strncmp(vd.name,"OV511",5)==0){
			fprintf(stderr,"I found %s\n", vd.name);
			getWinInfo();
			showWinInfo();
			getChanInfo();
			getColorInfo();
			setCapSize((int)width, (int)height);		
			setChannel((int)channel, (int)signal);
			setColors((int)b, (int)h, (int)cl, (int)ct, (int)w, 24, VIDEO_PALETTE_RGB24);
		} else {
			if (strncmp(vd.name, "BT848", 5)==0){
				fprintf(stderr,"I found %s\n", vd.name);
				getChanInfo();
				getMmapInfo();
				getColorInfo();
				setCapSize((int)width, (int)height);		
				setChannel((int)channel, (int)signal);
				setColors((int)b, (int)h, (int)cl, (int)ct, (int)w, 24, VIDEO_PALETTE_RGB24);
				setMmap();
			} else {
				fprintf(stderr,"%s not yet supported\n", vd.name);
				exit(1);
			}
		}
}

/*
 * Class:     VideoCapture
 * Method:    selectChannel
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_VideoCapture_selectChannel (JNIEnv *env, jobject obj, jint channel, jint signal) {
	/* select channel */
	setChannel((int)channel, (int)signal);
}

/*
 * Class:     VideoCapture
 * Method:    setColors
 * Signature: (IIIIIII)V
 */
JNIEXPORT void JNICALL Java_VideoCapture_setColors (
	JNIEnv *env, 
	jobject obj, 
	jint b, 
	jint h, 
	jint cl, 
	jint ct, 
	jint w, 
	jint d, 
	jint p) {
		/* set colors (almost 0-65535) */
		setColors((int)b, (int)h, (int)cl, (int)ct, (int)w, (int)d, (int)p);
	}
	
/*
 * Class:     VideoCapture
 * Method:    showInfo
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_VideoCapture_showInfo (JNIEnv *env, jobject obj) {
	showCapInfo();
	showChanInfo();
	showMmapInfo();
	showWinInfo();
	showColorInfo();
}

/*
 * Class:     VideoCapture
 * Method:    setCapSize
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_VideoCapture_setCapSize (JNIEnv *env, jobject obj, jint width, jint height) {
	setCapSize((int)width, (int)height);
}

/*
 * Class:     VideoCapture
 * Method:    capture
 * Signature: (I[I)V
 */
JNIEXPORT void JNICALL Java_VideoCapture_capture__I_3I (JNIEnv *env, jobject obj, jint fb, jintArray buf) {
	int i;
	jint *bf;
	jint temp[3];
	bf = (*env)->GetIntArrayElements(env, buf, NULL);		

	if (strcmp(vd.name,"QuickCam")==0){
		fprintf(stderr,"This method can't use on %s\n", vd.name);
		return;
	}

	/* start capture to frame "fb" */
	vmm.frame = (int)fb;
	vmm.width=WIDTH;
	vmm.height=HEIGHT;
	vmm.format=VIDEO_PALETTE_RGB24;
	status = ioctl(fd, VIDIOCMCAPTURE, &vmm);
	if (status == -1) {
		perror("error ioctl(VIDIOCMCAPTURE)");
		exit(1);
	}
	/* wait the end of capture to frame "fb" */
	n = fb;
	status = ioctl(fd, VIDIOCSYNC, &n);
	if (status == -1) {
		perror("error ioctl(VIDIOCSYNC)");
		exit(1);
	}
        	
	/* cast charArray -> intArray */
	for (i=0, p=map+vm.offsets[fb]; i<WIDTH*HEIGHT; i++, p+=3) {
		temp[0]=p[0];temp[1]=p[1];temp[2]=p[2];		
		bf[i]=((255<<24)&0xff000000)|
		      ((temp[2]<<16)&0xff0000)|
		      ((temp[1]<<8)&0xff00)|
		      (temp[0]&0xff);
	}
	(*env)->ReleaseIntArrayElements(env, buf, bf, 0);
 }

/*
 * Class:     VideoCapture
 * Method:    capture
 * Signature: ([B)V
 */
void JNICALL Java_VideoCapture_capture___3B(JNIEnv *env, jobject obj, jbyteArray buf){
	jbyte *bf;
	int i;
	unsigned char *buffer;
	
	if (strncmp(vd.name, "BT848", 5)==0){
		fprintf(stderr,"This method can't use on %s\n", vd.name);
		return;
	}

	bf = (*env)->GetByteArrayElements(env, buf, NULL);
	
	buffer = malloc(3*WIDTH * HEIGHT);			
	do {
	status = read(fd, buffer, 3*WIDTH * HEIGHT);
	} while ((status==-1)&&(errno==EINTR));
	if (status<0) {
		perror("READ BUFFER error");
		exit(1);
	}
	for (i=0; i<WIDTH*HEIGHT*3; i++) {
		bf[i]=(jbyte)(buffer[i]&0xff);
	}
	free(buffer);
	(*env)->ReleaseByteArrayElements(env, buf, bf, 0);
}

/*
 * Class:     VideoCapture
 * Method:    capture
 * Signature: ([B)V
 */
void JNICALL Java_VideoCapture_capture___3I(JNIEnv *env, jobject obj, jintArray buf){
	jint *bf;
	int i,r,g,b;
	unsigned char *buffer;
	
	if (strncmp(vd.name, "BT848", 5)==0){
		fprintf(stderr,"This method can't use on %s\n", vd.name);
		return;
	}
	bf = (*env)->GetIntArrayElements(env, buf, NULL);
       	buffer = malloc(3*WIDTH * HEIGHT);			
	do {
	status = read(fd, buffer, 3*WIDTH * HEIGHT);
	} while ((status==-1)&&(errno==EINTR));
	if (status<0) {
		perror("READ BUFFER error");
		exit(1);
	}
	for (i=0; i<WIDTH*HEIGHT; i++) {
	        r=buffer[i*3+2];
		g=buffer[i*3+1];
		b=buffer[i*3];
		bf[i]=(jint)(
			     ((255<<24)&0xff000000)|
			     ((r<<16)&0xff0000)|
			     ((g<<8)&0xff00)|
			     (b&0xff));
	}
	free(buffer);
	(*env)->ReleaseIntArrayElements(env, buf, bf, 0);
}

/*
 * Class:     VideoCapture
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_VideoCapture_close (JNIEnv *env, jobject obj) {
	if (strncmp(vd.name, "BT848", 5)==0){releaseMmap();}
    closeDev();
}




