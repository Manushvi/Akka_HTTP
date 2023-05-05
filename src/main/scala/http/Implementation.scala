package http
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

case class User(id: Int , name: String, startTime: String)

trait UserJsonProtocol extends DefaultJsonProtocol{
  implicit val userFormat = jsonFormat3(User)
}
object Implementation extends App with UserJsonProtocol with SprayJsonSupport{
  implicit val system = ActorSystem("Implementation")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  object CreateUserRoute {
    val createUserRequestHandler = new CreateUserRequestHandler()
    val route: Route = path("users") {
      post {
        entity(as[User]) { user =>
          createUserRequestHandler.createUser(user)
          complete("User created successfully")
        }
      }
    }
  }

  object GetUserRoute {
    val getUserRequestHandler = new GetUserRequestHandler()
    val route: Route = path("users" / IntNumber) { id =>
      get {
        getUserRequestHandler.getUser(id) match {
          case Some(user) => complete(user)
          case None => complete("User not found")
        }
      }
    }
  }

  object GetAllUserRoute {
    val requestHandler = new GetAllUserRequestHandler
    val route: Route = path("users") {
      get {
        val users = requestHandler.getAllUsers()
        complete(users)
      }
    }
  }

  object UpdateUserRoute {
    val updateUserRoute = path("users" / IntNumber) {id =>
      patch {
        entity(as[User]) { user =>
          val requestHandler = new UpdateUserRequestHandler()
          requestHandler.updateUser(id, user.name, user.startTime)
          complete(s"User with id $id has been updated.")
        }
      }
    }
    val route: Route = updateUserRoute
  }

  val routes = GetUserRoute.route ~ GetAllUserRoute.route ~ CreateUserRoute.route ~ UpdateUserRoute.route
  Http().bindAndHandle(routes,"localhost",8080)

}

