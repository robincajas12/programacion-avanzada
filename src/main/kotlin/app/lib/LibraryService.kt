package example.app.lib


import app.data.*
import kotlinx.coroutines.delay

object LibraryService {

    // 1. FUNCIONES PURAS

    // Función pura 1: Calcular la multa acumulada por días de retraso
    fun calcularMulta(diasRetraso: Int, tarifaDiaria: Double): Double =
        if (diasRetraso <= 0) 0.0 else diasRetraso * tarifaDiaria

    // Función pura 2: Aplicar descuento a una multa
    fun calcularDescuentoMulta(multa: Double, porcentaje: Double): Double =
        if (multa <= 0.0 || porcentaje <= 0.0) multa else multa * (1.0 - (porcentaje / 100.0))

    // Función pura 3: Generar mensaje de confirmación de préstamo
    fun generarMensajePrestamo(libro: String, usuario: String): String =
        "Préstamo registrado: '$libro' entregado a $usuario."

    // Función pura 4: Determinar la categoría de socio según la edad
    fun determinarCategoriaSocio(edad: Int): String = when {
        edad < 12 -> "Infantil"
        edad in 12..18 -> "Juvenil"
        edad in 19..60 -> "Adulto"
        else -> "Adulto Mayor"
    }

    // Función pura 5: Calcular el promedio de edad de los usuarios
    fun calcularPromedioEdad(edades: List<Int>): Double =
        if (edades.isEmpty()) 0.0 else edades.sum().toDouble() / edades.size


    // 2. FUNCIONES DE ORDEN SUPERIOR & LAMBDAS

    // FOS 1: Filtrar y mapear elementos genéricos usando transformaciones personalizadas
    fun <T, R> filtrarYTransformar(lista: List<T>, filtro: (T) -> Boolean, transformacion: (T) -> R): List<R> =
        lista.filter(filtro).map(transformacion)

    // FOS 2: Reducir una lista de préstamos a un resumen textual agrupado
    fun resumirPrestamos(prestamos: List<Prestamo>, formateador: (Prestamo) -> String): String =
        if (prestamos.isEmpty()) "Sin préstamos"
        else prestamos.map(formateador).reduce { acc, s -> "$acc\n$s" }

    // FOS 3: Verificar condiciones de la colección
    fun verificarTodosCumplen(usuarios: List<Usuario>, condicion: (Usuario) -> Boolean): Boolean =
        usuarios.all(condicion)

    // FOS 4: Ordenar y agrupar libros por autor
    fun agruparLibrosPorAutor(libros: List<Libro>): Map<String, List<Libro>> =
        libros.sortedBy { it.titulo }.groupBy { it.autor }


    // 5. CORUTINAS (Mínimo 2 con suspend, delay, async, await, launch)

    // Corutina 1: Carga asíncrona simulada de base de datos de libros
    suspend fun simularCargaLibrosAsync(): Result<List<Libro>> {
        delay(3*1000)
        val libros = listOf(
            Libro("El Quijote", "Miguel de Cervantes"),
            Libro("Cien Años de Soledad", "Gabriel García Márquez"),
            Libro("Ficciones", "Jorge Luis Borges")
        )
        return Result.Success(libros)
    }

    // Corutina 2: Procesar préstamo en segundo plano validando condiciones
    suspend fun procesarPrestamoAsync(libro: String, usuario: String): Result<String> {
        delay(800)
        return if (libro.isBlank() || usuario.isBlank()) {
            Result.Failure(IllegalArgumentException("Libro o usuario vacío"))
        } else {
            Result.Success(generarMensajePrestamo(libro, usuario))
        }
    }


    // 6. RECURSIÓN

    // Búsqueda recursiva de libros que coincidan parcialmente con un título
    fun buscarLibroRecursivo(libros: List<Libro>, query: String, index: Int = 0): Libro? {
        if (index >= libros.size) return null
        if (libros[index].titulo.contains(query, ignoreCase = true)) {
            return libros[index]
        }
        return buscarLibroRecursivo(libros, query, index + 1)
    }


    // 7. CORECURSIÓN / SECUENCIAS INFINITAS(Mínimo 1)

    // Generador infinito de tokens de transacción único usando Sequence y yield
    val generadorDeTokens: Sequence<String> = sequence {
        var contador = 1001
        while (true) {
            yield("TX-LIB-$contador")
            contador++
        }
    }

    // Método para obtener una cantidad N de tokens nuevos
    fun obtenerTokensNuevos(cantidad: Int): List<String> =
        generadorDeTokens.take(cantidad).toList()



    // 8. COMPOSICIÓN DE FUNCIONES / PIPELINE

    // Composición: Obtiene nombres de usuarios mayores de edad en mayúsculas y ordenados
    fun obtenerNombresMayoresOrdenados(usuarios: List<Usuario>): List<String> =
        usuarios
            .filter { it.edad >= 18 }
            .map { it.nombre.uppercase() }
            .sortedBy { it }


    // 9. MANEJO DE ERRORES SIN EXCEPCIONES

    
    // Validación pura que retorna un Result con éxito o error de negocio
    fun validarRegistroUsuario(nombre: String, edad: Int): Result<Usuario> {
        return when {
            nombre.isBlank() -> Result.Failure(IllegalArgumentException("El nombre no puede estar vacío."))
            edad < 0 -> Result.Failure(IllegalArgumentException("La edad no puede ser negativa."))
            edad > 120 -> Result.Failure(IllegalArgumentException("Edad fuera de rango permitido."))
            else -> Result.Success(Usuario(nombre, edad))
        }
    }
}
