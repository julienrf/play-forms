package julienrf.forms.codecs

import org.scalacheck.Prop._
import org.scalacheck.Properties

object CodecTest extends Properties("Codec") {

  object laws {
    def encodeDecode[A, B](codec: Codec[A, B], b: B): Boolean =
      succeeds(b, codec.decode(codec.encode(b)))
  }

  val text = forAll { (s: String) =>
    s.nonEmpty ==> {
      val codec = Codec.text
      laws.encodeDecode(codec, s) &&
      fails(Seq(Error.Required), codec.decode(None)) &&
      fails(Seq(Error.Required), codec.decode(Some(Nil))) &&
      fails(Seq(Error.Required), codec.decode(Some(Seq("")))) // empty text is not valid
    }
  }
  val int = forAll { (n: Int) =>
    val codec = Codec.int
    laws.encodeDecode(codec, n) &&
    codec.decode(Some(Nil)) == Left(Seq(Error.Required))
  }
  val boolean = forAll { (b: Boolean) =>
    laws.encodeDecode(Codec.boolean, b)
  }
  val greaterOrEqual = forAll { (n: Int, m: Int) =>
    val constraint = Constraint.greaterOrEqual(m)
    val result = constraint.decode(n)
    if (n >= m) succeeds(n, result) else result == Left(Seq(Error.MustBeAtLeast(m)))
  }
  val oneOf = forAll { (map: Map[Int, String], s: String) =>
    (map.values.forall(_.nonEmpty) && s.nonEmpty && !map.values.exists(_ == s)) ==> {
      val codec = Codec.oneOf(map)
      map.keys.forall(n => laws.encodeDecode(codec, n)) &&
      fails(Seq(Error.Undefined), codec.decode(Some(Seq(s))))
    }
  }
  val severalOf = forAll { (map: Map[Int, String], s: String) =>
    (map.values.forall(_.nonEmpty) && s.nonEmpty && !map.values.exists(_ == s)) ==> {
      val codec = Codec.severalOf(map)
      val nss = for {
        i <- 0 until map.keys.size
        j <- 0 until i
      } yield map.keys.slice(j, i)
      nss.forall(ns => laws.encodeDecode(codec, ns.to[Seq])) &&
      fails(Seq(Error.Undefined), codec.decode(Some(Seq(s)))) &&
      map.values.forall(s2 => fails(Seq(Error.Undefined), codec.decode(Some(Seq(s, s2))))) // One wrong key makes the whole thing fail
    }
  }

  property("codecs") = text && int && boolean && greaterOrEqual && oneOf && severalOf

  property("constraints") = {
    def laws[A](constraint: Constraint[A], a: A): Boolean = {
      val decode = constraint.decode(a) == (constraint.validate(a) match {
        case Some(es) => Left(es)
        case None => Right(a)
      })
      val encode = constraint.encode(a) == a
      decode && encode
    }
    forAll { (n: Int, m: Int) => laws(Constraint.greaterOrEqual(n), m) }
  }

  val kleisli = {
    object laws {
      def decode[A, B](codec1: Codec[A, B], codec2: Codec[B, _], a: A): Boolean =
        (codec1 >=> codec2).decode(a) == codec1.decode(a).right.flatMap(codec2.decode)
      def encode[B, C](codec1: Codec[_, B], codec2: Codec[B, C], c: C): Boolean =
        (codec1 >=> codec2).encode(c) == codec1.encode(codec2.encode(c))
    }
    forAll { (n: Int, m: Int, b: Boolean) =>
      laws.decode(Codec.int, Constraint.greaterOrEqual(m), if (b) Some(Seq(n.toString)) else None) &&
      laws.encode(Codec.int, Constraint.greaterOrEqual(m), n)
    }
  }
  val orElse = {
    object laws {
      def decode[A](codec1: Codec[A, _], codec2: Codec[A, _], a: A): Boolean =
        (codec1 || codec2).decode(a) == (codec1.decode(a) match {
          case Right(b) => Right(Left(b))
          case Left(_) => codec2.decode(a).right.map(c => Right(c))
        })
      def encode[A, B, C](codec1: Codec[A, B], codec2: Codec[A, C], bOrC: Either[B, C]): Boolean =
        (codec1 || codec2).encode(bOrC) == (bOrC match {
          case Left(b) => codec1.encode(b)
          case Right(c) => codec2.encode(c)
        })
    }
    forAll { (s: Option[Seq[String]]) => laws.decode(Codec.boolean, Codec.int, s) } && // FIXME Should use a more specific generator
    forAll { (bOrN: Either[Boolean, Int]) => laws.encode(Codec.boolean, Codec.int, bOrN) }
  }
  val opt = {
    object laws {
      def decode[A](codec: Codec[Option[A], _], a: Option[A]): Boolean =
        succeeds(codec.decode(a).right.toOption, codec.?.decode(a))
      def encode[A, B](codec: Codec[Option[A], B], maybeB: Option[B]): Boolean =
        codec.?.encode(maybeB) == (maybeB flatMap codec.encode)
    }
    forAll { (maybeN: Option[Int]) =>
      laws.decode(Codec.int, maybeN.map(n => Seq(n.toString))) &&
      laws.encode(Codec.int, maybeN)
    }
  }
  val and = {
    def laws[A](constraint1: Constraint[A], constraint2: Constraint[A], a: A): Boolean = {
      val constraint = constraint1 && constraint2
      val decode = constraint.decode(a) == ((constraint1.decode(a), constraint2.decode(a)) match {
        case (Right(`a`), Right(`a`)) => Right(a)
        case (Right(`a`), Left(es)) => Left(es)
        case (Left(es), Right(`a`)) => Left(es)
        case (Left(es1), Left(es2)) => Left(es1 ++ es2) // Errors are accumulated
      })
      val encode = constraint.encode(a) == constraint2.encode(a)
      decode && encode
    }
    forAll { (x: Int, y: Int, z: Int) => laws(Constraint.greaterOrEqual(x), Constraint.greaterOrEqual(y), z) }
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
