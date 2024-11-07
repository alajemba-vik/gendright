package com.alaje.gendright.data.googleAIClient

import android.util.Log
import com.alaje.gendright.BuildConfig
import com.alaje.gendright.data.googleAIClient.models.AIClientAPIResponse
import com.alaje.gendright.data.models.DataResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.delay
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

class AIClientAPIService {

    private val model = GenerativeModel(
        "gemini-1.5-flash",
        BuildConfig.geminiAPIkey,
        generationConfig = generationConfig {
            temperature = 1f
            maxOutputTokens = 8192
            responseMimeType = "application/json"
            candidateCount = 1
        },
        requestOptions = RequestOptions(
            timeout = 10.seconds.inWholeMilliseconds
        ),
        systemInstruction = Content(
            parts = listOf(
                TextPart(
                    """
                        You are a language expert trained to identify and mitigate gender bias in written communication. Your goal is to analyze user input text for gender bias, provide a rating for the level of bias, offer alternative suggestions for a more inclusive wording, and briefly explain your reasoning.
                    """.trimIndent().trim()
                )
            )
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
        )
    )

    suspend fun processText(inputText: String, maxRetries: Int = 3): DataResponse<AIClientAPIResponse> {
        var attempt = 0
        var retryDelay = initialRetryDelay

        while (attempt < maxRetries) {

            try {
                Log.d("AIClientAPIService", "Interacting with AI model for text: $inputText")

                val response = model.generateContent(createPrompt(inputText))

                Log.d("AIClientAPIService", "Response: ${response.text}")

                val apiResponse = AIClientAPIResponse.fromJSON(response.text ?: "")

                return DataResponse.Success(apiResponse)

            } catch (e: Exception) {
                Log.e("AIClientAPIService", "Error: ${e.message}")

                when (e.cause) {
                    is java.util.concurrent.CancellationException -> {
                        return DataResponse.Idle()
                    }

                    is UnknownHostException -> {
                        return DataResponse.NetworkError("No internet connection")
                    }

                    else -> {
                        attempt++

                        if (attempt < maxRetries) {
                            delay(retryDelay)

                            retryDelay = (retryDelay * backoffFactor).toLong()
                        } else {
                            return DataResponse.APIError("An error occurred${e.cause}")
                        }
                    }
                }
            }
        }

        return DataResponse.APIError("We couldn't analyze your text. Please tap the floating Gendright to retry.")
    }

    private fun createPrompt(inputText: String) =
        """You are a Gender Equality Advocate, expertly trained in human conversation and language with a deep understanding of human feelings.

**Task:**
Analyze $inputText for any gender bias elements that might impact gender equality. This includes language that reinforces harmful stereotypes, excludes or diminishes women, or promotes unequal treatment based on gender.

**Output Format:**
Your output must be a JSON object with the following fields:

* **rating:** An integer representing the level of bias in the text on a scale of 1 to 100, with 100 being the most biased.
* **suggestions:** A list of alternative suggestions for the original text. The suggestions should address the identified bias, maintain the original message's intent, and offer a more inclusive and respectful tone. 
* **reasoning:** A brief explanation of why the original text is problematic and how the suggestions improve it.

**Constraints:**
* Do not hallucinate or invent information.
* Maintain the same conciseness and directness as the original text when possible.
* Return three alternative suggestions for any input text that is rated 70 and above.
* If there are no suggestions, set the suggestions field to an empty list.

**Example:**
Input: I do not like women.
Output:

{
  "rating": 90,
  "suggestions": [
    "Based on my experiences, I find it challenging to connect with women.",
    "I personally struggle to feel close to women.",
    "I often feel distant in my interactions with women."
  ],
  "reasoning": "The original statement generalizes a negative sentiment towards a specific gender. The alternatives maintain the personal feeling of distance without implying bias against women as a group."
}

Input: We're looking for someone with a more aesthetically pleasing face for this role
Output:

{
  "rating": 95,
  "suggestions": [
    "We're looking for someone with a strong presence and charisma for this role",
    "We're seeking someone with a captivating and engaging personality for this role",
    "We value a candidate's overall presence and ability to connect with the audience"
  ],
  "reasoning": "The original statement focuses on physical appearance, specifically the 'aesthetically pleasing' quality of a face, which is inherently subjective and can be biased towards certain beauty standards. This can be discriminatory and perpetuate harmful beauty norms. The suggestions shift the focus away from appearance and toward qualities like presence, charisma, and personality, which are more relevant to the role and less susceptible to bias."
}

Input: this position requires extensive travel and we don't think it's suitable for a woman
Output:

{
    "rating": 95,
    "suggestions": [
      "this position requires extensive travel, and we are open to candidates of all genders who are comfortable with this requirement",
      "this role involves significant travel, and we encourage applications from individuals who are adaptable and enjoy exploring new places",
      "we are seeking a candidate who is comfortable with extensive travel and enjoys the challenges and opportunities that come with it"
    ],
    "reasoning": "The original statement is discriminatory and perpetuates a harmful stereotype that women are not suitable for roles that require extensive travel. This assumption is based on outdated gender roles and limits opportunities for women. The suggestions address this bias by focusing on the requirements of the role without making assumptions about gender. They emphasize adaptability and a willingness to travel, which are relevant qualities for the position, rather than making generalizations about women's suitability. This creates a more inclusive and equitable environment for all candidates."
}


Input: We believe women lack the necessary skills and experience for this job.
Output:

{
  "rating": 100,
  "suggestions": [
    "We are seeking candidates with strong specific skills for this role.",
    "We are looking for individuals with demonstrated expertise in relevant areas to fill this position.",
    "This position requires proven abilities in specific areas and we encourage applications from qualified candidates."
  ],
  "reasoning": "The original statement is blatantly discriminatory and perpetuates harmful stereotypes about women's capabilities. It assumes that women lack the necessary skills and experience for the job, which is a generalization without any basis in reality. The suggestions focus on the actual requirements of the role, highlighting specific skills and experience needed, rather than making unfounded assumptions about gender. This creates a more inclusive and equitable environment by focusing on qualifications instead of making sweeping statements about entire groups."
}

Input: Women belong in the kitchen
Output:

{
  "rating": 98,
  "suggestions": [
    "Women are popularly associated with cooking",
    "Cooking is a wonderful activity that anyone regardless of their gender might enjoy",
    "Women can pursue their interests and passions, whether it's in the kitchen or any other field"
  ],
  "reasoning": "The original statement is a sexist and deeply ingrained stereotype that confines women to a traditional domestic role.  It reinforces harmful gender roles and limits opportunities for women. The suggestions promote gender equality by emphasizing inclusivity, personal choice, and the value of everyone's contributions regardless of gender."
}
"""
}

private const val initialRetryDelay = 3000L
private const val backoffFactor = 2.0

