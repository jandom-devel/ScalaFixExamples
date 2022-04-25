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
    def scalafixWithoutBoxes() = {
        val eqs = FiniteEquationSystem(body, Set(), 0 to length, Relation(Seq.empty[(Int, Int)]))
        val sol = RoundRobinSolver(eqs)(InputAssignment(0))
    }

    @Benchmark
    def scalafixWithBoxes() = {
        val eqs = FiniteEquationSystem(body, Set(), 0 to length, Relation(Seq.empty[(Int, Int)]))
        val box = Box( { (x: Int, y: Int) => if x > y then x else y }, true )
        val boxes = BoxAssignment(box)
        val eqs2 = eqs.withBoxes(boxes)
        val sol = RoundRobinSolver(eqs2)(InputAssignment(0))
        validate(sol)
    }
 
    def hashMap(withBoxes: Boolean, withInlineBody: Boolean) = {
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
                    if withBoxes 
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
    def hashMapWithoutBoxes() = hashMap(false, true)


    @Benchmark
    def hashMapWithBoxes() = hashMap(true, true)

    def array(withBoxes: Boolean, withInlineBody: Boolean) = {
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
                    if withBoxes 
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