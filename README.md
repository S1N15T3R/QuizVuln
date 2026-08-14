<div align="center">

# 🛡️ QuizVuln

### Master the **OWASP Top 10** by playing  an AI-powered Android quiz that generates fresh, never-repeated vulnerability questions on demand.

[![Platform](https://img.shields.io/badge/platform-Android%2024%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Build](https://img.shields.io/badge/build-Gradle%208.13-blueviolet)](https://gradle.org/)
[![API](https://img.shields.io/badge/AI-OpenRouter%20%2F%20DeepSeek-FF6B00)](https://openrouter.ai/)
[![License](https://img.shields.io/badge/license-MIT-green)](#license)
[![Min SDK](https://img.shields.io/badge/minSdk-24%20(Android%207.0)-purple)](https://developer.android.com/)

<br>

> _"The best way to learn web security is to be quizzed on it until the answers stick."_

</div>

---

## 📱 What is QuizVuln?

**QuizVuln** is a native Android application built to help developers, students, and security enthusiasts
sharpen their knowledge of the **OWASP Top 10** web application security risks. Instead of a fixed
question bank that you memorize once and forget, QuizVuln **generates brand-new questions on the fly**
using a Large Language Model (via [OpenRouter](https://openrouter.ai/) → DeepSeek Chat).

Pick a topic, pick a difficulty, pick how many questions you want — and the app builds a unique quiz
for you in seconds. Every attempt is saved locally so you can track progress over time.

---

## ✨ Features

| | Feature | Description |
|---|---------|-------------|
| 🤖 | **AI-Generated Questions** | Questions are synthesized live by an LLM  no two quizzes are ever the same. |
| 🎯 | **OWASP Top 10 Coverage** | Drill into any of the 10 categories, from Injection to SSRF. |
| 🎚️ | **Difficulty Levels** | `easy` · `hard` · `advanced` to grow with you. |
| 🔢 | **Flexible Length** | 5, 10, or 20 questions per round. |
| 📊 | **Stats & Charts** | Visualize performance with MPAndroidChart. |
| 🔍 | **Search History** | Filter past attempts by category or difficulty. |
| 👤 | **Profiles** | Local player profile with points and avatar. |
| 📈 | **Review Mode** | Re-read every question, your answer, and the correct one. |
| 🎉 | **Confetti & Animations** | Lottie + confetti make progress feel good. |
| 💾 | **Offline History** | All results persist locally via Room (SQLite). |

---

## 🧠 The OWASP Top 10, in-app

When you tap **Play Now**, you can choose to focus on any of these categories:

```
1.  Broken Access Control
2.  Cryptographic Failures
3.  Injection
4.  Insecure Design
5.  Security Misconfiguration
6.  Vulnerable and Outdated Components
7.  Identification and Authentication Failures
8.  Software and Data Integrity Failures
9.  Security Logging and Monitoring Failures
10. Server-Side Request Forgery (SSRF)
```

---

## 🛠️ Tech Stack

<div align="center">

| Layer | Technology |
|-------|------------|
| **Language** | Java 11 |
| **UI** | Material Design 3 · ViewBinding · Lottie |
| **Architecture** | Activities · Adapters · DAO pattern |
| **Networking** | Retrofit 2 · OkHttp 3 · Gson |
| **AI Backend** | OpenRouter API → `deepseek/deepseek-chat` |
| **Local Storage** | Room (SQLite) |
| **Charts** | MPAndroidChart |
| **Images** | Glide |
| **Build** | Gradle 8.13 (Kotlin DSL version catalog) |
| **Min / Target SDK** | 24 / 34 |

</div>

---

## 🗂️ Project Structure

```
QuizVuln/
├── app/
│   ├── src/main/java/com/team404bnf/quizvuln/
│   │   ├── activities/        # Splash, Dashboard, Quiz, Result, Review, Profile, Stats
│   │   ├── adapters/          # RecyclerView adapters for results
│   │   ├── dao/               # Room DAOs (Profile, Result, QuizResult)
│   │   ├── database/          # Room AppDatabase
│   │   ├── models/            # Profile, QuizQuestion, QuizResult, Result
│   │   └── network/           # OpenRouter client + Retrofit service
│   └── src/main/res/          # Layouts, drawables, animations, values
├── gradle/                    # Wrapper + version catalog
├── build.gradle               # Project config
├── gradle.properties          
├── local.properties           
└── QuizVuln.apk               # Debug build (ready to install)
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest stable) or just the Android SDK (API 34)
- **JDK 11+**
- An **[OpenRouter API key](https://openrouter.ai/keys)** (free tier works)

### 1. Clone

```bash
git clone git@github.com:S1N15T3R/QuizVuln.git
cd QuizVuln
```

### 2. Add your API key

 Create your own local config:

```bash
cp gradle.properties.example gradle.properties
# then edit gradle.properties and set:
# OPENROUTER_API_KEY=your_openrouter_key_here
```

### 3. Build & run

```bash
./gradlew assembleDebug      # builds app-debug.apk
# or just hit ▶ Run in Android Studio with an emulator / device
```

### 📦 Install the included APK

A ready-to-install debug build ships with the repo:

```
QuizVuln.apk
```

Enable **Install from unknown sources** on your Android device, transfer the APK,
and tap to install. (Requires Android 7.0 / API 24+.)

---

## 🔐 How the AI Quiz Works

1. You choose **# questions**, a **vulnerability category**, and a **difficulty**.
2. The app builds a strict JSON prompt and POSTs it to OpenRouter
   (`chat/completions`, model `deepseek/deepseek-chat`) with your key as a Bearer token.
3. The model returns questions conforming to a fixed schema
   (`question`, `options[A–D]`, `correct_answer`, `hint`, `difficulty`, `category`).
4. The quiz is played offline; the result is stored locally in Room for later review.

---

## 🤝 Contributing

Pull requests are welcome! For larger changes, please open an issue first to discuss
what you'd like to change.

1. Fork the repo
2. Create a branch (`git checkout -b feature/amazing`)
3. Commit your changes
4. Push (`git push origin feature/amazing`)
5. Open a PR

---

## ⚠️ Disclaimer

QuizVuln is an **educational** tool intended to help learners understand web security
concepts. Quiz content is AI-generated and may contain inaccuracies always verify
against the [official OWASP Top 10](https://owasp.org/Top10/) documentation.

---

## 📄 License

Released under the [MIT License](https://opensource.org/licenses/MIT).

<div align="center">

**Built to make web security practice addictive.** 🛡️📱

</div>
