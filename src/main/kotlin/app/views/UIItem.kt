package example.app.views


interface UIItem<T> {
    fun getItemID(): String
    fun get() : T;
    fun set(item: T)
}