package xyz.schnabl

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class TestUtil {}

fun readJson(fileName: String): String {
    return TestUtil::class.java.getResource(fileName).readText()
}

fun requestBodyFromJson(fileName: String): RequestBody {
    return readJson(fileName).toRequestBody("application/json".toMediaType())
}
