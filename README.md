# Typly - Modern Chat Application

Typly is a next-gen chat app developed with Jetpack Compose and Firebase. It includes everything you'd expect from a modern messaging platform: real-time conversations with typing indicators, searchable chat history, push notifications, voice calling, and secure AES-encrypted tokens. Designed with glassmorphic aesthetics and built on clean architecture principles, Typly is not just a demo, it’s a complete, production-ready messaging experience.

<img width="200" height="200" alt="image" src="https://github.com/user-attachments/assets/4e6e56c5-e293-4d51-acf2-65a440c65f25" /> 

## >🖼️Screenshots

<h3>🚀 Onboarding Screens</h3>
<p align="center">
  <img src="https://github.com/user-attachments/assets/b1827dfd-a1e8-4500-812f-4a5ec498fcc2" width="220" />
  <img src="https://github.com/user-attachments/assets/23bd743f-0363-4c2b-82a1-64935e78725d" width="220" />
  <img src="https://github.com/user-attachments/assets/985baa9d-f27e-4b03-895d-33349e76dc8d" width="220" />
</p>

<h3>🔐 Authentication</h3>
<p align="center">
  <img src="https://github.com/user-attachments/assets/8557ee35-f7d6-4039-bd52-8b14cc8566ec" width="220" />
</p>

<h3>💬 In-App Experience</h3>
<p align="center">
  <img src="https://github.com/user-attachments/assets/65173abb-1528-4026-ad94-f2714d782262" width="220" />
  <img src="https://github.com/user-attachments/assets/150ea867-79ef-49a9-be1f-bcd6ed6a559e" width="220" />
</p>



## 🚀 Features

### 📱 Core Functionality
- **Real-time messaging** with text and image support
- **Voice calls** powered by Agora SDK
- **User authentication** (Email/Password & Google Sign-In)
- **Profile management** with image upload
- **Push notifications** via Firebase Cloud Messaging
- **User search** and discovery

### 🔒 Security Features
- **End-to-end encrypted tokens** for chat sessions
- **Secure token management** with AES encryption
- **Chat access control** and blocking system
- **Security audit logging**
- **Permission-based access** to device features

### 🎨 UI/UX
- **Glassmorphism design** with modern aesthetics
- **Dark theme** with beautiful gradients
- **Smooth animations** and transitions
- **Responsive layout** for all screen sizes
- **Onboarding experience** for new users

## 🛠️ Technical Stack

### Frontend
- **Jetpack Compose** - Modern Android UI toolkit
- **Navigation Compose** - Type-safe navigation
- **Material 3** - Latest Material Design components
- **Coil** - Image loading and caching
- **Accompanist** - Additional Compose utilities

### Backend & Services
- **Firebase Authentication** - User management
- **Firestore Database** - Real-time data storage
- **Firebase Storage** - Image and file storage
- **Firebase Cloud Messaging** - Push notifications
- **Agora SDK** - Voice call functionality

### Architecture
- **MVVM** - Model-View-ViewModel pattern
- **Clean Architecture** - Separation of concerns
- **Dependency Injection** - Hilt/Dagger
- **Repository Pattern** - Data layer abstraction
- **Use Cases** - Business logic encapsulation

## 📋 Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24 (API level 24) or higher
- Kotlin 1.8+
- Java 8+

## 🔧 Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/mertcoder/typly.git
cd typly
```

### 2. Firebase Configuration
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app to your project
3. Download `google-services.json` and place it in the `app/` directory
4. Enable the following Firebase services:
   - Authentication (Email/Password & Google)
   - Firestore Database
   - Cloud Storage
   - Cloud Messaging

### 3. Agora Configuration
1. Create an account at [Agora.io](https://www.agora.io)
2. Create a new project and get your App ID
3. Add your App ID to the project configuration

### 4. Build Configuration
1. Open the project in Android Studio
2. Sync project with Gradle files
3. Build and run the application

## 🔐 Security Notes

⚠️ **IMPORTANT**: This repository contains example configurations. For production use:

1. **Never commit sensitive files**:
   - `google-services.json` (already in .gitignore)
   - API keys or secrets
   - Keystore files

2. **Update security configurations**:
   - Change the SECRET_KEY in `SecureTokenManager`
   - Use environment variables for sensitive data
   - Review and disable debug logging for production

3. **Implement additional security measures**:
   - Enable ProGuard/R8 obfuscation
   - Add certificate pinning
   - Implement rate limiting

## 📁 Project Structure

```
app/
├── src/main/java/com/typly/app/
│   ├── data/           # Data layer (repositories, DTOs)
│   ├── domain/         # Domain layer (use cases, models)
│   ├── presentation/   # UI layer (screens, components)
│   ├── services/       # Background services (FCM)
│   ├── util/           # Utilities and helpers
│   └── di/             # Dependency injection modules
├── res/                # Resources (layouts, strings, etc.)
└── build.gradle.kts    # App-level build configuration
```

## 🎯 Key Components

### Authentication Flow
- Splash screen with auto-login
- Onboarding for new users
- Login/Register with validation
- Profile setup completion

### Main Application
- Home screen with navigation
- Chat list and conversations
- User search and discovery
- Profile management
- Settings and preferences

### Security System
- Token-based authentication
- Encrypted chat sessions
- Access control validation
- Audit logging

## 🚀 Building for Production

1. **Update build configuration**:
   ```kotlin
   buildTypes {
       release {
           isMinifyEnabled = true
           proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
           signingConfig = signingConfigs.getByName("release")
       }
   }
   ```

2. **Configure signing**:
   - Create a release keystore
   - Update signing configuration
   - Never commit keystore files

3. **Security hardening**:
   - Enable R8 full mode
   - Remove debug logging
   - Add obfuscation rules

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in this repository
- Contact: [mertakybusiness@gmail.com]

## 🙏 Acknowledgments

- Firebase team for excellent backend services
- Agora team for voice call capabilities
- Android Jetpack Compose team
- All open-source contributors

---

**⚠️ Security Disclaimer**: This is a demonstration project. Ensure proper security measures are implemented before using in production environments.
