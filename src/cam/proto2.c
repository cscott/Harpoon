/*
 *
 * MIT Screen capture utility
 * This code is based on the landscape camera software 
 * for iPAQ H3600 Mercury Backpaq by HP/Compaq Research Labs
 *
 * Copyright 2001-2002 MIT LCS, HP/Compaq
 *
 * Use consistent with the GNU GPL is permitted, provided that this
 * copyright notice is preserved in its entirety in all copies and
 * derived works.
 *
 * NEITHER MIT NOR HEWLETT PACKARD CORPORATION MAKE ANY WARRANTIES,
 * EXPRESSED OR IMPLIED, AS TO THE USEFULNESS OR CORRECTNESS OF THIS
 * CODE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 *
 * Modified by: Eugene Weinstein & Brian Avery
 *              <ecoder@mit.edu>, <B.Avery@hp.com>
 *              10 Dec 2002
 * Original Author: Andrew Christian 
 *         <andrew.christian@compaq.com>
 *         4 June 2001 
 */

#ifdef HAVE_CONFIG_H
#  include <config.h>
#endif

#include <unistd.h>
#include <stdio.h>

#include <string.h>
#include <getopt.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <pthread.h>
#include <linux/types.h> 
#include <signal.h>
#include <gtk/gtk.h>
#include <errno.h>

#include "interface.h"
#include "support.h"
#include "main.h"
#include "save.h"
#include "inet.h"

#define FLOAT_SCALE_FACTOR   16
#define FLOAT_SCALE_VALUE   (1<<FLOAT_SCALE_FACTOR)

#define MAKE_FLOAT(x) ((int)((x) * FLOAT_SCALE_VALUE + 0.5))

// White balance parameters
#define GREEN_VALUE 1.289
#define BLUE_VALUE 1.170


// Windows present in the GTK interface
GtkWidget *topwindow;
GtkWidget *mainwindow;
GtkWidget *viewwindow;

gboolean gQuitFlag = FALSE;
gboolean gResetCounters = FALSE;
struct CaptureData gCamera;

int width_table[] = { 160, 320, 640 };
int height_table[] = { 120, 240, 480 };

// A bunch of booleans set by cmd line params
int send_images = 0;
int save_jpegs = 0;
int save_pgms = 0;
int save_ppms = 0;
int talk_to_galaudio = 0;
int listen_for_commands = 0;
int face_detect_t = 0; 

char *serv_host_addr;
int serv_tcp_port = 0;
char *dir;
char *id = NULL;
int galaudio_port = 0;
int listen_port = 0;


// Socket file descriptor
int sockfd = 0;

/***************************************/

/* Gamma table for gamma = 0.450000 */
unsigned char gamma_table[] = { 0, 
                                4,   9,  13,  18,  22,  26,  30,  33,  36,  40,  42,  45,  48,  50,  53,  55, 
                                57,  59,  61,  63,  65,  67,  69,  71,  73,  75,  76,  78,  80,  81,  83,  84, 
                                86,  87,  89,  90,  92,  93,  95,  96,  97,  99, 100, 101, 103, 104, 105, 106, 
                                108, 109, 110, 111, 112, 114, 115, 116, 117, 118, 119, 120, 121, 123, 124, 125, 
                                126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 
                                142, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 151, 152, 153, 154, 155, 
                                156, 156, 157, 158, 159, 160, 161, 161, 162, 163, 164, 165, 165, 166, 167, 168, 
                                169, 169, 170, 171, 172, 172, 173, 174, 175, 175, 176, 177, 178, 178, 179, 180, 
                                180, 181, 182, 183, 183, 184, 185, 185, 186, 187, 188, 188, 189, 190, 190, 191, 
                                192, 192, 193, 194, 194, 195, 196, 196, 197, 198, 198, 199, 200, 200, 201, 201, 
                                202, 203, 203, 204, 205, 205, 206, 207, 207, 208, 208, 209, 210, 210, 211, 211, 
                                212, 213, 213, 214, 214, 215, 216, 216, 217, 217, 218, 219, 219, 220, 220, 221, 
                                221, 222, 223, 223, 224, 224, 225, 225, 226, 227, 227, 228, 228, 229, 229, 230, 
                                231, 231, 232, 232, 233, 233, 234, 234, 235, 235, 236, 236, 237, 238, 238, 239, 
                                239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246, 246, 247, 
                                247, 248, 248, 249, 250, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255 };


/***************************************/

/***************************************/
/*      Camera Initialization          */
/***************************************/


