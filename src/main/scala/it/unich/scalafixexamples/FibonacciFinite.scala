package it.unich.scalafixexamples

import it.unich.scalafix.*
import it.unich.scalafix.finite
import it.unich.scalafix.utils.Relation

object FibonacciFinite extends App:

  finiteFibo

  def finiteFibo: Unit =
    val body: Body[Int, BigInt] =
      (rho: Assignment[Int, BigInt]) =>
        (u: Int) => if u <= 1 then 1 else rho(u - 1) + rho(u - 2)

    val infl = Relation((i: Int) => Set(i - 1, i - 2))
    val unknowns = 0 to 10
    val inputUnknows = Set(0, 1)
    val eqs = finite.FiniteEquationSystem(body, infl, unknowns, inputUnknows)
    val sol = finite.WorkListSolver(eqs)(Assignment(1))
    println(sol)
