# Encryptionkit: Cybersecurity & Technical Context

"Military-grade privacy for your app's data."

## Overview
Encryptionkit is a high-assurance security framework for Android. It provides a robust abstraction layer over the **Android Keystore System** and **Java Cryptography Architecture (JCA)**, enforcing modern cryptographic standards and hardware-backed security. It is designed to mitigate common vulnerabilities like hardcoded keys, insecure random number generation, and lack of integrity checks.

## Security Architecture
The library implements a **Defense-in-Depth** strategy, ensuring that sensitive data is protected even if the application process or the OS is partially compromised.

### Cryptographic Principles
- **Authenticated Encryption (AEAD)**: Uses **AES-GCM** by default to provide both confidentiality and authenticity, preventing ciphertext tampering attacks.
- **Hardware-Backed Isolation**: Leverages **Trusted Execution Environment (TEE)** and **StrongBox (Secure Element)** to ensure private key material never enters the application process memory.
- **Least Privilege**: Cryptographic keys are generated with restricted `KeyProperties` (e.g., specific purposes like `PURPOSE_ENCRYPT` only).

## Core Features (Hardened)
- **Android Keystore Management**: 
    - **Hardware Security Level**: Prefers `StrongBox` when available, falling back to `TEE`.
    - **User Authentication Binding**: Integration with `BiometricPrompt` for "Auth-per-use" or "Time-bound" key access.
    - **Key Attestation**: Support for verifying that keys are genuinely hardware-backed and haven't been tampered with.
- **Symmetric Encryption (AES)**:
    - **AES/GCM/NoPadding (256-bit)**: The primary standard for data at rest. Enforces unique, non-deterministic IV generation via `SecureRandom`.
    - **AES/CBC/PKCS7Padding**: Provided for legacy interoperability, requiring manual HMAC for integrity.
- **Asymmetric Cryptography**:
    - **RSA-OAEP (2048/4096-bit)**: Secure key wrapping and small data encryption using SHA-256 for both main and MGF1 digests.
    - **ECDSA (secp256r1)**: Modern, efficient digital signatures for data provenance and non-repudiation.
- **Integrity & Hashing**:
    - **HMAC-SHA256**: Keyed-hash message authentication using Keystore-backed secret keys.
    - **SHA-256/512**: For secure one-way hashing and data fingerprinting.
- **Secure Data Storage**:
    - **Encrypted Preferences**: High-level API for secure key-value storage (alternative to the deprecated Jetpack Security library).
    - **Database Field Encryption**: Transparent encryption/decryption hooks for SQL-based storage.

## Implementation Standards (Compliance)
- **Algorithm Selection**: strictly follows [NIST](https://www.nist.gov/) and [Android Security](https://developer.android.com/privacy-and-security/cryptography) recommendations.
- **Entropy**: All nonces, IVs, and salts are generated using `java.security.SecureRandom`.
- **Provider Policy**: Always uses the default system provider for JCA, except when explicitly interacting with `AndroidKeyStore`, to ensure system-wide security patches are applied.
- **Zero-Trust Memory**: Use of `CharArray` or `ByteArray` for sensitive data to allow for explicit clearing (wiping) from memory where possible.

## Testing & Validation
- **Cryptographic KATs (Known Answer Tests)**: Verification of algorithm implementations against standard test vectors.
- **Security Audits**: Code paths are designed for easy auditing, separating key management from encryption logic.
- **Device Compatibility**: Instrumented tests to ensure correct behavior across diverse Android OEM Keystore implementations.
