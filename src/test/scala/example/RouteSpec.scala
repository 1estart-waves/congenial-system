package example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should._
import org.scalatest.flatspec.AnyFlatSpec

import Hello.route3
import akka.http.scaladsl.server.MissingQueryParamRejection

import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Arbitrary
import org.scalatest.freespec.AnyFreeSpec
import scala.util.Try

class RouteSpec  extends AnyFreeSpec with  ScalatestRouteTest with Matchers with ScalaCheckPropertyChecks {
//   "not pass positive int" in {
//     Get("ping") ~> route3 ~> check {
//        assert(!handled)
//        rejection shouldEqual MissingQueryParamRejection("color")
//   }
// }



  "gen negative ints and str" in {
      val genStr: Gen[String] = Gen.alphaNumStr suchThat (s => Try(java.lang.Integer.parseInt(s)).isFailure)
      val negativeInt: Gen[String] = for (i <- Arbitrary.arbitrary[Int] suchThat (_ < 0)) yield i.toString
      val nonPositiveIntStrings = Gen.oneOf(genStr, negativeInt)

      

      val urls = Gen.oneOf("abc", "bcd")
      
      val gen = for {
          from <- nonPositiveIntStrings
          to <- nonPositiveIntStrings
          url <- urls
      } yield (url, from, to)

      forAll(gen) {
          case s => 
              println(s)
              true shouldBe true
      }
  }


}
