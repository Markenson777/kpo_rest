data class Menu(
    var dishes: MutableList<Dish> = mutableListOf()
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendLine("Menu:")
        dishes.forEachIndexed { _, dish ->
            sb.appendLine("${dish.id}. ${dish.name} - $${dish.price} - ${dish.timeToCookInMin} minutes to cook")
        }
        return sb.toString()
    }
}