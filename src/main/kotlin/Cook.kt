data class Cook(
    var id: Int,
    var currentDish: DishToCook? = null,
    var allDishes: MutableList<DishToCook> = mutableListOf(),
    var timeToFinishCurrentDish: Int = 0,
    var timeToFinishAllAssignedDishes: Int = 0,
    var totalDishesInQueue: Int = 0
) {
    override fun toString(): String {
        val allDishesString = allDishes.joinToString("\n") { it.toString() }
        return """
            |Cook ID: $id
            |Current Dish: ${currentDish?.name ?: "No orders"}
            |All Dishes: $allDishesString
            |Time to Finish Current Dish: $timeToFinishCurrentDish minutes
            |Time to Finish All Assigned Dishes: $timeToFinishAllAssignedDishes minutes
            |Total Dishes in Queue: $totalDishesInQueue
        """.trimMargin()
    }
}