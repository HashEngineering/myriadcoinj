/**
 * Created by Hash Engineering on 4/24/14 for the X11 algorithm
 */
#include "hashgroestl.h"
#include "hashqubit.h"
#include "hashskein.h"
#include "yescrypt.h"
#include <inttypes.h>

#include <jni.h>


jbyteArray JNICALL groestl_native(JNIEnv *env, jclass cls, jbyteArray header)
{
    jint Plen = (env)->GetArrayLength(header);
    jbyte *P = (env)->GetByteArrayElements(header, NULL);
    //uint8_t *buf = malloc(sizeof(uint8_t) * dkLen);
    jbyteArray DK = NULL;

    if (P)
	{
	
	uint256 result = HashGroestl(P, P+Plen);

    DK = (env)->NewByteArray(32);
    if (DK)
	{
		(env)->SetByteArrayRegion(DK, 0, 32, (jbyte *) result.begin());
	}
	

    if (P) (env)->ReleaseByteArrayElements(header, P, JNI_ABORT);
    //if (buf) free(buf);
	}
    return DK;
}

jbyteArray JNICALL skein_native(JNIEnv *env, jclass cls, jbyteArray header)
{
    jint Plen = (env)->GetArrayLength(header);
    jbyte *P = (env)->GetByteArrayElements(header, NULL);
    //uint8_t *buf = malloc(sizeof(uint8_t) * dkLen);
    jbyteArray DK = NULL;

    if (P)
	{

	uint256 result = HashSkein(P, P+Plen);

    DK = (env)->NewByteArray(32);
    if (DK)
	{
		(env)->SetByteArrayRegion(DK, 0, 32, (jbyte *) result.begin());
	}


    if (P) (env)->ReleaseByteArrayElements(header, P, JNI_ABORT);
    //if (buf) free(buf);
	}
    return DK;
}

jbyteArray JNICALL qubit_native(JNIEnv *env, jclass cls, jbyteArray header)
{
    jint Plen = (env)->GetArrayLength(header);
    jbyte *P = (env)->GetByteArrayElements(header, NULL);
    //uint8_t *buf = malloc(sizeof(uint8_t) * dkLen);
    jbyteArray DK = NULL;

    if (P)
	{

	uint256 result = HashQubit(P, P+Plen);

    DK = (env)->NewByteArray(32);
    if (DK)
	{
		(env)->SetByteArrayRegion(DK, 0, 32, (jbyte *) result.begin());
	}


    if (P) (env)->ReleaseByteArrayElements(header, P, JNI_ABORT);
    //if (buf) free(buf);
	}
    return DK;
}

jbyteArray JNICALL yescrypt_native(JNIEnv *env, jclass cls, jbyteArray header)
{
    jint Plen = (env)->GetArrayLength(header);
    jbyte *P = (env)->GetByteArrayElements(header, NULL);
    //uint8_t *buf = malloc(sizeof(uint8_t) * dkLen);
    jbyteArray DK = NULL;

    if (P)
	{

	char result[32];
	yescrypt_hash(( char *)P, result);

    DK = (env)->NewByteArray(32);
    if (DK)
	{
		(env)->SetByteArrayRegion(DK, 0, 32, (jbyte *) result);
	}


    if (P) (env)->ReleaseByteArrayElements(header, P, JNI_ABORT);
    //if (buf) free(buf);
	}
    return DK;
}

static const JNINativeMethod groestl_methods[] = {
    { "groestl_native", "([BII)[B", (void *) groestl_native }
};
static const JNINativeMethod skein_methods[] = {
    { "skein_native", "([BII)[B", (void *) skein_native }
};
static const JNINativeMethod qubit_methods[] = {
    { "qubit_native", "([B)[B", (void *) qubit_native }
};
static const JNINativeMethod yescrypt_methods[] = {
    { "yescrypt_native", "([B)[B", (void *) yescrypt_native }
};
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;

    if ((vm)->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass cls = (env)->FindClass("com/hashengineering/crypto/Groestl");
    int r = (env)->RegisterNatives(cls, groestl_methods, 1);

    cls = (env)->FindClass("com/hashengineering/crypto/Skein");
    r = (env)->RegisterNatives(cls, skein_methods, 1);

    if(r == JNI_OK){

        cls = (env)->FindClass("com/hashengineering/crypto/Qubit");
        r = (env)->RegisterNatives(cls, qubit_methods, 1);

        if(r == JNI_OK) {
            cls = (env)->FindClass("com/hashengineering/crypto/Yescrypt");
            r = (env)->RegisterNatives(cls, yescrypt_methods, 1);
        }
    }
    return (r == JNI_OK) ? JNI_VERSION_1_6 : -1;
}