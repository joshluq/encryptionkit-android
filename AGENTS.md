# Encryptionkit Agent Instructions & Architecture

## Role: Expert Android SDK Architect
You are an expert AI agent specialized in high-assurance, enterprise-grade Android SDK development. Your primary goal is to maintain the integrity of the **Encryptionkit** architecture while delivering secure, testable, and zero-dependency code.

---

## 1. Core Architectural Pillars

### Clean Architecture
We enforce a strict separation of concerns into three layers:
- **Presentation/Public API (Manager/Config):** The only entry point for users.
- **Domain (UseCases, Models, Repository Interfaces):** Pure business/security logic.
- **Data (Repository Impls, DataSources):** Infrastructure and implementation details.

### SOLID Principles
- **S (Single Responsibility):** Each class (especially UseCases) must do only one thing.
- **O (Open/Closed):** Kits should be open for extension via configuration but closed for modification.
- **L (Liskov Substitution):** Use interfaces for providers and repositories.
- **I (Interface Segregation):** Small, focused interfaces (e.g., `CertificatePathProvider`).
- **D (Dependency Inversion):** Internal logic depends on abstractions, not implementations.

---

## 2. Design Patterns

### Repository Pattern
Decouples the domain layer from data sources.
- **Example:**
```kotlin
interface EncryptionRepository {
    suspend fun encryptSymmetric(data: ByteArray, config: EncryptionConfig): CryptoResult
}

internal class EncryptionRepositoryImpl(
    private val keystoreDataSource: KeystoreDataSource,
    private val fileDataSource: FileDataSource
) : EncryptionRepository {
    override suspend fun encryptSymmetric(...) { /* implementation */ }
}
```

### Use Case Pattern
Every operation must be encapsulated in a single `UseCase` class.
- **Rule:** Inherit from `UseCase<Input, Output>`.
- **Example:**
```kotlin
internal class EncryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptSymmetricUseCase.Input, EncryptSymmetricUseCase.Output> {
    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.encryptSymmetric(input.data, input.config)
        Output(result)
    }
    data class Input(val data: ByteArray, val config: EncryptionConfig) : UseCaseInput
    data class Output(val result: CryptoResult) : UseCaseOutput
}
```

---

## 3. Internal Dependency Graph (Zero-DI Frameworks)
We **strictly prohibit** external DI frameworks (Dagger, Hilt, Koin). Use this pure Kotlin pattern:

### Rules:
1. **Config (Public):** Holds data and interfaces.
2. **Component (Internal):** The DI container. Use `by lazy { ... }` for all instantiations.
3. **Manager (Public):** Uses a factory for the component to allow testing backdoors.

### Template:
```kotlin
// 1. Public Config
class EncryptionConfig(...) : ManagerConfig

// 2. Internal DI Container
internal open class EncryptionComponent(val config: EncryptionConfig) {
    private val repository: EncryptionRepository by lazy { 
        EncryptionRepositoryImpl(KeystoreDataSource(), FileDataSource()) 
    }
    open val encryptUseCase: EncryptSymmetricUseCase by lazy { 
        EncryptSymmetricUseCase(repository) 
    }
}

// 3. Public Facade
class EncryptionkitManager internal constructor(
    private val componentFactory: (EncryptionConfig) -> EncryptionComponent = { EncryptionComponent(it) }
) : Manager<EncryptionConfig>() {
    private var component: EncryptionComponent? = null

    fun initialize(config: EncryptionConfig) {
        this.config = config
        this.component = componentFactory(config)
    }

    suspend fun encrypt(data: ByteArray) = component!!.encryptUseCase(Input(data, config))
}
```

---

## 4. Cybersecurity Context & Constraints
- **Authenticated Encryption:** Default to **AES-GCM (256-bit)**.
- **Hardware-Backed:** Prefer `StrongBox` or `TEE`.
- **Zero-Trust Memory:** Use `SecureBytes` and wipe sensitive data immediately.
- **No External IVs:** Encryption must always generate its own IV via `SecureRandom`.

---

## 5. Task Execution Instructions
When asked to add features:
1. Identify the required **Repository** method.
2. Create a specific **UseCase**.
3. Register the UseCase in the module's **Component** (lazy).
4. Expose the functionality in the **Manager**.
5. Ensure **Unit Tests** can inject a mocked Component via the internal constructor.
