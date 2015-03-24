package julienrf.forms.codecs

import julienrf.forms.FieldData
import org.scalacheck.{Prop, Properties}
import org.scalacheck.Prop._

object CodecTest extends Properties("Codec") {

  def encodeDecode[A, B](codec: Codec[A, B], b: B): Boolean =
    codec.encode(b).exists(a => succeeds(b, codec.decode(a)))

  val text = forAll { (s: String) =>
    s.nonEmpty ==> {
      val codec = Codec.text
      encodeDecode(codec, s) &&
      fails(Seq(Error.Required), codec.decode(Nil)) &&
      fails(Seq(Error.Required), codec.decode(Seq(""))) // empty text is not valid
    }
  }
  val int = forAll { (n: Int) =>
    val codec = Codec.int
    encodeDecode(codec, n) &&
    codec.decode(Nil) == Left(Seq(Error.Required))
  }
  val min = forAll { (n: Int, m: Int) =>
    val constraint = Codec.min(m)
    val result = constraint.decode(n)
    if (n >= m) succeeds(n, result) else result == Left(Seq(Error.MustBeAtLeast(m)))
  }
  val oneOf = forAll { (map: Map[Int, String], s: String) =>
    (map.values.forall(_.nonEmpty) && s.nonEmpty && map.values.find(_ == s).isEmpty) ==> {
      val codec = Codec.oneOf(map)
      map.keys.forall(n => encodeDecode(codec, n)) &&
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
      nss.forall(ns => encodeDecode(codec, ns.to[Seq])) &&
      fails(Seq(Error.Undefined), codec.decode(Seq(s))) &&
      map.values.forall(s2 => fails(Seq(Error.Undefined), codec.decode(Seq(s, s2)))) // One wrong key makes the whole thing fail
    }
  }

  property("codecs") = text && int && min && oneOf && severalOf

  val kleisli = undecided
  val or = undecided
  val opt = {
    // FIXME I’d like to write forAll { (rule: Rule[A, B], a: A, b: B) => ... }
    val rule = Codec.min(42)
    rule.decode(0).isLeft && succeeds(None, rule.?.decode(0)) &&
    succeeds(42, rule.decode(42)) && succeeds(Some(42), rule.?.decode(42))
  }

  property("composition") = kleisli && or && opt

  def succeeds[A](a: A, result: Either[Seq[Throwable], A]): Boolean = result match {
    case Right(ra) => a == ra
    case Left(_) => false
  }

  def fails(expectedErrs: Seq[Throwable], result: Either[Seq[Throwable], _]): Boolean = result match {
    case Right(_) => false
    case Left(ts) => expectedErrs.to[Set] == ts.to[Set]
  }

}
