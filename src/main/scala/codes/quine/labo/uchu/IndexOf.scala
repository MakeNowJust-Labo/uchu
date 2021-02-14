package codes.quine.labo.uchu

import codes.quine.labo.uchu.Card._

/** IndexOf is an indexer. */
trait IndexOf[A] extends (A => N) {
  def apply(x: A): N
}

/** IndexOf is utilities for indexing a value.
  *
  * The methods in this are used for implementing [[Universe.indexOf]].
  */
object IndexOf {

  /** Builds an indexer from a function. */
  def apply[A](f: A => N): IndexOf[A] = new IndexOf[A] {
    def apply(x: A): N = f(x)
  }

  /** Delays building an indexer. */
  def delay[A](i: => IndexOf[A]): IndexOf[A] = IndexOf(x => i(x))

  /** Indexes a [[Nothing]] value (an empty function). */
  def nothing: IndexOf[Nothing] = IndexOf[Nothing](_ => throw new IllegalArgumentException)

  /** Indexes an [[Unit]] value. */
  def unit: IndexOf[Unit] = IndexOf(_ => N.Zero)

  /** Indexes a [[Boolean]] value. */
  val boolean: IndexOf[Boolean] = IndexOf {
    case false => N.Zero
    case true  => N.One
  }

  /** Indexes a [[BigInt]] value. */
  val bigInt: IndexOf[BigInt] =
    IndexOf(x => if (x < 0) N((-x * 2) - 1) else N(x * 2))

  /** Indexes a [[Byte]] value. */
  val byte: IndexOf[Byte] = IndexOf(x => bigInt(BigInt(x)))

  /** Indexes a [[Short]] value. */
  val short: IndexOf[Short] = IndexOf(x => bigInt(BigInt(x)))

  /** Indexes an [[Int]] value. */
  val int: IndexOf[Int] = IndexOf(x => bigInt(BigInt(x)))

  /** Indexes a [[Long]] value. */
  val long: IndexOf[Long] = IndexOf(x => bigInt(BigInt(x)))

  /** Indexes a pair of values. */
  def tuple2[A, B](ix: IndexOf[A], cx: Card, iy: IndexOf[B], cy: Card): IndexOf[(A, B)] = {
    def finFin(nx: N, ny: N): IndexOf[(A, B)] = {
      val (min, max, landscape) = if (nx < ny) (nx, ny, false) else (ny, nx, true)
      val upper = min * (min + 1) / 2
      val diagonals = min * (max - min)
      IndexOf { case (x, y) =>
        val (kx, ky) = (ix(x), iy(y))
        val d = kx + ky
        if (d < min) d * (d + 1) / 2 + ky
        else if (d < max) upper + (d - min) * min + (if (landscape) ky else nx - kx - 1)
        else {
          val j = d - max
          upper + diagonals + ((min - j) + (min - 1)) * j / 2 + (nx - kx - 1)
        }
      }
    }

    def finInf(n: N, landscape: Boolean): IndexOf[(A, B)] = {
      val upper = n * (n + 1) / 2
      IndexOf { case (x, y) =>
        val (kx, ky) = (ix(x), iy(y))
        val d = kx + ky
        if (d < n) d * (d + 1) / 2 + (d - kx)
        else upper + (d - n) * n + (if (landscape) ky else n - kx - 1)
      }
    }

    def infInf: IndexOf[(A, B)] =
      IndexOf { case (x, y) =>
        val (kx, ky) = (ix(x), iy(y))
        val d = kx + ky
        d * (d + 1) / 2 + (d - kx)
      }

    (cx, cy) match {
      case (Small(nx), Small(ny)) => finFin(nx, ny)
      case (Small(n), _)          => finInf(n, false)
      case (_, Small(n))          => finInf(n, true)
      case (_, _)                 => infInf
    }
  }

