/*
 * Command line test software for the iPAQ H3600 Backpaq camera
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
#include <asm/arch/h3600_backpaq_camera.h>

#define DEBUG_STRING(x)  fprintf(stderr, "%s(%d): %s\n", __FILE__, __LINE__, x); fflush(stdout)
#define VIDEO_DEVICE "/dev/v4l/video0"

#define FHEIGHT 482
#define FWIDTH  644
#define FSIZE   ((unsigned long)FHEIGHT * (unsigned long)FWIDTH)

struct video_capability vcap;
struct video_channel    vchn;
struct video_picture    vpic;
struct video_window     vwin;
struct h3600_backpaq_camera_params  params;
struct h3600_backpaq_camera_type    ctype;
struct h3600_backpaq_camera_philips philips;

#define DESIRED_RAW  0
#define DESIRED_GREY 1
#define DESIRED_RGB  2
#define DESIRED_YUV  3
#define DESIRED_MODE_COUNT 4
char * desired_mode_name[] = { "raw", "grey", "rgb", "yuv" };

#define DESIRED_VGA  0
#define DESIRED_CIF  1
#define DESIRED_QVGA 2
#define DESIRED_QCIF 3
#define DESIRED_MIN  4
#define DESIRED_SIZE_COUNT 5
char * desired_size_name[] = { "vga", "cif", "qvga", "qcif", "min" };
int width_table[] = { 640, 352, 320, 176, 160 };
int height_table[] = { 480, 288, 240, 144, 120 };

int desired_to_palette[] = {
	VIDEO_PALETTE_RAW,
	VIDEO_PALETTE_GREY,
	VIDEO_PALETTE_RGB24,
	VIDEO_PALETTE_YUV422
};


int desired_size = 0;
int desired_mode  = DESIRED_GREY;
int desired_file = 1;
int desired_brightness = 128;
int desired_contrast = 128;
int desired_fps   = 5;
int desired_power = 12;
int desired_gain = 0;
int desired_poll = 0;
int desired_flip = 0;

int setup_camera( int fd )
{
	if ( ioctl(fd, VIDIOCGCAP, &vcap )) {
		perror("Unable to read capabilities");
		return -1;
	}

	if ( ioctl(fd, VIDIOCGPICT, &vpic )) {
		perror("Unable to read picture information");
		return -1;
	}

	/* Fix picture information */
	vpic.palette = desired_to_palette[desired_mode];
	vpic.brightness = ((unsigned int)desired_brightness) * 256;
	vpic.contrast = (unsigned int)desired_contrast * 256;

	if ( ioctl(fd, VIDIOCSPICT, &vpic )) {
		perror("Unable to set picture information");
		return -1;
	}

	if ( ioctl(fd, VIDIOCGPICT, &vpic )) {
		perror("Unable to read picture information");
		return -1;
	}

	if ( ioctl(fd, VIDIOCGWIN, &vwin )) {
		perror("Unable to read video capture window information");
		return -1;
	}

	/* Fix the capture window stuff */
	if ( desired_mode != DESIRED_RAW ) {
		vwin.width = width_table[desired_size];
		vwin.height = height_table[desired_size];

		if ( ioctl(fd, VIDIOCSWIN, &vwin )) {
			perror("Unable to set video capture window information");
			return -1;
		}

		if ( ioctl(fd, VIDIOCGWIN, &vwin )) {
			perror("Unable to read video capture window information");
			return -1;
		}
	}

	/* Read the camera type */
	if ( ioctl(fd, H3600CAM_G_TYPE, &ctype)) {
		perror("Unable to read camera type");
		return -1;
	}
	
	switch ( ctype.type ) {
	case H3600_SMAL:
		fprintf(stderr, "SMaL Camera\n");

		/* Set up the parameters */
		if ( ioctl(fd, H3600CAM_G_PARAMS, &params )) {
			perror("Unable to read parameters");
			return -1;
		}

		params.clock_divisor = 1280 / desired_fps;
		params.power_setting = desired_power;
		params.gain_format = desired_gain;
		params.read_polling_mode = desired_poll;
		params.flip = desired_flip;

		if ( ioctl(fd, H3600CAM_S_PARAMS, &params )) {
			perror("Unable to set parameters");
			return -1;
		}
		if ( ioctl(fd, H3600CAM_G_PARAMS, &params )) {
			perror("Unable to read parameters");
			return -1;
		}
		break;

	case H3600_PHILIPS:
		fprintf(stderr, "Philips Camera\n");
		/* Set up the parameters */
		if ( ioctl(fd, H3600CAM_PH_G_PARAMS, &philips )) {
			perror("Unable to read parameters");
			return -1;
		}

		philips.clock_divisor     = 45 / desired_fps;
		philips.read_polling_mode = desired_poll;
		philips.flip              = desired_flip;

		if ( ioctl(fd, H3600CAM_PH_S_PARAMS, &philips )) {
			perror("Unable to set parameters");
			return -1;
		}
		if ( ioctl(fd, H3600CAM_PH_G_PARAMS, &philips )) {
			perror("Unable to read parameters");
			return -1;
		}
		break;
	}


	fprintf(stderr,"Video capabilities of %s\n", vcap.name);
	fprintf(stderr,"Depth %3d    Palette %3d\n", vpic.depth, vpic.palette);
	fprintf(stderr,"Width %3d    Height  %3d\n", vwin.width, vwin.height);
	switch ( ctype.type ) {
	case H3600_SMAL:
		fprintf(stderr,"Power %3d    Gain    %3d\n", params.power_setting, params.gain_format);
		fprintf(stderr,"Bright 0x%04x  Divisor 0x%04x\n", vpic.brightness, params.clock_divisor);
		fprintf(stderr,"Flip  %3d\n", params.flip);
		break;
	case H3600_PHILIPS:
		fprintf(stderr,"Bright 0x%04x  Divisor 0x%04x\n", vpic.brightness, philips.clock_divisor);
		fprintf(stderr,"Flip  %3d\n", philips.flip);
		break;
	}
	fflush(stderr);
	return ( vwin.height * vwin.width * vpic.depth / 8 );
}

