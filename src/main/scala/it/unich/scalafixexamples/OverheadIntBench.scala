package examples.it.unich.scalafixexamples

import it.unich.scalafix.*
import it.unich.scalafix.finite.*
import it.unich.scalafix.utils.Relation
import it.unich.scalafix.assignments.InputAssignment

import org.openjdk.jmh.annotations.*

import scala.collection.mutable

@State(Scope.Benchmark)
@Warmup(iterations = 3)
class OverheadIntBench:

    inline def chainEquation(length: Int): Body[Int, Int] = 
        (rho: Int => Int) =>
            (u: Int) =>
                if u == 0
                then rho(length - 1) min limit
                else rho(u-1) + 1
    
    val length = 200
    val limit = 2_000_000
    val body = chainEquation(length)

    def validate(rho: Assignment[Int, Int]) = {
        for i <- 0 until length do
            assert(rho(i) == limit + i)
    }

    @Benchmark
    def scalafixWithoutCombos() = {
        val eqs = FiniteEquationSystem(body, Set(), 0 to length, Relation(Seq.empty[(Int, Int)]))
        val sol = RoundRobinSolver(eqs)(InputAssignment(0))
    }

    @Benchmark
    def scalafixWithCombos() = {
        val eqs = FiniteEquationSystem(body, Set(), 0 to length, Relation(Seq.empty[(Int, Int)]))
        val combo = Combo( { (x: Int, y: Int) => if x > y then x else y }, true )
        val combos = ComboAssignment(combo)
        val eqs2 = eqs.withCombos(combos)
        val sol = RoundRobinSolver(eqs2)(InputAssignment(0))
        validate(sol)
    }
 
    def hashMap(withCombos: Boolean, withInlineBody: Boolean) = {
        var rho = mutable.Map[Int, Int]().withDefaultValue(0)
        var dirty = true
        var i = 0
        while dirty do
            dirty = false
            i = 0
            while i < length do
                val v = rho(i)
                val vtmp = 
                    if withInlineBody
                    then 
                        if i == 0
                        then rho(length - 1) min limit
                        else rho(i-1) + 1
                    else
                        body(rho)(i)
                var vnew = 
                    if withCombos 
                    then 
                        if vtmp > v then vtmp else v
                    else 
                        vtmp
                if v != vnew then
                    rho(i) = vnew
                    dirty = true
                i += 1
    }

    @Benchmark
    def hashMapWithoutCombos() = hashMap(false, true)


    @Benchmark
    def hashMapWithCombos() = hashMap(true, true)

    def array(withCombos: Boolean, withInlineBody: Boolean) = {
        var rho = Array.fill(length)(0)
        var dirty = true
        var i = 0
        while dirty do
            dirty = false
            i = 0
            while i < length do
                val v = rho(i)
                val vtmp = 
                    if withInlineBody
                    then 
                        if i == 0
                        then rho(length - 1) min limit
                        else rho(i-1) + 1
                    else
                        body(rho)(i)
                var vnew = 
                    if withCombos 
                    then 
                        if vtmp > v then vtmp else v
                    else 
                        vtmp
                if v != vnew then
                    rho(i) = vnew
                    dirty = true
                i += 1
    }

    @Benchmark
    def arrayNotInlined() = array(false, false)

    @Benchmark
    def arrayInlined() = array(false, true)
    
end OverheadIntBench