#define CAMERA_IOCTL(TYPE,DATA) \
        do { int result; do {                                          \
		result = ioctl(camera->video_fd, TYPE, &camera->DATA); \
		if ( result && errno != ENODEV ) {                     \
                        fprintf(stderr, "%s:%d: ", __FILE__, __LINE__);\
			perror("ioctl: Unable to " #TYPE);             \
			exit(1);                                       \
		}                                                      \
	} while ( result ); } while (0)


/* Function: set_camera_info
   Input: CaptureData struct
   Returns: nothing

   What it does: Initialize various values inside the CaptureData
   struct, such as the dimensions, brightnes. Performs the IOCTLS to
   program these params into the camera.
*/
void set_camera_info( struct CaptureData *camera )
{
    camera->vpic.palette = VIDEO_PALETTE_RGB24;
    camera->vwin.width = width_table[camera->desired.capture_size];
    camera->vwin.height = height_table[camera->desired.capture_size];

    camera->vpic.brightness = camera->desired.brightness;
    camera->params.gain_format = camera->desired.gain_format;
	
    CAMERA_IOCTL( VIDIOCSPICT, vpic );
    CAMERA_IOCTL( VIDIOCSWIN, vwin );
    switch (camera->type.type) {
    case H3600_SMAL:
        /* Set up the parameters */
        if ( ioctl(camera->video_fd, H3600CAM_G_PARAMS, &camera->params )) {
            perror("Unable to read parameters");
            return;
        }

        camera->params.clock_divisor = 1280 / camera->desired.fps;
        camera->params.power_setting = camera->desired.power;
        camera->params.power_mgmt = camera->desired.power_mgmt;
        camera->params.special_modes = camera->desired.special_modes;
        camera->params.flip = camera->desired.flip;
        CAMERA_IOCTL( H3600CAM_S_PARAMS, params );
        break;
    case H3600_PHILIPS:
        /* Set up the parameters */
        if ( ioctl(camera->video_fd, H3600CAM_PH_G_PARAMS, &camera->philips )) {
            perror("Unable to read parameters");
            return;
        }
        // since we have the e_shut in 2 places tied to brightness and
        // here, we need to set this or we rewrite the brightness back to the original value
        // every time !!!!
        // rigthshifted to match the range of the electronic shutter
        camera->philips.electronic_shutter = camera->vpic.brightness >> 8; 
        camera->philips.clock_divisor = 45 / camera->desired.fps;
        camera->philips.flip = camera->desired.flip;
        CAMERA_IOCTL( H3600CAM_PH_S_PARAMS, philips);

        // new we need to set the variable philips gain
        CAMERA_IOCTL( H3600CAM_PH_SET_PGA, params.gain_format);
        break;
    }
}


/* Function: get_camera_info
   Input: CaptureData struct
   Returns: nothing

   What it does: Fetches various setting values from the camera using
   IOCTLs and stuffs them in the "params" and "philips" globals.
*/
void get_camera_info( struct CaptureData *camera )
{
    CAMERA_IOCTL( H3600CAM_G_TYPE, type );
    CAMERA_IOCTL( VIDIOCGWIN, vwin );
    CAMERA_IOCTL( VIDIOCGPICT, vpic );
    switch (camera->type.type) {
    case H3600_SMAL:
        CAMERA_IOCTL( H3600CAM_G_PARAMS, params );
        break;
    case H3600_PHILIPS:
        CAMERA_IOCTL( H3600CAM_G_PARAMS, philips );
        break;
    }
}

/* Function: setup_memory_mapping
   Input: CamptureData struct
   Returns: integer return value

   What it does: Not quite sure what this does :)
*/
int setup_memory_mapping( struct CaptureData *camera )
{
    int retval = 0;

    if ( (retval = ioctl(camera->video_fd,VIDIOCGMBUF,&camera->vmbuf)) < 0 ){
        perror("failed VIDIOCGMBUF\n");
        return -1;
    }  

    camera->videoBuffer = (unsigned char *) mmap(0, camera->vmbuf.size, 
                                                 PROT_READ|PROT_WRITE, 
                                                 MAP_SHARED, camera->video_fd, 0);
    if ( camera->videoBuffer == MAP_FAILED ) {
        perror("unable to map memory\n");
        return -1;
    }

    camera->vmap[0].frame = 0;
    camera->vmap[0].format = camera->vpic.palette;
    camera->vmap[0].width  = camera->vwin.width;
    camera->vmap[0].height = camera->vwin.height;

    camera->vmap[1].frame = 1;
    camera->vmap[1].format = camera->vpic.palette;
    camera->vmap[1].width  = camera->vwin.width;
    camera->vmap[1].height = camera->vwin.height;
    return 0;
}


/***************************************/
/*      GTK widget manipulations       */
/***************************************/

/* The following on_*_changed functions just make some adjustments
   when the appropriate items in the GTK interface are modified:
   brightness, frames per second, gain 
*/

void on_brightness_value_changed(GtkAdjustment *adj, struct CaptureData *camera )
{
    pthread_mutex_lock(&camera->desired.lock);
    camera->desired.brightness = ((unsigned short)(adj->value))*128;
    camera->desired.dirty = TRUE;
    pthread_mutex_unlock(&camera->desired.lock);
}

void on_fps_value_changed(GtkAdjustment *adj, struct CaptureData *camera )
{
    pthread_mutex_lock(&camera->desired.lock);
    camera->desired.fps = adj->value;
    camera->desired.dirty = TRUE;
    gResetCounters = TRUE;
    pthread_mutex_unlock(&camera->desired.lock);
}

// for the philips hscale control, small uses radio buttons
void on_gain_value_changed(GtkAdjustment *adj, struct CaptureData *camera )
{
    pthread_mutex_lock(&camera->desired.lock);
    camera->desired.gain_format = adj->value;
    camera->desired.dirty = TRUE;
    gResetCounters = TRUE;	

	
    pthread_mutex_unlock(&camera->desired.lock);
}

#define FIND_AN_ADJUSTMENT(x,y,z) \
        hscale = lookup_widget( mainwindow, x ); \
        g_assert(hscale); \
        camera->y = gtk_range_get_adjustment(GTK_RANGE(hscale)); \
	gtk_signal_connect (GTK_OBJECT (camera->y), "value-changed", \
			    GTK_SIGNAL_FUNC (z),  camera)


/* Function: display_on_statusbar
   Input: CaptureData struct
   Returns: nothing

   What it does: Displays a string on the "status2" statusbar. This is
   usually the status of a face ID request
*/
void display_on_statusbar( struct CaptureData *camera, char *str) 
{ 
    // The statusbar must be already initialized.

    // The statusbar is a stack, so we need to first pop the old value
    // and then push the new one.
    gtk_statusbar_pop( GTK_STATUSBAR(camera->status2), camera->context_id2 );
    gtk_statusbar_push( GTK_STATUSBAR(camera->status2), camera->context_id2, str);
}




/***************************************/
/*      GTK initialization             */
/***************************************/


/* Function: init_capture_widgets
   Input: CaptureData struct
   Returns: nothing

   What it does: Initialize various values inside the GTK widgets
*/

void init_capture_widgets( struct CaptureData *camera )
{
    GtkWidget *hscale;

    /* Get the brightness adjustment */

    FIND_AN_ADJUSTMENT("hscale_brightness",brightness_adj,on_brightness_value_changed);
    FIND_AN_ADJUSTMENT("hscale_fps",fps_adj,on_fps_value_changed);
    // only for philips camera
    if (camera->type.type == H3600_PHILIPS)
        FIND_AN_ADJUSTMENT("hscale_gain",gain_adj,on_gain_value_changed);
    gtk_adjustment_set_value(camera->brightness_adj, camera->vpic.brightness / 128);
    switch ( camera->type.type ) {
    case H3600_SMAL:
        gtk_adjustment_set_value(camera->fps_adj, 1280 / camera->params.clock_divisor);
        break;
    case H3600_PHILIPS:
        gtk_adjustment_set_value(camera->fps_adj, 45 / camera->philips.clock_divisor);
        break;
    }

    camera->status = lookup_widget( mainwindow, "statusbar" );
    camera->status2 = lookup_widget( mainwindow, "statusbar2" );

    g_assert(camera->status);
    camera->context_id = gtk_statusbar_get_context_id( GTK_STATUSBAR(camera->status), "main" );
    camera->context_id2 = gtk_statusbar_get_context_id( GTK_STATUSBAR(camera->status), "main2" );
    gtk_statusbar_push( GTK_STATUSBAR(camera->status), camera->context_id, "Iniitalizing");
}

/***************************************/
/*      Image processing               */
/***************************************/

static inline unsigned char clipit( int value ) {
    if ( value < 0 ) return 0;
    if ( value > 255 ) return 255;
    return value;
}


/* Function: white_balance
   Input: An array of RGB pixel values, and green and blue multipliers
   Returns: nothing

   What it does: Adjust the RGB values to reflect the desired white
   balance
*/
void white_balance( char *arr, int len, 
		    double green_value, double blue_value )
{
    int i;
    int green = MAKE_FLOAT(green_value);
    int blue = MAKE_FLOAT(blue_value);

    for ( i = 0 ; i < len ; i+=3 ) {
        arr++; // Don't do anything to the red
            
        *arr = clipit((green * (*arr)) >> FLOAT_SCALE_FACTOR);
        arr++;
        *arr = clipit((blue * (*arr)) >> FLOAT_SCALE_FACTOR);
        arr++;
    }
}



#define FLOAT_SCALE_FACTOR   16
#define FLOAT_SCALE_VALUE   (1<<FLOAT_SCALE_FACTOR)
#define MAKE_FLOAT(x) ((int)((x) * FLOAT_SCALE_VALUE + 0.5))

#define A11 MAKE_FLOAT(1.28033)
#define A12 MAKE_FLOAT(-0.245018)
#define A13 MAKE_FLOAT(-0.0570431)
#define A21 MAKE_FLOAT(-0.119674)
#define A22 MAKE_FLOAT(1.21658)
#define A23 A13
#define A31 A21
#define A32 A12
#define A33 MAKE_FLOAT(1.69296)



/* Function: color_correct
   Input: An array of RGB pixel values
   Returns: nothing

   What it does: Adjust the RGB values to reflect the color
   correction. The RGB values for each pixel are multiplied by the
   appropriate value in the gamma table.
*/
void color_correct( char * dest, unsigned char *source, int len )
{
    int i;
    int red, green, blue;

    for ( i = 0 ; i < len ; i+=3 ) {
        red   = gamma_table[*source++];
        green = gamma_table[*source++];
        blue  = gamma_table[*source++];
        *dest++ = clipit( (A11 * red + A12 * green + A13 * blue) >> FLOAT_SCALE_FACTOR);
        *dest++ = clipit( (A21 * red + A22 * green + A23 * blue) >> FLOAT_SCALE_FACTOR);
        *dest++ = clipit( (A31 * red + A32 * green + A33 * blue) >> FLOAT_SCALE_FACTOR);
    }
}





/***************************************/
/*      Open and close the camera      */
/***************************************/

void open_camera( struct CaptureData *camera )
{
    camera->video_fd = open( "/dev/v4l/video0", O_RDWR);
    if ( camera->video_fd < 0 ) {
        perror("Unable to open /dev/v4l/video0");
        exit(1);
    }

    if ( ioctl(camera->video_fd, H3600CAM_RESET, NULL ) != 0 ) {
        perror("ioctl H3600CAM_RESET");
        exit(1);
    }
    if ( ioctl(camera->video_fd, VIDIOCGCAP, &camera->vcap) != 0 ) {
        perror("ioctl VIDIOCGCAP");
        exit(1);
    }

    get_camera_info( camera );
    camera->desired.brightness = 400 * 128;
    camera->desired.fps = 10;
    camera->desired.power = camera->params.power_setting;
    camera->desired.gain_format = camera->params.gain_format;
    camera->desired.power_mgmt = camera->params.power_mgmt;
    camera->desired.special_modes = camera->params.special_modes;
    camera->desired.flip = camera->params.flip;
    camera->desired.dirty = TRUE;
}

void close_camera( struct CaptureData *camera )
{
    if ( camera->video_fd > 0 ) {
        close(camera->video_fd);
        camera->video_fd = 0;
    }
}


/***************************************/
/*      Display thread                 */
/***************************************/


/* Function: grab_image
   Input: CaptureData struct
   Returns: nothing

   What it does: Takes an image from the camera using an ioctl
*/
void grab_image( struct CaptureData *camera )
{
    /* Move preferences over */
    pthread_mutex_lock( &camera->desired.lock );
    while (camera->desired.pause) 
        pthread_cond_wait( &camera->desired.cond, &camera->desired.lock );

    if (camera->desired.dirty){
        set_camera_info(camera);
        get_camera_info(camera);
        camera->desired.dirty = FALSE;
    }

    camera->vmap[camera->grab_frame].width = camera->vwin.width;
    camera->vmap[camera->grab_frame].height = camera->vwin.height;
    camera->vmap[camera->grab_frame].format = camera->vpic.palette;
    pthread_mutex_unlock( &camera->desired.lock );

    while ( ioctl(camera->video_fd, 
                  VIDIOCMCAPTURE, 
                  &camera->vmap[camera->grab_frame]) != 0 ) {
        usleep( 250000 );
    }
}




/* Function: process_images
   Input: CaptureData struct
   Returns: nothing

   What it does: Process an image from the camera. Do some color
   correction and white balancing, and if required, make the call out
   to save the image to a file or send it out to a Face ID server.  
*/
void process_image( struct CaptureData *camera )
{
    unsigned char *data;
    struct ImageData *image = &(camera->image);
    char recvline[MAXLINE];
    char output_string[MAXLINE];      
    char login_str[MAXLINE];
    
    char* ptr;
    int nwritten;
    int num_faces;

    gboolean snapshot = FALSE;
    gboolean login = FALSE;
    pthread_mutex_lock(&image->lock);

    /* Copy the parameters and data used in the capture */
    memcpy( &image->vmap, &camera->vmap[camera->grab_frame], sizeof(image->vmap));
    data = camera->videoBuffer + camera->vmbuf.offsets[camera->grab_frame];
    color_correct( image->data, data, image->vmap.width * image->vmap.height * 3 );
    if (camera->type.type == H3600_SMAL) {
        // correct for the abundance of red in the SMAL cameras
        white_balance( image->data, image->vmap.width * image->vmap.height * 3, GREEN_VALUE, BLUE_VALUE);
    }

    pthread_mutex_unlock(&image->lock);

    /* Release this frame */
    CAMERA_IOCTL( VIDIOCSYNC, grab_frame );
    camera->grab_frame = 1 - camera->grab_frame;

    /* If this was a snapshot, set the camera to pause */
    pthread_mutex_lock( &camera->desired.lock );
    if ( camera->desired.runstate == ERS_SNAPSHOT ) {
        camera->desired.runstate = ERS_NORMAL;
        snapshot = TRUE;
    }

    /* If this was a login, send info to server */
    if ( camera->login_button_clicked == TRUE ) { 
        camera->login_button_clicked = FALSE;
        login = TRUE;
    }
    pthread_mutex_unlock( &camera->desired.lock );


    // If we are taking snapshots (either for saving to files of for
    // writing out to a Face ID server), handle that here.
    if ( snapshot ) {
        if (camera->send_back_image == FALSE) {
            display_on_statusbar(camera, "Snapshotting");
        }
        pthread_mutex_lock( &camera->desired.lock );
        
        // Either send or save the image here
        save_and_send_images(image, send_images, save_ppms,
                             save_pgms, dir, serv_host_addr, 
                             serv_tcp_port, recvline, camera->name,
                             id, camera->send_back_image);
        pthread_mutex_unlock( &camera->desired.lock );

        // Get the response from the Face ID server
        camera->response = recvline;
        camera->response_dirty = FALSE;

        /* Figure out where the reply string ends and the metadata
           begins. The metadata starts with a <meta> tag, so we look for
           the '<meta>' */

        strcpy(output_string,recvline);
        if ((ptr = strstr(output_string,"<meta>"))) {
            *ptr = '\0';
        }

        if (camera->send_back_image == FALSE) {
            // We don't need to update the status bar if we're only
            // sending to an application, i.e. the request to take the
            // picture came from a third party.
            
            display_on_statusbar(camera, output_string);
        }


    }
    
    if (login == TRUE) {
        // If the login button was pressed, notify the Face ID server.

        printf("Sending login button command\n");

        sprintf (login_str, "login_received ID: %s\n", id);
        printf ("Sending %s", login_str);
        send_str(login_str, "", 0, serv_host_addr, serv_tcp_port, recvline);
    }


}

/* Function: display_image
   Input: CaptureData struct
   Returns: nothing

   What it does: Takes an image read from the camera and draws it on
   the screen using the drawing_area GTK widget.
*/
void display_image( struct CaptureData *camera )
{
    struct DisplayArea *area = &(camera->displayarea[camera->current_display]);
    GdkRectangle update_rec;

    update_rec.x = 0;
    update_rec.y = 0;
    update_rec.width = area->drawing_area->allocation.width;
    update_rec.height = area->drawing_area->allocation.height;
      
    gdk_threads_enter();
    if ( !gQuitFlag ) {
        area->dirty = TRUE;
        gtk_widget_draw( area->drawing_area, &update_rec );
    }
    gdk_threads_leave();
}


/* Function: fix_statusbar
   Input: CaptureData struct
   Returns: nothing

   What it does: Calculate current and average frame rate, and display
   it on the (first) statusbar.
*/
void fix_statusbar( struct CaptureData *camera )
{
    gdouble thetime;
    static gdouble oldtime = 0.0;
    static int frames_displayed = 0;
    char tbuff[1024]; 

    frames_displayed += 1;
    thetime = g_timer_elapsed( camera->timer, NULL );
    if ( thetime != 0.0 && thetime > oldtime ) {
        sprintf( tbuff, "Speed (fps) - Average: %6.2f  Current: %6.2f",
                 frames_displayed / thetime, 1.0 / (thetime - oldtime));
    }
    else {
        sprintf( tbuff, "Speed (fps) - Unknown!" );
    }
    oldtime = thetime;

    gdk_threads_enter();
    if ( !gQuitFlag ) {
        gtk_statusbar_pop( GTK_STATUSBAR(camera->status), camera->context_id );
        gtk_statusbar_push( GTK_STATUSBAR(camera->status), camera->context_id, tbuff );

        if ( gResetCounters ) {   /* Place this insed the gdk_threads_enter() section */
            frames_displayed = 0;
            oldtime = 0.0;
            g_timer_reset( camera->timer );
            gResetCounters = FALSE;
        }
    }
    gdk_threads_leave();
}


/* Function: draw_image
   Input: nothing
   Returns: nothing

   What it does: This is the main function for the image processing
   thread. It basically calls four functions that read and do various
   things with an image in an endless loop.
*/
void *draw_image( void *param )
{
    while( !gQuitFlag ) {
        grab_image( &gCamera );
        process_image( &gCamera );
        display_image( &gCamera  );
        fix_statusbar( &gCamera );
    }
    return NULL;
}

/***************************************/
/*      Socket  thread                 */
/***************************************/

/* We have two sockets that we are controlling, one for communicating
   with the FD/FR server on a bidirectional control socket (this is
   not really being used right now), and one for reading control
   commands from a back-end application.
   
   Note that there is another piece of socket code inside
   save_and_send_image to communicate the picture to the FD/FR server
   when a snapshot is triggered. The code that appears here only runs
   in the socket thread.
*/


/* Function: init_socket
   Input: ip address, port number, and a name for the identification string
   Returns: a file handle to the socket

   What it does: Initializes a socket to the specified machine/port
   number. Return the file descriptor for the socket.
*/
int init_socket ( char *ip_addr, int port_num, char *id ) {
    int sockfd;
    char headers[MAXLINE];

    int nwritten;
    sockfd = init_connection (ip_addr, port_num);

    sprintf(headers,"Camera ID: %s\n",id);
    nwritten = send_str1(sockfd, headers);

    return sockfd;
}
    
/* Function: init_socket
   Input: port number
   Returns: a file handle to the socket

   What it does: Initializes a socket for reading input from third
   parties (mostly for login button requests)
*/
int init_read_socket ( int port_num ) {
    int sockfd;

    sockfd = init_read_connection (port_num);

    return sockfd;
}


/* chomp a string */
void chomp (char *str) { 
    int i;

    i = 0;
    while (str[i] != '\0') {
        ++i;
    }

    if (str[i-1] == '\n') {
        str[i-1] = '\0';
    }
}
    

/* Function: poll_socket
   Input: CamptureData struct
   Returns: nothing

   What it does: This is the main function for the socket polling
   thread thread. It opens both incoming and outgoing sockets, for
   receiving commands and writing out images on persistent sockets to
   third-parties, respectively. 
*/
void * poll_socket( void *arg )
{
    struct CaptureData *camera = (struct CaptureData*) arg;
    char recvline[MAXLINE];
    int read_sockfd;
    int nwritten;
    int len;
    char *temp;
    
    while( !gQuitFlag ) {
        read_sockfd = read_socket(camera->listen_socket, recvline);
        if (read_sockfd < 0) {
            // we had a read error, don't bother processing
            continue;
        }
        if (strstr(recvline, "Snapshot")) {
            camera->response_dirty = TRUE;

            if (strstr(recvline, "wait_button")) {
                printf ("Setting up for snapshot: waiting for button\n");
            } else {
                printf ("Setting up for snapshot\n");
                camera->desired.runstate = ERS_SNAPSHOT;  
            }
            if (strstr(recvline, "send_image")) {
                // We need to send back the image on the socket
                camera->send_back_image = TRUE;
            } else {
                // We need to send back the result on the socket
                camera->send_back_image = FALSE;
            }

            // We set up for snapshot here, then wait for the response to come
            // back so that we can send it the commanding application
            
            while (camera->response_dirty == TRUE) {
            }

            gdk_threads_enter();

            // got a new response, so write the response back to the
            // application
            nwritten = writen(read_sockfd, camera->response, 
                              strlen(camera->response));

            // got a new response, so write the image back to the
            // application
            if (camera->send_back_image == TRUE) {
                printf ("Sending back image\n");
                pthread_mutex_lock(&camera->image.lock);
                len = camera->image.vmap.width * camera->image.vmap.height * 3;
                // got a new response, so write back to the application
                nwritten = writen(read_sockfd, camera->image.data, 
                                  len);
                pthread_mutex_unlock(&camera->image.lock);
            }
            gdk_threads_leave();
            
        } else if ((temp = strstr(recvline, "Change_status"))) {
            // Change the statusbar to whatever the third party said
            // to change it to.
            temp += strlen("Change_status");

            // strip out spaces
            while (*temp == ' ') {
                temp++;
            }
            
            chomp(temp);
            printf ("Changing status to %s\n",temp);
            display_on_statusbar(camera, temp);
        } else if ((temp = strstr(recvline, "Wait_for_login"))) {
            // Got a command to wait for the login button to be
            // pressed from a third-party app.

            // This crashes if the login button requestor goes away --
            // fix this later.
            printf("waiting for login button\n");

            while (camera->login_button_clicked == FALSE) {
            }

            printf("login button pressed\n");
            nwritten = writen(read_sockfd, "Login button pressed\n", 
                              strlen("Login button pressed\n"));

            camera->login_button_clicked = FALSE; // Reset it
            
        }

        close (read_sockfd);
        
        // reset this for future use
        camera->send_back_image = FALSE;

            
    }
    
    return NULL;

}




static void
print_usage()
{
    printf ("Usage: landcam [options]\n\n");
    printf ("General options\n");
    printf ("  -send_images                Send images over network\n");
    printf ("  -server <machine name>      Server machine name or IP address\n");
    printf ("  -port_num <number>          Port number\n");
    printf ("  -save_ppms                  Save ppms\n");
    printf ("  -save_pgms                  Save pgms\n");
    printf ("  -dir <directory>            Directory to save files to; defaults to $HOME\n");
    printf ("  -id <string>                ID for face recognition domain\n");
    printf ("  -listen_port                Port to listen for snapshot requests on\n");
    printf ("  -face_detect                Run face detection algorithms on received images\n");
}
    


int main (int argc, char *argv[])
{
    int i;
    char *key;
    char *pEnd;
    pthread_t draw_thread;
    pthread_t socket_thread;
    char *server_name;
    struct hostent *hptr;

    char str[INET_ADDRSTRLEN];

    dir = getenv("HOME");

    if (argc>1) {
        if (strstr(argv[1], "-h")) {
            print_usage();
            exit(1);
        } else {
            for (i=1;i<argc;i++) { 
                key = argv[i];
                if (strcmp(key, "-send_images") == 0) {
                    send_images = 1;
                }
                else if (strcmp(key, "-server") == 0) {
                    server_name = strdup(argv[++i]);
                } else if (strcmp(key, "-port_num") == 0) {
                    serv_tcp_port = (int) strtol(argv[++i],&pEnd,0);
                } else if (strcmp(key, "-galaudio_port") == 0) {
                    galaudio_port = (int) strtol(argv[++i],&pEnd,0);

                    talk_to_galaudio = 1;
                } else if (strcmp(key, "-listen_port") == 0) {
                    listen_port = (int) strtol(argv[++i],&pEnd,0);

                    listen_for_commands = 1;
                } else if (strcmp(key, "-save_ppms") == 0) {
                    save_ppms = 1;
                } else if (strcmp(key, "-save_pgms") == 0) {
                    save_pgms = 1;
                } else if (strcmp(key, "-save_pgms") == 0) {
                    save_pgms = 1;
                } else if (strcmp(key, "-face_detect") == 0) {
                    face_detect_t = 1;
                } else if (strcmp(key, "-dir") == 0) {
                    dir = strdup(argv[++i]);
                } else if (strcmp(key, "-id") == 0) {
                    id = strdup(argv[++i]);
                } else {
                    printf ("Error: unknown option: %s\n",key);
                    print_usage();
                    exit(1);
                }
            }
        }
        if ((send_images == 1) && ((server_name == NULL) ||
                                   (serv_tcp_port == 0))) {
            printf ("Error: you must supply a server machine and port number\n"
                    "in order to send images to a server\n");
            print_usage();
            exit(1);
        }
                    
    }

    if (id) {
        printf("FR domain ID is %s\n",id);
    } else {
        printf("FR domain ID not specified, setting to \"test\"\n");
        id = "test";
    }


    if (send_images == 1) {
        // Do the IP address conversion for the server
        
        if ( (hptr = gethostbyname(server_name)) == NULL) {
            printf("gethostbyname error for host: %s: %s",
                   server_name, hstrerror(h_errno));
            exit(1);
        }
        
        
        // Yes, we're ignoring IPV6 here, but do we really need that?
        serv_host_addr = hptr->h_addr_list[0];
        
        printf("\taddress: %s\n",
               inet_ntop(hptr->h_addrtype, serv_host_addr, str, sizeof(str)));
        
        printf ("send_images: %d, server: %s, port: %d\n",send_images, str, serv_tcp_port);

        sockfd = init_socket(serv_host_addr, serv_tcp_port, id);
        

    }

    g_thread_init(NULL);
    gtk_set_locale ();
    gtk_init (&argc, &argv);
    gdk_rgb_init();

    // Initialize the storage structures
    memset(&gCamera,0,sizeof(struct CaptureData));
    gCamera.current_display = SMALL_IMAGE;
    pthread_mutex_init(&gCamera.desired.lock, NULL );
    pthread_cond_init(&gCamera.desired.cond, NULL );
    pthread_mutex_init(&gCamera.image.lock, NULL );
    
    if (listen_for_commands == 1) {
        printf("Listening for snapshot commands on port %d\n",listen_port);
        gCamera.listen_socket = init_read_socket(listen_port);
    }

    if (talk_to_galaudio == 1) {
        printf("sending to galaxy on port %d\n",galaudio_port);
    }

//    load_saved_images();

    open_camera( &gCamera );
    set_camera_info( &gCamera );
    get_camera_info( &gCamera );

    topwindow = create_topwindow ();
    if (gCamera.type.type == H3600_SMAL) {
        if (send_images == 1) {
            mainwindow = create_mainwindowS();
        } else {
            mainwindow = create_mainwindowS_nosend();
        }
    } else if (gCamera.type.type == H3600_PHILIPS) {
        if (send_images == 1) {
            mainwindow = create_mainwindowP();
        } else {
            mainwindow = create_mainwindowP_nosend();
        }
    } else{
        fprintf(stderr,"Unknown camera type = %d.  Make sure you've called get_camera_info already.\n",
                gCamera.type.type);
        exit(-1);
    }
	
	    
    gCamera.displayarea[LARGE_IMAGE].drawing_area = lookup_widget( topwindow, "drawingarea" );
    gCamera.displayarea[SMALL_IMAGE].drawing_area = lookup_widget( mainwindow, "drawingarea" );
    g_assert( gCamera.displayarea[LARGE_IMAGE].drawing_area 
              && gCamera.displayarea[SMALL_IMAGE].drawing_area );

    init_capture_widgets(&gCamera);
    gtk_widget_show (mainwindow);
	
    if ( setup_memory_mapping( &gCamera ))
        exit(1);


    // Switch flip and adjust the gain on starup for now
    gCamera.desired.flip = !(gCamera.desired.flip);
    gCamera.timer = g_timer_new();
    g_timer_start( gCamera.timer );

    
    // Initialize some state variables
    gCamera.send_back_image = FALSE;
    gCamera.login_button_clicked = FALSE;
    
    
    pthread_create(&draw_thread, NULL, draw_image, NULL);

    if (listen_for_commands) {
        pthread_create(&socket_thread, NULL, poll_socket, &gCamera);
    }

    gdk_threads_enter();
    gtk_main ();
    gdk_threads_leave();

    gQuitFlag = TRUE;
    pthread_join( draw_thread, NULL );

    close_camera( &gCamera );
	
    for ( i = 0 ; i < 2 ; i++ ) {
        if ( gCamera.displayarea[i].pixmap ) {
            gdk_pixmap_unref(gCamera.displayarea[i].pixmap);
            gCamera.displayarea[i].pixmap = NULL;
        }
    }

    g_timer_stop( gCamera.timer );
    g_timer_destroy(gCamera.timer);

    pthread_mutex_destroy( &gCamera.desired.lock );
    pthread_cond_destroy( &gCamera.desired.cond );
    pthread_mutex_destroy( &gCamera.image.lock );

    return 0;
}

