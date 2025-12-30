# Encryptionkit for Android 🛡️

**"Military-grade privacy for your app's data."**

Encryptionkit is a high-performance, security-focused library for Android designed to provide a robust abstraction layer over the **Android Keystore System** and **Java Cryptography Architecture (JCA)**. It enforces modern cryptographic standards and hardware-backed security to protect sensitive information at rest and in transit.

## 🚀 Key Features

- **Hardware-Backed Security**: Seamless integration with **TEE (Trusted Execution Environment)** and **StrongBox** to ensure keys never leave the secure hardware.
- **Authenticated Encryption**: Uses **AES-GCM (256-bit)** by default to ensure both data confidentiality and integrity (AEAD).
- **Zero-Trust Memory**: Optimized handling of sensitive data using **`SecureBytes`** to explicitly wipe data from memory after use.
- **Asymmetric Encryption**: Support for **RSA-OAEP (SHA-256)** with optional **Public Key Pinning** to prevent Man-in-the-Middle attacks.
- **Advanced Key Management**: Automatic generation and storage of keys within the Android Keystore with restricted purposes.
- **Biometric Integration**: Built-in support for `BiometricPrompt` to protect key usage with mandatory user authentication.
- **Clean Architecture**: Decoupled design with a simplified Builder pattern, removing the need for dependency injection frameworks in the consumer app.

## 🏗 Architecture

Encryptionkit follows a strict **Clean Architecture** to ensure that cryptographic logic is isolated, testable, and secure.

```mermaid
graph TD
    subgraph "SDK Layer (Public API)"
        Builder[EncryptionkitManager.Builder]
        Facade[EncryptionkitManager Facade]
    end

    subgraph "Domain Layer (Pure Kotlin)"
        UC[UseCases (Encrypt/Decrypt)]
        RepoInterface[EncryptionRepository]
        Models[SecureBytes, CryptoResult]
    end

    subgraph "Data Layer (Implementation)"
        RepoImpl[EncryptionRepositoryImpl]
        KS_DS[KeystoreDataSource]
        File_DS[FileDataSource]
    end

    subgraph "Android System"
        KS[Android Keystore System]
        TEE[TEE / StrongBox]
    end

    Builder -- constructs --> Facade
    Facade -- delegates to --> UC
    UC -- uses --> RepoInterface
    RepoImpl -- implements --> RepoInterface
    RepoImpl --> KS_DS
    RepoImpl --> File_DS
    KS_DS --> KS
    KS --> TEE
```

## 🛠 Usage Example

### 1. Initialize
Initialize the library using the Builder. No `Context` is required for the builder itself.

```kotlin
val encryptionManager = EncryptionkitManager.Builder()
    .setAlias("my_app_secure_key")
    .useStrongBox(true) // Prefer Secure Element
    .setRequireUserAuthentication(false)
    .build()
```

### 2. Encrypt Sensitive Data (Securely)
Use `SecureBytes` to ensure sensitive data is wiped from memory after the operation.

```kotlin
val sensitiveData = "Top Secret".toByteArray()
val secureBytes = SecureBytes(sensitiveData)

try {
    // The 'use' block automatically calls .close() to wipe memory
    val result = secureBytes.use { secure ->
        encryptionManager.encrypt(secure)
    }
    // 'result' contains ciphertext and IV
} catch (e: CryptoException) {
    // Handle error (e.g., Key Invalidated)
}
```

### 3. Decrypt
Retrieve the original information.

```kotlin
try {
    val decryptedBytes = encryptionManager.decrypt(result.ciphertext, result.iv)
    val originalString = String(decryptedBytes)
} catch (e: CryptoException) {
    // Handle decryption errors (integrity check failed)
}
```

### 4. Asymmetric Encryption (RSA) with Pinning
Encrypt data for a backend server using its public key.

```kotlin
val builder = EncryptionkitManager.Builder()
    .setCertificatePathProvider(MyCertProvider()) // Implements CertificatePathProvider
    .setPublicKeyPinning("a1b2c3d4...") // Expected SHA-256 hash of the Public Key
    .build()

val encryptedPayload = builder.encryptWithPublicKey(data)
```

## 📂 Project Structure

- `:library` (`es.joshluq.encryptionkit`)
    - `sdk`: Public API Facade and Builder.
    - `domain`: Pure Kotlin business logic (`model`, `usecase`, `repository`, `provider`).
    - `data`: Android-specific implementations (`datasource`, `repository impl`).
- `:showcase`: A sample app demonstrating all features, including TEE verification and secure memory usage.

## 🧪 Quality Assurance

- **Compliance**: Strictly follows **NIST** and **Android Security** best practices.
- **KDocs**: 100% complete API documentation.
- **Testing**: Comprehensive suite of unit tests for cryptographic logic and instrumented tests (AndroidTests) for Keystore validation.

---

*Developed with a security-first mindset.*
