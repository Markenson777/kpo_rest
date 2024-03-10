import com.google.gson.*
import java.io.File
import com.google.gson.reflect.TypeToken
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.IOException
import kotlin.concurrent.thread

class Restaurant {
    private val menu = Menu()
    private var revenue = 0
    private val amountOfCooks = 3
    private val cooks = mutableListOf<Cook>()
    private val orders = mutableListOf<Order>()

    init {
        // Загрузка данных из файлов JSON
        repeat(amountOfCooks) {
            cooks.add(Cook(it + 1))
        }

        loadDishes()
        val accountsMapE = loadEmployeeAccounts()
        employeeAccounts.addAll(accountsMapE.values)

        val accountsMapC = loadCustomerAccounts()
        customerAccounts.addAll(accountsMapC.values)

        readRevenueFromFile()
        readOrdersFromFile()

        startCookThreads()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendLine("Restaurant Information:")
        sb.appendLine("$menu")
        sb.appendLine("---------------------------------------------------")
        sb.appendLine("Revenue: $$revenue")
        sb.appendLine("---------------------------------------------------")
        sb.appendLine("Number of Cooks: $amountOfCooks")
        sb.appendLine("List of Cooks:")
        for (cook in cooks) {
            sb.appendLine(cook.toString())
            sb.appendLine("------------")
        }
        sb.appendLine("List of Orders:")
        for (order in orders) {
            sb.appendLine(order.toString())
        }
        return sb.toString()
    }

    private fun loadDishes() {
        // Загрузка данных о фильмах из файла JSON
        val gson = Gson()
        val dishesJson = File("src/main/kotlin/dishes.json").readText()

        val listType = object : TypeToken<List<Dish>>() {}.type
        val dishes: List<Dish> = gson.fromJson(dishesJson, listType)

        menu.dishes.addAll(dishes)
    }

    fun saveRevenue() {
        val fileName = "src/main/kotlin/revenue.txt"
        try {
            val file = File(fileName)
            val writer = BufferedWriter(FileWriter(file))
            writer.write(revenue.toString())
            writer.close()
            println("Revenue saved to $fileName successfully.")
        } catch (e: IOException) {
            println("Error occurred while saving revenue to $fileName: ${e.message}")
        }
    }

    fun saveOrders() {
        // Сохранение данных в файлы JSON
        val gson = Gson()
        val fileName = "src/main/kotlin/orders.json"

        println(orders)

        val ordersJson = gson.toJson(orders)
        File(fileName).writeText(ordersJson)
        printlnColor("Orders data saved successfully.", ConsoleColor.GREEN)
    }

    fun saveDishes() {
        // Сохранение данных в файлы JSON
        val gson = Gson()

        val fileName = "src/main/kotlin/dishes.json"

        val dishesJson = gson.toJson(menu.dishes)
        File(fileName).writeText(dishesJson)
        printlnColor("Dishes data saved successfully.", ConsoleColor.GREEN)
    }

    private fun readRevenueFromFile() {
        val fileName = "src/main/kotlin/revenue.txt"
        try {
            val file = File(fileName)
            if (file.exists()) {
                revenue = file.readText().toInt()
            }
        } catch (e: IOException) {
            println("Error occurred while reading revenue from $fileName: ${e.message}")
        }
    }

    private fun readOrdersFromFile() {
        val gson = Gson()
        val ordersJson = File("src/main/kotlin/orders.json").readText()

        val listType = object : TypeToken<List<Order>>() {}.type
        val ordersList: List<Order> = gson.fromJson(ordersJson, listType)

        orders.addAll(ordersList)
    }

    fun changeMenu() {
        println(menu)
        println("Please choose an option:")
        println("1. Add a new dish to the menu")
        println("2. Delete a dish from the menu")
        println("3. Edit a dish in the menu")
        println("0. Back to main menu")

        while (true) {
            val choice = readlnOrNull()?.toIntOrNull()
            println()

            when (choice) {
                0 -> {
                    processMenuForEmployee()
                    break
                }

                1 -> {
                    addDishToMenu()
                    break
                }

                2 -> {
                    deleteDishFromMenu()
                    break
                }

                3 -> {
                    editDishInMenu()
                    break
                }

                else -> {
                    printlnColor("Invalid choice!", ConsoleColor.RED)
                    printColor(
                        "Please enter a valid number from 1 to 3 depending on the desired action: ",
                        ConsoleColor.YELLOW
                    )
                }
            }
        }
    }

