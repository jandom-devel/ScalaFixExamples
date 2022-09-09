/** Copyright 2022 Francesca Scozzari <francesca.scozzari@unich.it> and Gianluca
  * Amato <gianluca.amato@unich.it>
  *
  * This file is part of ScalaFixExamples, a set of examples for the ScalaFix
  * library. ScalaFixExamples is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * ScalaFixExamples is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of a MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * ScalaFix. If not, see <http://www.gnu.org/licenses/>.
  */

package it.unich.scalafixexamples

import it.unich.jppl.*
import it.unich.scalafix.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.utils.Relation

import org.openjdk.jmh.annotations.*
import scala.collection.mutable
import scala.reflect.ClassTag

class OverheadPPLBenchSolvers[P <: Property[P] : ClassTag](dom: Domain[P]):

  val length = 100
  val limit = 2000
  val body = chainEquation(length)

  val poly0 = dom.createFrom(
    ConstraintSystem.of(
      Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL)
    )
  )
  val incrExpr = LinearExpression.of(1, 1)
  val refineConstr = Constraint.of(
    LinearExpression.of(-limit, 1),
    Constraint.ConstraintType.LESS_OR_EQUAL
  )

  def chainEquation(length: Int): Body[Int, P] =
    (rho: Int => P) =>
      (u: Int) =>
        if u == 0
        then rho(length - 1).clone().refineWith(refineConstr).upperBound(poly0)
        else rho(u - 1).clone().affineImage(0, incrExpr)

  def validate(rho: Int => P) =
    for i <- 0 until length do
      assert(
        rho(i).equals(
          dom.createFrom(
            ConstraintSystem.of(
              Constraint.of(
                LinearExpression.of(-i, 1),
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

  def validateWithCombos(rho: Int => P) =
    for i <- 0 until length do
      assert(
        rho(i).equals(
          dom.createFrom(
            ConstraintSystem.of(
              Constraint.of(
                LinearExpression.of(-i, 1),
                Constraint.ConstraintType.GREATER_OR_EQUAL
              )
            )
          )
        )
      )

  val eqs = FiniteEquationSystem[Int, P](
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    )
  val combos = ComboAssignment(
      Combo( (x: P, y: P) => y.clone().upperBound(x).widening(x) , isIdempotent = true)
  )
  val eqs2 = eqs.withCombos(combos)

  def scalafix(withCombos: Boolean) =
    val realEqs = if withCombos then eqs2 else eqs
    RoundRobinSolver(realEqs)(Assignment(dom.createEmpty(1)))

  def hashMap(withCombos: Boolean) =
    var rho = mutable.Map[Int, P]().withDefaultValue(dom.createEmpty(1))
    var dirty = true
    var i = 0
    while dirty do
      dirty = false
      i = 0
      while i < length do
        val v = rho(i)
        val vtmp = body(rho)(i)
        val vnew =
          if withCombos
          then vtmp.clone().upperBound(v).widening(v)
          else vtmp
        if vnew != v then
          rho(i) = vnew
          dirty = true
        i += 1
    rho

  def array(withCombos: Boolean) =
    var rho = Array.fill(length)(dom.createEmpty(1))
    var dirty = true
    var i = 0
    while dirty do
      dirty = false
      i = 0
      while i < length do
        val v = rho(i)
        val vtmp = body(rho)(i)
        val vnew =
          if withCombos
          then vtmp.clone().upperBound(v).widening(v)
          else vtmp
        if vnew != v then
          rho(i) = vnew
          dirty = true
        i += 1
    rho

  // validating
  validate(scalafix(false))
  validateWithCombos(scalafix(true))
  validate(hashMap(false))
  validateWithCombos(hashMap(true))
  validate(array(false))
  validateWithCombos(array(true))

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(value = 1)
class OverheadPPLBench:

  val polySolvers = OverheadPPLBenchSolvers(CPolyhedronDomain())
  val boxSolvers = OverheadPPLBenchSolvers(DoubleBoxDomain())

  @Benchmark
  def scalafixPolyWithoutCombos() = polySolvers.scalafix(false)

  @Benchmark
  def scalafixPolyWithCombos() = polySolvers.scalafix(true)

  @Benchmark
  def hashMapPolWithoutCombos() = polySolvers.hashMap(false)

  @Benchmark
  def hashMapPolWithCombos() = polySolvers.hashMap(true)

  @Benchmark
  def arrayPolWithoutCombos() = polySolvers.array(false)

  @Benchmark
  def arrayPolWithCombos() = polySolvers.array(true)

  @Benchmark
  def scalafixBoxWithoutCombos() = boxSolvers.scalafix(false)

  @Benchmark
  def scalafixBoxWithCombos() = boxSolvers.scalafix(true)

  @Benchmark
  def hashMapBoxWithoutCombos() = boxSolvers.hashMap(false)

  @Benchmark
  def hashMapBoxWithCombos() = boxSolvers.hashMap(true)

  @Benchmark
  def arrayBoxWithoutCombos() = boxSolvers.array(false)

  @Benchmark
  def arrayBoxWithCombos() = boxSolvers.array(true)
