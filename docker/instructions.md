# Instructions for the ScalaFix artifacts

The artifact is made available as a Docker image `scalafix-examples.tgz`. Load this image in your
Docker environment with `docker load scalafix-examples.tgz`. Then, you can create a container and 
run it with `docker run -it scalafix-examples`.

The image `scalafix-examples` contains an installation of Fedora 36 with Scalafix and all its dependencies. Once started, the bash shell is executed as the user `scalafix` in the working directory
`/home/scalafix/ScalafixExamples`. This directory contains the source code of the ScalaFix examples. Some of these are taken from the paper, others are new. The directory `/home/scalafix/Scalafix`, instead, contain a copy of the Scalafix source code.

## Examples

The image comes with a few examples on the use of ScalaFix:

* Launch the examples with the command `sbt run` (`sbt` is the Scala Build Tool, the standard build system for Scala).
* Choose the number of the example you want to run. These examples are not particularly significative without looking at the source code. It is possible to browse the source code with either the `emacs` or `vi` text editors, or directly from the GitHub repositories: https://github.com/jandom-devel/ScalaFix for  ScalaFix and https://github.com/jandom-devel/ScalaFixExamples for ScalaFixExamples.

The following are the examples presented in the paper

  * [Fibonacci](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): this finds the 6th Fibonacci number using an equation system with an infinite number of unkowns (see Section 2.1 in the paper)
  * [FibonacciFinite](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): this finds the 6th Fibonacci number using an equation system with an finite number of unkowns (see Section 2.2 in the paper)
  * [JPPLBoxExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): anaysis of a simple program with the box domain (see Section 2.3 in the paper)
  * [JPPLBoxWithWideningExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxExample but with the addition of widenings (see Section 3 in e paper)
  * [JPPLBoxWithWideningAutomaticExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxWithWideningExample but widening points are automatically computed by ScalaFix (see Section 3.1 in the paper)
  * [JPPLBoxGraphBasedExampleExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxExample, but the equation system is given using the control flow hyper-graph (see Section 4 in the paper)
  * [JPPLBoxLocalizedExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): analysis of a program with two nested loops using localized widening and narrowing (see Section 4.1 in the paper)
  * [JPPLBoxNotLocalizedExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxLocalizedExample but using standard (non-localized) widening and narrowing (see Section 4.1 in the paper)
  * [JPPLBoxLocalizedExampleSimpleAPIExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxLocalizedExample but using the high-level API (see Section 5 in the paper)
  * [JPPLBoxWarrowingSimpleAPIExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxLocalizedExample but using warrowing instead of widening + narrowing (see Section 5 in the paper)

The following are new examples not included in the paper:

  * [JPPLBoxNotLocalizedSimpleAPIExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): like JPPLBoxLocalizedExampleSimpleAPIExample but using standard (non-localized) widening + narrowing
  * [JPPLPolyhedron*](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/): there are the same examples of the JPPLBox* series, but using the domain of closed   polyhedra instead of the domain of intervals.
  * [InfiniteESExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): a complex example of an infinite equation system taken from ... solved using the worklist equation solver.
  * [InfiniteESWithPriorityExample]: like InfiniteESExample but using the priority worklist solver.
  * [ReachingDefinitionsExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): an example of reaching definition analysis taken from the dragon book.
  * [ReachingDefinitionsExampleGraph](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/Fibonacci.scala): an example of reaching definition analysis taken from the dragon book implemented using a graph-based equation system.

## Benchmarks

Benchmarks may be run with `sbt jmh:run`. Since this executes all the benchmarks, it takes a lot of time.
It is possible to execute the single benchmark with `sbt jmh:run <classname>` where `<classname>` may be:
 
  * BodyBench: it 
  * BodyBenchAllObject: it 
  * BodyBenchObject
  * OverheadBoxBench:
  * OverheadGraphBench:
  * OverheadIntBench:
  * OverheadReachingDefsBench:
  