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
    fun update(id: String, value: Any)
    {
        items = items.map { if(it.getItemID() == id) {
            it.set(value)
            it
        } else it }.toMutableList()
    }
    fun render()
    {
        clearConsole()
        items.forEach { println(it) }
    }
    fun clearConsole() {
        print("\u001b[H\u001b[2J")
        System.out.flush()
    }
}


