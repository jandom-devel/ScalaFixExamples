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
import it.unich.scalafix.infinite.*
import it.unich.scalafix.utils.Domain

private type SimpleSolver[U, V] =
  (
      SimpleEquationSystem[U, V],
      Assignment[U, V],
      Seq[U]
  ) => MutableAssignment[U, V]

class InfiniteESExample(solver: SimpleSolver[Int, Int]) {

  def buildEquationSystem(): SimpleEquationSystem[Int, Int] = {

    /** `simpleEqs` is the infinite equation system implicitly defined for any
      * natural number n as follows:
      *
      * x_{2n}   = max ( x_{x_2n}} , n )
      * x_{2n+1} = x_{6n+4}
      *
      * whose corresponding equation system is:
      *
      * x_0 = max ( x_{x_0}, 0)
      * x_1 = y_4
      * x_2 = max ( x_{x_2}, 1)
      * x_3 = y_10
      * x_4 = max ( x_{x_4}, 2)
      * ....
      *
      * where the variables x_0, x_1, x_2, ... are the unknowns 0,1,2,.. of the
      * equation system.
      * 
      * This example comes from: Gianluca Amato, Francesca Scozzari, Helmut
      * Seidl, Kalmer Apinis, Vesal Vojdani. Efficiently intertwining widening
      * and narrowing. Science of Computer Programming, Volume 120, 2016
      */
    val simpleEqs = SimpleEquationSystem[Int, Int](
      (rho: Int => Int) => (x: Int) =>
        if x % 2 == 0
        then rho(rho(x)) max x / 2
        else
          val n = (x - 1) / 2
          rho(6 * n + 4)
    )

    val maxCombo = ComboAssignment( (x: Int, y: Int) => x max y )
    simpleEqs.withCombos(maxCombo)
  }

  def run() = {
    val simpleEqs = buildEquationSystem()
    val startRho = Assignment(0)
    val simpleSolver = solver(simpleEqs, startRho, Seq(4))
    println(simpleSolver)
  }

}

object InfiniteESExample extends App {
  InfiniteESExample(WorkListSolver(_)(_, _)).run();
}

object InfiniteESWithPriorityExample extends App {
  InfiniteESExample(PriorityWorkListSolver(_)(_, _)).run();
}
