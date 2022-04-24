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
import it.unich.scalafix.assignments.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.lattice.Domain
import it.unich.scalafix.utils.Relation

object ReachingDefinitionsExample extends App {

  var eqs: FiniteEquationSystem[Int, Set[Int]] = FiniteEquationSystem(
    body = { (rho: Assignment[Int, Set[Int]]) =>
      {
        case 1 => Set(1) -- Set(4, 7)
        case 2 => Set(2) ++ (rho(1) -- Set(5))
        case 3 => Set(3) ++ (rho(2) -- Set())
        case 4 => Set(4) ++ (rho(3) ++ rho(7) ++ rho(6) -- Set(1, 7))
        case 5 => Set(5) ++ (rho(4) -- Set(2))
        case 6 => Set(6) ++ rho(5) -- Set(3)
        case 7 => Set(6) ++ rho(5) -- Set(1, 4)
      }
    },
    inputUnknowns = Set(),
    unknowns = Range(1, 8),
    infl = Relation(
      Map(
        1 -> Set(2),
        2 -> Set(3),
        3 -> Set(4),
        4 -> Set(5),
        5 -> Set(6, 7),
        6 -> Set(4),
        7 -> Set(4)
      )
    )
  )
  val sol = WorkListSolver(eqs)(InputAssignment(Set.empty[Int]))
  println(sol)
}

object ReachingDefinitionsExampleGraph extends App {

  implicit object SetIntDomain extends Domain[Set[Int]] {
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
  }

  var eqs: GraphEquationSystem[Int, Set[Int], String] = GraphEquationSystem(
    unknowns = Range(0, 8),
    inputUnknowns = Set(),
    edgeAction = { (rho: Assignment[Int, Set[Int]]) =>
      {
        case "01"   => Set(1) ++ (rho(0) -- Set(4, 7))
        case "12"   => Set(2) ++ (rho(1) -- Set(5))
        case "23"   => Set(3) ++ (rho(2) -- Set())
        case "3674" => Set(4) ++ (rho(3) ++ rho(6) ++ rho(7) -- Set(1, 7))
        case "45"   => Set(5) ++ (rho(4) -- Set(2))
        case "56"   => Set(6) ++ rho(5) -- Set(3)
        case "57"   => Set(6) ++ rho(5) -- Set(1, 4)
      }
    },
    source = {
      case "01"   => Set(0)
      case "12"   => Set(1)
      case "23"   => Set(2)
      case "3674" => Set(3, 6, 7)
      case "45"   => Set(4)
      case "56"   => Set(5)
      case "57"   => Set(5)
    },
    target = {
      case "01"   => 1
      case "12"   => 2
      case "23"   => 3
      case "3674" => 4
      case "45"   => 5
      case "56"   => 6
      case "57"   => 7
    },
    outgoing = {
      case 0 => Set("01")
      case 1 => Set("12")
      case 2 => Set("23")
      case 3 => Set("3674")
      case 4 => Set("45")
      case 5 => Set("56", "57")
      case 6 => Set("3674")
      case 7 => Set("3674")
    },
    ingoing = {
      case 0 => Set()
      case 1 => Set("01")
      case 2 => Set("12")
      case 3 => Set("23")
      case 4 => Set("3674")
      case 5 => Set("45")
      case 6 => Set("56")
      case 7 => Set("57")
    }
  )
  val sol = WorkListSolver(eqs)(InputAssignment(Set[Int]()))
  println(sol)
}

class JPPLExample[P <: it.unich.jppl.Property[P]](val dom: it.unich.jppl.Domain[P]) {
  def run() = {

    val c =
      Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL)
    val cs = ConstraintSystem.of(c)
    val box0 = dom.createFrom(cs) // altro esempio: (int, int), oppure Sign

    // val body = new Body[] {}
    // val equationSystem = FiniteEquationSystem()

    // x=0; [0] while [1] (x<=10) [2] x=x+1 [3];

