structure blk {
  reserved byte[512];
}

structure fatblk subtype of blk {
  int sects[127];
  int nextfatblk;
}

structure olehdr {
  byte magic[8];
  int unk1;
  int unk2;
  int unk3;
  int unk4;
  int unk5;
  int unk6;
  int unk7;
  int unk8;
  int unk9;
  int num_FAT_blocks;
  int root_start_block;
  int unk10;
  int unk11;
  int dir_flag;
  int unk12;
  int FAT_next_block;
  int num_extra_FAT_blocks;
  int sects[109];
}

structure ole {
  olehdr hdr;
  blk blocks[size/literal(512)-literal(1)];
}

structure oledir {
  byte name[64];
  short namsiz;
  byte type;
  byte filler1;
  int prev_dirent;
  int next_dirent;
  int dir_dirent;
  int unk1;
  int unk2;
  int unk3;
  int unk4;
  int unk5;
  int secs1;
  int days1;
  int secs2;
  int days2;
  int start_block;
  int size;
  int unk6;
}

structure directory {
  byte name[64];
  int type;
  int level;
  int start_block;
  int size;
  int next;
  int prev;
  int dir;
  int s1;
  int s2;
  int d1;
  int d2;
}
