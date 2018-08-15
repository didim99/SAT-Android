//
// Created by didim99 on 04.08.17.
//

#ifndef SAT_CRYPTO_H
#define SAT_CRYPTO_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "main.h"
#include "lz.h"

// Global variables
int textureWidth;
int textureHeight;

/*
Version codes:
  20: for SA 1.4.x
  21: for SA 1.5.0 or newer
*/
#define VER_CODE_20 20
#define VER_CODE_21 21

/*
  Error codes:
  -1: Unable to open input file
  -2: Unable to open output file
  -3: Not enough RAM
  -4: Unknown version code
  -5: Attempt to decrypt an unencrypted file
  -6: Attempt to encrypt an already encrypted file
*/
#define ERR_INPUT_FIFE         -1
#define ERR_OUTPUT_FIFE        -2
#define ERR_NOT_ENOUGH_MEMORY  -3
#define ERR_UNKNOWN_VERCODE    -4
#define ERR_NOT_ENCRYPTED      -5
#define ERR_ALREADY_ENCRYPTED  -6

//Function prototypes
long getFileSize (FILE *fp);
long compressTexture (const char *name, int sizeX, int sizeY);
long uncompressTexture (const char *name);
long compressFile (const char *name, int verCode);
long uncompressFile (const char *name);

#ifdef __cplusplus
}
#endif

#endif //SAT_CRYPTO_H
