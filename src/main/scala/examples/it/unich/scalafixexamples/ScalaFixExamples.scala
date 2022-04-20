/**
  * Copyright 2022 Francesca Scozzari <francesca.scozzari@unich.it>
  * and Gianluca Amato <gianluca.amato@unich.it>
  *   
  * This file is part of ScalaFixExamples, a set of examples for the
  * ScalaFix library.
  * ScalaFixExamples is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ScalaFixExamples is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of a
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with ScalaFix.  If not, see <http://www.gnu.org/licenses/>.
  */

package it.unich.scalafixexamples

import it.unich.scalafix._
import it.unich.scalafix.assignments.InputAssignment
import it.unich.scalafix.finite.FiniteEquationSystem
import it.unich.scalafix.finite.KleeneSolver
import it.unich.scalafix.utils.Relation

import parma_polyhedra_library._

object PolyhedronExample extends App {
  PPLLoader()
  Parma_Polyhedra_Library.initialize_library();
  val cs = new Constraint_System
  val x0 = new Variable(0)
  val constraint = new Constraint(
    new Linear_Expression_Variable(x0),
    Relation_Symbol.EQUAL,
    new Linear_Expression_Coefficient(new Coefficient(0))
  )
  cs.add(constraint)
  val pol0 = new C_Polyhedron(cs) // altro esempio: (int, int), oppure Sign

  // val box = new Double_Box(cs)

  // val body = new Body[] {}
  // val equationSystem = FiniteEquationSystem()

  // x=0; [0] while [1] (x<=10) [2] x=x+1 [3];

  val simpleEqs: FiniteEquationSystem[Int, C_Polyhedron] = FiniteEquationSystem(
    body = { (rho: Int => C_Polyhedron) =>
      {
        case 0 => pol0
        case 1 => {
          val pol = new C_Polyhedron(rho(0))
          pol.upper_bound_assign(rho(3))
          pol
        }
        case 2 => {
          val pol = new C_Polyhedron(rho(1))
          pol.refine_with_constraint(
            new Constraint(
              new Linear_Expression_Variable(x0),
              Relation_Symbol.LESS_OR_EQUAL,
              new Linear_Expression_Coefficient(new Coefficient(10))
            )
          )
          pol
        }
        case 3 => {
          val pol = new C_Polyhedron(rho(2))
          pol.affine_image(
            x0,
            new Linear_Expression_Sum(
              new Linear_Expression_Variable(x0),
              new Linear_Expression_Coefficient(new Coefficient(1))
            ),
            new Coefficient(1)
          ) // denominator
          pol
        }
      }
    },
    inputUnknowns = Set(), // Set(0, 1, 2, 3),
    initial =
      new C_Polyhedron(
        1,
        Degenerate_Element.EMPTY
      ), // InputAssignment.conditional(3, 10.0, 0.0),
    unknowns = Set(0, 1, 2, 3),
    infl = Relation(Map(0 -> Set(1), 1 -> Set(2), 2 -> Set(3), 3 -> Set(1)))
  )

  // private val simpleEqsStrategy = HierarchicalOrdering(Left, Val(0), Left, Val(1), Val(2), Val(3), Right, Right)
  val solver = KleeneSolver(simpleEqs)()

  // mettere il widening!!! trovare in che punto metterlo?

  // private val wideningBox: Box[Double] = { (x1: Double, x2: Double) => if (x2 > x1) Double.PositiveInfinity else x1 }
  // private val maxBox: Box[Double] = { (x: Double, y: Double) => x max y }
  // private val lastBox: Box[Double] = { (_: Double, x2: Double) => x2 }

  // private val startRho: InputAssignment[Int, Double] = simpleEqs.initial

  // private type SimpleSolver[U, V] = (FiniteEquationSystem[U, V], InputAssignment[U, V]) => IOAssignment[U, V]
  println(solver)
}

