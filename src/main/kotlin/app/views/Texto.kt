package example.app.views

import lombok.ToString

@ToString
class Texto(state: IUseState<String>) : Item<String>(state) {

}