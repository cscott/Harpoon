#include<string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include "common.h"

char * copystr(const char *buf) {
  int i;
  if (buf==NULL)
    return NULL;
  for(i=0;;i++)
    if (buf[i]==0) {
      char *ptr=new char[i+1];
      memcpy(ptr,buf,i+1);
      return ptr;
    }
}


unsigned int hashstring(char *strptr) {
  unsigned int hashcode=0;
  int *intptr=(int *) strptr;
  if(intptr==NULL)
    return 0;
  while(1) {
    int copy1=*intptr;
    if((copy1&0xFF000000)&&
       (copy1&0xFF0000)&&
       (copy1&0xFF00)&&
       (copy1&0xFF)) {
      hashcode^=*intptr;
      intptr++;
    } else {
      if (!copy1&0xFF000000)
	hashcode^=copy1&0xFF000000;
      else if (!copy1&0xFF0000)
	hashcode^=copy1&0xFF0000;
      else if (!copy1&0xFF00)
	hashcode^=copy1&0xFF00;
      else if (!copy1&0xFF)
	hashcode^=copy1&0xFF;
      return hashcode;
    }
  }
}

int equivalentstrings(char *str1, char *str2) {
  if ((str1!=NULL)&&(str2!=NULL)) {
    if (strcmp(str1,str2)!=0)
      return 0;
    else
      return 1;
  } else if ((str1==NULL)&&(str2==NULL))
    return 1;
  else return 0;
}

