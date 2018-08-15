/*
  sasbx/ambbmp encryption-decryption library ver. 0.3.4
  based on BCL ver. 1.2.0
  Written by OKOB @ 2017-05-16
  Modified by didim99 @ 2018-01-17
*/

#include "crypto.h"

long getFileSize (FILE *fp)
{
  long lPos1 = ftell(fp);
  fseek(fp, 0L, SEEK_END);
  long lPos2 = ftell(fp);
  fseek(fp, lPos1, SEEK_SET);
  return lPos2;
}



long compressTexture (const char *name, int sizeX, int sizeY)
{
  /* open input file */
  FILE *fp = fopen(name, "rb");
  if (!fp) return ERR_INPUT_FIFE;

  /* get input file size */
  size_t lSz = (size_t) getFileSize(fp);

  /* read input file */
  unsigned char *lpInMem = (unsigned char *) malloc(lSz);
  if (!lpInMem)
  {
    fclose(fp);
    return ERR_NOT_ENOUGH_MEMORY;
  }
  fread(lpInMem, 1, lSz, fp);
  fclose(fp);
  remove(name);

  /* allocate memory */
  unsigned int *lpTmpMem = (unsigned int *) malloc(4 * lSz + 0x40000);
  if (!lpTmpMem)
  {
    free(lpInMem);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  unsigned char *lpOutMem = (unsigned char *) malloc((104 * lSz + 50) / 100 + 384);
  if (!lpOutMem)
  {
    free(lpInMem);
    free(lpTmpMem);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  /* Compression */
  long lOutSz = LZ_CompressFast(lpInMem, lpOutMem, (unsigned int) lSz, lpTmpMem);

  /* write output file */
  fp = fopen(name, "wb");
  if (!fp)
  {
    free(lpInMem);
    free(lpTmpMem);
    free(lpOutMem);
    return ERR_OUTPUT_FIFE;
  }
  fwrite(&sizeX, 1, 4, fp);
  fwrite(&sizeY, 1, 4, fp);
  fwrite(&lOutSz, 1, 4, fp);
  fwrite(lpOutMem, 1, (size_t) lOutSz, fp);
  fclose(fp);

  /* free memory */
  free(lpInMem);
  free(lpTmpMem);
  free(lpOutMem);

  return lOutSz;
}



long uncompressTexture (const char *name)
{
  /* open input file */
  FILE *fp = fopen(name, "rb");
  if (!fp) return ERR_INPUT_FIFE;

  /* get texture parameters */
  extern int textureWidth, textureHeight;
  fread(&textureWidth, 1, 4, fp);
  fread(&textureHeight, 1, 4, fp);
  int tmpSz;
  fread(&tmpSz, 1, 4, fp);
  size_t lSz = (size_t) tmpSz;
  LOG_D("Texture info:\n  width: %d height: %d length: %lu", textureWidth, textureHeight, lSz);

  /* read input fiule */
  unsigned char *lpInMem = (unsigned char *)malloc(lSz);
  if (!lpInMem)
  {
    fclose(fp);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  /* read input file */
  fread(lpInMem, 1, lSz, fp);
  fclose(fp);
  remove(name);

  /* allocate memory */
  unsigned char *lpOutMem = (unsigned char *)malloc(25 * lSz);
  if (!lpOutMem)
  {
    free(lpInMem);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  /* Decompression */
  long lOutSz = LZ_Uncompress(lpInMem, lpOutMem, (unsigned int) lSz);

  /* write output file */
  fp = fopen(name, "wb");
  if (!fp)
  {
    free(lpInMem);
    free(lpOutMem);
    return ERR_OUTPUT_FIFE;
  }
  fwrite(lpOutMem, 1u, (size_t) lOutSz, fp);
  fclose(fp);

  /* free memory */
  free(lpInMem);
  free(lpOutMem);

  return lOutSz;
}



long compressFile (const char *name, int verCode)
{
  /* version code validation */
  if (verCode < VER_CODE_20 || verCode > VER_CODE_21)
    return ERR_UNKNOWN_VERCODE;

  /* open input file */
  FILE *fp = fopen(name, "rb");
  if (!fp) return ERR_INPUT_FIFE;

  /* get input file size */
  size_t lSz = (size_t) getFileSize(fp);

  /* error correction */
  char verMarker[7];
  fgets(verMarker, 7, fp);

  if (strcmp(verMarker, "system") == 0)
  {
    fseek(fp, 0, SEEK_SET);
  }
  else
  {
    fclose(fp);
    return ERR_ALREADY_ENCRYPTED;
  }

  /* read input fiule */
  unsigned char *lpInMem = (unsigned char *)malloc(lSz);
  if (!lpInMem)
  {
    fclose(fp);
    return ERR_NOT_ENOUGH_MEMORY;
  }
  fread(lpInMem, 1, lSz, fp);
  fclose(fp);
  remove(name);

  /* allocate memory */
  unsigned int *lpTmpMem = (unsigned int *)malloc(4 * lSz + 0x40000);
  if (!lpTmpMem)
  {
    free(lpInMem);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  unsigned char *lpOutMem = (unsigned char *) malloc((104 * lSz + 50) / 100 + 384);
  if (!lpOutMem)
  {
    free(lpInMem);
    free(lpTmpMem);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  /* encryption */
  long lOutSz = LZ_CompressFast(lpInMem, lpOutMem, (unsigned int) lSz, lpTmpMem);
  for(long i = 0; lOutSz > i; i += 2)
    lpOutMem[i] = ~lpOutMem[i];

  /* write output file */
  fp = fopen(name, "wb");
  if (!fp)
  {
    free(lpInMem);
    free(lpOutMem);
    free(lpOutMem);
    return ERR_OUTPUT_FIFE;
  }

  if (verCode == 21) fputc('&', fp);
  fwrite(lpOutMem, 1, (size_t) lOutSz, fp);
  fclose(fp);

  /* free memory */
  free(lpInMem);
  free(lpTmpMem);
  free(lpOutMem);

  return lOutSz;
}



long uncompressFile (const char *name)
{
  /* open input file */
  FILE *fp = fopen(name, "rb");
  if (!fp) return ERR_INPUT_FIFE;

  /* get input file size */
  size_t lSz = (size_t) getFileSize(fp);

  /* version detection */
  char verMarker[7];
  fgets(verMarker, 7, fp);

  if ((strcmp(verMarker, "system") == 0) || (strcmp(verMarker, "modspa") == 0)) {
    fclose(fp);
    return ERR_NOT_ENCRYPTED;
  } else if (verMarker[0] != 0x26)
    fseek(fp, 0, SEEK_SET);

  else {
    lSz--;
    fseek(fp, 1, SEEK_SET);
  }

  /* read input file */
  unsigned char *lpInMem = (unsigned char *)malloc(lSz);
  if (!lpInMem)
  {
    fclose(fp);
    return ERR_NOT_ENOUGH_MEMORY;
  }
  fread(lpInMem, 1, lSz, fp);
  fclose(fp);
  remove(name);

  /* decryption */
  for (long i = 0; lSz > i; i += 2) lpInMem[i] = ~lpInMem[i];
  size_t lBufSz = 10000000;
  if ((100 * lSz) <= 10000000) lBufSz = 100 * lSz;
  unsigned char *lpOutMem = (unsigned char *)malloc(lBufSz);
  if (!lpOutMem)
  {
    free(lpInMem);
    return ERR_NOT_ENOUGH_MEMORY;
  }

  long lOutSz = LZ_Uncompress (lpInMem, lpOutMem, (unsigned int) lSz);

  /* write output file */
  fp = fopen(name, "wb");
  if (!fp)
  {
    free(lpInMem);
    free(lpOutMem);
    return ERR_OUTPUT_FIFE;
  }

  fwrite(lpOutMem, 1u, (size_t) lOutSz, fp);
  fclose(fp);

  /* free memory */
  free(lpInMem);
  free(lpOutMem);

  return lOutSz;
}