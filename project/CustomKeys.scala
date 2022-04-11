import sbt._

object CustomKeys {
  val pplJar = settingKey[Option[String]]("Location of the PPL library")
}

