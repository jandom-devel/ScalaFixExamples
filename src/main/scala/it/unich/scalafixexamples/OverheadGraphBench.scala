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
import it.unich.scalafix.graphs.*
import org.openjdk.jmh.annotations.*
import it.unich.scalafix.graphs.SimpleGraphEquationSystem

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(value = 1)
class OverheadGraphBench:

    val length = 2000
    val limit = 20000
    

    def chainEquation(length: Int): Body[Int, Int] =
        (rho: Int => Int) =>
        (u: Int) =>
            u match {
                case 0 => rho(length - 1) min limit
                case 1 => rho(0) + 1
                case _ => (rho(u-1) + 1) max (rho(u-2) +1)
            }


    val body = chainEquation(length)
    val x = """
    def chainEquationGraph(length: Int) = GraphBody[Int, Int, (Int, Int)] (
        sources = (e: (Int, Int)) => Set(e._1),
        target = (e: (Int, Int)) => e._2,
        outgoing = (n: Int) => 
            if n == 0
            then Set((0, 1), (0, 2))
            else if n < limit-2 then Set((n, n+1), (n, n+2))
            case 0 => Set((0, 1), (0, 2))
            case 1 => Set((1, 2), (1, 3))
            case _ => if (n < 
        }
    )
    """

