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

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import scala.collection.mutable
import math.Ordering.Implicits.infixOrderingOps

/** This is a variant of BodyBench which does not use primitive types so that
  * specialization has no impact.
  *
  * RESULTS:
  * curried: 16.227 ± 0.693  ms/op
  * curried_unopt:  17.884 ± 0.490  ms/op
  * uncurried: 15.996 ± 0.576  ms/op
  * method: 13.096 ± 0.264  ms/op
  */

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(value = 1)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class BodyBenchObject:

  type Body[U, V] = (U => V) => U => V
  type Body2[U, V] = (U => V, U) => V

  val length = 200000
  val limit = 2000

  val body: Body[Int, Integer] =
    (rho: Int => Integer) =>
      (u: Int) =>
        if u == 0
        then rho(length - 1) min limit
        else rho(u - 1) + 1

  val body2: Body2[Int, Integer] =
    (rho: Int => Integer, u: Int) =>
      if u == 0
      then rho(length - 1) min limit
      else rho(u - 1) + 1

  def body3(rho: Int => Integer, u: Int): Integer =
    if u == 0
    then rho(length - 1) min limit
    else rho(u - 1) + 1

  def validate(rho: Int => Integer) = {
    for i <- 0 until length do assert(rho(i) == limit + i)
  }

  @Benchmark
  def curried =
    val current = Array.fill[Integer](length)(0)
    val bodycurrent = body(current)
    var dirty = true
    while dirty do
      dirty = false
      var x = 0
      while x < length do
        val newval = bodycurrent(x)
        if newval != current(x) then
          current(x) = newval
          dirty = true
        x += 1
    current

  @Benchmark
  def curried_unopt =
    val current = Array.fill[Integer](length)(0)
    var dirty = true
    while dirty do
      dirty = false
      var x = 0
      while x < length do
        val newval = body(current)(x)
        if newval != current(x) then
          current(x) = newval
          dirty = true
        x += 1
    current

  @Benchmark
  def uncurried =
    val current = Array.fill[Integer](length)(0)
    var dirty = true
    while dirty do
      dirty = false
      var x = 0
      while x < length do
        val newval = body2.apply(current, x)
        if newval != current(x) then
          current(x) = newval
          dirty = true
        x += 1
    current

  @Benchmark
  def method =
    val current = Array.fill[Integer](length)(0)
    var dirty = true
    while dirty do
      dirty = false
      var x = 0
      while x < length do
        val newval = body3(current, x)
        if newval != current(x) then
          current(x) = newval
          dirty = true
        x += 1
    current
