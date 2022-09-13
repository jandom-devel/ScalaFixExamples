/** Copyright 2022 Gianluca Amato <gianluca.amato@unich.it>
  *        and Francesca Scozzari <francesca.scozzari@unich.it>
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
import it.unich.scalafix.infinite

import it.unich.jppl.Constraint.ConstraintType

import scala.collection.mutable

import java.util.Objects

/** Class used for unknowns.
  * @param pp
  *   a string for the program point
  * @param context
  *   an abstraction of the value of formal parameters for a function
  */
case class U[P <: Property[P]](pp: Int, context: P):
  /** We define an hash code for the unknowns. This is needed since the default
    * hash code for JPPL is not correct (equal objects may have different hash
    * codes). As a fast hack, we use the hash code of the String representation
    * of the hash code.
    */
  override def hashCode(): Int = Objects.hash(pp, context.toString)

/** Interface for widening of contexts in function calls.
  */
trait ContextWidening[P <: Property[P]]:
  def apply(u: U[P]): U[P]

/** A widening which does nothing.
  */
class NoContextWidening[P <: Property[P]] extends ContextWidening[P]:
  def apply(u: U[P]) = u

/** A widening which only keeps a single context for each function call.
  */
class SingleContextWidening[P <: Property[P]] extends ContextWidening[P]:
  val widen_data = mutable.Map.empty[Int, P]

  def apply(u: U[P]): U[P] =
    val optV = widen_data.get(u.pp)
    optV match
      case None =>
        widen_data(u.pp) = u.context.clone()
        u
      case Some(v) =>
        val vnew = v.upperBound(u.context).widening(u.context)
        widen_data(u.pp) = vnew
        U(u.pp, vnew)

/** An example of inter-procedeural analysis.
  *
  * @param dom
  *   domain for the analysis
  * @param widen
  *   widening for contexts
  */
class InterProcedualAnalysisExample[P <: Property[P]](
    dom: Domain[P],
    widen: ContextWidening[P]
):

  // the linear expression a+1
  val aplus1 = LinearExpression.of(1, 1)

  // the constraint system {i=0, j=0}
  val ijeq0 = ConstraintSystem.of(
    Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL),
    Constraint.of(LinearExpression.of(0, 0, 1), Constraint.ConstraintType.EQUAL)
  )

  /** A function for concatenating calling and return contexts. */
  def concatenateReturn(p: P, ret: P, commonDims: Long): P =
    val pdims = p.getSpaceDimension()
    val retdims = ret.getSpaceDimension()
    p.addSpaceDimensionsAndEmbed(retdims - commonDims)
    ret.addSpaceDimensionsAndEmbed(pdims - commonDims)
    val mapDims =
      (pdims - commonDims).until(retdims + pdims - commonDims) ++ 0L.until(
        pdims - commonDims
      )
    ret.mapSpaceDimensions(mapDims.toArray)
    p.intersection(ret)

  /*
   * The function is:
   *   function incr(a) {
   *      [1] b = a + 1
   *      [2] return b
   *      [3]
   *   }
   * The main program is:
   *       i = j = 0
   *   [4] j = incr(i)
   *   [5] i = incr(j)
   *   [6]
   */
  val initialBody: Body[U[P], P] = (rho: U[P] => P) =>
    case U(1, c) =>
      c.clone().addSpaceDimensionsAndEmbed(1)
    case U(2, c) =>
      rho(U(1, c)).clone().affineImage(1, aplus1)
    case U(3, c) =>
      rho(U(2, c))
    case U(4, c) =>
      dom.createFrom(ijeq0)
    case U(5, c) =>
      val call_context = rho(U(4, c)).clone().removeSpaceDimensions(Array(1))
      val return_context = rho(widen(U(3, call_context)))
      val p0 = rho(U(4, c)).clone().mapSpaceDimensions(Array(1, 0))
      val p1 = concatenateReturn(p0, return_context.clone(), 1)
      val result = p1.mapSpaceDimensions(Array(PPL.getNotADimension(), 0, 1))
      result
    case U(6, c) =>
      val call_context = rho(U(5, c)).clone().removeSpaceDimensions(Array(0))
      val return_context = rho(widen(U(3, call_context)))
      val p0 = rho(U(5, c)).clone()
      val p1 = concatenateReturn(p0, return_context.clone(), 1)
      val result = p1.mapSpaceDimensions(Array(PPL.getNotADimension(), 1, 0))
      result
  val eqs = EquationSystem(initialBody)

  val initialAssignment: Assignment[U[P], P] = Assignment(dom.createEmpty(2))

  def run() =
    val wanted = Seq(U(6, dom.createEmpty(0)))
    PPL.ioSetVariableOutputFunction("v"+_)
    val solution = infinite.WorkListSolver(eqs)(initialAssignment, wanted)
    for (pp <- wanted)
      println(s"${pp} -> ${solution(pp)}")
    println("\nComplete result:")
    println(
      solution.toSeq
        .sortBy(_._1.toString)
        .map(p => s"${p._1} -> ${p._2}")
        .mkString("", "\n", "")
    )

object IPAExampleBoxNoWidening extends App:
  InterProcedualAnalysisExample(DoubleBoxDomain(), NoContextWidening()).run()

object IPAExampleBoxWidening extends App:
  InterProcedualAnalysisExample(DoubleBoxDomain(), SingleContextWidening())
    .run()

object IPAExamplePolyNoWidening extends App:
  InterProcedualAnalysisExample(CPolyhedronDomain(), NoContextWidening()).run()

object IPAExamplePolyWidening extends App:
  InterProcedualAnalysisExample(CPolyhedronDomain(), SingleContextWidening())
    .run()
