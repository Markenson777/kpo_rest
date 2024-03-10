import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.io.File
import java.security.MessageDigest
import java.util.*
import java.nio.charset.StandardCharsets
import com.google.gson.annotations.SerializedName


var customerAccounts = mutableListOf<CustomerAccount>()
var employeeAccounts = mutableListOf<EmployeeAccount>()

fun encryptData(data: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val hashedBytes = md.digest(data.toByteArray(StandardCharsets.UTF_8))

    return Base64.getEncoder().encodeToString(hashedBytes)
}

var gson = Gson()

open class Account {
    @Transient
    open var status: String = "undefined status"
    open var encryptedEmail: String = ""
    open var encryptedPassword: String = ""
    open var name: String = "No name"
}

class EmployeeAccount : Account() {
    override var status: String = "employee"
    fun createAccount(email: String, password: String, username: String) {
        encryptedEmail = encryptData(email)
        encryptedPassword = encryptData(password)
        name = username

        val existingAccount = employeeAccounts.find { it.encryptedEmail == encryptedEmail }

        if (existingAccount == null) {
            // If no account with the same email exists, add the new account to the list
            employeeAccounts.add(this)
        }

        // Save the account to customerAccounts.json
        saveAccount()

        printlnColor("Your account has been successfully created!", ConsoleColor.GREEN)
    }

    private fun saveAccount() {
        val accountsJson = File("src/main/kotlin/employeeAccounts.json")
        val existingAccounts = if (accountsJson.exists()) {
            gson.fromJson(accountsJson.readText(), object : TypeToken<Map<String, EmployeeAccount>>() {}.type)
        } else {
            emptyMap<String, EmployeeAccount>()
        }

        val updatedAccounts = existingAccounts + (this.encryptedEmail to this)
        val updatedJson = gson.toJson(updatedAccounts)

        accountsJson.writeText(updatedJson)
    }
}

class CustomerAccount : Account() {
    override var status: String = "customer"
    var orders: MutableList<Order> = mutableListOf()
    var amountToPay: Int = 0
    fun createAccount(email: String, password: String, username: String) {
        encryptedEmail = encryptData(email)
        encryptedPassword = encryptData(password)
        name = username

        val existingAccount = customerAccounts.find { it.encryptedEmail == encryptedEmail }

        if (existingAccount == null) {
            // If no account with the same email exists, add the new account to the list
            customerAccounts.add(this)
        }

        // Save the account to customerAccounts.json
        saveAccount()

        printlnColor("Your account has been successfully created!", ConsoleColor.GREEN)
    }

    private fun saveAccount() {
        val accountsJson = File("src/main/kotlin/customerAccounts.json")
        val existingAccounts = if (accountsJson.exists()) {
            gson.fromJson(accountsJson.readText(), object : TypeToken<Map<String, CustomerAccount>>() {}.type)
        } else {
            emptyMap<String, CustomerAccount>()
        }

        val updatedAccounts = existingAccounts + (this.encryptedEmail to this)
        val updatedJson = gson.toJson(updatedAccounts)

        accountsJson.writeText(updatedJson)
    }
}

fun customerAuthorisation(): Any? {
    println("Let's create a new account for you or log in to an existing one!")
    printlnColor("If you want to return to main menu just type 'exit'.", ConsoleColor.BLACK)
    print("Enter your email: ")
    val email = readlnOrNull() ?: ""

    if (email.trim().lowercase(Locale.getDefault()) == "exit") {
        println()
        return processStartingMenu()
    } else {
        println()
        return signUpOrSignIn(email, "customer")
    }
}

fun employeeAuthorisation(): Any? {
    println("Let's create a new account for you or log in to an existing one!")
    println("As you want to sign in or sign up as an employee I need you to provide me a secret code or type 'exit' to return to main menu.")
    while (true) {
        printlnColor("Just type '1234':)", ConsoleColor.BLACK)
        printColor("Enter secret code: ", ConsoleColor.YELLOW)
        val secretCode = readlnOrNull() ?: ""
        if (secretCode.trim().lowercase(Locale.getDefault()) == "exit") {
            println()
            return processStartingMenu()
        }
        if (secretCode.trim().lowercase(Locale.getDefault()) == "1234") {
            printlnColor("Access granted!", ConsoleColor.GREEN)
            break
        } else {
            printlnColor("Wrong code!\nTry again or type 'exit' to return to main menu", ConsoleColor.RED)
        }
    }
    print("Enter your email: ")
    val email = readlnOrNull() ?: ""

    if (email.trim().lowercase(Locale.getDefault()) == "exit") {
        println()
        return processStartingMenu()
    } else {
        println()
        return signUpOrSignIn(email, "employee")
    }
}

