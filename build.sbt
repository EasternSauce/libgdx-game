name := "libgdx_game"

version := "0.1"

scalaVersion := "2.13.6"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.badlogicgames.gdx" % "gdx" % "1.9.14"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-box2d" % "1.9.14"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % "1.9.14"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-freetype" % "1.9.14"

libraryDependencies += "com.badlogicgames.gdx" % "gdx-platform" % "1.9.14" classifier "natives-desktop"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-box2d-platform" % "1.9.14" classifier "natives-desktop"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-freetype-platform" % "1.9.14" classifier "natives-desktop"

libraryDependencies += "space.earlygrey" % "shapedrawer" % "2.4.0"

libraryDependencies += "io.circe" % "circe-core_2.13" % "0.14.1"

libraryDependencies += "io.circe" %% "circe-parser" % "0.14.1"

libraryDependencies += "io.circe" %% "circe-generic" % "0.14.1"

scalacOptions += "-deprecation"