  /** Indexes a list. */
  def list[A](i: IndexOf[A], c: Card): IndexOf[List[A]] =
    new IndexOf[List[A]] { iList =>
      private[this] val iCons = tuple2(i, c, iList, Inf)
      def apply(xs: List[A]): N = xs match {
        case Nil     => N.Zero
        case x :: xs => iCons((x, xs)) + 1
      }
    }

  /** Indexes a set. */
  def set[A](i: IndexOf[A]): IndexOf[Set[A]] =
    IndexOf(_.map(i).foldLeft(N(0)) { case (acc, k) => acc | (N.One << k) })

  /** Indexes a map. */
  def map[A, B](ix: IndexOf[A], cx: Fin, iy: IndexOf[B], cy: Card): IndexOf[Map[A, B]] = {
    val cListLeN = Card.sumOfGeometric(One, cy + 1, cx - 1)
    val iCons = tuple2(listLeN(option(iy), cy + 1, cx - 1), cListLeN, iy, cy)
    IndexOf { map =>
      if (map.isEmpty) N.Zero
      else {
        val imap = map.map { case (k, v) => (ix(k), v) }
        val (max, maxX) = imap.maxBy(_._1)
        val list = List.unfold(N.Zero)(i => if (i >= max) None else Some((imap.get(i), i + 1)))
        iCons((list, maxX)) + 1
      }
    }
  }

  /** Indexes a function. */
  def function1[A, B](xs: LazyList[A], cx: Fin, iy: IndexOf[B], cy: Card): IndexOf[A => B] = {
    val iListN = listN(iy, cy, cx)
    IndexOf { f => iListN(xs.map(f).toList) }
  }

  /** Indexes a partial function. */
  def partialFunction[A, B](
      xs: LazyList[A],
      ix: IndexOf[A],
      cx: Fin,
      iy: IndexOf[B],
      cy: Card
  ): IndexOf[PartialFunction[A, B]] = {
    val iMap = map(ix, cx, iy, cy)
    IndexOf { pf =>
      val map = xs.flatMap(x => pf.lift(x).map((x, _))).toMap
      iMap(map)
    }
  }

  /** Indexes an optional value. */
  def option[A](i: IndexOf[A]): IndexOf[Option[A]] = IndexOf {
    case None    => N.Zero
    case Some(x) => i(x) + 1
  }

  /** Indexes an either value. */
  def either[A, B](ix: IndexOf[A], cx: Card, iy: IndexOf[B], cy: Card): IndexOf[Either[A, B]] =
    IndexOf {
      case Left(x) =>
        cy match {
          case Small(ny) =>
            val kx = ix(x)
            if (kx >= ny) ny * 2 + (kx - ny) else kx * 2
          case _ => ix(x) * 2
        }
      case Right(y) =>
        cx match {
          case Small(nx) =>
            val ky = iy(y)
            if (ky >= nx) nx * 2 + (ky - nx) else ky * 2 + 1
          case _ => iy(y) * 2 + 1
        }
    }

  /** Indexes a list which sizes up to the given parameter. */
  private def listLeN[A](i: IndexOf[A], c: Card, size: Fin): IndexOf[List[A]] =
    if (size.isZero) IndexOf(_ => N.Zero)
    else {
      val cListLeN = Card.sumOfGeometric(One, c, size)
      val iCons = tuple2(i, c, delay(listLeN(i, c, size - 1)), cListLeN)
      IndexOf {
        case Nil     => N.Zero
        case x :: xs => iCons((x, xs)) + 1
      }
    }

  /** Indexes a list which sizes to the given parameter. */
  private def listN[A](i: IndexOf[A], c: Card, size: Fin): IndexOf[List[A]] =
    if (size.isZero) IndexOf(_ => N.Zero)
    else {
      val cListN = c ** (size - 1)
      val iCons = tuple2(i, c, IndexOf[List[A]](xs => listN(i, c, size - 1)(xs)), cListN)
      IndexOf {
        case Nil     => throw new IllegalArgumentException // unreachable
        case x :: xs => iCons((x, xs))
      }
    }
}