    private fun addDishToMenu() {
        println("Adding a New Dish to the Menu:")

        print("Enter the name of the dish: ")
        val name = readlnOrNull() ?: ""

        print("Enter the price of the dish: ")
        val price = readlnOrNull()?.toIntOrNull() ?: 0

        print("Enter the time to cook the dish (in minutes): ")
        val timeToCookInMin = readlnOrNull()?.toIntOrNull() ?: 0

        // Validate dish details
        if (price <= 0 || timeToCookInMin <= 0) {
            println("Price and time to cook must be greater than 0.")
            changeMenu()
            return
        }

        val id = menu.dishes.maxOfOrNull { it.id }?.plus(1) ?: 1

        // Create the new dish
        val newDish = Dish(name = name, id = id, price = price, timeToCookInMin = timeToCookInMin)

        // Add the dish to the menu
        menu.dishes.add(newDish)

        println("Dish '$name' has been added to the menu with ID $id.")
        changeMenu()
    }

    private fun deleteDishFromMenu() {
        println("Menu:")
        menu.dishes.forEach { dish ->
            println("${dish.id}. ${dish.name} - $${dish.price}")
        }

        println("Enter the ID of the dish you want to delete or '0' to exit:")
        val id = readlnOrNull()?.toIntOrNull()
        if (id == 0) {
            changeMenu()
            return
        }
        val menuDish = menu.dishes.find { it.id == id }
        if (menuDish != null) {
            menu.dishes.remove(menuDish)
            println("Dish '${menuDish.name}' with ID $id has been deleted from the menu.")
        } else {
            println("Dish with ID $id not found in the menu.")
        }
        changeMenu()
    }

    private fun editDishInMenu() {
        println("Editing a Dish in the Menu:")
        println(menu)

        print("Enter the ID of the dish you want to edit or '0' to exit: ")
        val dishId = readlnOrNull()?.toIntOrNull()

        if (dishId == 0) {
            changeMenu()
            return
        }

        val dishToEdit = menu.dishes.find { it.id == dishId }

        if (dishToEdit != null) {
            println("Selected Dish: ${dishToEdit.name} - $${dishToEdit.price} - ${dishToEdit.timeToCookInMin} min")

            println("Choose what to change:")
            println("1. Name")
            println("2. Price")
            println("3. Time to Cook")
            println("0. Back to main menu")

            print("Enter your choice: ")
            val choice = readlnOrNull()?.toIntOrNull()
            while (true) {
                when (choice) {
                    0 -> {
                        changeMenu()
                        break
                    }

                    1 -> {
                        print("Enter the new name: ")
                        val newName = readlnOrNull() ?: ""
                        dishToEdit.name = newName
                        changeMenu()
                        break
                    }

                    2 -> {
                        print("Enter the new price: ")
                        val newPrice = readlnOrNull()?.toIntOrNull() ?: 0
                        if (newPrice <= 0) {
                            println("Invalid price. Changes cannot be saved.")
                            editDishInMenu()
                            break
                        } else {
                            dishToEdit.price = newPrice
                            println("Changes saved successfully.")
                            changeMenu()
                            break
                        }
                    }

                    3 -> {
                        print("Enter the new time to cook (in minutes): ")
                        val newTimeToCook = readlnOrNull()?.toIntOrNull() ?: 0
                        if (newTimeToCook <= 0) {
                            println("Invalid time. Changes cannot be saved.")
                            editDishInMenu()
                            break
                        } else {
                            dishToEdit.timeToCookInMin = newTimeToCook
                            println("Changes saved successfully.")
                            changeMenu()
                            break
                        }
                    }

                    else -> {
                        printlnColor("Invalid choice!", ConsoleColor.RED)
                        printColor(
                            "Please enter a valid number from 1 to 3 depending on the desired action: ",
                            ConsoleColor.YELLOW
                        )
                    }
                }
            }
        } else {
            println("Dish with ID $dishId not found in the menu.")
            changeMenu()
        }
    }

    fun takeOrderFromCustomer(customer: CustomerAccount) {
        println(menu)

        var selectedDishes: MutableList<DishToCook>

        while (true) {
            var allChoicesValid = true
            println("Please enter the numbers of the dishes you want separated by commas (e.g., 1,2,3,1,2,4) or type '0' to return back:")
            val selectedDishNumbersInput = readlnOrNull()

            if (selectedDishNumbersInput == "0") {
                println()
                return processUserMenu(customer)
            }

            val selectedDishNumbers = selectedDishNumbersInput?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()

            selectedDishes = mutableListOf()
            for (number in selectedDishNumbers) {
                val menuDish = menu.dishes.find { it.id == number }
                if (menuDish != null) {
                    val dishToCook = DishToCook(
                        name = menuDish.name,
                        price = menuDish.price,
                        timeToCookInMin = menuDish.timeToCookInMin,
                        status = "Placed",
                        id = number
                    )
                    selectedDishes.add(dishToCook)
                } else {
                    printlnColor("Invalid choice of dish. Try one more time!", ConsoleColor.RED)
                    allChoicesValid = false
                    break
                }
            }

            if (allChoicesValid) {
                break
            }
        }

        val totalTimeToCook = selectedDishes.sumOf { it.timeToCookInMin }

        val order = Order(
            dishes = selectedDishes.toMutableList(),
            orderAmount = selectedDishes.sumOf { it.price },
            status = "Placed",
            totalTimeToCookInMin = totalTimeToCook,
            id = orders.size + 1,
            customer = customer
        )

        println("Your order has been placed! Thank you, ${order.customer.name}.")

        orders.add(order)
        customer.orders.add(order)
        customer.amountToPay += order.orderAmount

        order.dishes.forEach { dish ->
            dish.order = order
            assignDishToCook(dish) // Assign each dish to a cook
        }

        processMenuForCustomer(customer)
    }

