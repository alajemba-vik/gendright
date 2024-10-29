package com.alaje.gendright.data.googleAIClient

import android.util.Log
import com.alaje.gendright.BuildConfig
import com.alaje.gendright.data.googleAIClient.models.AIClientAPIResponse
import com.alaje.gendright.data.models.DataResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import java.net.UnknownHostException

class AIClientAPIService {

    private val model = GenerativeModel(
        "gemini-1.5-pro-002",
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
            parts = listOf(
                TextPart(
                    """If the sentence is rated 70 or higher on the bias scale, provide at most 
                        three alternative suggestions for the sentence that avoid gender bias. 
                        These alternative suggestions should relate back to the original sentence’s 
                        subject and retain the original sentence’s object (or implied object), 
                        while mitigating the identified biases. Additionally, the alternatives 
                        should aim for similar brevity and directness as the original sentence, 
                        where possible, while still effectively addressing the bias. Assign these 
                        suggestions to the output JSON’s suggestions field as a list of strings.
                        If the sentence is rated below 70 on the bias scale, the suggestions must be
                         an empty list. The reasoning should be brief and direct, clearly explaining
                          the potential for bias (or lack thereof).
                    """.trim()
                )
            ),
            role = "user"
        )
    )

    suspend fun processText(inputText: String): DataResponse<AIClientAPIResponse> {

        return try {
            val response = model.generateContent(createPrompt(inputText))

            Log.d("AIClientAPIService", "Response: ${response.text}")

            val apiResponse = AIClientAPIResponse.fromJSON(response.text ?: "")

            return DataResponse.Success(apiResponse)

        } catch (e: Exception) {
            Log.e("AIClientAPIService", "Error: ${e.message}")
            if (e.cause is UnknownHostException) {
                DataResponse.NetworkError("No internet connection")
            } else {
                DataResponse.APIError("An error occurred")
            }
        }
    }

    private fun createPrompt(inputText: String) =
        """Please analyze the this sentence "$inputText" for potential gender bias. 
            Identify any language that reinforces harmful stereotypes, excludes or diminishes women, 
            or promotes unequal treatment based on gender. Rate the sentence on a scale of 1 to 100, 
            with 100 representing the highest likelihood of gender bias.
            
            Most importantly, the alternatives must retain the same sentiment and focus as the original writing.
            Avoid generalized language such as "this person" or "some people" when the original sentence is more specific. Instead, maintain the same level of specificity in the alternatives.
    
            For example if the original sentence is this: "I do not like women.", 
            the output should be:
            {
              "rating": "90",
              "suggestions": [
                "Based on my experiences, I find it challenging to connect with women.",
                "I personally struggle to feel close to women.",
                "I often feel distant in my interactions with women."
              ],
              "reasoning": "The original statement generalizes a negative sentiment towards a specific gender. The alternatives maintain the personal feeling of distance without implying bias against women as a group."
            } 
        """
}
