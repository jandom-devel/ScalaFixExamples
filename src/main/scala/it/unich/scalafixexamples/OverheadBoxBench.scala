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
      Combo({ (x: DoubleBox, y: DoubleBox) => y.clone().upperBound(x).widening(x) }, true)
    val combos = ComboAssignment(combo)
    val eqs2 = eqs.withCombos(combos)
    RoundRobinSolver(eqs2)(Assignment(box0))  

  def hashMap(withCombos: Boolean) =
    var rho = mutable.Map[Int, DoubleBox]().withDefaultValue(box0)
    var dirty = true
    var i = 0
    while dirty do
      dirty = false
      i = 0
      while i < length do
        val v = rho(i)
        val vtmp = body(rho)(i)
        var vnew =
          if withCombos
          then vtmp.clone().upperBound(v).widening(v)
          else vtmp
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho

  @Benchmark
  def hashMapWithoutCombos() = hashMap(false)

  @Benchmark
  def hashMapWithCombos() = hashMap(true)

  def array(withCombos: Boolean) =
    var rho = Array.fill(length)(box0)
    var dirty = true
    var i = 0
    while dirty do
      dirty = false
      i = 0
      while i < length do
        val v = rho(i)
        val vtmp = body(rho)(i)
        var vnew =
          if withCombos
          then vtmp.clone().upperBound(v).widening(v)
          else vtmp
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho

  @Benchmark
  def arrayWithoutCombos() = array(false)

  @Benchmark
  def arrayWithCombos() = array(true)
