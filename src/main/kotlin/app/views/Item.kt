package example.app.views

import lombok.ToString
import java.time.LocalDateTime

@ToString
open class Item<T>(var item: IUseState<T>) : UIItem<T> {
    val ui: UI = UI.instance
    val id : String = LocalDateTime.now().toString();

    override fun getItemID(): String = id

    override fun get() :IUseState<T> = item
    override fun set(item: IUseState<T>) {
       this.item = item
        ui.render()
    }
    override fun save(item: IUseState<T>) {
        this.item = item
    }

    override fun render() {
        println("${this.item.getColor()}${this.item.get()}${Color.RESET}")
    }
}