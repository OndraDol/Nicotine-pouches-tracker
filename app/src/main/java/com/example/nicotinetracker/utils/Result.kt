package com.example.nicotinetracker.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val exception: Throwable? = null, 
        val message: String? = null
    ) : Result<Nothing>()
    object Loading : Result<Nothing>()

    // Rozšiřující metody pro snadnější práci s výsledky
    fun isSuccess() = this is Success
    fun isError() = this is Error
    fun isLoading() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }
}

// Extension funkce pro transformaci výsledků
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception, message)
        is Result.Loading -> Result.Loading
    }
}

// Globální error handler
object ErrorHandler {
    fun handleError(result: Result.Error) {
        // Centralizované logování chyb
        result.exception?.printStackTrace()
        
        // Volitelné odeslání chyby do crashlytiky
        // FirebaseCrashlytics.getInstance().recordException(result.exception)
    }
}
