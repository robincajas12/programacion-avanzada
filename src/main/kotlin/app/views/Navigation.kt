package example.app.views

class Navigation(
    var data: UseState<Map<String, List<UIItem<*>>>>
) : Item<Map<String, List<UIItem<*>>>>(data) {

    var current: String? = null

    override fun render() {
        // Obtenemos el mapa y renderizamos los ítems de la vista actual
        this.data.get()[current]?.forEach {
            it.render()
        }
    }

    fun setView(view: String) {
        current = view
        if (UI.instance.items.none { it.getItemID() == this.getItemID() }) {
            UI.instance.set<Map<String, List<UIItem<*>>>>(this)
        } else {
            UI.instance.render()
        }
    }
}