package http
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import scala.util.{Success, Failure}
import spray.json.DefaultJsonProtocol

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class User(id: Int , name: String, startTime: String, createdAt: String , password: String)

trait UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat5(User)
}

object Implementation extends App with UserJsonProtocol with SprayJsonSupport {
  implicit val system = ActorSystem("Implementation")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val currentDateTime = LocalDateTime.now()
  val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  val formattedDateTime = currentDateTime.format(formatter)


  val route: Route = path("users") {
    post {
      entity(as[User]) { user =>
        val createUserRequestHandler = new CreateUserRequestHandler()
        val userWithCreatedAt = user.copy(createdAt = formattedDateTime)
        createUserRequestHandler.createUser(userWithCreatedAt) match {
          case Success(_) =>
            println(s"User created successfully")
            complete(StatusCodes.OK, "User created successfully")
          case Failure(ex) =>
            println(s"User not created: ${ex.getMessage}")
            complete(StatusCodes.BadRequest, ex.getMessage)
        }
      }
    }
  } ~
    path("users" / IntNumber) { id =>
      get {
         if(id <= 0){
           complete(StatusCodes.BadRequest, "Invalid user Id")
         }
        else
         {
           val getUserRequestHandler = new GetUserRequestHandler()
             getUserRequestHandler.getUser(id) match {
             case Some(user) => complete(user)
             case None => complete(StatusCodes.NotFound, s"User with the id $id not found")
           }
         }
      }
    } ~
    path("users") {
        parameters('name.as[String] ,'password.as[String]) { (name, password) =>
          get {
            val requestHandler = new GetAllUserRequestHandler
            requestHandler.getAllUsers(name, password) match {
              case Some(users) =>
                println(s"Retrieved ${users.size} users")
                complete(StatusCodes.OK, users)
              case None =>
                println("Authentication failed")
                complete(StatusCodes.Unauthorized , "Provide correct Credentials")
            }
          }
        }
    }~
    path("users" / IntNumber) { id =>
      patch {
        entity(as[User]) { user =>
          val requestHandler = new UpdateUserRequestHandler()
          val userWithCreatedAt = user.copy(createdAt = formattedDateTime)
          requestHandler.updateUser(id, user.name, user.startTime, userWithCreatedAt.createdAt)
          complete(StatusCodes.OK ,s"User with id $id has been updated.")
        }
      }
    }

  Http().bindAndHandle(route,"localhost",8080)

}

