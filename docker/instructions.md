# Instructions for the ScalaFix artifact

The artifact is made available as a Docker image `scalafix-examples.tgz`. Load this image in your
Docker environment with 

`docker load -i scalafix-examples.tgz`

Then, you can create a container and run it with 

`docker run -it scalafix-examples`

The image `scalafix-examples` contains an installation of Fedora 36 with Scalafix and all its
dependencies. Once started, the bash shell is executed as the user `scalafix` in the working directory
`/home/scalafix/ScalaFixExamples`. This directory contains the source code of the ScalaFix examples.
In particular, according to the standard convention in the Scala world, the source code is in `/home/scalafix/ScalaFixExamples/src/main/scala/it/unich/scalafixexamples`. Some of these examples are taken from the paper, others are new.

## Examples

The image comes with some examples on the use of ScalaFix which can be executed using `sbt` - Scala Build Tool - the standard build system for Scala.

* Launch the Scala Build Tool with the command `sbt`  from the directory `/home/scalafix/ScalaFixExamples` . You will see the SBT prompt `sbt:ScalaFixExamples>` from which you can give the command `run` .
* Choose the number of the example you want to run. These examples are not particularly significative without looking at the source code. It is possible to browse the source code with either the `emacs` or `vi` text editors, or directly from the GitHub repositories: [https://github.com/jandom-devel/ScalaFix](https://github.com/jandom-devel/ScalaFix) for  ScalaFix and [https://github.com/jandom-devel/ScalaFixExamples](https://github.com/jandom-devel/ScalaFixExamples) for ScalaFixExamples.

The following are the examples presented in the paper:

  * [FibonacciExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/FibonacciExample.scala): this finds the 6th Fibonacci number using an equation system with an infinite number of unkowns (see Section 2.1 in the paper).
  * [FibonacciFiniteExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/FibonacciFiniteExample.scala): this finds the 6th Fibonacci number using an equation system with an finite number of unkowns (see Section 2.2 in the paper).
  * [JPPLBoxExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/JPPLExample.scala): anaysis of a simple program with the box domain (see Section 2.3 in the paper).
  * [JPPLBoxWithWideningExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/JPPLExample.scala): like JPPLBoxExample but with the addition of widenings (see Section 3 in e paper).
  * [JPPLBoxWithWideningAutomaticExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/JPPLExample.scala): like JPPLBoxWithWideningExample but widening points are automatically computed by ScalaFix (see Section 3.1 in the paper).
  * [JPPLBoxGraphBasedExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/JPPLExample.scala): like JPPLBoxExample, but the equation system is given using the control flow hyper-graph (see Section 4 in the paper).
  * [JPPLBoxLocalizedExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/LocalizedExample.scala): analysis of a program with two nested loops using localized widening and narrowing (see Section 4.1 in the paper).
  * [JPPLBoxNotLocalizedExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/LocalizedExample.scala): like JPPLBoxLocalizedExample but using standard (non-localized) widening and narrowing (see Section 4.1 in the paper).
  * [JPPLBoxLocalizedSimpleAPIExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/LocalizedExample.scala): like JPPLBoxLocalizedExample but using the high-level API (see Section 5 in the paper).
  * [JPPLBoxWarrowingSimpleAPIExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/LocalizedExample.scala): like JPPLBoxLocalizedExample but using warrowing instead of widening + narrowing (see Section 5 in the paper).

The following are new examples not included in the paper:

  * [JPPLBoxNotLocalizedSimpleAPIExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/LocalizedExample.scala): like JPPLBoxLocalizedExampleSimpleAPIExample but using standard (non-localized) widening and narrowing.
  * [JPPLPolyhedron*](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/JPPLExample.scala): there are the same examples of the JPPLBox* series, but using the domain of closed   polyhedra instead of the domain of intervals.
  * [InfiniteESExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/InfiniteESExample.scala): a complex example of an infinite equation system solved using the worklist equation solver, taken from:
  [Gianluca Amato, Francesca Scozzari, Helmut Seidl, Kalmer Apinis, Vesal Vojdani. Efficiently intertwining widening and narrowing. Science of Computer Programming, Volume 120, 2016](https://doi.org/10.1016/j.scico.2015.12.005).
  * [InfiniteESWithPriorityExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/InfiniteESExample.scala): like InfiniteESExample but using the priority worklist solver.
  * [ReachingDefinitionsExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/ReachingDefinitionsExample.scala): an example of reaching definition analysis taken from the dragon book.
  * [ReachingDefinitionsGraphExample](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/ReachingDefinitionsExample.scala): an example of reaching definition analysis taken from the dragon book implemented using a graph-based equation system.

## Benchmarks

Benchmarks may be run from the SBT prompt with `Jmh/run` . Since this executes all the benchmarks, it takes a lot of time (about 45 minutes).
It is possible to execute a single benchmark with `Jmh/run <classname>` where `<classname>` may be:
 
  * [OverheadIntBench](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/OverheadIntBench.scala) Benchmarks the equation system (1) in Section 6 with different solver, usign both ScalaFix and ad-hoc solvers. It takes about 16 minutes.
  * [OverheadBoxBench](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/OverheadBoxBench.scala) Benchmarks the equation system (2) in Section 6 with different solver, using both ScalaFix and ad-hoc solvers.  It takes about 8 minutes.
  * [OverheadReachingDefsBench](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/OverheadReachingDefsBench.scala) Benchmakrs the equation system in the `ReachingDefinitionsExample` using both ScalaFix and ad-hoc solvers. It takes about 20 minutes.

## Modify the examples
You may modify the examples using either the `emacs` or `vi` text editors and run them. For instance, to modify the FibonacciExample you can edit the code, from the directory `/home/scalafix/ScalaFixExamples` with:

`emacs src/main/scala/it/unich/scalafixexamples/Fibonacci.scala`

Now you can modify the line of code:

`val sol = infinite.WorkListSolver(eqs)(Assignment(1), Set(6))`

which computes the 6th number of Fibonacci with:

`val sol = infinite.WorkListSolver(eqs)(Assignment(1), Set(42))`

to compute the 42nd number of Fibonacci. Then give the commands `sbt` and `run` and choose the example `it.unich.scalafixexamples.Fibonacci`.

## Source code

The directory `/home/scalafix/ScalaFix` contains a copy of the Scalafix source code. In particular:

  * `/home/scalafix/ScalaFix/core/src/main/scala/it/unich/scalafix/` contains the ScalaFix source code;
  * `/home/scalafix/ScalaFix/core/src/test/scala/it/unich/scalafix/` contains the unit tests;
  * `/home/scalafix/ScalaFix/bench/src/main/scala/it/unich/scalafix/jmh/` contains benchmarks.

## Other

From the `/home/scalafix/ScalaFix` directory you can run the ScalaFix unit tests with `sbt` and, from the SBT prompt `sbt:scalafix>` , give the command `test` (note that this SBT prompt is different from the previous one since you must be in the `/home/scalafix/ScalaFix` directory). From the same SBT prompt you can execute the benchmarks with `bench/Jmh/run` (it takes about 33 minutes).

The Dockerfile which generates the current image is available in:

`/home/scalafix/ScalaFixExamples/docker`.