void usage(char *name)
{
	fprintf(stderr,"Usage: %s [opts] [filename]\n\n", name);
	fprintf(stderr,"Options\n");
	fprintf(stderr,"  -s val    Size: vga, cif, qvga, qcif, or min\n");
	fprintf(stderr,"  -m mode   Mode: rgb, raw, yuv, or grey\n");
	fprintf(stderr,"  -d        Debug: no output\n");
	fprintf(stderr,"  -b level  Brightness [0 to 255]\n");
	fprintf(stderr,"  -c contrast Contrast [0 to 255]\n");
	fprintf(stderr,"  -f fps    Frames per second [1 to 100]\n");
	fprintf(stderr,"  -i        Invert image\n");
	fprintf(stderr,"  -p power  Power level [0 to 15]\n");
	fprintf(stderr,"  -n        No interrupts; running in polling mode\n");
	fprintf(stderr,"  -g gain   Gain [0 to 4]\n");
	fprintf(stderr,"  -h        Help\n");
	exit(1);
}

char * get_opts( int argc, char **argv )
{
	while ( 1 ) {
		int c;

		c = getopt( argc, argv, "s:m:db:c:f:g:p:nih");
		switch (c) {
		case 's': {
			int i;
			for ( i = 0 ; i < DESIRED_SIZE_COUNT ; i++ ) {
				if ( !strcasecmp(optarg,desired_size_name[i])) {
					desired_size = i;
					break;
				}
			}
			if ( i == DESIRED_SIZE_COUNT ) {
				fprintf(stderr,"Unrecognized size %s\n", optarg);
				usage(*argv);
			}
			break;
		}
		case 'm': {
			int i;
			for ( i = 0 ; i < DESIRED_MODE_COUNT ; i++ ) {
				if ( !strcasecmp(optarg,desired_mode_name[i])) {
					desired_mode = i;
					break;
				}
			}
			if ( i == DESIRED_MODE_COUNT ) {
				fprintf(stderr,"Unrecognized mode %s\n", optarg);
				usage(*argv);
			}
			break;
		}
		case 'i':
			desired_flip = 1;
			break;
		case 'd':
			desired_file = 0;
			break;
		case 'b':
			desired_brightness = atoi(optarg);
			if ( desired_brightness > 255 )
				usage(*argv);
			break;
		case 'c':
			desired_contrast = atoi(optarg);
			if ( desired_contrast > 255 )
				usage(*argv);
			break;
		case 'f': {
			desired_fps = atoi(optarg);
			if ( desired_fps < 1 || desired_fps > 100 )
				usage(*argv);
			break;
		}
		case 'p':
			desired_power = atoi(optarg);
			if ( desired_power < 0 || desired_power > 15 )
				usage(*argv);
			break;
		case 'g':
			desired_gain = atoi(optarg);
			if ( desired_gain < 0 || desired_gain > 4 )
				usage(*argv);
			break;
		case 'n':
			desired_poll = 1;
			break;
		case 'h':
			usage(*argv);
			break;
		case EOF:
			return ( optind < argc ? argv[optind] : NULL );
		default:
			fprintf(stderr,"Hit default\n");
			usage(*argv);
			break;
		}
	}
	return NULL;
}


