#include "IPaqServoController.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <termios.h>
#include <unistd.h>
#include <stdio.h>

static int fd;

void setline(int fd, int flags, int speed) 
{
  struct termios t;
  tcgetattr(fd, &t);
  
  t.c_cflag = flags | CREAD | HUPCL | CLOCAL;
  t.c_iflag = IGNBRK | IGNPAR;
  t.c_oflag = 0;
  t.c_lflag = 0;
  t.c_cc[VMIN] = 1;
  t.c_cc[VTIME] = 0;

  cfsetispeed(&t, speed);
  cfsetospeed(&t, speed);

  tcsetattr(fd, TCSANOW, &t);
}

JNIEXPORT void JNICALL Java_IPaqServoController_setup(JNIEnv *env, jclass claz) {
  if ((fd = open("/dev/ttySA0", O_RDWR | O_NOCTTY )) < 0) {
    perror("unable to open serial device");
    return;
  }
  setline(fd, CS8, B19200);
}

JNIEXPORT void JNICALL Java_IPaqServoController_sendSerial(JNIEnv *env, jclass claz, jbyte byte) {
  write(fd,&byte,1);
}

JNIEXPORT jbyte JNICALL Java_IPaqServoController_readSerial(JNIEnv *env, jclass claz) {
  jbyte byte = 0;
  while (read(fd,&byte,1)==0) {}
  return byte;
}

