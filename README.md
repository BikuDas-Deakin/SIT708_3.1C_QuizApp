# SIT708 Quiz App – Task 3.1

An Android multiple-choice quiz application built for SIT708 Credit Task 3.1.

## Features

- **Welcome Screen** – Enter your name to begin the quiz
- **Quiz Screen** – 5 multiple-choice questions with answer selection
- **Visual Feedback** – Correct answers turn green; wrong answers turn red (correct also revealed)
- **Real-Time Progress Bar** – Updates dynamically as questions are answered
- **Results Screen** – Shows final score with option to retake or finish
- **Session Persistence** – User's name is retained when taking a new quiz
- **Dark / Light Mode Toggle** – Persists across all screens throughout the session

## Subtasks Completed

| Subtask | Description | Status |
|---------|-------------|--------|
| 1 | Answer Selection & Visual Feedback | ✅ |
| 2 | Real-Time Progress Tracking | ✅ |
| 3 | Final Score & Session Persistence | ✅ |
| 4 | Research Report – On-Device LLM Integration | ✅ (in submission doc) |
| 5 | Dark Mode / Light Mode Toggle | ✅ |

## Project Structure

```
app/src/main/
├── java/com/example/quizapp/
│   ├── MainActivity.java       # Welcome screen
│   ├── QuizActivity.java       # Quiz logic + progress bar + visual feedback
│   ├── ResultsActivity.java    # Results + session persistence
│   └── Question.java           # Question data model
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_quiz.xml
│   │   └── activity_results.xml
│   ├── values/
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── values-night/
│       └── themes.xml
└── AndroidManifest.xml
```

## How to Run

1. Clone the repository
2. Open in Android Studio (Hedgehog or later recommended)
3. Sync Gradle dependencies
4. Run on an emulator or physical device (API 24+)

## Tech Stack

- Java
- Android SDK (API 24+, target 34)
- Material Design 3 Components
- SharedPreferences (session persistence)
- AppCompatDelegate (theme switching)

## LLM Declaration
I declare that I have used a Large Language Model (ChatGPT) as a supplementary tool while completing this assignment. The majority of the work, including the design, coding, and implementation, was completed independently by me.
The AI tool was used to provide limited assistance in the following areas:
•	Clarifying concepts related to Android development and Java implementation
•	Debugging minor issues and improving code structure
•	Refining parts of the research report for clarity and coherence
•	Assisting with documentation such as the README file
