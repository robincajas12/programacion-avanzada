package example.app.views

class Tabla<T : Any>(data: UseState<List<T>>) : Item<List<T>>(data) {

    override fun render() {
        val dataList = this.get().get() ?: return
        if (dataList.isEmpty()) return

        val fields = dataList[0].javaClass.declaredFields

        val anchoPorColumna = fields.associate { field ->
            field.isAccessible = true
            val maxLen = dataList.map { item ->
                field.get(item)?.toString()?.length ?: 4
            }.maxOrNull() ?: 0

            // El ancho es el máximo entre el nombre del campo y el valor más largo
            field.name to maxOf(field.name.length, maxLen)
        }

        val headers = fields.joinToString(" | ") { field ->
            field.name.uppercase().padEnd(anchoPorColumna[field.name]!!)
        }
        println("${this.get().getColor()}$headers${Color.RESET}")
        println("-".repeat(headers.length + (fields.size - 1) * 3))

        dataList.forEach { item ->
            val row = fields.joinToString(" | ") { field ->
                field.isAccessible = true
                val valor = field.get(item)?.toString() ?: "null"

                val valorFormateado = if (valor.length > anchoPorColumna[field.name]!!) {
                    valor.take(anchoPorColumna[field.name]!! - 3) + "..."
                } else {
                    valor.padEnd(anchoPorColumna[field.name]!!)
                }
                valorFormateado
            }
            println(row)
        }
    }}