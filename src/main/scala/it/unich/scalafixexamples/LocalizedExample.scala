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
import it.unich.scalafix.highlevel.*
import it.unich.scalafix.utils.Relation

object LocalizedEquationSystems:

  // the constraint system {i=0}
  val csieq0 = ConstraintSystem.of(
    Constraint.of(LinearExpression.of(0, 1, 0), Constraint.ConstraintType.EQUAL)
  )

  // the constraint system {j=0}
  val csjeq0 = ConstraintSystem.of(
    Constraint.of(LinearExpression.of(0, 0, 1), Constraint.ConstraintType.EQUAL)
  )

  // the constraint i<10
  val ciless10 = Constraint.of(
    LinearExpression.of(-10, 1, 0),
    Constraint.ConstraintType.LESS_THAN
  )

  // the constraint i>=10
  val cigeq10 = Constraint.of(
    LinearExpression.of(-10, 1, 0),
    Constraint.ConstraintType.GREATER_OR_EQUAL
  )

  // the constraint j<10
  val cjless10 = Constraint.of(
    LinearExpression.of(-10, 0, 1),
    Constraint.ConstraintType.LESS_THAN
  )

  // the constraint j>=10
  val cjgeq10 = Constraint.of(
    LinearExpression.of(-10, 0, 1),
    Constraint.ConstraintType.GREATER_OR_EQUAL
  )

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
    * where the program points [0],...,[9] are the unknowns of the equation
    * system.
    *
    * This example comes from: Gianluca Amato, Francesca Scozzari, Helmut Seidl,
    * Kalmer Apinis, Vesal Vojdani. Efficiently intertwining widening and
    * narrowing. Science of Computer Programming, Volume 120, 2016
    */
  def buildGraphEQS[P <: Property[P]](dom: jppl.Domain[P]) =

    val graphBody = GraphBody[Int, P, String](
      sources = Relation(
        "inOuterLoop" -> 0,
        "outerLoop" -> 9,
        "i>=10" -> 1,
        "i<10" -> 1,
        "j=0" -> 2,
        "inInnerLoop" -> 4,
        "innerLoop" -> 7,
        "j<10" -> 5,
        "j=j+1" -> 6,
        "j>=10" -> 5,
        "i=i+1" -> 8
      ),
      target = Map(
        "i=0" -> 0,
        "inOuterLoop" -> 1,
        "outerLoop" -> 1,
        "i>=10" -> 3,
        "i<10" -> 2,
        "j=0" -> 4,
        "inInnerLoop" -> 5,
        "innerLoop" -> 5,
        "j<10" -> 6,
        "j=j+1" -> 7,
        "j>=10" -> 8,
        "i=i+1" -> 9
      ),
      ingoing = Relation(
        0 -> "i=0",
        1 -> "inOuterLoop",
        1 -> "outerLoop",
        2 -> "i<10",
        3 -> "i>=10",
        4 -> "j=0",
        5 -> "inInnerLoop",
        5 -> "innerLoop",
        6 -> "j<10",
        7 -> "j=j+1",
        8 -> "j>=10",
        9 -> "i=i+1"
      ),
      outgoing = Relation(
        0 -> "inOuterLoop",
        1 -> "i<10",
        1 -> "i>=10",
        2 -> "j=0",
        4 -> "inInnerLoop",
        5 -> "j<10",
        5 -> "j>=10",
        6 -> "j=j+1",
        7 -> "innerLoop",
        8 -> "i=i+1",
        9 -> "outerLoop"
      ),
      edgeAction = { (rho: Assignment[Int, P]) =>
        {
          case "i=0"         => dom.createFrom(csieq0)
          case "inOuterLoop" => rho(0)
          case "outerLoop"   => rho(9)
          case "i<10"        => rho(1).clone().refineWith(ciless10)
          case "i>=10"       => rho(1).clone().refineWith(cigeq10)
          case "j=0"         => rho(2).clone().affineImage(1, LinearExpression.of(0, 0, 0))
          case "inInnerLoop" => rho(4)
          case "innerLoop"   => rho(7)
          case "j<10"        => rho(5).clone().refineWith(cjless10)
          case "j>=10"       => rho(5).clone().refineWith(cjgeq10)
          case "j=j+1"       => rho(6).clone().affineImage(1, LinearExpression.of(1, 0, 1))
          case "i=i+1"       => rho(7).clone().affineImage(0, LinearExpression.of(1, 1, 0))
        }
      },
      combiner = { _.clone().upperBound(_) },
      unknowns = 0 to 9
    )

    GraphEquationSystem(
      initialGraph = graphBody,
      inputUnknowns = Set(0)
    )

