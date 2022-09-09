/** Copyright 2022 Gianluca Amato <gianluca.amato@unich.it>
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

import scala.collection.mutable
import it.unich.jppl.Constraint.ConstraintType

/** Class used for labels of program points.
  * @param pp
  *   a string for the program point
  * @param context
  *   an abstract object containing the calling context (actual parameters)
  */
case class U[P <: Property[P]](pp: String, context: P):
  override def hashCode(): Int = 3 * pp.hashCode + context.toString.hashCode

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
  val widen_data = mutable.Map.empty[String, P]

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

  // the linear expression x+1
  val xplus1 = LinearExpression.of(1, 1)

  // the constraint system {x=0, y=0}
  val xyeq0 = ConstraintSystem.of(
    Constraint.of(LinearExpression.of(0, 1), Constraint.ConstraintType.EQUAL),
    Constraint.of(LinearExpression.of(0, 0, 1), Constraint.ConstraintType.EQUAL)
  )

  /** A function for concatenating calling and return contexts. */
  def concatenateReturn(p: P, ret: P, commonDims: Long): P =
    val pdims = p.getSpaceDimension()
    val retdims = ret.getSpaceDimension()
    p.addSpaceDimensionsAndEmbed(retdims-commonDims)
    ret.addSpaceDimensionsAndEmbed(pdims-commonDims)
    val mapDims = (pdims-commonDims).until(retdims+pdims-commonDims) ++ 0L.until(pdims-commonDims)
    ret.mapSpaceDimensions(mapDims.toArray)
    p.intersection(ret)

  /*
   * the function is:
   *   incr(x) = x+1
   * the program is:
   *   x=y=0
   *   p0 y=incr(x)
   *   p1 x=incr(y)
   *   p2
   */
  val initialBody: Body[U[P], P] = (rho: U[P] => P) =>
    case U("incr_start", i) =>
      i.clone()
    case U("incr_end", i) =>
      rho(U("incr_start", i)).clone().addSpaceDimensionsAndEmbed(1).affineImage(1, xplus1)
    case U("p0", i) =>
      dom.createFrom(xyeq0)
    case U("p1", i) =>
      val p0 = rho(U("p0", i)).clone()
      val p0WithParameters = p0.mapSpaceDimensions(Array(1,0))
      val call_context = p0WithParameters.clone().removeSpaceDimensions(Array(0))
      val result = rho(widen(U("incr_end", call_context))).clone()
      concatenateReturn(p0WithParameters, result, 1)
        .mapSpaceDimensions(Array(PPL.getNotADimension(), 0, 1))
    case U("p2", i) =>
      val p1 = rho(U("p1", i)).clone()
      val p1WithParameters = p1
      val call_context = p1WithParameters.clone().removeSpaceDimensions(Array(0))
      val result = rho(widen(U("incr_end", call_context))).clone()
      concatenateReturn(p1WithParameters, result, 1)
        .mapSpaceDimensions(Array(PPL.getNotADimension(), 1, 0))

  val eqs = EquationSystem(initialBody)

  val initialAssignment: Assignment[U[P], P] =
    case U("incr_start", _) => dom.createEmpty(1)
    case U("incr_end", _)   => dom.createEmpty(2)
    case _                  => dom.createEmpty(2)

  def run() =
    val wanted = Seq(U("p2", dom.createEmpty(0)))
    PPL.ioSetVariableOutputFunction({
      case 0 => "x"
      case 1 => "y"
      case i => "x" + i
    })
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