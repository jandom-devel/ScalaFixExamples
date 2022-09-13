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

import it.unich.scalafix.*
import it.unich.scalafix.assignments.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.utils.Relation
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import scala.collection.mutable

@State(Scope.Benchmark)
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

  def validate(rho: Assignment[Int, Int]) =
    for i <- 0 until length do assert(rho(i) == limit + i)

  @Benchmark
  def scalafixWithoutCombos() =
    val eqs = FiniteEquationSystem(
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    )
    val sol = RoundRobinSolver(eqs)(Assignment(0))

  @Benchmark
  def scalafixWithCombos() =
    val eqs = FiniteEquationSystem(
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    )
    val combo = Combo({ (x: Int, y: Int) => if x > y then x else y }, true)
    val combos = ComboAssignment(combo)
    val eqs2 = eqs.withCombos(combos)
    val sol = RoundRobinSolver(eqs2)(Assignment(0))

  // Version using Scalafix with a custom mutable assignment based on arrays.
  @Benchmark
  def scalafixIntWithoutCombos() =
    val eqs = new SimpleFiniteEquationSystem(
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    ):
      override def getMutableAssignment(rho: Assignment[Int, Int]) =
        ArrayBasedMutableAssignment(rho, unknowns.max + 1)

    val sol = RoundRobinSolver(eqs)(Assignment(0))

  def MyRoundRobinSolver(
      eqs: SimpleFiniteEquationSystem[Int, Int],
      start: Assignment[Int, Int]
  ) =
    // this is the single line which has the biggest impact on performance
    // val current = eqs.getMutableAssignment(start)
    val current = Array.fill(length)(0)
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

  // A custom round robin solver using the ScalaFix equation system array-based assignments.
  @Benchmark
  def myroundrobin() = {
    val eqs = new SimpleFiniteEquationSystem(
      initialBody = body,
      initialInfl = Relation(),
      unknowns = 0 until length,
      inputUnknowns = Set()
    ):
      override def getMutableAssignment(rho: Assignment[Int, Int]) =
        ArrayBasedMutableAssignment(rho, unknowns.max + 1)

    val sol = MyRoundRobinSolver(eqs, Assignment(0))
  }

  def hashMap(withCombos: Boolean, withInlineBody: Boolean) =
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

  @Benchmark
  def hashMapNotInlinedWithoutCombos() = hashMap(false, false)

  @Benchmark
  def hashMapNotInlinedWithCombos() = hashMap(true, false)

  @Benchmark
  def hashMapInlinedWithoutCombos() = hashMap(false, true)

  @Benchmark
  def hashMapInlinedWithCombos() = hashMap(true, true)

  def array(withCombos: Boolean, withInlineBody: Boolean) =
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

  @Benchmark
  def arrayNotInlinedWithoutCombos() = array(false, false)

  @Benchmark
  def arrayNotInlinedWithCombos() = array(true, false)

  @Benchmark
  def arrayInlinedWithoutCombos() = array(false, true)

  @Benchmark
  def arrayInlinedWithCombos() = array(false, true)