object BoxExample extends App {
  PPLLoader()
  Parma_Polyhedra_Library.initialize_library()
  val cs = new Constraint_System
  val x0 = new Variable(0)
  val constraint = new Constraint(
    new Linear_Expression_Variable(x0),
    Relation_Symbol.EQUAL,
    new Linear_Expression_Coefficient(new Coefficient(0))
  )
  cs.add(constraint)
  val box0 = new Double_Box(cs) // altro esempio: (int, int), oppure Sign
  // val body = new Body[] {}
  // val equationSystem = FiniteEquationSystem()

  // x=0; [0] while [1] (x<=10) [2] x=x+1 [3];

  val simpleEqs: FiniteEquationSystem[Int, Double_Box] = FiniteEquationSystem(
    body = { (rho: Int => Double_Box) =>
      {
        case 0 => box0
        case 1 => {
          val box = new Double_Box(rho(0))
          box.upper_bound_assign(rho(3))
          box
        }
        case 2 => {
          val box = new Double_Box(rho(1))
          box.refine_with_constraint(
            new Constraint(
              new Linear_Expression_Variable(x0),
              Relation_Symbol.LESS_OR_EQUAL,
              new Linear_Expression_Coefficient(new Coefficient(10))
            )
          )
          box
        }
        case 3 => {
          val box = new Double_Box(rho(2))
          box.affine_image(
            x0,
            new Linear_Expression_Sum(
              new Linear_Expression_Variable(x0),
              new Linear_Expression_Coefficient(new Coefficient(1))
            ),
            new Coefficient(1)
          ) // denominator
          box
        }
      }
    },
    inputUnknowns = Set(), // Set(0, 1, 2, 3),
    initial =
      new Double_Box(
        1,
        Degenerate_Element.EMPTY
      ), // InputAssignment.conditional(3, 10.0, 0.0),
    unknowns = Set(0, 1, 2, 3),
    infl = Relation(Map(0 -> Set(1), 1 -> Set(2), 2 -> Set(3), 3 -> Set(1)))
  )

  // private val simpleEqsStrategy = HierarchicalOrdering(Left, Val(0), Left, Val(1), Val(2), Val(3), Right, Right)
  val solver = KleeneSolver(simpleEqs)()

  // mettere il widening!!! trovare in che punto metterlo?

  // private val wideningBox: Box[Double] = { (x1: Double, x2: Double) => if (x2 > x1) Double.PositiveInfinity else x1 }
  // private val maxBox: Box[Double] = { (x: Double, y: Double) => x max y }
  // private val lastBox: Box[Double] = { (_: Double, x2: Double) => x2 }

  // private val startRho: InputAssignment[Int, Double] = simpleEqs.initial

  // private type SimpleSolver[U, V] = (FiniteEquationSystem[U, V], InputAssignment[U, V]) => IOAssignment[U, V]
  println(solver)
}

object JPPLBoxExample extends App {
  // usa i nuovi binding
  import it.unich.jppl._
  import it.unich.jppl.Domain._
  val cs = new ConstraintSystem
  val x0 = 0
  val constraint = new it.unich.jppl.Constraint(
    new LinearExpression(0, 1),
    Constraint.ConstraintType.EQUAL
  )
  cs.add(constraint)

  val box0 = new DoubleBox(cs) // altro esempio: (int, int), oppure Sign

  // val body = new Body[] {}
  // val equationSystem = FiniteEquationSystem()

  // x=0; [0] while [1] (x<=10) [2] x=x+1 [3];

  val simpleEqs: FiniteEquationSystem[Int, DoubleBox] = FiniteEquationSystem(
    body = { (rho: Int => DoubleBox) =>
      {
        case 0 => box0
        case 1 => new DoubleBox(rho(0)).upperBoundAssign(rho(3))
        case 2 =>
          new DoubleBox(rho(1)).refineWithConstraint(
            new Constraint(
              new LinearExpression(-10, 1),
              Constraint.ConstraintType.LESS_OR_EQUAL
            )
          )
        case 3 =>
          new DoubleBox(rho(2))
            .affineImage(x0, new LinearExpression(1, 1), new Coefficient(1))
      }
    },
    inputUnknowns = Set(), // Set(0, 1, 2, 3),
    initial =
      new DoubleBox(
        1,
        Domain.DegenerateElement.EMPTY
      ), // InputAssignment.conditional(3, 10.0, 0.0),
    unknowns = Set(0, 1, 2, 3),
    infl = Relation(Map(0 -> Set(1), 1 -> Set(2), 2 -> Set(3), 3 -> Set(1)))
  )

