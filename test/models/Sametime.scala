package models

import com.lotus.sametime.core.comparch.STSession;

object Sametime {

  def main(args: Array[String]): Unit = {
    println("hello, test")
    var javatest = new SametimeJava
    println(javatest.toUpper("abCD"))
    println("abc")
  }

}