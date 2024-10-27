package com.alaje.gendright.googleAIClient.models

import com.alaje.gendright.googleAIClient.parseJsonObject

data class AIClientAPIResponse(
    val rating: Int,
    val suggestions: List<String>,
    val reasoning: String
){
    companion object {
        fun fromJSON(json: String): AIClientAPIResponse {
            return parseJsonObject(
                json
            ) { jsonObject ->
                AIClientAPIResponse(
                    jsonObject.getInt("rating"),
                    jsonObject.getJSONArray("suggestions").let { suggestionsProperty ->
                        val suggestions = mutableListOf<String>()
                        for (i in 0 until suggestionsProperty.length()) {
                            suggestions.add(suggestionsProperty.getString(i))
                        }
                        suggestions
                    },
                    jsonObject.getString("reasoning")
                )
            }
        }
    }
}