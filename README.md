

#  SmartKitch – AI-Powered Smart Kitchen App

SmartKitch is an **AI-powered smart kitchen assistant** designed to help users manage their kitchen efficiently, reduce food waste, and cook smarter.
The app is especially built for **households and students living in dormitories**, with a focus on simplicity, automation, and usability.

---

##  Features

###  Smart Inventory Management

* Add food items manually or by scanning
* Auto-categorization (Fridge / Freezer / Pantry)
* Track quantity and expiry dates

###  Expiry Alerts & Waste Reduction

* Get notified before items expire
* Auto-remove expired items (optional)
* Expired items suggested directly into shopping list

###  Smart Shopping List

* Create a cart-like shopping list
* Suggested items based on expired or missing inventory
* Share shopping list via **WhatsApp, LINE(Taiwan), or any sharing app**

###  AI Recipe Suggestions

* Get recipe recommendations based on available ingredients
* Step-by-step cooking mode with:

    * Large readable instructions
    * Voice guidance
    * Timer support
    * Cooking progress indicator

###  Authentication

* Email & Password login
* Google Sign-In
* LINE Login for Taiwan Users

###  Localization & Preferences

* Multi-language support (English + planned Traditional Chinese)
* Cuisine preferences
* Spice level & cooking time preferences

---

##  Tech Stack

* **Platform:** Android
* **Language:** Kotlin
* **UI:** Jetpack Compose
* **Architecture:** MVVM
* **Dependency Injection:** Hilt
* **Backend:** Firebase

    * Authentication
    * Firestore Database
    * Cloud Storage
* **Build System:** Gradle (KTS)
* **Version Control:** Git & GitHub

---

##  Project Architecture

```
com.smartkitch.app
│
├── data
│   ├── repository
│   └── models
│
├── domain
│   └── repository
│
├── presentation
│   ├── screens
│   ├── viewmodels
│   └── components
│
├── di
│   └── AppModule.kt
│
├── util
└── ui.theme
```

---

##  Getting Started

### Prerequisites

* Android Studio (latest version recommended)
* Android SDK 33+
* Firebase project setup

### Steps to Run Locally

1. Clone the repository

   ```bash
   git clone https://github.com/your-username/SmartKitch.git
   ```
2. Open the project in **Android Studio**
3. Add your `google-services.json` file
4. Sync Gradle
5. Run on an emulator or physical device

---

##  Privacy & Security

* User data is securely stored using Firebase
* No sensitive information is shared with third parties
* Camera access is used **only for scanning food items**
* Privacy policy is provided for Play Store compliance

---

## 🎓 Academic Context

This application is **developed by a student of National Taipei University (NTPU)**
as an academic and practical project aimed at solving real-life problems faced by:

* Students living in dormitories
* Small households managing daily cooking and groceries

---

## 🛣️ Future Roadmap

* Traditional Chinese (Taiwanese) language support
* Barcode & AI-based food recognition
* Nutrition & calorie tracking
* Cloud sync across multiple devices
* Smart meal planning for the week
* Wearable & smart home integration

---

##  Release & Distribution

* Distributed via **Google Play Store**
* Internal testing enabled through Play Console
* Android App Bundle (.aab) used for releases

---

##  Developer

**Lokendra Singh && Johnny**
Students of 
National Taipei University (NTPU)

---

##  License

This project is for **educational and academic purposes**.
All rights reserved © SmartKitch.

---


