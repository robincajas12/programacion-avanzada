package app.repository

import app.data.Prestamo
import app.lib.Database
import example.app.views.UseState

object PrestamoRepository {
    val state = UseState(Database.prestamos)

    fun getAll(): List<Prestamo> = state.get()

    fun add(prestamo: Prestamo) {
        state.set(state.get() + prestamo)
    }

    fun delete(token: String): Boolean {
        val originalList = state.get()
        val exists = originalList.any { it.tokenTransaccion.equals(token, ignoreCase = true) }
        if (exists) {
            state.set(originalList.filter { !it.tokenTransaccion.equals(token, ignoreCase = true) })
        }
        return exists
    }

    fun deleteByUsuario(nombreUsuario: String) {
        state.set(state.get().filter { !it.usuario.equals(nombreUsuario, ignoreCase = true) })
    }

    fun deleteByLibro(tituloLibro: String) {
        state.set(state.get().filter { !it.libro.equals(tituloLibro, ignoreCase = true) })
    }
}
