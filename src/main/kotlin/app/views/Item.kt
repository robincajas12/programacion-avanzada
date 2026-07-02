package example.app.views

import lombok.ToString
import java.time.LocalDateTime
import java.util.Date

@ToString
open class Item<T>(var item : T) : UIItem<T> {
    val ui: UI = UI.instance
    val id : String = LocalDateTime.now().toString();
    init {
        ui.set(this)
    }
    override fun getItemID(): String = id

    override fun get() : T = item

    override fun set(item: T) {
       this.item = item
        ui.render()
    }
}