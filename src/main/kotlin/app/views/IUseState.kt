package example.app.views

interface IUseState<T> {
    fun get(): T
    fun set(item: T)
    fun setUIItem(uiItem: UIItem<T>)
    fun save(item: T)
    fun setColor(color : Color)
    fun saveColor(color: Color)
    public fun getColor(): Color?;

}