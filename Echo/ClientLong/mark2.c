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
long time0, time1, time2,time0n;
int ack, i,j,k, ofs, len, rLen, tmp, msgs, fd_max;
int *sock, s, bLength;
char *inbuffer, *outbuffer;
fd_set rtemp, rperm;
int *count;

int main(int argc, char **argv)
{
    puts("Mark v5 - C");
    switch(argc) {
      case 6: mLength= atoi(argv[5]);
      case 5: nMessages= atoi(argv[4]);
      case 4: nClients= atoi(argv[3]);
      case 3: port= atoi(argv[2]);
      case 2: hostname= argv[1];
	break;
    case 1: 
      printf("mark2 hostname port nClients nMessages mLength\n");
      exit(1);
      break;
      default: perror("too many arguments"); exit(1);
    }
    
    message= malloc(mLength);
    for (i=0; i<mLength; i++)
	message[i]= i%10 + 48;

    time0n= getMillis();

    inbuffer=malloc(100);
    s=tcpconnect(synchost, 4001);
    while (rLen<=0)
      rLen= read(s, inbuffer, 5);
    free(inbuffer);


    bLength= mLength+1;
    inbuffer= malloc(bLength);
    outbuffer= malloc(bLength);
    
    sprintf(outbuffer, "%s\n", message);

    puts("Opening connections...");
    time0= getMillis();
    
    sock= malloc(nClients * sizeof(int));
    
    for (i=0; i<nClients; i++) {
      if (i%100 == 0) printf("%d connections\n",i+1);
      if ( (sock[i] = tcpconnect(hostname, port)) == -1)
	fatal("Unable to open connection #%d\n", i+1);
      if (sock[i]>fd_max) fd_max= sock[i];
    }
    //synchronize everyone
    s=tcpconnect(synchost, 4002);
    while (rLen<=0)
      rLen= read(s, inbuffer, 5);


    fd_max++;   // safety

    msgs= nClients * nMessages;

    time1= getMillis();

    puts("Sending messages...");

    FD_ZERO(&rperm);
    
    count= malloc(fd_max*sizeof(int));

    for (k=0; k<nClients; k++) {
	if ( (tmp= write(sock[k], outbuffer, bLength)) != bLength)
	  fatal("write: trouble writing data: %d/%d written\n", tmp, bLength);
	FD_SET(sock[k],&rperm);
	count[sock[k]]= 1;
    }

    ack= 0;
    
    while (ack < msgs) {
      memcpy(&rtemp, &rperm, sizeof(fd_set));      
      select(fd_max, &rtemp, NULL, NULL, NULL);

      for (k=3; k<fd_max; k++) 
	if (FD_ISSET(k, &rtemp))
	  if (count[k]<=nMessages) {

	    // read
	    ofs=0; len= bLength;
	
	    while(len>0) {
	      rLen= read(k, inbuffer + ofs, len);
	      if (rLen<0) fatal("read: failed to get message back");
	      ofs+= rLen;
	      len-= rLen;
	    }

	    ack++;
	    if (ack%1000 == 0)
	      printf("Received %d / %d\n", ack, msgs);
	    
	    // write 
	    if (count[k]<nMessages) {
	      if ( (tmp= write(k, outbuffer, bLength)) != bLength)
		fatal("write: trouble writing data: %d/%d written\n", tmp, bLength);
	      count[k]++;
	    }
	  } else fatal("read: got unexpected message (#%d) on fd %d\n",count[k],k);
    }
    time2= getMillis();

    printf("\nTest complete:\n\n");
    printf("#Clients: %d\n",nClients);
    printf("#Messages/ client: %d\n",nMessages);
    printf("#Acknowledged: %d\n",ack);
    printf("Opening rate: %ld connections/s\n", nClients*1000/(time0n-time0) );
    printf("Throughput: %ld messages/s\n", msgs*1000/(time2-time1) );
    printf("Opening time: %ld", time0n-time0);
    printf("Waiting time: %ld", time1-time0n);
    printf("Running time: %ld", time2-time1);
    printf("Overall: %ld messages/s\n", msgs*1000/(time2-time0) );
    
    for (k=0; k<nClients; k++) close(sock[k]);

    return 0;
}
    
