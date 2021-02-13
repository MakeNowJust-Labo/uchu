package codes.quine.labo.uchu

import codes.quine.labo.uchu.Cardinality._

class UniverseSuite extends munit.FunSuite {
  test("Universe.apply") {
    assertEquals(Universe[Boolean].enumerate, LazyList(false, true))
  }

  test("Universe.of") {
    assertEquals(Universe.of(LazyList.from(0), BigInt(_: Int)).enumerate.take(2), LazyList(0, 1))
    assertEquals(Universe.of(LazyList.from(0), BigInt(_: Int)).cardinality, Inf)
    assertEquals(Universe.of(LazyList.from(0), BigInt(_: Int)).indexOf(0), 0: BigInt)
    assertEquals(Universe.of(LazyList(0, 1), Fin(2), BigInt(_: Int)).enumerate, LazyList(0, 1))
    assertEquals(Universe.of(LazyList(0, 1), Fin(2), BigInt(_: Int)).cardinality, Fin(2))
    assertEquals(Universe.of(LazyList(0, 1), Fin(2), BigInt(_: Int)).indexOf(0), 0: BigInt)
  }

  test("Universe.finite") {
    assertEquals(Universe.finite[Unit].enumerate, LazyList(()))
    assertEquals(Universe.finite[Unit].cardinality, Fin(1))
  }

  test("Universe.bigInt") {
    assertEquals(Universe.bigInt.enumerate.take(5), LazyList[BigInt](0, -1, 1, -2, 2))
    assertEquals(Universe.bigInt.cardinality, Inf)
  }

  test("Universe.tuple2") {
    assertEquals(
      Universe.tuple2(Universe[Boolean], Universe[BigInt]).enumerate.take(4),
      LazyList[(Boolean, BigInt)]((false, 0), (true, 0), (false, -1), (true, -1))
    )
    assertEquals(Universe.tuple2(Universe[Boolean], Universe[BigInt]).cardinality, Inf)
    assertEquals(Universe.tuple2(Universe[Boolean], Universe[Boolean]).cardinality, Fin(4))
  }

  test("Universe.list") {
    assertEquals(
      Universe.list(Universe[BigInt]).enumerate.take(4),
      LazyList[List[BigInt]](List.empty, List(0), List(-1), List(0, 0))
    )
    assertEquals(Universe.list(Universe[BigInt]).cardinality, Inf)
  }

  test("Universe.map") {
    assertEquals(
      Universe.map(Finite[Boolean], Universe[BigInt]).enumerate.take(4),
      LazyList[Map[Boolean, BigInt]](Map.empty, Map(false -> 0), Map(true -> 0), Map(false -> -1))
    )
    assertEquals(Universe.map(Finite[Boolean], Universe[BigInt]).cardinality, Inf)
    assertEquals(Universe.map(Finite[Boolean], Universe[Boolean]).cardinality, Fin(9))
  }

  test("Universe.option") {
    assertEquals(
      Universe.option(Universe[BigInt]).enumerate.take(4),
      LazyList[Option[BigInt]](None, Some(0), Some(-1), Some(1))
    )
    assertEquals(Universe.option(Universe[BigInt]).cardinality, Inf)
    assertEquals(Universe.option(Universe[Boolean]).cardinality, Fin(3))
  }

  test("Universe.either") {
    assertEquals(
      Universe.either(Universe[Boolean], Universe[BigInt]).enumerate.take(4),
      LazyList[Either[Boolean, BigInt]](Left(false), Right(0), Left(true), Right(-1))
    )
    assertEquals(Universe.either(Universe[Boolean], Universe[BigInt]).cardinality, Inf)
    assertEquals(Universe.either(Universe[Boolean], Universe[Boolean]).cardinality, Fin(4))
  }

  test("Universe.function1") {
    assertEquals(
      Universe.function1(Finite[Boolean], Universe[BigInt]).enumerate.take(4),
      LazyList[Map[Boolean, BigInt]](
        Map(false -> 0, true -> 0),
        Map(false -> -1, true -> 0),
        Map(false -> 0, true -> -1),
        Map(false -> 1, true -> 0)
      )
    )
    assertEquals(Universe.function1(Finite[Boolean], Universe[BigInt]).cardinality, Inf)
    assertEquals(Universe.function1(Finite[Boolean], Universe[Boolean]).cardinality, Fin(4))
  }

  test("Universe.partialFunction") {
    assertEquals(
      Universe.partialFunction(Finite[Boolean], Universe[BigInt]).enumerate.take(4),
      LazyList[Map[Boolean, BigInt]](Map.empty, Map(false -> 0), Map(true -> 0), Map(false -> -1))
    )
    assertEquals(Universe.partialFunction(Finite[Boolean], Universe[BigInt]).cardinality, Inf)
    assertEquals(Universe.partialFunction(Finite[Boolean], Universe[Boolean]).cardinality, Fin(9))
  }
}
