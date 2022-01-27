package scala

import scala.lexer._
import java.io._

object HelloSbt {
    def main(args: Array[String]) = {
        val fileName = "test.in"
        val fileReader = new BufferedReader(new FileReader(fileName))
        val lexier = new Lexier(fileReader)
        println(lexier)
        var res = lexier.next_token()
        while (res != null) {
            println(res)
            res = lexier.next_token()
        }
    }
}