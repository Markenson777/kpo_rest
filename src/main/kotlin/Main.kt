import kotlin.system.exitProcess

val restaurant = Restaurant()

fun main() {
    printlnColor("Welcome to the restaurant!", ConsoleColor.CYAN)
    println()

    processStartingMenu()
}

fun processStartingMenu() {
    printlnColor("Please choose an action.", ConsoleColor.GREEN)
    println("1. Sign in or sign up as a restaurant customer")
    println("2. Sign in or sign up as a restaurant administrator")
    println("3. Exit the restaurant")
    printColor("Enter a number from 1 to 3 depending on the desired action: ", ConsoleColor.YELLOW)

    while (true) {
        val choice = readlnOrNull()?.toIntOrNull()
        println()

        when (choice) {
            1 -> {
                val customer = customerAuthorisation() as CustomerAccount
                processUserMenu(customer)
                break
            }

            2 -> {
                val employee = employeeAuthorisation() as EmployeeAccount
                processUserMenu(employee)
                break
            }

            3 -> {
                printlnColor("Exiting the restaurant. Goodbye!", ConsoleColor.BLUE)
                restaurant.saveRevenue()
//                restaurant.saveOrders()
                restaurant.saveDishes()
                exitProcess(0)
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

fun processUserMenu(user: Account) {
    if (user.status == "employee") {
        processMenuForEmployee()
    } else {
        processMenuForCustomer(user as CustomerAccount)
    }
}

fun processMenuForCustomer(customer: CustomerAccount) {
    printlnColor("Please choose an action.", ConsoleColor.GREEN)
    println("1. Place an order")
    println("2. Change the order")
    println("3. Cancel the order")
    println("4. Check the status of the order")
    println("5. Pay for the order")
    println("6. Log out")
    println("7. Exit the restaurant")
    printColor("Enter a number from 1 to 7 depending on the desired action: ", ConsoleColor.YELLOW)

    while (true) {
        val choice = readlnOrNull()?.toIntOrNull()
        println()

        when (choice) {
            1 -> {
                restaurant.takeOrderFromCustomer(customer)
                break
            }

            2 -> {
                restaurant.addDishesToOrder(customer)
                break
            }

            3 -> {
                restaurant.cancelTheOrder(customer)
                break
            }

            4 -> {
                restaurant.showStatusOfOrders(customer)
                break
            }

            5 -> {
                restaurant.payForTheOrders(customer)
                break
            }

            6 -> {
                processStartingMenu()
                break
            }

            7 -> {
                printlnColor("Exiting the restaurant. Goodbye!", ConsoleColor.BLUE)
                restaurant.saveRevenue()
//                restaurant.saveOrders()
                restaurant.saveDishes()
                exitProcess(0)
            }

            else -> {
                printlnColor("Invalid choice!", ConsoleColor.RED)
                printColor(
                    "Please enter a valid number from 1 to 7 depending on the desired action: ",
                    ConsoleColor.YELLOW
                )
            }
        }
    }
}

fun processMenuForEmployee() {
    printlnColor("Please choose an action.", ConsoleColor.GREEN)
    println("1. Change the menu")
    println("2. Log out")
    println("3. Exit the Restaurant")
    printColor("Enter a number from 1 to 3 depending on the desired action: ", ConsoleColor.YELLOW)

    while (true) {
        val choice = readlnOrNull()?.toIntOrNull()
        println()

        when (choice) {
            1 -> {
                restaurant.changeMenu()
                break
            }

            2 -> {
                processStartingMenu()
                break
            }

            3 -> {
                printlnColor("Exiting the restaurant. Goodbye!", ConsoleColor.BLUE)
                restaurant.saveRevenue()
//                restaurant.saveOrders()
                restaurant.saveDishes()
                exitProcess(0)
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

fun printlnColor(text: String, color: ConsoleColor) {
    println("${color.code}$text${ConsoleColor.RESET.code}")
}

fun printColor(text: String, color: ConsoleColor) {
    print("${color.code}$text${ConsoleColor.RESET.code}")
}

enum class ConsoleColor(val code: String) {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    CYAN("\u001B[36m")
}