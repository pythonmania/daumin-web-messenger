package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import models._

import akka.actor._
import akka.util.duration._

object Application extends Controller {

  /**
   * Just display the home page.
   */
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  /**
   * Display the chat room page.
   */
  def chatRoom(username: String, password: String) = Action { implicit request =>
    println(username)
    println(password)
    Ok(views.html.chatRoom(username))
//      Redirect(routes.Application.index).flashing(
//        "error" -> "Please choose a valid username.")
//    }
  }

  /**
   * Handles the chat websocket.
   */
  def chat(username: String) = WebSocket.async[JsValue] { request =>
    ChatRoom.join(username)

  }

}
