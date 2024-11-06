package com.alaje.gendright.data.googleAIClient

import android.util.Log
import com.alaje.gendright.data.googleAIClient.models.AIClientAPIResponse
import com.alaje.gendright.data.models.DataResponse
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.HarmBlockThreshold
import com.google.firebase.vertexai.type.HarmCategory
import com.google.firebase.vertexai.type.RequestOptions
import com.google.firebase.vertexai.type.SafetySetting
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

class AIClientAPIService {

    private val model = Firebase.vertexAI.generativeModel(
        "gemini-1.5-flash",
        generationConfig = generationConfig {
            temperature = 1.5f
            maxOutputTokens = 8192
            responseMimeType = "application/json"
            candidateCount = 1
        },
        requestOptions = RequestOptions(
            timeoutInMillis = 10.seconds.inWholeMilliseconds
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.NONE),
        )
    )

    suspend fun processText(inputText: String): DataResponse<AIClientAPIResponse> {

        return try {
            Log.d("AIClientAPIService", "Interacting with AI model for text: $inputText")

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
        """You are an expert on human conversation and language have a great understanding of human feelings. Given the text: "$inputText", analyze if it contains any gender bias or impacts gender equality and then provide a suitable response in the form of a JSON object with a rating, suggestions and a brief reasoning.
            Your job is to analyze text for potential gender bias elements, based on the user input. You will identify any language that reinforces harmful stereotypes, excludes or diminishes women, or promotes unequal treatment based on gender. Do not hallucinate. 
            You must provide rate the sentence on a scale of 1 to 100, with 100 representing the highest likelihood of gender bias. 
            You must suggest alternatives that address all of that the user's original text contains. 
            You must not include a full stop at the end of the entire text if the original text does not contain one at the end. 
            Your output must be in a JSON format with three JSON fields: rating, suggestions and reasoning.
            You must return three alternative suggestions for any input text that is rated 70 and above. 
            The suggestions must serve as appropriate replacements of the original text.
            The suggestions must maintain the same conciseness and directness as the original text when possible.  
Provide:
1. **Rating:** This is the level of bias in the text on a scale of 1 to 100, with 100 being the most biased.
2. **Suggestions:** These are alternatives for the original text that sounds more polite, less generalizing and still conveys the same message as the original text. The rephrased text must still convey the original message without being offensive. For example, instead of saying "I hate women," rephrase it as "I've found it difficult to interact with women in my life."
3. **Reasoning:** This is a brief explanation why the original text is problematic

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
You must set the suggestions in the JSON as an empty list if there are no suggestions.
        """
}
