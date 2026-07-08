package app.repository

import app.data.Configuracion
import app.lib.Database
import example.app.views.UseState

object ConfiguracionRepository {
    val state = UseState(Database.configuracion)

    fun getAll(): List<Configuracion> = state.get()

    fun updateByIndex(index: Int, nuevoEstado: String): Boolean {
        val originalList = state.get()
        if (index in originalList.indices) {
            val updatedList = originalList.toMutableList()
            updatedList[index] = updatedList[index].copy(estado = nuevoEstado)
            state.set(updatedList)
            return true
        }
        return false
    }
}
