# ScalaFixExamples

This is a set of examples for the [ScalaFix](https://github.com/jandom-devel/ScalaFix) library.

## Installing dependencies

[ScalaFixExamples](https://github.com/jandom-devel/ScalaFixExamples) depends on:
   * [ScalaFix](https://github.com/jandom-devel/ScalaFix), a fixpoint engine;
   * [JPPL](https://github.com/jandom-devel/JPPL), Java bindings for the [Parma Polyhedra Library](http://bugseng.com/products/ppl/) (`PPL`);
   * [JGMP](https://github.com/jandom-devel/JGMP), Java bindings for the [GNU Multiple Precision Arithmetic Library](https://gmplib.org/) (`GMP`);

These dependencies are automatically fetched from the Maven repositories by the build tool. Unfortunately, both `JPPL` and `JGMP` depends on native C libraries. If these libraries are missing, any attempt to execute an example or a benchmark using the `PPL` will fail. Instructions for installing these libraries are system dependent.

### Linux

The simplest way to install `PPL` and `GMP` is through the packages available in most of the standard Linux distributions. In particular:
  * Debian and derivatives, such as Ubuntu: install the `libppl-c4` package;
  * Fedora: install the `ppl` package;
  * Arch Linux: install the `ppl` package.

In all these cases, GMP is also automatically installed.

If the PPL is not available as a package, you can download the source code and compile the C language interface. However, the shared library `libppl_c.so` should be installed in one of the system directories for shared libraries, otherwise the JVM might not find it.

## Executing examples and benchmarks

In order to compile and execute the examples, you need the [Scala Build Tool](https://www.scala-sbt.org/) (SBT) installed in your computer. Then, launching the `sbt` command inside the root directory of the repository (the folder containing the `build.sbt` file) will execute the interactive build tool where you can:

  * execute benchmarks with the command `Jmh/run` . Since this executes all the benchmarks, it takes a lot of time (about 45 minutes). It is possible to execute a single benchmark with `Jmh/run <classname>` where `<classname>` may be:
    * [OverheadIntBench](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/OverheadIntBench.scala) Benchmarks the equation system (1) in Section 6 with different solver, usign both ScalaFix and ad-hoc solvers. It takes about 16 minutes.
    * [OverheadBoxBench](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/OverheadBoxBench.scala) Benchmarks the equation system (2) in Section 6 with different solver, using both ScalaFix and ad-hoc solvers.  It takes about 8 minutes.
    * [OverheadReachingDefsBench](https://github.com/jandom-devel/ScalaFixExamples/blob/master/src/main/scala/it/unich/scalafixexamples/OverheadReachingDefsBench.scala) Benchmakrs the equation system in the `ReachingDefinitionsExample` using both ScalaFix and ad-hoc solvers. It takes about 20 minutes.
  * execute examples by giving the command `run ` and choosing the number of the example you want.

## Docker

In the `docker` directory you may find the recipes to build a docker image with `ScalaFixExamples` and the relevant libraries. Just execute `docker-compose up` inside the docker directory in a system where Docker and `docker-compose` are installed. Note that, due to [this issue](https://github.com/moby/moby/commit/9f6b562dd12ef7b1f9e2f8e6f2ab6477790a6594), a recent version of docker is required (20.10.16 and laters should be fine).

## Accessing ScalaFix source code

The source code of `ScalaFix` is not automatically fetched by the build tool. If you want to explore the code, you may either download or browse it interactively from the [ScalaFix](https://github.com/jandom-devel/ScalaFix) git repository. The source code of ScalaFix is organized as follows:

  * `core/src/main/scala/it/unich/scalafix/` contains the ScalaFix source code;
  * `core/src/test/scala/it/unich/scalafix/` contains the unit tests;
  * `bench/src/main/scala/it/unich/scalafix/jmh/` contains benchmarks.

From the ScalaFix root directory you can launch `sbt` and:
  * execute the unit tests with the command `test`;
  * execute benchmarks (they are different from the benchmarks in `ScalaFixExample`) with the command `bench/Jmh/run` (it takes about 33 minutes).
