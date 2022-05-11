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

/** This is a variant of BodyBenchObject where both values and unknowns are
  * objects.
  *
  * RESULTS:
  * curried        avgt    5  54.253 ± 4.619  ms/op
  * curried_unopt  avgt    5  60.194 ± 3.964  ms/op
  * method         avgt    5  53.826 ± 2.190  ms/op
  * uncurried      avgt    5  53.143 ± 0.736  ms/op
  *
  * The implementation significantly slower than the others is the curred version
  * used in unoptimized way.
  */

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(value = 1)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class BodyBenchAllObject:

  type Body[U, V] = (U => V) => U => V
  type Body2[U, V] = (U => V, U) => V

  val length = 200000
  val limit = 2000

  val body: Body[Integer, Integer] =
    (rho: Integer => Integer) =>
      (u: Integer) =>
        if u == 0
        then rho(length - 1) min limit
        else rho(u - 1) + 1

  val body2: Body2[Integer, Integer] =
    (rho: Integer => Integer, u: Integer) =>
      if u == 0
      then rho(length - 1) min limit
      else rho(u - 1) + 1

  def body3(rho: Integer => Integer, u: Integer): Integer =
    if u == 0
    then rho(length - 1) min limit
    else rho(u - 1) + 1

  def validate(rho: Integer => Integer) = {
    for i <- 0 until length do assert(rho(i) == limit + i)
  }

  @Benchmark
  def curried =
    val current = mutable.Map.empty[Integer, Integer].withDefaultValue(0)
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
    val current = mutable.Map.empty[Integer, Integer].withDefaultValue(0)
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
    val current = mutable.Map.empty[Integer, Integer].withDefaultValue(0)
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
    val current = mutable.Map.empty[Integer, Integer].withDefaultValue(0)
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
