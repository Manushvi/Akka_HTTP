package http

import java.sql.{Connection, DriverManager}

object DatabaseConnection {
  private   val driver = "com.mysql.cj.jdbc.Driver"
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
class CreateUserRequestHandler {
  def createUser(user: User): Unit = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.prepareStatement("INSERT INTO users (id , name, startTime) VALUES (?, ? , ?)")
    statement.setInt(1, user.id)
    statement.setString(2, user.name)
    statement.setString(2, user.startTime)
    statement.executeUpdate()
  }
}

class GetUserRequestHandler {
  def getUser(id: Int): Option[User] = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")
    statement.setInt(1, id)
    val result = statement.executeQuery()
    if (result.next()) {
      val name = result.getString("name")
      val startTime = result.getString("startTime")
      Some(User(id , name , startTime))
    } else {
      None
    }
  }
}

class GetAllUserRequestHandler {
  def getAllUsers(): Seq[User] = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.createStatement()
    val result = statement.executeQuery("SELECT * FROM users")
    var users = Seq.empty[User]
    while (result.next()) {
      val id = result.getInt("id")
      val name = result.getString("name")
      val startTime = result.getString("startTime")
      users = users :+ User(id, name, startTime)
    }
    users
  }
}

class UpdateUserRequestHandler {
  def updateUser(id: Int, name: String, startTime: String): Unit = {
    val connection = DatabaseConnection.getConnection()
    val statement = connection.prepareStatement("UPDATE users SET name = ?, startTime = ? WHERE id = ?")
    statement.setInt(1, id)
    statement.setString(2, name)
    statement.setString(3,startTime)
    statement.executeUpdate()
  }
}







