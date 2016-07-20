
// g++ -shared -o libResourceInfo.so ResourceInfo.C -I/home/jan/NoCsBack/jdk1.5.0/include/ -I/home/jan/NoCsBack/jdk1.5.0/include/linux/

// g++ -shared -o libResourceInfo.so ResourceInfo.C -I/s/pkg/linux/j2se/jdk1.5.0_05/include/ -I/s/pkg/linux/j2se/jdk1.5.0_05/include/linux/

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/resource.h>

#include "jeans_resource_ResourceInfo.h"

void skipspaces(FILE* file, int nb) {
	int ctr = 0;
	int ch = fgetc(file);
	while (ctr < nb) {
		if (ch == ' ') ctr++;
		if (ctr < nb) ch = fgetc(file);
	}
}

unsigned long getnumber(FILE* file) {
	unsigned long num = 0;
	int ch = fgetc(file);
	while (ch != ' ') {
		num *= 10;
		num += ch - '0';
		ch = fgetc(file);
	}
	return num;
}

unsigned long getMemUsage() {
	char fname[100];
	sprintf(fname, "/proc/%d/stat", getpid());
	FILE* procfile = fopen(fname, "r");
	if (procfile != NULL) {
		skipspaces(procfile, 23);
		unsigned long size = getnumber(procfile)*(sysconf(_SC_PAGE_SIZE)/1024);
		fclose(procfile);
		return size;
	} else {
		return 0;
	}
}

JNIEXPORT jlong JNICALL Java_jeans_resource_ResourceInfo_getCPUTime(JNIEnv *env, jclass cls) {
	struct rusage usage;

	getrusage(RUSAGE_SELF, &usage);
	return (usage.ru_utime.tv_sec)*1000 + (usage.ru_utime.tv_usec / 1000);
}

JNIEXPORT jlong JNICALL Java_jeans_resource_ResourceInfo_getMemorySize(JNIEnv *env, jclass cls) {
	return getMemUsage();
}
