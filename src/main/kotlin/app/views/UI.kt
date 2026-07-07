package example.app.views


class UI {
    private constructor(){}
    companion object {
        val instance = UI()
    }
    var items: MutableList<UIItem<Any>> = mutableListOf()

    fun <T> set(item: UIItem<T>)
    {
        clearConsole()
        items.add((item as UIItem<Any>))
        render()
    }
    fun remove(id: String) {
        items.removeIf { it.getItemID() == id }
    }
    fun update(id: String, value: IUseState<Any>)
    {
        items = items.map { if(it.getItemID() == id) {
            it.set(value )
            it
        } else it }.toMutableList()
    }
    fun render()
    {
        clearConsole()
        items.forEach { it.render() }
    }
    fun clearConsole_() {
        print("\u001b[H\u001b[2J")
        System.out.flush()
    }
    fun clearConsole() {
        val process = if (System.getProperty("os.name").contains("Windows")) {
            ProcessBuilder("cmd", "/c", "cls").inheritIO().start()
        } else {
            ProcessBuilder("clear").inheritIO().start()
        }
        process.waitFor()
    }

    fun launchConsole(){
        val javaBin = System.getProperty("java.home") + "/bin/java"
        val classpath = System.getProperty("java.class.path")
        val className = System.getProperty("sun.java.command").split(" ")[0]

        // 2. Construcción del comando para una nueva ventana
        // "start" en Windows abre una consola nueva y separada
        val processBuilder = ProcessBuilder(
            "cmd", "/c", "start", "cmd.exe", "/k",
            "\"$javaBin\" -cp \"$classpath\" $className run-as-external"
        )

        processBuilder.start()
        System.exit(0)
    }
}


