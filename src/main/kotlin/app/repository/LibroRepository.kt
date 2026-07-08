package app.repository

import app.data.Libro
import app.data.Autor
import example.app.views.UseState
import example.app.lib.LibraryService

object LibroRepository {
    val state = UseState(listOf<Libro>())
    val autoresState = UseState(listOf<Autor>())

    fun getAll(): List<Libro> = state.get()

    fun initialize(libros: List<Libro>) {
        state.set(libros)
        actualizarAutores()
    }

    fun add(libro: Libro) {
        state.set(state.get() + libro)
        actualizarAutores()
    }

    fun delete(titulo: String): Boolean {
        val originalList = state.get()
        val exists = originalList.any { it.titulo.equals(titulo, ignoreCase = true) }
        if (exists) {
            state.set(originalList.filter { !it.titulo.equals(titulo, ignoreCase = true) })
            actualizarAutores()
        }
        return exists
    }

    private fun actualizarAutores() {
        autoresState.set(LibraryService.obtenerAutoresUnicos(state.get()).toList())
    }
}
