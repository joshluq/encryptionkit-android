# Encryptionkit

This is an Android library project generated from the Templatekit template.

## Structure

### `library/`
The main reusable Android library component. This is the artifact that will be consumed by other projects.

- **`src/main/java/`**: Main library source code
- **`src/test/java/`**: Unit tests (JVM)
- **`src/androidTest/java/`**: Instrumented tests (device/emulator)
- **`src/main/res/`**: Library resources
- **`consumer-rules.pro`**: ProGuard rules for library consumers

### `showcase/`
A demonstration Android application that consumes the library. Use this app to:

- Test the library API during development
- Showcase how to use the library
- Develop and validate features in an integrated environment
- Run instrumented tests against the library

The showcase app uses the same base package as the library (plus `.showcase`) for seamless integration.

## Building

### Compile the library
```bash
./gradlew :library:assemble
```

### Run library tests
```bash
./gradlew :library:test
```

### Build the showcase app
```bash
./gradlew :showcase:assembleDebug
```

### Run showcase instrumented tests
```bash
./gradlew :showcase:connectedAndroidTest
```

## Development Workflow

1. **Add library code** to `library/src/main/java/es/joshluq/encryptionkit/`
2. **Write unit tests** in `library/src/test/java/es/joshluq/encryptionkit/`
3. **Write instrumented tests** in `library/src/androidTest/java/es/joshluq/encryptionkit/`
4. **Integrate the library** in the showcase app at `showcase/src/main/java/es.joshluq.encryptionkit/showcase/` to validate the consumer experience
5. **Add showcase tests** in `showcase/src/test/java/es.joshluq.encryptionkit/showcase/` or `showcase/src/androidTest/java/es.joshluq.encryptionkit/showcase/`
6. **Use the showcase app** to develop and test features in a real Android environment

**Note:** The package structure is automatically created during template generation. All source files are organized with the correct package structure from the start.

This Consumer-Driven pattern ensures your library API is always tested in a realistic consumer context.

## Configuration

This generated project includes a `project-config.properties` file at the project root with overridable values:

- `catalogVersion` : the version coordinate used by the version catalog (e.g. `es.joshluq.kit.pluginkit:catalog:0.0.1-SNAPSHOT`).
- `libraryVersion` : the default version for the `:library` artifact (e.g. `1.0.0`).

Edit `project-config.properties` in the generated project to change these values without modifying build scripts directly.
