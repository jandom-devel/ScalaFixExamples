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
import it.unich.scalafix.infinite

object FibonacciExample extends App:

  infiniteFibo

  def infiniteFibo: Unit =
    val body: Body[Int, BigInt] =
      (rho: Assignment[Int, BigInt]) =>
        (u: Int) => if u <= 1 then 1 else rho(u - 1) + rho(u - 2)

    val eqs = EquationSystem(body)
    val sol = infinite.WorkListSolver(eqs)(Assignment(1), Set(6))
    println(sol)
