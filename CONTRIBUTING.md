# Contributing to Typly

Thank you for your interest in contributing to Typly! We welcome contributions from the community and are pleased to have you join us.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Security Guidelines](#security-guidelines)
- [Issue Reporting](#issue-reporting)

## 📜 Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to [maintainer-email].

### Our Standards

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24 (API level 24) or higher
- Kotlin 1.8+
- Java 8+
- Git

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/yourusername/typly.git
   cd typly
   ```

## 🛠️ Development Setup

### 1. Environment Configuration

1. Copy the example properties file:
   ```bash
   cp local.properties.example local.properties
   ```

2. Fill in your actual values in `local.properties`:
   ```properties
   GOOGLE_WEB_CLIENT_ID=your_google_client_id
   SECRET_KEY=your_16_byte_secret_key
   ```

3. Add your `google-services.json` file to the `app/` directory

### 2. Build the Project

```bash
./gradlew build
```

### 3. Run Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 🤝 How to Contribute

### Types of Contributions

We welcome several types of contributions:

- **Bug fixes** - Fix issues in the codebase
- **Feature development** - Add new features
- **Documentation** - Improve docs, README, etc.
- **Testing** - Add or improve tests
- **Performance** - Optimize existing code
- **Security** - Enhance security measures
- **UI/UX** - Improve user interface and experience

### Before You Start

1. **Check existing issues** - Look for existing issues that match your contribution
2. **Create an issue** - If none exists, create one describing your planned contribution
3. **Discuss the approach** - Comment on the issue to discuss your approach
4. **Get assignment** - Wait for maintainer approval before starting work

## 🔄 Pull Request Process

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-number-description
```

### 2. Make Your Changes

- Follow the [coding standards](#coding-standards)
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass

### 3. Commit Your Changes

Use conventional commit messages:

```bash
git commit -m "feat: add user profile image upload"
git commit -m "fix: resolve chat message ordering issue"
git commit -m "docs: update API documentation"
```

### 4. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 5. Create Pull Request

1. Go to the original repository on GitHub
2. Click "New Pull Request"
3. Fill out the PR template
4. Link related issues
5. Request review from maintainers

### Pull Request Guidelines

- **One feature per PR** - Keep PRs focused and atomic
- **Clear description** - Explain what and why, not just how
- **Screenshots** - Include screenshots for UI changes
- **Tests** - Ensure all tests pass
- **Documentation** - Update relevant documentation
- **No merge conflicts** - Rebase if needed

## 📝 Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getUserById(userId: String): User? {
        return try {
            firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject<User>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: ${e.message}", e)
            null
        }
    }
}
```

### Architecture Guidelines

- **MVVM Pattern** - Use ViewModel for UI-related data
- **Clean Architecture** - Separate data, domain, and presentation layers
- **Repository Pattern** - Abstract data sources
- **Use Cases** - Encapsulate business logic
- **Dependency Injection** - Use Hilt for DI

### Documentation Standards

- **KDoc comments** - Document all public APIs
- **Class documentation** - Explain purpose and usage
- **Function documentation** - Document parameters and return values
- **Complex logic** - Add inline comments for clarity

```kotlin
/**
 * Repository for managing user authentication operations.
 * 
 * Provides methods for login, registration, and user session management
 * using Firebase Authentication service.
 * 
 * @property auth Firebase Authentication instance
 * @property firestore Firestore database instance for user data
 */
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    /**
     * Authenticates user with email and password.
     * 
     * @param email User's email address
     * @param password User's password
     * @return Flow of AuthResult indicating success or failure
     */
    suspend fun login(email: String, password: String): Flow<AuthResult>
}
```

## 🔒 Security Guidelines

### Sensitive Data

- **Never commit** API keys, passwords, or secrets
- **Use local.properties** for sensitive configuration
- **Review changes** before committing to avoid leaks

### Logging

- **No sensitive data** in logs (passwords, tokens, personal info)
- **Use appropriate log levels** (Debug for development, Error for problems)
- **Disable debug logs** in production builds

```kotlin
// ✅ Good
Log.d(TAG, "User login attempt for email: ${email.take(3)}***")

// ❌ Bad
Log.d(TAG, "User login: email=$email, password=$password")
```

### Code Security

- **Input validation** - Validate all user inputs
- **SQL injection prevention** - Use parameterized queries
- **XSS prevention** - Sanitize output
- **Authentication checks** - Verify user permissions

## 🐛 Issue Reporting

### Bug Reports

When reporting bugs, please include:

- **Clear title** - Describe the issue briefly
- **Steps to reproduce** - Detailed reproduction steps
- **Expected behavior** - What should happen
- **Actual behavior** - What actually happens
- **Screenshots** - Visual evidence if applicable
- **Environment details** - OS version, device, app version
- **Logs** - Relevant log output (without sensitive data)

### Feature Requests

For new features, please include:

- **Problem description** - What problem does this solve?
- **Proposed solution** - How should it work?
- **Alternatives considered** - Other approaches you thought about
- **Additional context** - Any other relevant information

## 🏷️ Labels and Milestones

We use labels to categorize issues and PRs:

- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Improvements or additions to documentation
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `security` - Security-related issues
- `performance` - Performance improvements

## 🎯 Development Workflow

### Branch Naming

- `feature/description` - New features
- `fix/issue-number` - Bug fixes
- `docs/description` - Documentation updates
- `refactor/description` - Code refactoring
- `test/description` - Test additions/improvements

### Commit Message Format

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, etc.)
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Build process or auxiliary tool changes

### Release Process

1. **Version bump** - Update version in `build.gradle.kts`
2. **Changelog** - Update CHANGELOG.md
3. **Tag release** - Create git tag
4. **GitHub release** - Create release on GitHub
5. **Deploy** - Deploy to app stores (if applicable)

## 📞 Getting Help

- **GitHub Issues** - For bugs and feature requests
- **GitHub Discussions** - For questions and general discussion
- **Email** - [maintainer-email] for private inquiries

## 🙏 Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes for significant contributions
- GitHub contributors graph

Thank you for contributing to Typly! 🚀