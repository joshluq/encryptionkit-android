package es.joshluq.encryptionkit.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all use case inputs.
 */
interface UseCaseInput

/**
 * Base interface for all use case outputs.
 */
interface UseCaseOutput

/**
 * Represents a generic Use Case in Clean Architecture.
 *
 * @param I Input type that must implement [UseCaseInput].
 * @param O Output type that must implement [UseCaseOutput].
 */
interface UseCase<in I : UseCaseInput, out O : UseCaseOutput> {
    /**
     * Executes the business logic of the use case.
     *
     * @param input The parameters required for the operation.
     * @return A [Flow] emitting the result of the operation.
     */
    suspend operator fun invoke(input: I): Result<O>
}

/**
 * Represents a standard empty output for Use Cases that do not return a specific value.
 */
object NoneOutput : UseCaseOutput
