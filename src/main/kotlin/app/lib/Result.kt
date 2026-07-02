package example.app.lib

sealed class ResultAsync< out T> {
    data class Success<T>(val result: T) : ResultAsync<T>()
    data class Failure(val exception: Exception): ResultAsync<Nothing>()
    object Loading : ResultAsync<Nothing>()
}

sealed class Result<out T> {
    data class Success<T>(val result: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}