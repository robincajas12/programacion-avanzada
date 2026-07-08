package app.repository

import app.data.Usuario
import app.lib.Database
import example.app.views.UseState

object UsuarioRepository {
    val state = UseState(Database.usuarios)

    fun getAll(): List<Usuario> = state.get()

    fun add(usuario: Usuario) {
        state.set(state.get() + usuario)
    }

    fun delete(nombre: String): Boolean {
        val originalList = state.get()
        val exists = originalList.any { it.nombre.equals(nombre, ignoreCase = true) }
        if (exists) {
            state.set(originalList.filter { !it.nombre.equals(nombre, ignoreCase = true) })
        }
        return exists
    }

    fun updateEdad(nombre: String, nuevaEdad: Int, nuevaCategoria: String): Boolean {
        val originalList = state.get()
        val index = originalList.indexOfFirst { it.nombre.equals(nombre, ignoreCase = true) }
        if (index != -1) {
            val updatedList = originalList.toMutableList()
            updatedList[index] = updatedList[index].copy(edad = nuevaEdad, categoria = nuevaCategoria)
            state.set(updatedList)
            return true
        }
        return false
    }
}
