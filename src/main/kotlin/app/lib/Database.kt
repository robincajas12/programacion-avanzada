package app.lib

import app.data.*
import example.app.lib.LibraryService

object Database {
    val usuarios = listOf(
        Usuario("Alice", 23, LibraryService.determinarCategoriaSocio(23)),
        Usuario("Bob", 34, LibraryService.determinarCategoriaSocio(34)),
        Usuario("Carlos", 10, LibraryService.determinarCategoriaSocio(10))
    )
    val prestamos = listOf(
        Prestamo("El Quijote", "Alice", LibraryService.obtenerTokensNuevos(1).first()),
        Prestamo("Ficciones", "Bob", LibraryService.obtenerTokensNuevos(2).last())
    )
    val configuracion = listOf(
        Configuracion("Límite de Días", "15 días"),
        Configuracion("Multa Diaria", "$1.50 USD"),
        Configuracion("Notificaciones", "Habilitadas")
    )
}