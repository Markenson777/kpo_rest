data class DishToCook(
    var name: String,
    var price: Int,
    var timeToCookInMin: Int,
    var status: String,
    val id: Int,
    var order: Order? = null,
    var cook: Cook? = null
) {
    override fun toString(): String {
        return "Dish: name='$name', price=$price, timeToCookInMin=$timeToCookInMin, status='$status', id=$id"
    }
}
