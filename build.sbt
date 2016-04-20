name := "Sengab-backend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

libraryDependencies += "com.couchbase.client" % "java-client" % "2.2.6"

libraryDependencies += "io.reactivex" %% "rxscala" % "0.26.0"

libraryDependencies += "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"

// test
libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.13"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
