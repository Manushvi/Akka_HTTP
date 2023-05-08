package http

import java.sql.{Connection, DriverManager}
import scala.util.{Try, Success, Failure}


// This object is for connection with local database....
object DatabaseConnection {
  private val driver = "com.mysql.cj.jdbc.Driver"
  private val url = "jdbc:mysql://localhost/db"
  private val username = "manu"
  private val password = "manu@1234"
  private var connection: Option[Connection] = None

  def getConnection(): Connection = {
    if (connection.isEmpty || connection.get.isClosed()) {
      Class.forName(driver)
      connection = Some(DriverManager.getConnection(url, username, password))
    }
    connection.get
  }
}

// This is for Creating a new user in the table with password validation...
class CreateUserRequestHandler {
  def createUser(user: User): Try[Unit] = {
    if (!validatePassword(user.password)) {
      Failure(new IllegalArgumentException("Password is not valid....Make stronger password above length 8.."))
    } else {
      val connection = DatabaseConnection.getConnection()
      val statement = connection.prepareStatement("INSERT INTO USERS(id, name, startTime, createdAt, password) VALUES (?,?,?,?,?)")
      statement.setInt(1, user.id)
      statement.setString(2, user.name)
      statement.setString(3, user.startTime)
      statement.setString(4, user.createdAt)
      statement.setString(5, user.password)
      statement.executeUpdate()
      Success(())
    }
  }
  def validatePassword(password: String): Boolean = {
    val hasDigit = password.matches(".*\\d.*")
    val hasChar = password.matches(".*[a-zA-Z].*")
    val hasSpecialChar = password.matches(".*[!@#$%^&*(){}\\[\\]|;:'\"<>,.?/~`_-].*")
    val hasValidLength = password.length > 8
    hasDigit && hasChar && hasSpecialChar && hasValidLength
  }
}

// this is for getting a specific user with writing their particular id...
class GetUserRequestHandler {
  def getUser(id: Int): Option[User] = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.prepareStatement("SELECT * FROM USERS WHERE id = ?")
    statement.setInt(1, id)
    val result = statement.executeQuery()
    if (result.next()) {
      val name = result.getString("name")
      val startTime = result.getString("startTime")
      val createdAt = result.getString("createdAt")
      val password = result.getString("password")
      Some(User(id , name , startTime , createdAt,password))
    } else {
      None
    }
  }
}

object UserDB {
  def authenticateUser(name: String, password: String): Option[User] = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.createStatement()
    val query = s"SELECT * FROM USERS WHERE name='$name' AND password='$password'"
    val result = statement.executeQuery(query)

    if (result.next()) {
      val id = result.getInt("id")
      val name = result.getString("name")
      val startTime = result.getString("startTime")
      val createdAt = result.getString("createdAt")
      val password = result.getString("password")
      Some(User(id, name, startTime, createdAt, password))
    } else {
      None
    }
  }
}
// this is for getting all users with authentication...
class GetAllUserRequestHandler {
  import UserDB._
  def getAllUsers(name: String, password: String): Option[Seq[User]] = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.createStatement()
    val authenticatedUser = UserDB.authenticateUser(name, password) // authenticate user using name and password
    if (authenticatedUser.isDefined) {
      val result = statement.executeQuery("SELECT * FROM USERS")
      var users = Seq.empty[User]
      while (result.next()) {
        val id = result.getInt("id")
        val name = result.getString("name")
        val startTime = result.getString("startTime")
        val createdAt = result.getString("createdAt")
        val password = result.getString("password")
        users = users :+ User(id, name, startTime, createdAt,password)
      }
      Some(users)
    } else {
      None
    }
  }
}

// this is for updating a user as id passed as a query parameter..
class UpdateUserRequestHandler {
  def updateUser(id: Int, name: String, startTime: String, createdAt: String): Unit = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.prepareStatement("UPDATE USERS SET name = ?, startTime = ? ,createdAt = ? , WHERE id = ?")
    statement.setString(1, name)
    statement.setString(2, startTime)
    statement.setString(3, createdAt)
    statement.setInt(4,id)
    statement.executeUpdate()
  }
}







