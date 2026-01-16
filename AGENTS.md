# Encryptionkit: Cybersecurity & Technical Context

"Military-grade privacy for your app's data."

## Overview
Encryptionkit is a high-assurance security framework for Android. It provides a robust abstraction layer over the **Android Keystore System** and **Java Cryptography Architecture (JCA)**, enforcing modern cryptographic standards and hardware-backed security. It is designed to mitigate common vulnerabilities like hardcoded keys, insecure random number generation, memory leaks of sensitive data, and Man-in-the-Middle attacks.

## Security Architecture
The library implements a **Defense-in-Depth** strategy, ensuring that sensitive data is protected even if the application process or the OS is partially compromised.

### Cryptographic Principles
- **Authenticated Encryption (AEAD)**: Uses **AES-GCM (256-bit)** by default to provide both confidentiality and authenticity, preventing ciphertext tampering attacks.
- **Hardware-Backed Isolation**: Leverages **Trusted Execution Environment (TEE)** and **StrongBox (Secure Element)** to ensure private key material never enters the application process memory.
- **Least Privilege**: Cryptographic keys are generated with restricted `KeyProperties` (e.g., `PURPOSE_ENCRYPT | PURPOSE_DECRYPT` only).
- **Zero-Trust Memory**: Implementation of `SecureBytes` pattern to wrap sensitive data and wipe it (overwrite with zeros) from the Heap immediately after use, mitigating memory dump attacks.

## Core Features (Hardened)
- **Android Keystore Management**: 
    - **Hardware Security Level**: Prefers `StrongBox` when available, falling back to `TEE`.
    - **User Authentication Binding**: Integration with `BiometricPrompt` for "Auth-per-use" or "Time-bound" key access.
    - **Security Verification**: Capability to query the runtime security level (`StrongBox`, `TEE`, or `Software`) of the generated key.
    - **Lifecycle Management**: Support for secure key deletion.
    - **Granular Error Handling**: Detailed failure reasons (`KEY_PERMANENTLY_INVALIDATED`, `USER_NOT_AUTHENTICATED`) to guide the UI flow.
- **Symmetric Encryption (AES)**:
    - **AES/GCM/NoPadding (256-bit)**: The primary standard for data at rest. Enforces unique, non-deterministic IV generation via `SecureRandom` (never accepts external IVs for encryption).
- **Asymmetric Cryptography**:
    - **RSA-OAEP (2048/4096-bit)**: Secure key wrapping using **SHA-256** for both main and MGF1 digests.
    - **Public Key Pinning**: Optional validation of the public key's fingerprint (SHA-256) before encryption to prevent MITM attacks.
    - **Certificate Integration**: Flexible `CertificatePathProvider` interface to load public keys from X.509 certificates.
- **Integrity & Hashing**:
    - **SHA-256**: Recommended standard for secure one-way hashing.
    - **MD5**: Supported for legacy compatibility (not recommended for new systems).

## Implementation Standards (Compliance)
- **Algorithm Selection**: Strictly follows [NIST](https://www.nist.gov/) and [Android Security](https://developer.android.com/privacy-and-security/cryptography) recommendations.
- **Entropy**: All nonces, IVs, and salts are generated using `java.security.SecureRandom`.
- **Provider Policy**: Always uses the default system provider for JCA, except when explicitly interacting with `AndroidKeyStore`.
- **Memory Safety**: Sensitive payloads are handled via `SecureBytes` implementing `AutoCloseable` for deterministic memory clearing.

## Testing & Validation
- **Cryptographic KATs**: Verification of algorithm implementations against standard test vectors.
- **Security Audits**: Isolated code paths designed for easy auditing, separating key management from encryption logic.
- **Device Compatibility**: Instrumented tests to ensure correct behavior across diverse Android OEM Keystore implementations.
