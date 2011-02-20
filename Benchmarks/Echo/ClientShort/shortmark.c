#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdarg.h>
#include <sys/time.h>
#include <errno.h>

#define bzero(ptr, size) memset (ptr, 0, size)


void
fatal (const char *msg, ...)
{
  va_list ap;

  fprintf (stderr, "fatal: ");
  va_start (ap, msg);
  vfprintf (stderr, msg, ap);
  va_end (ap);
  exit (1);
}


/* Create a TCP connection to host and port.  Returns a file
 * descriptor on success, -1 on error. */
int tcpconnect (char *host, int port)
{
  struct hostent *h;
  struct sockaddr_in sa;
  int s;

  /* Get the address of the host at which to finger from the
   * hostname.  */
  h = gethostbyname (host);
  if (!h || h->h_length != sizeof (struct in_addr)) {
    fprintf (stderr, "%s: no such host\n", host);
    return -1;
  }

  /* Create a TCP socket. */
  s = socket (AF_INET, SOCK_STREAM, 0);

  /* Use bind to set an address and port number for our end of the
   * finger TCP connection. */
  bzero (&sa, sizeof (sa));
  sa.sin_family = AF_INET;
  sa.sin_port = htons (0);                  /* tells OS to choose a port */
  sa.sin_addr.s_addr = htonl (INADDR_ANY);  /* tells OS to choose IP addr */
  if (bind (s, (struct sockaddr *) &sa, sizeof (sa)) < 0) {
    perror("bind: returned nonzero");
    close (s);
    return -1;
  }

  /* Now use h to set set the destination address. */
  sa.sin_port = htons (port);
  sa.sin_addr = *(struct in_addr *) h->h_addr;

  /* And connect to the server */
  if (connect (s, (struct sockaddr *) &sa, sizeof (sa)) < 0) {
    perror (host);
    close (s);
    return -1;
  }

  return s;
}

struct timeval tme;

long getMillis()
{
    if (gettimeofday(&tme, NULL)) fatal("gettimeofday: unknown error\n");

    return tme.tv_sec*1000 + tme.tv_usec/1000;
}
int nMessages= 2000, nClients= 64, port= 4000, mLength= 31;
char *hostname= "localhost";
char *synchost= "speed-test.lcs.mit.edu";
char *message= NULL;
long time0, time1, time2;
int ack, i,j,k, ofs, len, rLen, tmp, msgs, fd_max;
int *sock, s, bLength;
char *inbuffer, *outbuffer;
fd_set rtemp, rperm,rtemp2;
int *count;
int counter=0,maxfiles=11000,moff=0;
long filename;
int temp;
int main(int argc, char **argv)
{
    puts("Mark v5 - C");
    switch(argc) {
    case 7: maxfiles=atoi(argv[6]);
    case 6: moff= atoi(argv[5]);
    case 5: nMessages = atoi(argv[4]);
    case 4: nClients= atoi(argv[3]);
    case 3: port= atoi(argv[2]);
    case 2: hostname= argv[1];
      break;
    case 1: 
      printf("mark hostname port #clients #connections offset maxfiles");
      break;
    default: perror("too many arguments"); exit(1);
    }
    

    
    


    inbuffer= malloc(1000);
    outbuffer= malloc(200);
    s=tcpconnect(synchost, 4001);
    rLen=0;
    while (rLen<=0)
      rLen= read(s, inbuffer, 5);

//    sprintf(outbuffer, "%s\n", message);

    puts("Opening connections...");
    time0= getMillis();
    
    sock= malloc(nClients * sizeof(int));
    
    for (i=0; i<nClients; i++) {
      if (i%100 == 0) printf("%d connections\n",i+1);
      if ( (sock[i] = tcpconnect(hostname, port)) == -1)
	fatal("Unable to open connection #%d\n", i+1);
      if (sock[i]>fd_max) fd_max= sock[i];
    }

    fd_max++;   // safety

    msgs= nClients * nMessages;

    time1= getMillis();

    puts("Sending messages...");

    FD_ZERO(&rperm);
    
    count= malloc(fd_max*sizeof(int));

    for (k=0; k<nClients; k++) {
      filename=((counter+moff)*31)%maxfiles;
      counter++;
      bLength=sprintf(outbuffer,"ping567890");
      if ( (tmp= write(sock[k], outbuffer, bLength)) != bLength)
	fatal("write: trouble writing data: %d/%d written\n", tmp, bLength);
      FD_SET(sock[k],&rperm);
      count[sock[k]]= 0;
    }

    ack= 0;
    
    while (ack < msgs) {
      memcpy(&rtemp, &rperm, sizeof(fd_set));      
      memcpy(&rtemp2, &rperm, sizeof(fd_set));      
      if (select(fd_max, &rtemp, NULL, &rtemp2, NULL)==-1)
	printf("errno: %d",errno);

      for (k=3; k<fd_max; k++) 
	if ((FD_ISSET(k, &rtemp))
	  	  ||FD_ISSET(k, &rtemp2))
	   {
	     if (FD_ISSET(k, &rtemp2))
	     printf("errorX");
	     // read

	     
	     //     printf("prepping to read k=%d\n",k);
	     rLen= read(k, inbuffer, 1000);
	     
	     if (rLen!=10) {
	       if (rLen==-1)
		printf("error %d\n",errno);

	       printf("%d\n",rLen);
	       printf("%s\n",inbuffer);
	       printf("\n");
	     }

	     
	      // write 
	      ack++;
	     if (ack%1000 == 0)
	       printf("Received %d / %d\n", ack, msgs);
	      close(k);
	      
	      count[k]=0;
	      FD_CLR(k,&rperm);
	      if (counter<(nMessages*nClients)) {
		if ( (temp = tcpconnect(hostname, port)) == -1)
		  fatal("Unable to open connection #%d\n", i+1);
		if (temp>=fd_max) fd_max= temp+1;
		counter++;
		filename=((counter+moff)*31)%maxfiles;
		bLength=sprintf(outbuffer,"ping567890");
		if ( (tmp= write(temp, outbuffer, bLength)) != bLength)
		  fatal("write: trouble writing data: %d/%d written\n", tmp, bLength);
		//printf("Adding %d\n",temp);
		FD_SET(temp,&rperm);
	      }
	   }
    }
    time2= getMillis();

    printf("\nTest complete:\n\n");
    printf("#Clients: %d\n",nClients);
    printf("#Messages/ client: %d\n",nMessages);
    printf("#Acknowledged: %d\n",ack);
    printf("Opening rate: %ld connections/s\n", nClients*1000/(time1-time0) );
    printf("Elapsed time: %ld ms",time2-time0);
    printf("Throughput: %ld messages/s\n", msgs*1000/(time2-time1) );
    printf("Overall: %ld messages/s\n", msgs*1000/(time2-time0) );
    
    for (k=0; k<nClients; k++) close(sock[k]);

    return 0;
}
    
