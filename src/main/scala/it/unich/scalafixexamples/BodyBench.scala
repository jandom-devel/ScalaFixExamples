/**
 * This is an example comparing two implementations of Body, in the
 * curried and uncurried variants.
 *
 * RESULTS:
 * curried: 1.659 ± 0.014  ms/op
 * curried_unopt:  2.503 ± 0.360  ms/op
 * uncurried: 3.928 ± 0.063  ms/op
 * method: 1.212 ± 0.013  ms/op
 * method_func: 1.211 ± 0.007  ms/op
 *
 * Interestingly, method_func is faster than uncurried. The problem is that uncurried requires boxing of all ints,
 * (since Function2 is type polymorphic) while in method_func some ints may be passed to the apply method as unboxed
 * primitive values. Note that Function2 has some specialization, but we need the case in which the first argument is
 * specialized and the second argument is not, which is probably not included. For the same reasong, curried and
 * curried_unopt are faster than uncurried, since in this case the applciation of the second argument is specialized.
 */
package it.unich.scalafixexamples

import scala.collection.mutable

import org.openjdk.jmh.annotations.*

import java.util.concurrent.TimeUnit

class OtherFunction extends Function2[Int => Int, Int, Int]:
  val length = 200000
  val limit = 2000

  override def apply(rho: Int => Int, u: Int): Int =
    if u == 0
      then rho(length - 1) min limit
      else rho(u - 1) + 1

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(value = 1)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class BodyBench:

  type Body[U, V] = (U => V) => U => V
  type Body2[U, V] = (U => V, U) => V

  val length = 200000
  val limit = 2000

  val body : Body[Int, Int] =
    (rho: Int => Int) =>
      (u: Int) =>
        if u == 0
        then rho(length - 1) min limit
        else rho(u - 1) + 1

  val body2: Body2[Int, Int] =
    (rho: Int => Int, u: Int) =>
      if u == 0
      then rho(length - 1) min limit
      else rho(u - 1) + 1

  def body3(rho: Int => Int, u: Int): Int =
     if u == 0
        then rho(length - 1) min limit
        else rho(u - 1) + 1

  def validate(rho: Int => Int) = {
    for i <- 0 until length do assert(rho(i) == limit + i)
  }

  @Benchmark
  def curried =
    val current = Array.fill(length)(0)
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
    val current = Array.fill(length)(0)
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
    val current = Array.fill(length)(0)
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
    val current = Array.fill(length)(0)
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

  @Benchmark
  def method_func =
    val func = OtherFunction()
    val current = Array.fill(length)(0)
    var dirty = true
    while dirty do
      dirty = false
      var x = 0
      while x < length do
        val newval = func(current, x)
        if newval != current(x) then
          current(x) = newval
          dirty = true
        x += 1
    current