package it.unich.scalafixexamples

import it.unich.scalafix.*
import it.unich.scalafix.assignments.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.utils.Relation

import org.openjdk.jmh.annotations.*

import scala.collection.mutable

@State(Scope.Benchmark)
@Warmup(iterations = 3)
class OverheadIntBench:

  def chainEquation(length: Int): Body[Int, Int] =
    (rho: Int => Int) =>
      (u: Int) =>
        if u == 0
        then rho(length - 1) min limit
        else rho(u - 1) + 1

  val length = 200000
  val limit = 2000
  val body = chainEquation(length)

  def validate(rho: Assignment[Int, Int]) = {
    for i <- 0 until length do assert(rho(i) == limit + i)
  }

  @Benchmark
  def scalafixWithoutCombos() = {
    val eqs = FiniteEquationSystem(
      body,
      Set(),
      0 until length,
      Relation(Seq.empty[(Int, Int)])
    )
    val sol = RoundRobinSolver(eqs)(Assignment(0))
  }

  @Benchmark
  def scalafixWithCombos() = {
    val eqs = FiniteEquationSystem(
      body,
      Set(),
      0 until length,
      Relation(Seq.empty[(Int, Int)])
    )
    val combo = Combo({ (x: Int, y: Int) => if x > y then x else y }, true)
    val combos = ComboAssignment(combo)
    val eqs2 = eqs.withCombos(combos)
    val sol = RoundRobinSolver(eqs2)(Assignment(0))
  }

  @Benchmark
  def scalafixIntWithoutCombos() = {
    val eqs = new SimpleFiniteEquationSystem(
      body,
      Set(),
      0 until length,
      Relation(Seq.empty[(Int, Int)]),
      None
    ) {
      override def getMutableAssignment(rho: Assignment[Int, Int]) =
        ArrayBasedMutableAssignment(rho, unknowns.max + 1)
    }
    val sol = RoundRobinSolver(eqs)(Assignment(0))
  }

  def MyRoundRobinSolver(
      eqs: FiniteEquationSystem[Int, Int],
      start: Assignment[Int, Int]
  ) = {
    // this is the single line which has the biggest impact on performance
    val current = eqs.getMutableAssignment(start)
    // val current = Array.fill(length)(0)
    val eqsbody = eqs.body(current)
    var dirty = true
    while dirty do
      dirty = false
      var x = 0
      // for x <- eqs.unknowns do
      while x < length do
        val newval = eqsbody(x)
        if newval != current(x) then
          current(x) = newval
          dirty = true
        x += 1
    current
  }

  @Benchmark
  @CompilerControl(CompilerControl.Mode.INLINE)
  def myroundrobin() = {
    val eqs = new SimpleFiniteEquationSystem(
      body,
      Set(),
      0 until length,
      Relation(Seq.empty[(Int, Int)]),
      None
    ) {
      override def getMutableAssignment(rho: Assignment[Int, Int]) =
        ArrayBasedMutableAssignment(rho, unknowns.max + 1)
    }
    val sol = MyRoundRobinSolver(eqs, Assignment(0))
  }

  def hashMap(withCombos: Boolean, withInlineBody: Boolean) = {
    val rho = mutable.Map.empty[Int, Int].withDefaultValue(0)
    var bodyrho = body(rho)
    var dirty = true
    var i = 0
    while dirty do
      dirty = false
      i = 0
      while i < length do
        val v = rho(i)
        val vtmp =
          if withInlineBody
          then
            if i == 0
            then rho(length - 1) min limit
            else rho(i - 1) + 1
          else bodyrho(i)
        var vnew =
          if withCombos
          then if vtmp > v then vtmp else v
          else vtmp
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho
  }

  @Benchmark
  def hashMapWithoutCombos() = hashMap(false, true)

  @Benchmark
  def hashMapWithCombos() = hashMap(true, true)

  def array(withCombos: Boolean, withInlineBody: Boolean) = {
    val rho = Array.fill(length)(0)
    val bodyrho = body(rho)
    var dirty = true
    var i = 0
    while dirty do
      dirty = false
      i = 0
      while i < length do
        val v = rho(i)
        val vtmp =
          if withInlineBody
          then
            if i == 0
            then rho(length - 1) min limit
            else rho(i - 1) + 1
          else bodyrho(i)
        var vnew =
          if withCombos
          then if vtmp > v then vtmp else v
          else vtmp
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho
  }

  @Benchmark
  def arrayNotInlined() = array(false, false)

  @Benchmark
  def arrayInlined() = array(false, true)

end OverheadIntBench
