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

import it.unich.jppl.Property
import it.unich.scalafix.utils.Domain

given DomainForJPPLProperty[P <: Property[P]]: Domain[P] with

  def lteq(x: P, y: P): Boolean = y.contains(x)

  override def gteq(x: P, y: P): Boolean = x.contains(y)

  override def lt(x: P, y: P): Boolean = y.strictlyContains(x)

  override def gt(x: P, y: P): Boolean = x.strictlyContains(y)

  override def tryCompare(x: P, y: P): Option[Int] =
    if y.strictlyContains(x) then Some(1)
    else if x.strictlyContains(y)then Some(-1)
    else if x == y then Some(0)
    else None

  override def upperBound(x: P, y: P): P = x.clone().upperBound(y)
