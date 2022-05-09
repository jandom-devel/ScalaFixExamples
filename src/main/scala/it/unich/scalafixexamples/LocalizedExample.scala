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

import it.unich.jppl
import it.unich.jppl.*
import it.unich.scalafix.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.graphs.*
//import it.unich.scalafix.lattice.Domain
import it.unich.scalafix.utils.Relation

object LocalizedEquationSystems:

  // the constraint system {i=0}
  val csieq0 = ConstraintSystem.of(
    Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL)
  )

  // the constraint system {j=0}
  val csjeq0 = ConstraintSystem.of(
    Constraint.of(LinearExpression.of(0, 2), Constraint.ConstraintType.EQUAL)
  )

  // the constraint i<10
  val ciless10 = Constraint.of(
    LinearExpression.of(-10, 1),
    Constraint.ConstraintType.LESS_THAN
  )

  // the constraint i>=10
  val cigeq10 = Constraint.of(
    LinearExpression.of(-10, 1),
    Constraint.ConstraintType.GREATER_OR_EQUAL
  )

  // the constraint j<10
  val cjless10 = Constraint.of(
    LinearExpression.of(-10, 2),
    Constraint.ConstraintType.LESS_THAN
  )

  // the constraint j>=10
  val cjgeq10 = Constraint.of(
    LinearExpression.of(-10, 2),
    Constraint.ConstraintType.GREATER_OR_EQUAL
  )

  def buildFiniteEQS[P <: Property[P]](dom: jppl.Domain[P]) =

    /** initialCs is a constraint system with the constraint: i=0 where i is the
      * unknown of index 0.
      */
    val initialCs = dom.createFrom(csieq0)

    /** This is the equation system corresponding to the program:
      * ```
      *     i = 0
      * [0] while [1] (i<10){
      * [2]   j = 0
      * [4]   while [5] (j<10) 
      * [6]     j = j+1 [8]
      * [7]   i = i+1   [9]
      *     } [3]
      * ```
      * where the program points [0],...,[9] are the unknowns of the
      * equation system.
      *
      * This example comes from: Gianluca Amato, Francesca Scozzari, Helmut
      * Seidl, Kalmer Apinis, Vesal Vojdani. Efficiently intertwining widening
      * and narrowing. Science of Computer Programming, Volume 120, 2016
      */
    FiniteEquationSystem( 
      initialBody = (rho: Int => P) => {
        case 0 => initialCs
        case 1 => rho(0).clone().upperBound(rho(9))
        case 2 => rho(1).clone().refineWith(ciless10)
        case 3 => rho(1).clone().refineWith(cigeq10)
        case 4 => rho(2).clone().affineImage(1, LinearExpression.of(0, 0, 0))  
        case 5 => rho(4).clone().upperBound(rho(8))
        case 6 => rho(5).clone().refineWith(cjless10)
        case 7 => rho(5).clone().refineWith(cjgeq10)
        case 8 => rho(6).clone().affineImage(2, LinearExpression.of(1, 0, 1))  
        case 9 => rho(7).clone().affineImage(1, LinearExpression.of(1, 1, 0))  
      },
      initialInfl = Relation(0 -> 1, 1 -> 2, 1 -> 3, 2 -> 4, 4 -> 5,
                      5 -> 6, 5 -> 7, 6 -> 8, 7 -> 9, 8 -> 5, 9 -> 1),
      inputUnknowns = Set(0),
      unknowns = 0 to 9
    )

  def buildGraphEQS[P <: Property[P]](using dom: jppl.Domain[P]) =

    val graphBody = GraphBody[Int, P, String](
      sources = Relation("enter" -> 0, "x<=10" -> 1, "x=x+1" -> 2, "loop" -> 3),
      target =  Map("x=0" -> 0, "enter" -> 1, "x<=10" -> 2, "x=x+1" -> 3, "loop" -> 1),
      
      ingoing = Relation(
        0 -> "x=0",
        1 -> "enter",
        1 -> "loop",
        2 -> "x<=10",
        3 -> "x=x+1"
      ),
      outgoing = Relation(
        0 -> "enter",
        1 -> "x<=10",
        2 -> "x=x+1",
        3 -> "loop"
      ),
      edgeAction = { (rho: Assignment[Int, P]) =>
        {
          case "i=0" => dom.createFrom(csieq0)
          case "outerLoop" => rho(0).clone().upperBound(rho(9)) // ??? rho(0) ?
          case "i<10" => rho(1).clone().refineWith(ciless10)
          case "i>=10" => rho(1).clone().refineWith(cigeq10)
          case "j=0" => rho(2).clone().affineImage(1, LinearExpression.of(0, 0, 0))  
          case "innerLoop" => rho(4).clone().upperBound(rho(8)) // ???
          case "j<10" => rho(5).clone().refineWith(cjless10)
          case "j>=10" => rho(5).clone().refineWith(cjgeq10)
          case "j=j+1" => rho(6).clone().affineImage(2, LinearExpression.of(1, 0, 1))  
          case "i=i+1" => rho(7).clone().affineImage(1, LinearExpression.of(1, 1, 0))  
        }
      }
    )

    /** simpleEqs is the equation system corresponding to the program:
      * ```
      *     x=0;
      * [0] while [1] (x<=10) {
      * [2]   x=x+1;
      * [3] }
      * ```
      * where the program points [0],[1],[2],[3] are the unknowns of the
      * equation system.
      *
      * This example comes from: Gianluca Amato, Francesca Scozzari, Helmut
      * Seidl, Kalmer Apinis, Vesal Vojdani. Efficiently intertwining widening
      * and narrowing. Science of Computer Programming, Volume 120, 2016
      */
    GraphEquationSystem(
      initialGraph = graphBody,
      unknowns = 0 to 3,
      inputUnknowns = Set(0)
    )

class Localized[P <: Property[P]](val dom: jppl.Domain[P]):
  def run() =
    PPL.ioSetVariableOutputFunction((i: Long) => "x" + i)
    val simpleEqs = LocalizedEquationSystems.buildFiniteEQS(dom)
    val solution = WorkListSolver(simpleEqs)(Assignment(dom.createEmpty(1)))
    println(solution)

class LocalizedExample[P <: Property[P]](using dom: jppl.Domain[P]):
  def run() =
    PPL.ioSetVariableOutputFunction((i: Long) => "x" + i)
    val simpleEqs = LocalizedEquationSystems.buildGraphEQS
    val widening = Combo[P]((x: P, y: P) => y.clone().upperBound(x).widening(x))
    val ordering = DFOrdering(simpleEqs)
    println(ordering)
    val comboAssignment = ComboAssignment(widening).restrict(ordering)
    val simpleEqsWithWidening = simpleEqs.withCombos(comboAssignment)
    val solution =
      WorkListSolver(simpleEqsWithWidening)(Assignment(dom.createEmpty(1)))
    println(solution)

object JPPLBoxLocalizedExample extends App:
  JPPLGraphBasedExample[DoubleBox](using new DoubleBoxDomain()).run()

object JPPLPolyhedronLocalizedExample extends App:
  JPPLGraphBasedExample[CPolyhedron](using new CPolyhedronDomain()).run()
