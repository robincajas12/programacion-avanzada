package example


import app.data.*
import app.lib.Database
import app.repository.*
import example.app.views.*
import example.app.lib.LibraryService
import example.app.lib.Result
import example.app.views.ascii.ASCCI
import kotlinx.coroutines.*



fun main(args: Array<String>) = runBlocking {
    if (args.isEmpty()) {
        UI.instance.launchConsole()
        return@runBlocking
    }

    // Inicialización del título estático superior
    val titulo = Texto(UseState(ASCCI.UCE_BIBLIOTECA
    ).apply { setColor(Color.CYAN) })
    UI.instance.set(titulo)

    // Cargando datos simulados asíncronamente
    val cargandoTexto = Texto(UseState(ASCCI.LOADING).apply { setColor(Color.AMARILLO) })
    UI.instance.set(cargandoTexto)
    val librosCargadosResult = withContext(Dispatchers.Default) {
        LibraryService.simularCargaLibrosAsync()
    }
    UI.instance.remove(cargandoTexto.getItemID())

    val librosIniciales = when (librosCargadosResult) {
        is Result.Success -> librosCargadosResult.result
        is Result.Failure -> listOf(Libro("El Quijote", "Miguel de Cervantes"))
    }

    // Inicializar LibroRepository con los libros cargados
    LibroRepository.initialize(librosIniciales)

    // Estados reactivos expuestos por los Repositorios
    val usuariosState = UsuarioRepository.state
    val librosState = LibroRepository.state
    val prestamosState = PrestamoRepository.state
    val configuracionState = ConfiguracionRepository.state
    val autoresState = LibroRepository.autoresState

    val adultosReporteState = UseState(listOf<Usuario>())
    val resultadoBusquedaState = UseState(listOf<Libro>())

    fun calcularEstadisticas(): List<Estadistica> {
        val totalLibros = LibroRepository.getAll().size
        val totalUsuarios = UsuarioRepository.getAll().size
        val totalPrestamos = PrestamoRepository.getAll().size
        val edades = UsuarioRepository.getAll().map { it.edad }
        val promedioEdad = LibraryService.calcularPromedioEdad(edades)
        val todosAdultos = LibraryService.verificarTodosCumplen(UsuarioRepository.getAll()) { it.edad >= 18 }

        return listOf(
            Estadistica("Total Libros", totalLibros.toString()),
            Estadistica("Usuarios Registrados", totalUsuarios.toString()),
            Estadistica("Préstamos Activos", totalPrestamos.toString()),
            Estadistica("Promedio de Edad", String.format("%.2f años", promedioEdad)),
            Estadistica("¿Todos son adultos?", if (todosAdultos) "Sí" else "No")
        )
    }

    val estadisticasState = UseState(calcularEstadisticas())

    // Configuración del mapa de vistas
    val vistas = mutableMapOf<String, List<UIItem<*>>>()
    
    // Vistas principales
    vistas["USUARIOS"] = listOf(
        Texto(UseState("--- LISTA DE USUARIOS ---").apply { setColor(Color.AZUL) }),
        Tabla(usuariosState)
    )
    vistas["LIBROS"] = listOf(
        Texto(UseState("--- LISTA DE LIBROS ---").apply { setColor(Color.CYAN) }),
        Tabla(librosState)
    )
    vistas["PRESTAMOS"] = listOf(
        Texto(UseState("--- PRÉSTAMOS ACTIVOS ---").apply { setColor(Color.VERDE) }),
        Tabla(prestamosState)
    )
    vistas["ESTADISTICAS"] = listOf(
        Texto(UseState("--- ESTADÍSTICAS DE LA BIBLIOTECA ---").apply { setColor(Color.AMARILLO) }),
        Tabla(estadisticasState)
    )
    vistas["CONFIGURACION"] = listOf(
        Texto(UseState("--- CONFIGURACIÓN GENERAL ---").apply { setColor(Color.AZUL) }),
        Tabla(configuracionState)
    )
    vistas["AUTORES"] = listOf(
        Texto(UseState("--- AUTORES ÚNICOS (SET) ---").apply { setColor(Color.CYAN) }),
        Tabla(autoresState)
    )

    // ------------ Vistas de Formularios y Reports --------
    vistas["NUEVO_USUARIO"] = listOf(
        Texto(UseState("--- FORMULARIO: REGISTRAR NUEVO USUARIO ---").apply { setColor(Color.AZUL) }),
        Texto(UseState("Por favor ingresa los datos solicitados a continuación:\n"))
    )
    vistas["NUEVO_LIBRO"] = listOf(
        Texto(UseState("--- FORMULARIO: REGISTRAR NUEVO LIBRO ---").apply { setColor(Color.AZUL) }),
        Texto(UseState("Por favor ingresa los datos solicitados a continuación:\n"))
    )
    vistas["NUEVO_PRESTAMO"] = listOf(
        Texto(UseState("--- FORMULARIO: REGISTRAR PRÉSTAMO ---").apply { setColor(Color.AZUL) }),
        Texto(UseState("Por favor ingresa los datos solicitados a continuación:\n"))
    )
    vistas["BUSCAR_LIBRO"] = listOf(
        Texto(UseState("--- BÚSQUEDA RECURSIVA DE LIBROS ---").apply { setColor(Color.AZUL) }),
        Tabla(resultadoBusquedaState)
    )
    vistas["REPORTE_ADULTOS"] = listOf(
        Texto(UseState("--- REPORTE: USUARIOS ADULTOS (MAYORES DE EDAD) ---").apply { setColor(Color.AZUL) }),
        Tabla(adultosReporteState)
    )

    val nav = Navigation(UseState(vistas))

    val menu = Texto(UseState(
        "\n[1] Ver Usuarios   [2] Ver Libros     [3] Ver Préstamos\n" +
        "[4] Estadísticas   [5] Configuración\n" +
        "--------------------------------------------------\n" +
        "[6] Registrar Usuario [7] Registrar Libro   [8] Solicitar Préstamo\n" +
        "[9] Buscar Libro (Recursivo)  [0] Reporte Adultos (Composición)\n" +
        "[u] Autores Únicos (Set)      [d] Eliminar Registro          [e] Actualizar Datos\n" +
        "[c] Salir\n\n" +
        "Escribe una opción: "
    ).apply { setColor(Color.AMARILLO) })

    nav.setView("USUARIOS")
    UI.instance.set(menu)

    while (true) {
        val input = readlnOrNull() ?: "c"
        if (input == "c") break

        when (input) {
            "1" -> nav.setView("USUARIOS")
            "2" -> nav.setView("LIBROS")
            "3" -> nav.setView("PRESTAMOS")
            "4" -> {
                estadisticasState.set(calcularEstadisticas())
                nav.setView("ESTADISTICAS")
            }
            "5" -> nav.setView("CONFIGURACION")
            "6" -> {
                // Mostrar formulario usando la vista estructurada NUEVO_USUARIO
                nav.setView("NUEVO_USUARIO")
                print("Nombre del usuario: ")
                val nombre = readlnOrNull()?.trim() ?: ""
                print("Edad: ")
                val edad = readlnOrNull()?.toIntOrNull() ?: -1

                when (val validacion = LibraryService.validarRegistroUsuario(nombre, edad)) {
                    is Result.Success -> {
                        val nuevoUsuario = validacion.result.copy(categoria = LibraryService.determinarCategoriaSocio(edad))
                        UsuarioRepository.add(nuevoUsuario)
                        estadisticasState.set(calcularEstadisticas())
                        println("\n[ÉXITO] Usuario registrado: ${nuevoUsuario.nombre} (${nuevoUsuario.categoria})")
                    }
                    is Result.Failure -> {
                        println("\n[ERROR] No se pudo registrar: ${validacion.exception.message}")
                    }
                }
                print("\nPresiona Enter para volver a la lista...")
                readlnOrNull()
                nav.setView("USUARIOS")
            }
            "7" -> {
                nav.setView("NUEVO_LIBRO")
                print("Título del Libro: ")
                val tit = readlnOrNull()?.trim() ?: ""
                print("Autor: ")
                val aut = readlnOrNull()?.trim() ?: ""
                if (tit.isNotEmpty() && aut.isNotEmpty()) {
                    LibroRepository.add(Libro(tit, aut))
                    estadisticasState.set(calcularEstadisticas())
                    println("\n[ÉXITO] Libro registrado con éxito.")
                } else {
                    println("\n[ERROR] Datos incompletos.")
                }
                print("\nPresiona Enter para volver a la lista...")
                readlnOrNull()
                nav.setView("LIBROS")
            }
            "8" -> {
                // Mostrar formulario usando la vista estructurada NUEVO_PRESTAMO
                nav.setView("NUEVO_PRESTAMO")
                print("Título del Libro: ")
                val tit = readlnOrNull()?.trim() ?: ""
                print("Nombre del Usuario: ")
                val usr = readlnOrNull()?.trim() ?: ""

                println("\nProcesando préstamo asíncronamente...")
                val job = launch {
                    val disponibilidadResult = LibraryService.verificarDisponibilidadPrestamoAsync(
                        tit, usr, LibroRepository.getAll(), UsuarioRepository.getAll(), PrestamoRepository.getAll()
                    )
                    when (disponibilidadResult) {
                        is Result.Success -> {
                            val prestamoResult = LibraryService.procesarPrestamoAsync(tit, usr)
                            when (prestamoResult) {
                                is Result.Success -> {
                                    val tokenUnico = LibraryService.obtenerTokensNuevos(PrestamoRepository.getAll().size + 1).last()
                                    val nuevoPrestamo = Prestamo(tit, usr, tokenUnico)
                                    PrestamoRepository.add(nuevoPrestamo)
                                    estadisticasState.set(calcularEstadisticas())
                                    println("\n[ÉXITO] ${prestamoResult.result}")
                                    println("Token generado: $tokenUnico")
                                }
                                is Result.Failure -> {
                                    println("\n[ERROR] Falló al procesar el préstamo: ${prestamoResult.exception.message}")
                                }
                            }
                        }
                        is Result.Failure -> {
                            println("\n[ERROR] Validación fallida: ${disponibilidadResult.exception.message}")
                        }
                    }
                }
                job.join()
                print("\nPresiona Enter para volver a la lista...")
                readlnOrNull()
                nav.setView("PRESTAMOS")
            }
            "9" -> {
                // Inicializar y mostrar vista de búsqueda
                resultadoBusquedaState.set(emptyList())
                nav.setView("BUSCAR_LIBRO")
                print("Escribe el título para buscar de forma recursiva: ")
                val query = readlnOrNull()?.trim() ?: ""
                
                val resultado = LibraryService.buscarLibroRecursivo(LibroRepository.getAll(), query)
                if (resultado != null) {
                    resultadoBusquedaState.set(listOf(resultado))
                    nav.setView("BUSCAR_LIBRO") // Renderiza la tabla con el libro encontrado
                    println("\n¡Libro encontrado con éxito!")
                } else {
                    println("\nNo se encontró ningún libro que coincida con '$query'.")
                }
                print("\nPresiona Enter para continuar...")
                readlnOrNull()
                nav.setView("LIBROS")
            }
            "0" -> {
                // Filtrar utilizando composición y mostrar en la vista estructurada REPORTE_ADULTOS
                val nombresAdultos = LibraryService.obtenerNombresMayoresOrdenados(UsuarioRepository.getAll())
                
                val adultosFiltrados = UsuarioRepository.getAll().filter { u ->
                    nombresAdultos.contains(u.nombre.uppercase())
                }.sortedBy { it.nombre }

                adultosReporteState.set(adultosFiltrados)
                nav.setView("REPORTE_ADULTOS")
                
                print("\nPresiona Enter para continuar...")
                readlnOrNull()
                nav.setView("USUARIOS")
            }
            "u" -> {
                nav.setView("AUTORES")
                print("\nPresiona Enter para continuar...")
                readlnOrNull()
                nav.setView("LIBROS")
            }
            "d" -> {
                UI.instance.clearConsole()
                println("=== ELIMINAR REGISTROS ===")
                println("[1] Eliminar Usuario")
                println("[2] Eliminar Libro")
                println("[3] Eliminar Préstamo")
                println("[c] Cancelar")
                print("\nSeleccione una opción: ")
                val deleteOpt = readlnOrNull()?.trim() ?: "c"
                when (deleteOpt) {
                    "1" -> {
                        println("\n--- ELIMINAR USUARIO ---")
                        val currentUsers = UsuarioRepository.getAll()
                        if (currentUsers.isEmpty()) {
                            println("No hay usuarios registrados.")
                        } else {
                            println("Usuarios actuales:")
                            currentUsers.forEach { println("- ${it.nombre} (${it.edad} años)") }
                            print("\nIngrese el nombre del usuario a eliminar: ")
                            val nombreEliminar = readlnOrNull()?.trim() ?: ""
                            val eliminado = UsuarioRepository.delete(nombreEliminar)
                            if (eliminado) {
                                PrestamoRepository.deleteByUsuario(nombreEliminar)
                                estadisticasState.set(calcularEstadisticas())
                                println("\n[ÉXITO] Usuario '$nombreEliminar' y sus préstamos asociados han sido eliminados.")
                            } else {
                                println("\n[ERROR] Usuario '$nombreEliminar' no encontrado.")
                            }
                        }
                    }
                    "2" -> {
                        println("\n--- ELIMINAR LIBRO ---")
                        val currentLibros = LibroRepository.getAll()
                        if (currentLibros.isEmpty()) {
                            println("No hay libros registrados.")
                        } else {
                            println("Libros actuales:")
                            currentLibros.forEach { println("- '${it.titulo}' por ${it.autor}") }
                            print("\nIngrese el título del libro a eliminar: ")
                            val tituloEliminar = readlnOrNull()?.trim() ?: ""
                            val eliminado = LibroRepository.delete(tituloEliminar)
                            if (eliminado) {
                                PrestamoRepository.deleteByLibro(tituloEliminar)
                                estadisticasState.set(calcularEstadisticas())
                                println("\n[ÉXITO] Libro '$tituloEliminar' y sus préstamos asociados han sido eliminados.")
                            } else {
                                println("\n[ERROR] Libro '$tituloEliminar' no encontrado.")
                            }
                        }
                    }
                    "3" -> {
                        println("\n--- ELIMINAR PRÉSTAMO ---")
                        val currentPrestamos = PrestamoRepository.getAll()
                        if (currentPrestamos.isEmpty()) {
                            println("No hay préstamos activos.")
                        } else {
                            println("Préstamos actuales:")
                            currentPrestamos.forEach { println("- Token: ${it.tokenTransaccion} | Libro: '${it.libro}' | Usuario: ${it.usuario}") }
                            print("\nIngrese el token del préstamo a eliminar: ")
                            val tokenEliminar = readlnOrNull()?.trim() ?: ""
                            val eliminado = PrestamoRepository.delete(tokenEliminar)
                            if (eliminado) {
                                estadisticasState.set(calcularEstadisticas())
                                println("\n[ÉXITO] Préstamo con token '$tokenEliminar' eliminado.")
                            } else {
                                println("\n[ERROR] Préstamo con token '$tokenEliminar' no encontrado.")
                            }
                        }
                    }
                }
                print("\nPresiona Enter para continuar...")
                readlnOrNull()
                UI.instance.render()
            }
            "e" -> {
                UI.instance.clearConsole()
                println("=== ACTUALIZAR DATOS ===")
                println("[1] Actualizar Edad de Usuario")
                println("[2] Actualizar Parámetro de Configuración")
                println("[c] Cancelar")
                print("\nSeleccione una opción: ")
                val updateOpt = readlnOrNull()?.trim() ?: "c"
                when (updateOpt) {
                    "1" -> {
                        println("\n--- ACTUALIZAR EDAD DE USUARIO ---")
                        val currentUsers = UsuarioRepository.getAll()
                        if (currentUsers.isEmpty()) {
                            println("No hay usuarios registrados.")
                        } else {
                            println("Usuarios actuales:")
                            currentUsers.forEach { println("- ${it.nombre} (${it.edad} años)") }
                            print("\nIngrese el nombre del usuario a actualizar: ")
                            val nombreActualizar = readlnOrNull()?.trim() ?: ""
                            val usuarioExiste = currentUsers.any { it.nombre.equals(nombreActualizar, ignoreCase = true) }
                            if (usuarioExiste) {
                                print("Ingrese la nueva edad: ")
                                val nuevaEdad = readlnOrNull()?.toIntOrNull() ?: -1
                                val validacion = LibraryService.validarRegistroUsuario(nombreActualizar, nuevaEdad)
                                when (validacion) {
                                    is Result.Success -> {
                                        val categoria = LibraryService.determinarCategoriaSocio(nuevaEdad)
                                        UsuarioRepository.updateEdad(nombreActualizar, nuevaEdad, categoria)
                                        estadisticasState.set(calcularEstadisticas())
                                        println("\n[ÉXITO] Edad de '$nombreActualizar' actualizada a $nuevaEdad ($categoria).")
                                    }
                                    is Result.Failure -> {
                                        println("\n[ERROR] Datos inválidos: ${validacion.exception.message}")
                                    }
                                }
                            } else {
                                println("\n[ERROR] Usuario '$nombreActualizar' no encontrado.")
                            }
                        }
                    }
                    "2" -> {
                        println("\n--- ACTUALIZAR CONFIGURACIÓN ---")
                        val currentConfig = ConfiguracionRepository.getAll()
                        if (currentConfig.isEmpty()) {
                            println("No hay parámetros de configuración.")
                        } else {
                            println("Configuraciones actuales:")
                            currentConfig.forEachIndexed { idx, config -> 
                                println("[${idx + 1}] ${config.opcion}: ${config.estado}") 
                            }
                            print("\nSeleccione el número de la configuración a actualizar: ")
                            val idxConfig = (readlnOrNull()?.toIntOrNull() ?: 0) - 1
                            if (idxConfig in currentConfig.indices) {
                                val configOriginal = currentConfig[idxConfig]
                                print("Ingrese el nuevo valor para '${configOriginal.opcion}': ")
                                val nuevoValor = readlnOrNull()?.trim() ?: ""
                                if (nuevoValor.isNotEmpty()) {
                                    ConfiguracionRepository.updateByIndex(idxConfig, nuevoValor)
                                    println("\n[ÉXITO] '${configOriginal.opcion}' actualizada a: $nuevoValor")
                                } else {
                                    println("\n[ERROR] El valor no puede estar vacío.")
                                }
                            } else {
                                println("\n[ERROR] Selección inválida.")
                            }
                        }
                    }
                }
                print("\nPresiona Enter para continuar...")
                readlnOrNull()
                UI.instance.render()
            }
            else -> {
                UI.instance.render()
            }
        }
    }
}
