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
import it.unich.scalafix.lattice.Domain
import it.unich.scalafix.utils.Relation

class JPPLExample[P <: it.unich.jppl.Property[P]](val dom: it.unich.jppl.Domain[P]) {

  def buildEquationSystem():FiniteEquationSystem[Int, P] = {
    /**
     * initialCs is a constraint system with the contraint:
     *  x=0
     * where x is the unknown of index 0.
     */
    val c =  Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL)
    val cs = ConstraintSystem.of(c)
    val initialCs = dom.createFrom(cs) 

    /**
     * simpleEqs is the equation system corresponding to the program:
     *
     *     x=0; 
     * [0] while [1] (x<=10) { 
     * [2]   x=x+1;
     * [3] }
     *
     * where the program points [0],[1],[2],[3] are the unknowns of the equation system.
     */
    val simpleEqs: FiniteEquationSystem[Int, P] = FiniteEquationSystem(
      body = { (rho: Int => P) =>
        {
          case 0 => initialCs
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
    simpleEqs
  }

  def run() = {
    val simpleEqs = buildEquationSystem()
    val solver = KleeneSolver(simpleEqs)(Assignment(dom.createEmpty(1)))
    println(solver)
  }

}
object JPPLBoxExample extends App {
  JPPLExample[DoubleBox](new DoubleBoxDomain()).run();
}
object JPPLPolyhedronExample extends App {
  JPPLExample[CPolyhedron](new CPolyhedronDomain()).run();
}

class JPPLWithWideningExample[P <: Property[P]](dom: it.unich.jppl.Domain[P]) {
  def run() = {
    val simpleEqs = JPPLExample[P](dom).buildEquationSystem()

    val widening = Combo[P]( {(x:P,y:P) => y.clone().upperBound(x).widening(x)} )
    val comboAssignment = ComboAssignment(widening).restrict(Set(1))
    
    val simpleEqsWithWidening= simpleEqs.withCombos(comboAssignment)
    val solution = KleeneSolver(simpleEqsWithWidening)(Assignment(dom.createEmpty(1)))

    println(solution)
  }
}
object JPPLBoxWithWideningExample extends App {

  JPPLWithWideningExample[DoubleBox](new DoubleBoxDomain()).run()
}

object JPPLPolyhedronWithWideningExample extends App {

  JPPLWithWideningExample[CPolyhedron](new CPolyhedronDomain()).run()
}
