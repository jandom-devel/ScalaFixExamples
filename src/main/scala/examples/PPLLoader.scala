package examples

import scala.sys.process.Process

import parma_polyhedra_library.Parma_Polyhedra_Library

object PPLLoader {

  def apply() {
    try {
      System.loadLibrary("ppl_java")
    } catch {
      case _: UnsatisfiedLinkError =>
        try {
          val path = Process("ppl-config -l").lazyLines.head
          System.load(path + "/ppl/libppl_java.so")
        } catch {
          case _: Throwable =>
        }

    }
  }
}
