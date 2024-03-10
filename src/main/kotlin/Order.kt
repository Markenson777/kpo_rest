data class Order(
    var dishes: MutableList<DishToCook> = mutableListOf(),
    var orderAmount: Int,
    var status: String,
    var totalTimeToCookInMin: Int,
    var id: Int,
    var customer: CustomerAccount
) {
    override fun toString(): String {
        val dishNames = dishes.joinToString(", ") { it.name }
        return """
            |Order ID: $id
            |Customer name: ${customer.name}
            |Dishes: $dishNames
            |Order Amount: $$orderAmount
            |Status: $status
            |Total Time to Cook: $totalTimeToCookInMin minutes
        """.trimMargin()
    }
}