#define FLOAT_SCALE_FACTOR   16
#define FLOAT_SCALE_VALUE   (1<<FLOAT_SCALE_FACTOR)
#define MAKE_FLOAT(x) ((int)(x * FLOAT_SCALE_VALUE + 0.5))

unsigned char clipit( int value )
{
	if ( value < 0 )
		return 0;
	if ( value > 255 )
		return 255;
	return value;
}

#define PUSH_ITEM(x) \
      fprintf(file, "%c", clipit((x) >> FLOAT_SCALE_FACTOR))


int color_bars_y[] = { 180, 162, 131, 112, 84, 65, 35, 16 };
int color_bars_cb[] = { 128, 44, 156, 72, 184, 100, 212, 128 };
int color_bars_cr[] = { 128, 142, 44, 58, 198, 212, 114, 128 };
 
#undef YUV_COLOR_BARS

void make_rgb_from_yuv( FILE *file, unsigned char *buf, int len )
{
	int x, y, i;
	x = 0;
	y = 0;

	for ( i = 0 ; i < len ; i+=4 ) {
		int y1, cb, y2, cr;
		int dy1, dy2, dr, dg, db;
#ifdef YUV_COLOR_BARS
		int index;
#endif

		y1 = *buf++;
		cb = *buf++;
		y2 = *buf++;
		cr = *buf++;

#ifdef YUV_COLOR_BARS
		index = x * 8 / width_table[desired_size];
		y1 = y2 = color_bars_y[index];
		cb = color_bars_cb[index];
		cr = color_bars_cr[index];
#endif

		dy1 = MAKE_FLOAT(1.164) * (y1 - 16);
		dy2 = MAKE_FLOAT(1.164) * (y2 - 16);

		dr  = MAKE_FLOAT(1.596) * (cr - 128);
		dg  = MAKE_FLOAT(-0.813) * (cr - 128) - MAKE_FLOAT(0.391) * (cb - 128);
		db  = MAKE_FLOAT(2.018) * (cb - 128);

		PUSH_ITEM(dy1 + dr);
		PUSH_ITEM(dy1 + dg);
		PUSH_ITEM(dy1 + db);
		x++;

		PUSH_ITEM(dy2 + dr);
		PUSH_ITEM(dy2 + dg);
		PUSH_ITEM(dy2 + db);
		x++;

		if ( x >= width_table[desired_size] ) {
			y++;
			x = 0;
		}
	}
}



/* See "Video Demystified, pg 16, third edition */
#define R_TO_Y     MAKE_FLOAT(0.257)
#define G_TO_Y     MAKE_FLOAT(0.504)
#define B_TO_Y     MAKE_FLOAT(0.098)
#define Y_OFFSET   16

#define R_TO_Cb    MAKE_FLOAT(-.148)
#define G_TO_Cb    MAKE_FLOAT(-.291)
#define B_TO_Cb    MAKE_FLOAT(0.439)
#define Cb_OFFSET  128

#define R_TO_Cr    MAKE_FLOAT(0.439)
#define G_TO_Cr    MAKE_FLOAT(-.368)
#define B_TO_Cr    MAKE_FLOAT(-.071)
#define Cr_OFFSET  128

#define Y_VALUE(red,green,blue) \
     ((((R_TO_Y * red) + (G_TO_Y * green) + (B_TO_Y * blue)) >> FLOAT_SCALE_FACTOR) + Y_OFFSET)

#define Cb_VALUE(red,green,blue) \
     ((((R_TO_Cb * red) + (G_TO_Cb * green) + (B_TO_Cb * blue)) >> FLOAT_SCALE_FACTOR)  + Cb_OFFSET)

#define Cr_VALUE(red,green,blue) \
     ((((R_TO_Cr * red) + (G_TO_Cr * green) + (B_TO_Cr * blue)) >> FLOAT_SCALE_FACTOR) + Cr_OFFSET)

#define DO_YUV_APIXEL_FORWARD(red,green,blue) \
    rvalue = red; \
    gvalue = green;  \
    bvalue = blue;  \
   *to++ = Y_VALUE(rvalue, gvalue, bvalue );    \
   *to++ = Cb_VALUE(rvalue, gvalue, bvalue );   \
    stored_value = Cr_VALUE(rvalue, gvalue, bvalue ); \
    from++

