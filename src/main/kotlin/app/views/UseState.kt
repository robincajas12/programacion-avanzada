package example.app.views

class UseState<T>(private var item : T) : IUseState<T> {
    private var uiItem : UIItem<T>? = null
    private var color : Color = Color.RESET
    public  override fun getColor() = color;
    override fun get(): T {
        return item
    }
    override fun setUIItem(uiItem: UIItem<T>){
        this.uiItem = uiItem
    }
    override fun set(item: T) {
        this.item = item
        uiItem?.set(this)

    }
    override fun save(item: T) {
        this.item = item
    }

    override fun setColor(color: Color) {
        this.color = color
        uiItem?.set(this)
    }

    override fun saveColor(color: Color) {
        this.color = color
    }

}