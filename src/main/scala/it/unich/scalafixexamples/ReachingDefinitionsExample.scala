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
import it.unich.scalafix.graphs.*
import it.unich.scalafix.lattice.Domain
import it.unich.scalafix.utils.Relation

object ReachingDefinitionsExample extends App:

  /** We consider the following program with 7 definitions:
    *
    * d1 --> i = m-1;
    * d2 --> j = n;
    * d3 --> a = u1;
    *        do
    * d4 -->   i = i+1;
    * d5 -->   j = j-1;
    *          if (e1) then
    * d6 -->     a = u2;
    *          else
    * d7 -->     i = u3
    *        while (e2)
    *
    * The example comes from: Alfred V. Aho, Ravi Sethi, Jeffrey D. Ullman.
    * Compilers. Principles, Techniques, and Tools Addison-Wesley Publishing
    * Company 1986
    */

  var eqs = finite.FiniteEquationSystem(
    initialBody = (rho: Assignment[Int, Set[Int]]) => {
      case 1 => Set(1) -- Set(4, 7)
      case 2 => Set(2) ++ (rho(1) -- Set(5))
      case 3 => Set(3) ++ (rho(2) -- Set())
      case 4 => Set(4) ++ (rho(3) ++ rho(7) ++ rho(6) -- Set(1, 7))
      case 5 => Set(5) ++ (rho(4) -- Set(2))
      case 6 => Set(6) ++ rho(5) -- Set(3)
      case 7 => Set(6) ++ rho(5) -- Set(1, 4)
    },
    initialInfl = Relation(
      Map(
        1 -> Set(2),
        2 -> Set(3),
        3 -> Set(4),
        4 -> Set(5),
        5 -> Set(6, 7),
        6 -> Set(4),
        7 -> Set(4)
      )
    ),
    inputUnknowns = Set(1),
    unknowns = 1 to 7
  )
  val sol = finite.WorkListSolver(eqs)(Assignment(Set.empty[Int]))
  println(sol)

object ReachingDefinitionsExampleGraph extends App:

  given Domain[Set[Int]] with
    extension (x: Set[Int]) def upperBound(y: Set[Int]) = x ++ y
    def tryCompare(x: Set[Int], y: Set[Int]): Option[Int] =
      if (x subsetOf y)
        if (x == y)
          Some(0)
        else
          Some(-1)
      else if (y subsetOf x)
        Some(1)
      else None
    def lteq(x: Set[Int], y: Set[Int]): Boolean = x subsetOf y

  val graph = Graph[Int, Set[Int], String](
    edgeAction = (rho: Assignment[Int, Set[Int]]) =>
      case "01"   => Set(1) ++ (rho(0) -- Set(4, 7))
      case "12"   => Set(2) ++ (rho(1) -- Set(5))
      case "23"   => Set(3) ++ (rho(2) -- Set())
      case "3674" => Set(4) ++ (rho(3) ++ rho(6) ++ rho(7) -- Set(1, 7))
      case "45"   => Set(5) ++ (rho(4) -- Set(2))
      case "56"   => Set(6) ++ rho(5) -- Set(3)
      case "57"   => Set(6) ++ rho(5) -- Set(1, 4),
    sources =
      case "01"   => Set(0)
      case "12"   => Set(1)
      case "23"   => Set(2)
      case "3674" => Set(3, 6, 7)
      case "45"   => Set(4)
      case "56"   => Set(5)
      case "57"   => Set(5)
    ,
    target =
      case "01"   => 1
      case "12"   => 2
      case "23"   => 3
      case "3674" => 4
      case "45"   => 5
      case "56"   => 6
      case "57"   => 7
    ,
    outgoing =
      case 0 => Set("01")
      case 1 => Set("12")
      case 2 => Set("23")
      case 3 => Set("3674")
      case 4 => Set("45")
      case 5 => Set("56", "57")
      case 6 => Set("3674")
      case 7 => Set("3674")
    ,
    ingoing =
      case 0 => Set()
      case 1 => Set("01")
      case 2 => Set("12")
      case 3 => Set("23")
      case 4 => Set("3674")
      case 5 => Set("45")
      case 6 => Set("56")
      case 7 => Set("57")
  )

  var eqs = GraphEquationSystem(
      initialGraph = graph,
      unknowns = 0 to 7,
      inputUnknowns = Set(0)
    )

  val sol = WorkListSolver(eqs)(Assignment(Set[Int]()))
  println(sol)
