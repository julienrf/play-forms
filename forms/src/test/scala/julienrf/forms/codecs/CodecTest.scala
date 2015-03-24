package julienrf.forms.codecs

import org.scalacheck.Prop._
import org.scalacheck.Properties

object CodecTest extends Properties("Codec") {

  object laws {
    def encodeDecode[A, B](codec: Codec[A, B], b: B): Boolean =
      codec.encode(b).exists(a => succeeds(b, codec.decode(a)))
  }

  val text = forAll { (s: String) =>
    s.nonEmpty ==> {
      val codec = Codec.text
      laws.encodeDecode(codec, s) &&
      fails(Seq(Error.Required), codec.decode(Nil)) &&
      fails(Seq(Error.Required), codec.decode(Seq(""))) // empty text is not valid
    }
  }
  val int = forAll { (n: Int) =>
    val codec = Codec.int
    laws.encodeDecode(codec, n) &&
    codec.decode(Nil) == Left(Seq(Error.Required))
  }
  val boolean = forAll { (b: Boolean) =>
    laws.encodeDecode(Codec.boolean, b)
  }
  val min = forAll { (n: Int, m: Int) =>
    val constraint = Constraint.min(m)
    val result = constraint.decode(n)
    if (n >= m) succeeds(n, result) else result == Left(Seq(Error.MustBeAtLeast(m)))
  }
  val oneOf = forAll { (map: Map[Int, String], s: String) =>
    (map.values.forall(_.nonEmpty) && s.nonEmpty && map.values.find(_ == s).isEmpty) ==> {
      val codec = Codec.oneOf(map)
      map.keys.forall(n => laws.encodeDecode(codec, n)) &&
      fails(Seq(Error.Undefined), codec.decode(Seq(s)))
    }
  }
  val severalOf = forAll { (map: Map[Int, String], s: String) =>
    (map.values.forall(_.nonEmpty) && s.nonEmpty && map.values.find(_ == s).isEmpty) ==> {
      val codec = Codec.severalOf(map)
      val nss = for {
        i <- 0 until map.keys.size
        j <- 0 until i
      } yield map.keys.slice(j, i)
      nss.forall(ns => laws.encodeDecode(codec, ns.to[Seq])) &&
      fails(Seq(Error.Undefined), codec.decode(Seq(s))) &&
      map.values.forall(s2 => fails(Seq(Error.Undefined), codec.decode(Seq(s, s2)))) // One wrong key makes the whole thing fail
    }
  }

  property("codecs") = text && int && boolean && min && oneOf && severalOf

  property("constraints") = {
    def laws[A](constraint: Constraint[A], a: A) = {
      val decode = constraint.decode(a) == (constraint.validate(a) match {
        case Some(es) => Left(es)
        case None => Right(a)
      })
      val encode = constraint.encode(a) == (constraint.validate(a) match {
        case Some(es) => None
        case None => Some(a)
      })
      decode && encode
    }
    forAll { (n: Int, m: Int) => laws(Constraint.min(n), m) }
  }

  val kleisli = {
    object laws {
      def decode[A, B](codec1: Codec[A, B], codec2: Codec[B, _], a: A) =
        (codec1 >=> codec2).decode(a) == codec1.decode(a).right.flatMap(codec2.decode)
      def encode[B, C](codec1: Codec[_, B], codec2: Codec[B, C], c: C) =
        (codec1 >=> codec2).encode(c) == codec2.encode(c).flatMap(codec1.encode)
    }
    forAll { (n: Int, m: Int, b: Boolean) =>
      laws.decode(Codec.int, Constraint.min(m), if (b) Seq(n.toString) else Nil) &&
      laws.encode(Codec.int, Constraint.min(m), n)
    }
  }
  val orElse = {
    object laws {
      def decode[A](codec1: Codec[A, _], codec2: Codec[A, _], a: A) =
        (codec1 || codec2).decode(a) == (codec1.decode(a) match {
          case Right(b) => Right(Left(b))
          case Left(_) => codec2.decode(a).right.map(c => Right(c))
        })
      def encode[A, B, C](codec1: Codec[A, B], codec2: Codec[A, C], bOrC: Either[B, C]) =
        (codec1 || codec2).encode(bOrC) == (bOrC match {
          case Left(b) => codec1.encode(b)
          case Right(c) => codec2.encode(c)
        })
    }
    forAll { (s: Seq[String]) => laws.decode(Codec.boolean, Codec.int, s) } && // FIXME Should use a more specific generator
    forAll { (bOrN: Either[Boolean, Int]) => laws.encode(Codec.boolean, Codec.int, bOrN) }
  }
  val opt = {
    object laws {
      def decode[A](codec: Codec[A, _], a: A) =
        succeeds(codec.decode(a).right.toOption, codec.?.decode(a))
      def encode[B](codec: Codec[_, B], maybeB: Option[B]) =
        codec.?.encode(maybeB) == (maybeB flatMap codec.encode)
    }
    forAll { (maybeN: Option[Int]) =>
      laws.decode(Codec.int, maybeN.to[Seq].map(_.toString)) &&
      laws.encode(Codec.int, maybeN)
    }
  }
  val and = {
    def laws[A](constraint1: Constraint[A], constraint2: Constraint[A], a: A) = {
      val constraint = constraint1 && constraint2
      val decode = constraint.decode(a) == ((constraint1.decode(a), constraint2.decode(a)) match {
        case (Right(`a`), Right(`a`)) => Right(a)
        case (Right(`a`), Left(es)) => Left(es)
        case (Left(es), Right(`a`)) => Left(es)
        case (Left(es1), Left(es2)) => Left(es1 ++ es2) // Errors are accumulated
      })
      val encode = constraint.encode(a) == constraint1.encode(a).flatMap(_ => constraint2.encode(a))
      decode && encode
    }
    forAll { (x: Int, y: Int, z: Int) => laws(Constraint.min(x), Constraint.min(y), z) }
  }

  property("combinators") = kleisli && orElse && opt && and

  def succeeds[A](a: A, result: Either[Seq[Throwable], A]): Boolean = result match {
    case Right(ra) => a == ra
    case Left(_) => false
  }

  def fails(expectedErrs: Seq[Throwable], result: Either[Seq[Throwable], _]): Boolean = result match {
    case Right(_) => false
    case Left(es) => expectedErrs.to[Set] == es.to[Set]
  }

}
