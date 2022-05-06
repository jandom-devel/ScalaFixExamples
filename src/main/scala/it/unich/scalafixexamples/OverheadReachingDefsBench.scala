package it.unich.scalafixexamples

import it.unich.jppl.*
import it.unich.scalafix.*
import it.unich.scalafix.finite.*
import org.openjdk.jmh.annotations.*

import scala.collection.mutable

@State(Scope.Benchmark)
@Warmup(iterations = 3)
class OverheadReachingDefsBench {

  val length = 8

  var eqs: SimpleFiniteEquationSystem[Int, Set[Int]] = FiniteEquationSystem(
    initialBody = { (rho: Int => Set[Int]) =>
      {
        case 1 => Set(1) -- Set(4, 7)
        case 2 => Set(2) ++ (rho(1) -- Set(5))
        case 3 => Set(3) ++ (rho(2) -- Set())
        case 4 => Set(4) ++ (rho(3) ++ rho(7) ++ rho(6) -- Set(1, 7))
        case 5 => Set(5) ++ (rho(4) -- Set(2))
        case 6 => Set(6) ++ rho(5) -- Set(3)
        case 7 => Set(6) ++ rho(5) -- Set(1, 4)
      }
    },
    initialInfl = InfluenceRelation(
      Map(
        1 -> Set(2),
        2 -> Set(3),
        3 -> Set(4),
        4 -> Set(5),
        5 -> Set(6, 7),
        6 -> Set(4),
        7 -> Set(4)
      )
    ),
    inputUnknowns = Set(),
    unknowns = Range(1, 8)
  )

  @Benchmark
  def scalafixWithoutCombos() = {
    RoundRobinSolver(eqs)(Assignment(Set()))
  }

  @Benchmark
  def hashMap() = {
    val rho = mutable.Map.empty[Int, Set[Int]].withDefaultValue(Set())
    var bodyrho = eqs.body(rho)
    var dirty = true
    var i = 1
    while dirty do
      dirty = false
      i = 1
      while i < length do
        val v = rho(i)
        val vnew = bodyrho(i)
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho
  }

  @Benchmark
  def array() = {
    val rho = Array.fill(length)(Set[Int]())
    var bodyrho = eqs.body(rho)
    var dirty = true
    var i = 1
    while dirty do
      dirty = false
      i = 1
      while i < length do
        val v = rho(i)
        val vnew = bodyrho(i)
        if v != vnew then
          rho(i) = vnew
          dirty = true
        i += 1
    rho
  }

}
