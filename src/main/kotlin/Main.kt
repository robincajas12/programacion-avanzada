package example


import app.data.*
import app.lib.Database
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

    // Estados reactivos (List, Map, Set)
    val usuariosState = UseState(Database.usuarios)
    val librosState = UseState(librosIniciales)
    val prestamosState = UseState(Database.prestamos)
    val configuracionState = UseState(Database.configuracion)

    val adultosReporteState = UseState(listOf<Usuario>())
    val resultadoBusquedaState = UseState(listOf<Libro>())

    fun calcularEstadisticas(): List<Estadistica> {
        val totalLibros = librosState.get().size
        val totalUsuarios = usuariosState.get().size
        val totalPrestamos = prestamosState.get().size
        val edades = usuariosState.get().map { it.edad }
        val promedioEdad = LibraryService.calcularPromedioEdad(edades)
        val todosAdultos = LibraryService.verificarTodosCumplen(usuariosState.get()) { it.edad >= 18 }

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
                        usuariosState.set(usuariosState.get() + nuevoUsuario)
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
                    librosState.set(librosState.get() + Libro(tit, aut))
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
                    val prestamoResult = LibraryService.procesarPrestamoAsync(tit, usr)
                    when (prestamoResult) {
                        is Result.Success -> {
                            val tokenUnico = LibraryService.obtenerTokensNuevos(prestamosState.get().size + 1).last()
                            val nuevoPrestamo = Prestamo(tit, usr, tokenUnico)
                            prestamosState.set(prestamosState.get() + nuevoPrestamo)
                            estadisticasState.set(calcularEstadisticas())
                            println("\n[ÉXITO] ${prestamoResult.result}")
                            println("Token generado: $tokenUnico")
                        }
                        is Result.Failure -> {
                            println("\n[ERROR] Falló el préstamo: ${prestamoResult.exception.message}")
                        }
                        else -> {}
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
                
                val resultado = LibraryService.buscarLibroRecursivo(librosState.get(), query)
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
                val nombresAdultos = LibraryService.obtenerNombresMayoresOrdenados(usuariosState.get())
                
                val adultosFiltrados = usuariosState.get().filter { u ->
                    nombresAdultos.contains(u.nombre.uppercase())
                }.sortedBy { it.nombre }

                adultosReporteState.set(adultosFiltrados)
                nav.setView("REPORTE_ADULTOS")
                
                print("\nPresiona Enter para continuar...")
                readlnOrNull()
                nav.setView("USUARIOS")
            }
            else -> {
                UI.instance.render()
            }
        }
    }
}
