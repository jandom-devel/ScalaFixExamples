/**
 * This is a variant of BodyBench which does not use primitive types so that specialization has no impact.
 *
 * RESULTS:
 * curried: 16.227 ± 0.693  ms/op
 * curried_unopt:  17.884 ± 0.490  ms/op
 * uncurried: 15.996 ± 0.576  ms/op
 * method: 13.096 ± 0.264  ms/op
 */
package it.unich.scalafixexamples

import scala.collection.mutable

import org.openjdk.jmh.annotations.*

import java.util.concurrent.TimeUnit

import math.Ordering.Implicits.infixOrderingOps

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

  val body : Body[Int, Integer] =
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
