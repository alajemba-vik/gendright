Gendright
==============================

Gendright is an Android accessibility service designed to help users identify and rephrase gender-biased language in real-time as they type. By analyzing text for gender-related biases, Gendright provides non-intrusive suggestions to promote inclusive language across various apps and social platforms.

Features
------------
- **Real-Time Suggestions** : Detects gender-biased phrases and suggests more inclusive alternatives.
- **Seamless Integration**: Works across Android apps with an accessible floating icon that users can tap to see suggestions.
- **Customizable Feedback**: Users can select and edit suggestions, fostering a more conscious and respectful communication style.

How it works
------------

GendRight uses Google AI client SDK with the Gemini 1.5 Flash model to analyze text and offer relevant suggestions. The app's implements dynamic colors to really provide users a more personal and safer feeling.

Getting started on testing locally
------------

## Prerequisites

- Kotlin knowledge
- Ability to use Android Studio and test apps in an emulator or physical device running Android API level 24 or higher
- A Firebase project connected to Gendright

## Setup

- You need an API key, and this can be created easily on Google AI Studio. The [docs](https://developer.android.com/ai/google-ai-client-sdk#generate-api-key) contain more info about this.
- Create a gendright.properties file in the root of your project, and place your API key in that file with the variable name `geminiAPIkey`. This is how the end result should look like in the gendright.properties file:
    ```
    geminiAPIkey=[your API key]
  ```
  where your API key replaces `[your API key]`

Technologies
------------

- Android
- Jetpack Compose with Kotlin
- Android Views with Kotlin
- Google AI Client SDK

Contributing to Gendright
------------
We love contributions! The codebase is straightforward enough for anyone to play around with and build more features that would improve the application. Please feel free to reach out to see how you can help.

Author
------------
- Victoria Alajemba - alaje.vik@gmail.com

Acknowledgments
------------
- Grateful to the Google team for making Gemini accessible
- Special thanks to my primary source of inspiration - Grammarly. 

License
------------
Gendright is licensed under the [MIT License](https://github.com/alajemba-vik/gendright/blob/main/LICENSE)
