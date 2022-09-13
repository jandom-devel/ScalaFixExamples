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
class OverheadReachingDefsBench {

  val length = 8

  var eqs: SimpleFiniteEquationSystem[Int, Set[Int]] = FiniteEquationSystem(
    initialBody = { (rho: Int => Set[Int]) =>
      {
        case 1 => Set(1) -- Set(4, 7)
        case 2 => Set(2) ++ (rho(1) -- Set(5))
        case 3 => Set(3) ++ (rho(2) -- Set(6))
        case 4 => Set(4) ++ (rho(3) ++ rho(7) ++ rho(6) -- Set(1, 7))
        case 5 => Set(5) ++ (rho(4) -- Set(2))
        case 6 => Set(6) ++ (rho(5) -- Set(3))
        case 7 => Set(7) ++ (rho(5) -- Set(1, 4))
      }
    },
    initialInfl =
      Relation(1 -> 2, 2 -> 3, 3 -> 4, 4 -> 5, 5 -> 6, 5 -> 7, 6 -> 4, 7 -> 4),
    inputUnknowns = Set(),
    unknowns = Range(1, 8)
  )

  @Benchmark
  def scalafix() = {
    RoundRobinSolver(eqs)(Assignment(Set()))
  }

  @Benchmark
  def hashMap() = {
    val rho = mutable.Map.empty[Int, Set[Int]].withDefaultValue(Set())
    var bodyrho = eqs.body(rho)
    var dirty = true
    var i = 1
    while dirty do
      dirty = false
      i = 1
      while i < length do
        val v = rho(i)
        val vnew = bodyrho(i)
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho
  }

  @Benchmark
  def array() = {
    val rho = Array.fill(length)(Set[Int]())
    var bodyrho = eqs.body(rho)
    var dirty = true
    var i = 1
    while dirty do
      dirty = false
      i = 1
      while i < length do
        val v = rho(i)
        val vnew = bodyrho(i)
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho
  }

}