    fun addDishesToOrder(customer: CustomerAccount) {
        if (customer.orders.isEmpty() || customer.orders.all { it.status == "Ready" || it.status == "Cancelled" || it.status == "Paid" }) {
            println("You don't have any orders to modify.")
            processMenuForCustomer(customer)
            return
        }

        println("Orders to modify:")
        customer.orders.filter { it.status != "Ready" && it.status != "Cancelled" && it.status != "Paid"}.forEach { println("Order ID: ${it.id}") }

        print("Enter the ID of the order you want to modify or type '0' to return back: ")
        val orderId = readlnOrNull()?.toIntOrNull()

        // Find the order with the specified ID
        val orderToModify = customer.orders.find { it.id == orderId }

        if (orderToModify == null || orderToModify.status == "Ready" || orderId == 0) {
            println("Invalid order ID or the order is already ready or you exited")
            processMenuForCustomer(customer)
            return
        }

        println(menu)

        var selectedDishes: MutableList<DishToCook>

        while (true) {
            var allChoicesValid = true
            println("Please enter the numbers of the dishes you want separated by commas (e.g., 1,2,3,1,2,4) or type '0' to return back:")
            val selectedDishNumbersInput = readlnOrNull()

            if (selectedDishNumbersInput == "0") {
                println()
                return processUserMenu(customer)
            }

            val selectedDishNumbers = selectedDishNumbersInput?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()

            selectedDishes = mutableListOf()
            for (number in selectedDishNumbers) {
                val menuDish = menu.dishes.find { it.id == number }
                if (menuDish != null) {
                    val dishToCook = DishToCook(
                        name = menuDish.name,
                        price = menuDish.price,
                        timeToCookInMin = menuDish.timeToCookInMin,
                        status = "Placed",
                        id = number
                    )
                    selectedDishes.add(dishToCook)
                } else {
                    printlnColor("Invalid choice of dish. Try one more time!", ConsoleColor.RED)
                    allChoicesValid = false
                    break
                }
            }

            if (allChoicesValid) {
                break
            }
        }

        orderToModify.dishes.addAll(selectedDishes)
        orderToModify.orderAmount += selectedDishes.sumOf { it.price }
        orderToModify.totalTimeToCookInMin += selectedDishes.sumOf { it.timeToCookInMin }

        println("Your order has been changed! Thank you, ${orderToModify.customer.name}.")

        customer.amountToPay += selectedDishes.sumOf { it.price }

        selectedDishes.forEach { dish ->
            dish.order = orderToModify
            assignDishToCook(dish) // Assign each dish to a cook
        }

        processMenuForCustomer(customer)
    }

    fun cancelTheOrder(customer: CustomerAccount) {
        if (customer.orders.isEmpty() || customer.orders.all { it.status == "Ready" || it.status == "Cancelled" || it.status == "Paid" }) {
            println("You don't have any orders to cancel.")
            processMenuForCustomer(customer)
            return
        }

        println("Orders to cancel:")
        customer.orders.filter { it.status != "Ready" && it.status != "Cancelled" && it.status != "Paid" }.forEach { println("Order ID: ${it.id}") }

        print("Enter the ID of the order you want to cancel or type '0' to return back: ")
        val orderId = readlnOrNull()?.toIntOrNull()

        // Find the order with the specified ID
        val orderToCancel = customer.orders.find { it.id == orderId }

        if (orderToCancel == null || orderToCancel.status == "Ready" || orderId == 0) {
            println("Invalid order ID or the order is already ready or you exited")
            processMenuForCustomer(customer)
            return
        }

        orderToCancel.status = "Cancelled"
        orderToCancel.dishes.forEach { it.status = "Cancelled" }

        val cooksPreparingOrder = restaurant.cooks.filter { cook -> cook.allDishes.any { it.order == orderToCancel } }

        // For each cook preparing the order
        cooksPreparingOrder.forEach { cook ->
            // Remove dishes associated with the cancelled order from the cook's queue
            val dishesToRemove = cook.allDishes.filter { it.order == orderToCancel }
            cook.allDishes.removeAll { it.order == orderToCancel }
            cook.totalDishesInQueue -= dishesToRemove.size
            cook.timeToFinishAllAssignedDishes -= dishesToRemove.sumOf { it.timeToCookInMin }
        }

        println("Your order has been canceled! Thank you, ${orderToCancel.customer.name}.")

        processMenuForCustomer(customer)
    }

