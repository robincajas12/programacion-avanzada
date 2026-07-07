package example.app.views
enum class Color(val codigo: String) {
    RESET("\u001B[0m"),
    ROJO("\u001B[31m"),
    VERDE("\u001B[32m"),
    AZUL("\u001B[34m"),
    AMARILLO("\u001B[33m"),
    CYAN("\u001B[36m");

    override fun toString(): String = codigo
}