package it.unich.scalafixexamples

import it.unich.scalafix.*
import it.unich.scalafix.infinite

object Fibonacci extends App:

  infiniteFibo

  def infiniteFibo: Unit =
    val body: Body[Int, BigInt] =
      (rho: Assignment[Int, BigInt]) =>
        (u: Int) => if u <= 1 then 1 else rho(u - 1) + rho(u - 2)

    val eqs = EquationSystem(body)
    val sol = infinite.WorkListSolver(eqs)(Assignment(1), Set(6))
    println(sol)