#define DO_YUV_BPIXEL_FORWARD(red,green,blue) \
   *to++ = Y_VALUE(red,green,blue); \
   *to++ = stored_value; \
    from++

int color_bars_red[] = { 255, 255, 0, 0, 255, 255, 0, 0};
int color_bars_green[] = { 255, 255, 255, 255, 0, 0, 0, 0 };
int color_bars_blue[] = { 255, 0, 255, 0, 255, 0, 255, 0 };

void create_color_bars( unsigned char *buf, int len )
{
	int x, y,i;
	unsigned char *to = buf;
	unsigned char *from = NULL;  /* Not used !*/
	int rvalue = 0, gvalue = 0, bvalue = 0;
	int index;
	int stored_value;
	int r, g, b;
	int y_cut = height_table[desired_size] * 3 / 4;
	x = y = 0;
	for ( i = 0 ; i < len ; i+= 4 ) {
		index = 8 * x / width_table[desired_size];
		if ( y < y_cut ) {
			r = color_bars_red[index];
			g = color_bars_green[index];
			b = color_bars_blue[index];
		}
		else {
			r = g = b = ( x * 255 / width_table[desired_size] );
		}
		DO_YUV_APIXEL_FORWARD( r, g, b );
		DO_YUV_BPIXEL_FORWARD( r, g, b );
		x += 2;
		if ( x >= width_table[desired_size] ) {
			x = 0;
			y++;
		}
	}
	printf("%d %d %d %d %d\n", x,y, rvalue, gvalue, bvalue);
	printf("%d %d %d\n", 
	       Y_VALUE(rvalue,gvalue,bvalue),
	       Cb_VALUE(rvalue,gvalue,bvalue),
	       Cr_VALUE(rvalue,gvalue,bvalue));
	       
}


void write_yuv_file( FILE *file, unsigned char *buf, int len )
{
	fprintf(file,"P6\n");
	fprintf(file,"# Creator Linux H3600 Camera driver - YUV mode\n");
	fprintf(file,"%d %d\n", vwin.width, vwin.height);
	fprintf(file,"255\n");
	make_rgb_from_yuv(file, buf, len);
}

void write_data_file( FILE *file, int value, char *mode, unsigned char *buf, int len ) 
{
	int i;
	fprintf(file,"P%d\n",value);
	fprintf(file,"# Creator Linux H3600 Camera driver - %s mode\n", mode);
	fprintf(file,"%d %d\n", vwin.width, vwin.height);
	fprintf(file,"255\n");
	for ( i = 0 ; i < len ; i++ )
		fprintf(file,"%c", buf[i] );
}

void write_file( char *name, unsigned char *buf, int len )
{
	FILE *file = stdout;

	if ( name ) {
		file = fopen(name,"w");
		if ( !file ) {
			perror("Unable to open output file\n");
			return;
		}
	}
	switch (desired_mode) {
	case DESIRED_RAW:
		write_data_file( file, 5, "raw", buf, len );
		break;
	case DESIRED_GREY:
		write_data_file( file, 5, "grey", buf, len );
		break;
	case DESIRED_RGB:
		write_data_file( file, 6, "color", buf, len );
		break;
	case DESIRED_YUV:
//		create_color_bars( buf, len );
		write_yuv_file( file, buf, len );
		break;
	}
	fclose(file);
}

int main(int argc, char **argv)
{
	int fd;
	unsigned char *buf;
	int result;

	char *filename = get_opts( argc, argv );
	int filesize = 0;

	if ((fd = open(VIDEO_DEVICE, O_RDONLY | O_NOCTTY )) < 0 ) {
		perror("Unable to open video device");
		return 1;
	}

	filesize = setup_camera(fd);
	if ( !filesize )
		return 1;

	buf = (unsigned char *) malloc( filesize );
	if ( !buf ) {
		perror("Unable to allocate memory");
		return -1;
	}
	
	DEBUG_STRING("Reading dummy frame\n");
	result = read( fd, buf, filesize );
	if ( result < 0 ) {
		perror("Failed reading frame");
		return 1;
	}
	result = read( fd, buf, filesize );
	if ( result < 0 ) {
		perror("Failed reading frame");
		return 1;
	}
	DEBUG_STRING("Finished reading\n");

	if ( desired_file )
		write_file( filename, buf, filesize );

	close(fd);

	return 0;
}







