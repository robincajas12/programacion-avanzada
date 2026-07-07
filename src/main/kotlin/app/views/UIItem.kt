package example.app.views


interface UIItem<T> {
    fun getItemID(): String
    fun get() : IUseState<T>;
    fun set(item: IUseState<T>)
     fun save(item: IUseState<T>)
     fun render()

}