    fun showStatusOfOrders(customer: CustomerAccount) {
        println("Your Orders:")
        customer.orders.forEachIndexed { index, order ->
            println("${index + 1}. Order ID: ${order.id}, Status: ${order.status}")
        }

        println("Press anything to return to main menu")
        readlnOrNull()
        processMenuForCustomer(customer)
    }

    fun payForTheOrders(customer: CustomerAccount) {
        val readyOrders = customer.orders.filter { it.status == "Ready" }

        if (readyOrders.isNotEmpty()) {
            var totalSum = 0
            println("Orders Ready for Payment:")
            readyOrders.forEachIndexed { _, order ->
                totalSum += order.orderAmount
                println("Order ${order.id}: Total Amount: $${order.orderAmount}")
            }

            println("Total Amount to Pay: $$totalSum")

            val creditCardNumber = "5609 1234 9999 7777"
            println("Credit Card Number to pay: $creditCardNumber")

            println("Type 'yes' if you have paid the total sum.")
            val input = readlnOrNull()

            if (input?.equals("yes", ignoreCase = true) == true) {
                readyOrders.forEach { it.status = "Paid" }
                revenue += totalSum
                println("Thank you for your payment! All orders have been marked as Paid.")
            } else {
                println("Payment process aborted. Orders status remains unchanged.")
            }
        } else {
            println("There are no orders ready for payment.")
        }

        processMenuForCustomer(customer)
    }

    private fun assignDishToCook(dish: DishToCook) {
        val availableCook =
            cooks.minByOrNull { it.timeToFinishAllAssignedDishes } ?: return // Find the cook who will be free soonest
        availableCook.allDishes.add(dish) // Add dish to the cook's list
        availableCook.totalDishesInQueue++
        availableCook.timeToFinishAllAssignedDishes += dish.timeToCookInMin // Update cook's time to finish all dishes
        dish.cook = availableCook // Assign the cook to the dish
    }

    private fun startCookThreads() {
        cooks.forEach { cook ->
            thread {
                while (true) {
                    val currentDish = cook.allDishes.firstOrNull() // Get the first dish in the queue
                    if (currentDish != null) {
                        printlnColor(
                            "I am cook ${cook.id}, started cooking ${currentDish.name} for ${currentDish.order?.customer?.name}. Will be ready in ${currentDish.timeToCookInMin} minutes(sec))",
                            ConsoleColor.CYAN
                        )
                        cook.currentDish = currentDish
                        cook.timeToFinishCurrentDish = currentDish.timeToCookInMin
                        cook.allDishes.removeAt(0) // Remove the processed dish from the cook's queue
                        cook.totalDishesInQueue--
                        currentDish.status = "Cooking"
                        currentDish.order?.status = "Cooking"
                        Thread.sleep(currentDish.timeToCookInMin.toLong() * 500) // Simulate cooking time

                        val cookingTimeMs = currentDish.timeToCookInMin * 500L // Convert cooking time to milliseconds
                        var elapsedTime = 0L // Initialize elapsed time

                        while (elapsedTime < cookingTimeMs) {
                            // Check if the order associated with the current dish is cancelled
                            if (currentDish.order?.status == "Cancelled") {
                                currentDish.status = "Cancelled"
                                break // Exit the loop if the order is cancelled
                            }
                            // Sleep for a short duration before checking again
                            Thread.sleep(100)
                            elapsedTime += 100
                        }

                        cook.timeToFinishAllAssignedDishes -= currentDish.timeToCookInMin
                        if (currentDish.status != "Cancelled") {
                            currentDish.status = "Ready"
                            printlnColor(
                                "Cook ${cook.id} finished preparing dish ${currentDish.name} for ${currentDish.order?.customer?.name}",
                                ConsoleColor.CYAN
                            )
                            if (currentDish.order?.dishes?.all { it.status == "Ready" } == true) {
                                currentDish.order?.status = "Ready" // Update order status to "Ready"
                                printlnColor(
                                    "Order №${currentDish.order!!.id} for ${currentDish.order!!.customer.name} is ready. Please, pay ${currentDish.order!!.orderAmount}$",
                                    ConsoleColor.GREEN
                                )
                            }
                        }

                    }
                    Thread.sleep(500)
                }
            }
        }
    }

}