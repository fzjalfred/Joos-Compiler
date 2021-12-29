scalaVersion := "2.13.3"

name := "CS444Compiler"

libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.6"

scalaSource in Compile := baseDirectory.value / "src"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}