fun signUpOrSignIn(email: String, status: String): Any? {
    val accounts = if (status == "customer") {
        loadCustomerAccounts()
    } else {
        loadEmployeeAccounts()
    }
    val hashedEnteredEmail = encryptData(email)

    if (doesEmailExist(email)) {
        printlnColor("You already have an account!", ConsoleColor.BLUE)

        if (accounts[hashedEnteredEmail]?.status != status) {
            printlnColor(
                "Sorry, you have already selected different role: $status. Please try again.", ConsoleColor.RED
            )
            return if (status == "customer") {
                customerAuthorisation()
            } else {
                employeeAuthorisation()
            }
        }

        while (true) {
            printColor("Enter your password: ", ConsoleColor.YELLOW)
            val enteredPassword = readlnOrNull() ?: ""

            if (isPasswordCorrect(email, enteredPassword, status)) {
                // Password is correct, log in
                val existingAccount = if (status == "customer") {
                    customerAccounts.find { it.encryptedEmail == hashedEnteredEmail }
                } else {
                    employeeAccounts.find { it.encryptedEmail == hashedEnteredEmail }
                }
                printlnColor("Logged in successfully!", ConsoleColor.GREEN)
                printlnColor("Now you can access all the functionality of our restaurant!", ConsoleColor.BLUE)
                return existingAccount
            } else {
                // Incorrect password, ask for a repeat
                printlnColor("Incorrect password. Please try again.", ConsoleColor.RED)
            }
        }
    } else {
        // The email doesn't exist, create a new account
        printColor("You don't have an account yet. Let's create one!\nCreate your password: ", ConsoleColor.BLUE)
        val password = readlnOrNull() ?: ""

        printColor("Enter your name: ", ConsoleColor.BLUE)
        val name = readlnOrNull() ?: ""

        if (status == "employee") {
            val user = EmployeeAccount()
            user.createAccount(email, password, name)
            printlnColor("Now you can access all the functionality of our restaurant!", ConsoleColor.BLUE)
            return user
        } else {
            val user = CustomerAccount()
            user.createAccount(email, password, name)
            printlnColor("Now you can access all the functionality of our restaurant!", ConsoleColor.BLUE)
            return user
        }
    }
}

fun doesEmailExist(email: String): Boolean {
    // Hash the entered email
    val hashedEnteredEmail = encryptData(email)

    // Load existing accounts from userAccounts.json
    val existingEmployeeAccounts = loadEmployeeAccounts()
    val existingCustomerAccounts = loadCustomerAccounts()

    // Check if the email exists in the accounts
    return existingEmployeeAccounts.keys.any { it == hashedEnteredEmail } || existingCustomerAccounts.keys.any { it == hashedEnteredEmail }
}

fun loadEmployeeAccounts(): Map<String, EmployeeAccount> {
    val gson = Gson()
    val accountsJson = File("src/main/kotlin/employeeAccounts.json").readText()
    val type = object : TypeToken<Map<String, EmployeeAccount>>() {}.type
    val accountList: Map<String, EmployeeAccount> = gson.fromJson(accountsJson, type)
//    val accountList: Map<String, EmployeeAccount> =
//        mapOf("8zmy0dwgAUxSwBghVcvscx54M7yr73UL/qG2XqcXrnw=" to EmployeeAccount().apply {
//            encryptedEmail = "8zmy0dwgAUxSwBghVcvscx54M7yr73UL/qG2XqcXrnw="
//            encryptedPassword = "A6xnQhbz4Vx2HuGl4lXwZ5U2I8iziLRFnhP5eNfIRvQ="
//            name = "no name"
//            status = "employee"
//        })

    return accountList

//    return if (accountsJson.exists()) {
//        println(accountsJson.readText())
//        gson.fromJson(accountsJson.readText(), type)
//    } else {
//        emptyMap()
//    }
}

fun loadCustomerAccounts(): Map<String, CustomerAccount> {
    val gson = Gson()
    val accountsJson = File("src/main/kotlin/customerAccounts.json").readText()

    val type: Type = object : TypeToken<Map<String, CustomerAccount>>() {}.type

//    return if (accountsJson.exists()) {
//        gson.fromJson(accountsJson.readText(), type)
//    } else {
//        emptyMap()
//    }

    val accountList: Map<String, CustomerAccount> = gson.fromJson(accountsJson, type)



    return accountList
}

fun isPasswordCorrect(email: String, enteredPassword: String, status: String): Boolean {
    // Load existing accounts from userAccounts.json
    val existingAccounts = if (status == "customer") {
        loadCustomerAccounts()
    } else {
        loadEmployeeAccounts()
    }

    // Hash the entered email
    val hashedEnteredEmail = encryptData(email)

    // Get the UserAccount associated with the entered email
    val userAccount = existingAccounts[hashedEnteredEmail]

    // Compare the entered password with the stored password
    return userAccount?.encryptedPassword == encryptData(enteredPassword)
}
