package it.unich.scalafixexamples

import it.unich.jppl.*
import it.unich.scalafix.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.utils.Relation
import org.openjdk.jmh.annotations.*

import scala.collection.mutable

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(value = 1)
class OverheadBoxBench:

  val length = 200
  val limit = 2000
  val body = chainEquation(length)

  val box0 = DoubleBox.from(
    ConstraintSystem.of(
      Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL)
    )
  )
  val incrExpr = LinearExpression.of(1, 1)
  val refineConstr = Constraint.of(
    LinearExpression.of(-limit, 1),
    Constraint.ConstraintType.LESS_OR_EQUAL
  )

  def chainEquation(length: Int): Body[Int, DoubleBox] =
    (rho: Int => DoubleBox) =>
      (u: Int) =>
        if u == 0
        then rho(length - 1).clone().refineWith(refineConstr)
        else rho(u - 1).clone().affineImage(0, incrExpr).upperBound(rho(u - 1))

  def validate(rho: Int => DoubleBox) =
    for i <- 0 until length do
      assert(
        rho(i).equals(
          DoubleBox.from(
            ConstraintSystem.of(
              Constraint.of(
                LinearExpression.of(0, 1),
                Constraint.ConstraintType.GREATER_OR_EQUAL
              ),
              Constraint.of(
                LinearExpression.of(-limit - i, 1),
                Constraint.ConstraintType.LESS_OR_EQUAL
              )
            )
          )
        )
      )

  @Benchmark
  def scalafixWithoutCombos() =
    val eqs = FiniteEquationSystem[Int, DoubleBox](
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    )
    RoundRobinSolver(eqs)(Assignment(box0))

  @Benchmark
  def scalafixWithCombos() =
    val eqs = FiniteEquationSystem(
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    )
    val combo =
      Combo({ (x: DoubleBox, y: DoubleBox) => y.clone().upperBound(x) }, true)
    val combos = ComboAssignment(combo)
    val eqs2 = eqs.withCombos(combos)
    RoundRobinSolver(eqs2)(Assignment(box0))  

  def hashMap(withCombos: Boolean, withInlineBody: Boolean) =
    var rho = mutable.Map[Int, DoubleBox]().withDefaultValue(box0)
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
            then rho(length - 1).clone().refineWith(refineConstr)
            else rho(i - 1).clone().affineImage(0, incrExpr)
          else body(rho)(i)
        var vnew =
          if withCombos
          then vtmp.clone().upperBound(v)
          else vtmp
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho

  @Benchmark
  def hashMapNotInlined() = hashMap(false, false)

  @Benchmark
  def hashMapInlined() = hashMap(false, true)

  def array(withCombos: Boolean, withInlineBody: Boolean) =
    var rho = Array.fill(length)(box0)
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
            then rho(length - 1).clone().refineWith(refineConstr)
            else
              rho(i - 1).clone().affineImage(0, incrExpr).upperBound(rho(i - 1))
          else body(rho)(i)
        var vnew =
          if withCombos
          then vtmp.clone().upperBound(v)
          else vtmp
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho

  @Benchmark
  def arrayNotInlined() = array(false, false)

  @Benchmark
  def arrayInlined() = array(false, true)
