Gendright
==============================

Gendright is an Android accessibility service designed to help users identify and rephrase gender-biased language in real-time as they type. By analyzing text for gender-related biases, Gendright provides non-intrusive suggestions to promote inclusive language across various apps and social platforms.

Features
------------
- **Real-Time Suggestions** : Detects gender-biased phrases and suggests more inclusive alternatives.
- **Seamless Integration**: Works across Android apps with an accessible floating icon that users can tap to see suggestions.
- **Customizable Feedback**: Users can select and edit suggestions, fostering a more conscious and respectful communication style.

How It Works
------------

GendRight uses Vertex AI Gemini API with the Gemini 1.5 Flash model to analyze text and offer relevant suggestions. Built with Firebase, the app ensures secure interaction with the model. The app's implements dynamic theming to really provide users a more personal and safe feeling.

Getting Started
------------
Test Gendright locally

## Prerequisites

- Kotlin knowledge
- Ability to use Android Studio and test apps in an emulator or physical device running Android API level 24 or higher
- A Firebase project connected to Gendright

## Setup

- [Set up Firebase and connect to Vertex AI](https://firebase.google.com/docs/vertex-ai/get-started?platform=android#set-up-firebase).
- Build to run Gendright on a physical Android device or emulator

Technologies
------------

- Android
- Jetpack Compose with Kotlin
- Android Views with Kotlin
- Vertex AI in Firebase (Gemini API)

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

Acknowledgments
------------
Gendright is licensed under the [MIT License]()