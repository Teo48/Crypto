#include <jni.h>
#include <string>
extern "C" JNIEXPORT jstring
JNICALL
Java_com_smd_cryptocurrency_domain_Keys_apiKey(JNIEnv *env, jobject object) {
    std::string api_key = "a92dbe33-bc38-4e8d-899f-96c434838dd8";
    return env->NewStringUTF(api_key.c_str());
}