  // private val simpleEqsStrategy = HierarchicalOrdering(Left, Val(0), Left, Val(1), Val(2), Val(3), Right, Right)
  val solver = KleeneSolver(simpleEqs)()

  // mettere il widening!!! trovare in che punto metterlo?

  // private val wideningBox: Box[Double] = { (x1: Double, x2: Double) => if (x2 > x1) Double.PositiveInfinity else x1 }
  // private val maxBox: Box[Double] = { (x: Double, y: Double) => x max y }
  // private val lastBox: Box[Double] = { (_: Double, x2: Double) => x2 }

  // private val startRho: InputAssignment[Int, Double] = simpleEqs.initial

  // private type SimpleSolver[U, V] = (FiniteEquationSystem[U, V], InputAssignment[U, V]) => IOAssignment[U, V]
  println(solver)
}

object GenericExample extends App {
  PPLLoader()
  Parma_Polyhedra_Library.initialize_library();
  val cs = new Constraint_System()
  val x0 = new Variable(0)
  val constraint = new Constraint(
    new Linear_Expression_Variable(x0),
    Relation_Symbol.EQUAL,
    new Linear_Expression_Coefficient(new Coefficient(0))
  )
  cs.add(constraint)
  val box0 = new Double_Box(cs) // altro esempio: (int, int), oppure Sign
  // val body = new Body[] {}
  // val equationSystem = FiniteEquationSystem()

  // x=0; [0] while [1] (x<=10) [2] x=x+1 [3];

  val simpleEqs: FiniteEquationSystem[Int, Double_Box] = FiniteEquationSystem(
    body = { (rho: Int => Double_Box) =>
      {
        case 0 => box0
        case 1 => {
          val box = new Double_Box(rho(0))
          box.upper_bound_assign(rho(3))
          box
        }
        case 2 => {
          val box = new Double_Box(rho(1))
          box.refine_with_constraint(
            new Constraint(
              new Linear_Expression_Variable(x0),
              Relation_Symbol.LESS_OR_EQUAL,
              new Linear_Expression_Coefficient(new Coefficient(10))
            )
          )
          box
        }
        case 3 => {
          val box = new Double_Box(rho(2))
          box.affine_image(
            x0,
            new Linear_Expression_Sum(
              new Linear_Expression_Variable(x0),
              new Linear_Expression_Coefficient(new Coefficient(1))
            ),
            new Coefficient(1)
          ) // denominator
          box
        }
      }
    },
    inputUnknowns = Set(), // Set(0, 1, 2, 3),
    initial =
      new Double_Box(
        1,
        Degenerate_Element.EMPTY
      ), // InputAssignment.conditional(3, 10.0, 0.0),
    unknowns = Set(0, 1, 2, 3),
    infl = Relation(Map(0 -> Set(1), 1 -> Set(2), 2 -> Set(3), 3 -> Set(1)))
  )

  // private val simpleEqsStrategy = HierarchicalOrdering(Left, Val(0), Left, Val(1), Val(2), Val(3), Right, Right)
  val solver = KleeneSolver(simpleEqs)()

  // mettere il widening!!! trovare in che punto metterlo?

  // private val wideningBox: Box[Double] = { (x1: Double, x2: Double) => if (x2 > x1) Double.PositiveInfinity else x1 }
  // private val maxBox: Box[Double] = { (x: Double, y: Double) => x max y }
  // private val lastBox: Box[Double] = { (_: Double, x2: Double) => x2 }

  // private val startRho: InputAssignment[Int, Double] = simpleEqs.initial

  // private type SimpleSolver[U, V] = (FiniteEquationSystem[U, V], InputAssignment[U, V]) => IOAssignment[U, V]
  println(solver)
}
