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

libraryDependencies += "com.couchbase.client" % "java-client" % "2.2.5"

libraryDependencies += "io.reactivex" %% "rxscala" % "0.26.0"

libraryDependencies += "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

//wartremoverErrors := Warts.allBut(Wart.Any)
//
//// Play Framework
//wartremoverWarnings ++= Seq(
//  PlayWart.CookiesPartial,
//  PlayWart.FlashPartial,
//  PlayWart.FormPartial,
//  PlayWart.HeadersPartial,
//  PlayWart.JsLookupResultPartial,
//  PlayWart.JsReadablePartial,
//  PlayWart.LangObject,
//  PlayWart.MessagesObject,
//  PlayWart.PlayGlobalExecutionContext,
//  PlayWart.SessionPartial)
//
//// Bonus Warts
//wartremoverWarnings ++= Seq(
//  PlayWart.DateFormatPartial,
//  PlayWart.FutureObject,
//  PlayWart.GenMapLikePartial,
//  PlayWart.GenTraversableLikeOps,
//  PlayWart.GenTraversableOnceOps,
//  PlayWart.OptionPartial,
//  PlayWart.ScalaGlobalExecutionContext,
//  PlayWart.StringOpsPartial,
//  PlayWart.TraversableOnceOps)
//
//wartremoverExcluded += crossTarget.value / "routes" / "main" / "controllers" / "ReverseRoutes.scala"
//wartremoverExcluded += crossTarget.value / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala"
//wartremoverExcluded += crossTarget.value / "routes" / "main" / "router" / "Routes.scala"
//wartremoverExcluded += crossTarget.value / "routes" / "main" / "router" / "RoutesPrefix.scala"