class LocalizedExample[P <: Property[P]](localized: Boolean)(using
    dom: jppl.Domain[P]
):
  def run() =
    PPL.ioSetVariableOutputFunction({
      case 0 => "i"
      case 1 => "j"
      case i => "x" + i
    })
    val eqs = LocalizedEquationSystems.buildGraphEQS(dom)
    val widening = Combo[P]((x: P, y: P) => y.clone().upperBound(x).widening(x))
    val ordering = DFOrdering(eqs)
    println(ordering)
    val wideningAssignment = ComboAssignment(widening).restrict(ordering)
    val eqsWithWidening =
      if localized
      then eqs.withLocalizedCombos(wideningAssignment, ordering)
      else eqs.withCombos(wideningAssignment)
    val solutionAscending =
      WorkListSolver(eqsWithWidening)(Assignment(dom.createEmpty(2)))

    val narrowing = Combo[P]((x: P, y: P) => y.clone().intersection(x))

    val narrowingAssignment = ComboAssignment(narrowing).restrict(ordering)
    val eqsWithNarrowing = eqs.withCombos(narrowingAssignment)
    val solution =
      WorkListSolver(eqsWithNarrowing)(solutionAscending)
    println(solution(3))

object JPPLBoxNotLocalizedExample extends App:
  LocalizedExample[DoubleBox](false)(using new DoubleBoxDomain()).run()

object JPPLPolyhedronNotLocalizedExample extends App:
  LocalizedExample[CPolyhedron](false)(using new CPolyhedronDomain()).run()

object JPPLBoxLocalizedExample extends App:
  LocalizedExample[DoubleBox](true)(using new DoubleBoxDomain()).run()

object JPPLPolyhedronLocalizedExample extends App:
  LocalizedExample[CPolyhedron](true)(using new CPolyhedronDomain()).run()

class JPPLSimpleAPIExample[P <: Property[P]](comboScope: ComboScope = ComboScope.Standard, comboStrategy: ComboStrategy = ComboStrategy.TwoPhases)(using dom: jppl.Domain[P]):
  def run() =
    PPL.ioSetVariableOutputFunction({
      case 0 => "i"
      case 1 => "j"
      case i => "x" + i
    })

    val eqs = LocalizedEquationSystems.buildGraphEQS(dom)
    val widening = Combo[P]((x, y) => y.clone().upperBound(x).widening(x))
    val narrowingList = dom.getNarrowings()
    val narrowing = if narrowingList.isEmpty 
      then Combo.left[P].delayed(1)
      else 
        val pplNarrowing = narrowingList.get(0).getNarrowing()
        Combo[P]((x, y) => pplNarrowing(y.clone().intersection(x), x))
    val params = Parameters[Int, P](
      solver = Solver.WorkListSolver,
      start = Assignment(dom.createEmpty(2)),
      comboScope = comboScope,
      comboStrategy = comboStrategy,
      widenings = ComboAssignment(widening),
      narrowings = ComboAssignment(narrowing)
    )
    val solution = FiniteFixpointSolver(eqs, params)
    println(solution(3))

object JPPLBoxNotLocalizedSimpleAPIExample extends App:
  JPPLSimpleAPIExample[DoubleBox]()(using new DoubleBoxDomain()).run()

object JPPLBoxLocalizedSimpleAPIExample extends App:
  JPPLSimpleAPIExample[DoubleBox](comboScope = ComboScope.Localized)(using new DoubleBoxDomain()).run()

object JPPLBoxWarrowingSimpleAPIExample extends App:
  JPPLSimpleAPIExample[DoubleBox](comboStrategy = ComboStrategy.Warrowing)(using new DoubleBoxDomain()).run()

object JPPLPolyhedronLocalizedSimpleAPIExample extends App:
  JPPLSimpleAPIExample[CPolyhedron](comboScope = ComboScope.Localized)(using new CPolyhedronDomain()).run()