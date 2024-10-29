package com.alaje.gendright.utils

import org.json.JSONObject

fun <T> parseJsonObject(
    json: String,
    resultParser: (JSONObject) -> T
): T {
    val jsonObject = JSONObject(json)
    return resultParser(jsonObject)
}
