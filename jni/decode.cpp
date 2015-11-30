#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "g726.h"

int main(int argc, char *argv[])
{

	g726_state_t *g_state726_16 = NULL; //for g726_16 
	g726_state_t *g_state726_24 = NULL; //for g726_24 
	g726_state_t *g_state726_32 = NULL; //for g726_32 
	g726_state_t *g_state726_40 = NULL; //for g726_40 
	
	g_state726_16 = (g726_state_t *)malloc(sizeof(g726_state_t));
	g_state726_16 = g726_init(g_state726_16, 8000*2);
	
	//g_state726_24 = (g726_state_t *)malloc(sizeof(g726_state_t));
	//g_state726_24 = g726_init(g_state726_24, 8000*3);
	
	//g_state726_32 = (g726_state_t *)malloc(sizeof(g726_state_t));
	//g_state726_32 = g726_init(g_state726_32, 8000*4);
	
	//g_state726_40 = (g726_state_t *)malloc(sizeof(g726_state_t));
	//g_state726_40 = g726_init(g_state726_40, 8000*5);

	//FILE *pInFile = fopen("encode_out.g726", "rb");
	FILE *pInFile = fopen("audio.g726", "rb");
	//FILE *pInFile = fopen(argv[1], "rb");
	FILE *pOutFile = fopen("decode_out.pcm", "wb");
	if (NULL == pInFile || NULL == pOutFile)
	{
		printf("open file failed\n");
		return 0;
	}
	
	int iRet = 0;
	int iRead = 0;

	
	unsigned char ucInBuff[640] = {0};
	unsigned char ucOutBuff[1024] = {0};
	while (1)
	{
		iRead = fread(ucInBuff, 1, 20, pInFile);
		if (iRead > 0)
		{
			iRet = g726_decode(g_state726_16, (short*)ucOutBuff, ucInBuff, 20);
			//iRet = g726_decode(g_state726_24, (short*)ucOutBuff, ucInBuff, 30);
			//iRet = g726_decode(g_state726_32, (short*)ucOutBuff, ucInBuff, 20);
			//iRet = g726_decode(g_state726_40, (short*)ucOutBuff, ucInBuff, 20);
			
			printf("iRet = %d\n", iRet);
			fwrite(ucOutBuff, sizeof(short), iRet, pOutFile);
			memset(ucInBuff, 0, sizeof(ucInBuff));
			memset(ucOutBuff, 0, sizeof(ucOutBuff));
		}
		else
		{
			printf("read the end\n");
			break;
		}
	}
	
	fclose(pInFile);
	fclose(pOutFile);
}

struct wave_header {
	char riff[4];
	unsigned long fileLength;
	char wavTag[4];
	char fmt[4];
	unsigned long size;
	unsigned short formatTag;
	unsigned short channel;
	unsigned long sampleRate;
	unsigned long bytePerSec;
	unsigned short blockAlign;
	unsigned short bitPerSample;
	char data[4];
	unsigned long dataSize;
};

void *createWaveHeader(int fileLength, short channel, int sampleRate, short bitPerSample)
{
	struct wave_header *header = (struct wave_header *)malloc(sizeof(struct wave_header));

	if (header == NULL) {
		return NULL;
	}

	// RIFF
	header->riff[0] = 'R';
	header->riff[1] = 'I';
	header->riff[2] = 'F';
	header->riff[3] = 'F';

	// file length
	header->fileLength = fileLength + (44 - 8);

	// WAVE
	header->wavTag[0] = 'W';
	header->wavTag[1] = 'A';
	header->wavTag[2] = 'V';
	header->wavTag[3] = 'E';

	// fmt
	header->fmt[0] = 'f';
	header->fmt[1] = 'm';
	header->fmt[2] = 't';
	header->fmt[3] = ' ';

	header->size = 16;
	header->formatTag = 1;
	header->channel = channel;
	header->sampleRate = sampleRate;
	header->bitPerSample = bitPerSample;
	header->blockAlign = (short)(header->channel * header->bitPerSample / 8);
	header->bytePerSec = header->blockAlign * header->sampleRate;

	// data
	header->data[0] = 'd';
	header->data[1] = 'a';
	header->data[2] = 't';
	header->data[3] = 'a';

	// data size
	header->dataSize = fileLength;
	return header;
}