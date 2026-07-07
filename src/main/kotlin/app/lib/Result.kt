package example.app.lib

sealed class Result<out T> {
    data class Success<T>(val result: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}