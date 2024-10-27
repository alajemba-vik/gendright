package com.alaje.gendright.googleAIClient

import android.util.Log
import com.alaje.gendright.BuildConfig
import com.alaje.gendright.googleAIClient.models.AIClientAPIResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

class AIClientAPIService {

    private val model = GenerativeModel(
        "gemini-1.5-flash",
        BuildConfig.geminiAPIkey,
        generationConfig = generationConfig {
            temperature = 1f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192
            responseMimeType = "application/json"
            candidateCount = 1
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
        ),
        systemInstruction = Content(
            role = "user",
            parts = listOf(
                TextPart(
                    """
                    Please analyze the following sentence for potential gender bias. 
                    Identify any language that reinforces harmful stereotypes, 
                    excludes or diminishes women, or promotes unequal treatment based on gender.
                    Rate the sentence on a scale of 1 to 100, with 100 representing the highest 
                    likelihood of gender bias.

                    If the sentence is rated 70 or higher on the bias scale, provide at most 
                    three alternative suggestions for the sentence that avoid gender bias. 
                    These alternative suggestions should relate back to the original 
                    sentence’s subject and retain the original sentence’s object (or implied object), 
                    while mitigating the identified biases. The alternatives must at least convey a
                    similar message in a more polite manner. Additionally, the alternatives should 
                    aim for similar brevity and directness as the original sentence, where possible, 
                    while still effectively addressing the bias. Assign these suggestions to the 
                    output JSON’s suggestions field as a list of strings.

                    If the sentence is rated below 70 on the bias scale, the suggestions must 
                    be an empty list. If suggestions are provided, a reasoning behind this must be provided.
                    The reasoning should be brief and direct, clearly explaining 
                    the potential for bias (or lack thereof).
                    
                    The output is a JSON with three properties: rating, suggestions and reasoning.
                    The rating and reasoning are strings. The suggestions is an array of strings
                """
                )
            )
        )
    )

    suspend fun processText(inputText: String): AIClientAPIResponse? {
        val response = model.generateContent(inputText)

        return try {
            Log.d("AIClientAPIService", "Response: ${response.text}")
            AIClientAPIResponse.fromJSON(response.text ?: "")

        } catch (e: Exception) {
            null
        }
    }

}

fun <T> parseJsonObject(
    json: String,
    resultParser: (JSONObject) -> T
): T {
    val jsonObject = JSONObject(json)
    return resultParser(jsonObject)
}