    val simpleEqs: FiniteEquationSystem[Int, P] = FiniteEquationSystem(
      body = { (rho: Int => P) =>
        {
          case 0 => box0
          case 1 => rho(0).clone().upperBound(rho(3))
          case 2 =>
            rho(1)
              .clone()
              .refineWith(
                Constraint.of(
                  LinearExpression.of(-10, 1),
                  Constraint.ConstraintType.LESS_OR_EQUAL
                )
              )
          case 3 => rho(2).clone().affineImage(0, LinearExpression.of(1, 1))
        }
      },
      inputUnknowns = Set(), // Set(0, 1, 2, 3),
      unknowns = Set(0, 1, 2, 3),
      infl = Relation(Map(0 -> Set(1), 1 -> Set(2), 2 -> Set(3), 3 -> Set(1)))
    )

    // private val simpleEqsStrategy = HierarchicalOrdering(Left, Val(0), Left, Val(1), Val(2), Val(3), Right, Right)
    val solver =
      KleeneSolver(simpleEqs)(InputAssignment(dom.createEmpty(1)))

    // mettere il widening!!! trovare in che punto metterlo?

    // private val wideningBox: Box[Double] = { (x1: Double, x2: Double) => if (x2 > x1) Double.PositiveInfinity else x1 }
    // private val maxBox: Box[Double] = { (x: Double, y: Double) => x max y }
    // private val lastBox: Box[Double] = { (_: Double, x2: Double) => x2 }

    // private val startRho: InputAssignment[Int, Double] = simpleEqs.initial

    // private type SimpleSolver[U, V] = (FiniteEquationSystem[U, V], InputAssignment[U, V]) => IOAssignment[U, V]
    println(solver)
  }
}
object JPPLPolyhedronExample extends App {
  JPPLExample[CPolyhedron](new CPolyhedronDomain()).run();
}
object JPPLBoxExample extends App {
  val c =
    Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL)
  val cs = ConstraintSystem.of(c)
  val box0 = DoubleBox.from(cs) // altro esempio: (int, int), oppure Sign

  // val body = new Body[] {}
  // val equationSystem = FiniteEquationSystem()

  // x=0; [0] while [1] (x<=10) [2] x=x+1 [3];

  val simpleEqs: FiniteEquationSystem[Int, DoubleBox] = FiniteEquationSystem(
    body = { (rho: Int => DoubleBox) =>
      {
        case 0 => box0
        case 1 => rho(0).clone().upperBound(rho(3))
        case 2 =>
          rho(1)
            .clone()
            .refineWith(
              Constraint.of(
                LinearExpression.of(-10, 1),
                Constraint.ConstraintType.LESS_OR_EQUAL
              )
            )
        case 3 => rho(2).clone().affineImage(0, LinearExpression.of(1, 1))
      }
    },
    inputUnknowns = Set(), // Set(0, 1, 2, 3),
    unknowns = Set(0, 1, 2, 3),
    infl = Relation(Map(0 -> Set(1), 1 -> Set(2), 2 -> Set(3), 3 -> Set(1)))
  )

  // private val simpleEqsStrategy = HierarchicalOrdering(Left, Val(0), Left, Val(1), Val(2), Val(3), Right, Right)
  val solver =
    KleeneSolver(simpleEqs)(InputAssignment(DoubleBox.empty(1)))

  // mettere il widening!!! trovare in che punto metterlo?

  // private val wideningBox: Box[Double] = { (x1: Double, x2: Double) => if (x2 > x1) Double.PositiveInfinity else x1 }
  // private val maxBox: Box[Double] = { (x: Double, y: Double) => x max y }
  // private val lastBox: Box[Double] = { (_: Double, x2: Double) => x2 }

  // private val startRho: InputAssignment[Int, Double] = simpleEqs.initial

  // private type SimpleSolver[U, V] = (FiniteEquationSystem[U, V], InputAssignment[U, V]) => IOAssignment[U, V]
  println(solver)
}
