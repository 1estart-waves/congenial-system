package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.PathMatcher1
import akka.http.scaladsl.server.PathMatchers.Remaining

import com.typesafe.config.ConfigFactory
import java.security.KeyStore
import java.io.InputStream
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.SSLContext
import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.ConnectionContext
import java.security.SecureRandom
import com.typesafe.config.Config
import akka.dispatch.ExecutionContexts
import monix.execution.Scheduler.computation
import akka.http.scaladsl.server.PathMatcher
import akka.http.javadsl.server.PathMatcher0
import scala.util.Try
import akka.http.scaladsl.server.ValidationRejection

object Hello extends App {

  val password: Array[Char] =
    "password".toCharArray // do not store passwords in code, read them from somewhere safe!

  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keystore: InputStream =
    getClass.getClassLoader.getResourceAsStream("keystore.p12")

  require(keystore != null, "Keystore required!")
  ks.load(keystore, password)

  val keyManagerFactory: KeyManagerFactory =
    KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)

  val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)

  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(
    keyManagerFactory.getKeyManagers,
    tmf.getTrustManagers,
    new SecureRandom
  )
  val https: HttpsConnectionContext = ConnectionContext.https(sslContext)
  lazy val enableHttp2: Config =
    ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")

  implicit val system = ActorSystem("Main", conf)
  lazy val conf = ConfigFactory.parseString(
    """akka.http.parsing.uri-parsing-mode = strict"""
  )

  implicit val materializer = ActorMaterializer.create(system)

//  lazy val enableHttp2: Config = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")

  def route1: Route = (get & path(Segment)) { value =>
    // `get` for HTTP GET method
    parameters(('offset.as[Int].?, 'limit.as[Int].?, 'matches.as[String].?)) {
      (offset, limit, matches) =>
        // println(matches.get)
        val p = matches.get.r.pattern
        val f = (key: String) => p.matcher(key).matches()
        matches.foreach(println)

        val l = List("AGREEMENTS_1234/1346-AB-44")
        entity(as[String]) { a =>
          complete(
            s"Hello World: value $value offset: ${offset.getOrElse(0)} limit: ${limit
              .getOrElse(10)} matches:" +
              s" ${matches.getOrElse("ulala")} res: ${l.filter(f).mkString}"
          )
        }

    }
  }

  def route2 =
      (get & path(Segment / Segment)) { (contractId, key) =>
        withExecutionContext(ExecutionContexts.fromExecutor(computation())) {
        complete(s"Hi ${key.toString()}")
        }
      }
    

  val isPositiveInt: String => Boolean = key => {
    val tryInt = Try(key.toInt)
    tryInt.isSuccess && tryInt.get >= 0
  }

  def isPositiveInt(keys: String*): Boolean = keys.forall(isPositiveInt)

  def validatePositiveInt(numbers: String*) = {
    validate(
      isPositiveInt(numbers: _*),
      s"Path components: [${numbers.filterNot(isPositiveInt).mkString("; ")}] must be positive integers"
    ) & provide(numbers)
  }

  import cats.implicits._
  import cats.instances._

  import cats.syntax.traverse.toTraverseOps
  import cats.implicits._

  import cats.Semigroup
  import cats.data.{NonEmptyList, OneAnd, Validated, ValidatedNel}
  import cats.implicits._

  case class PositiveInt(i: Int) {
    require(i > 0)
  }

  object PositiveInt {
    val stringToPositiveInt: String => ValidatedNel[String, PositiveInt] = s =>
      Validated
        .catchOnly[NumberFormatException](s.toInt)
        .leftMap(_ => NonEmptyList.of(s))
        .andThen {
          case i if i > 0 => PositiveInt(i).valid
          case negative   => NonEmptyList.of(negative.toString).invalid
        }

    def fromStr(keys: String*): ValidatedNel[String, List[PositiveInt]] =
      keys.toList.traverse(stringToPositiveInt)
  }

  object validUtils {
    implicit class processValNel[T](
        private val x: ValidatedNel[String, List[T]]
    ) extends AnyVal {
      def process(f: List[T] => StandardRoute): StandardRoute =
        x.fold(
          errors =>
            reject(
              ValidationRejection(
                s"Да что ж это такое? ${errors.toList.mkString("; ")}"
              )
            ),
          f
        )
    }
  }

  val stringToPositiveInt: String => Either[String, Int] = (k: String) =>
    Either
      .catchOnly[NumberFormatException](k.toInt)
      .leftMap(_ => "Your error for number format exception")
      .flatMap { i =>
        Either.cond(i >= 0, i, "Your error text positive int validation")
      }
  def fromStr(keys: String*): Either[String, List[Int]] =
    keys.toList
      .traverse(stringToPositiveInt)

  import validUtils._
  def route3: Route =
    withExecutionContext(ExecutionContexts.fromExecutor(computation())) {
      (get & path(Segment / Segment)) { (key, key2) =>
        PositiveInt
          .fromStr(key, key2)
          .process { case List(i, k) =>
            complete(s"Hi [${i.toString()}]")
          }
      }
    }

  implicit val generalRejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .result()

  val route = concat(
    pathPrefix("route") { route1 ~ route2 },
    pathPrefix("uuu") { route3 }
  )
  // Http()
  Http()
    .bindAndHandle(route, "127.0.0.1", 9000)
  // , connectionContext = https)

}
