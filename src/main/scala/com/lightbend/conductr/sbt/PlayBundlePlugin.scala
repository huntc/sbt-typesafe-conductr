package com.lightbend.conductr.sbt

import sbt._
import sbt.Keys._

import scala.util.{ Failure, Success }

object PlayBundlePlugin extends AutoPlugin {

  import PlayBundleImport._
  import BundlePlugin.autoImport._

  val autoImport = PlayBundleImport

  private val classLoader = this.getClass.getClassLoader

  override def requires =
    withContextClassloader(classLoader) { loader =>
      val isPlayProject = (Reflection.getSingletonObject[Plugins.Basic](classLoader, "play.sbt.Play$") orElse Reflection.getSingletonObject[Plugins.Basic](classLoader, "play.Play$")).isSuccess
      if (isPlayProject) BundlePlugin else NoOpPlugin
    }

  override def trigger = allRequirements

  override def projectSettings =
    Seq(
      BundleKeys.nrOfCpus := PlayBundleKeyDefaults.nrOfCpus,
      BundleKeys.memory := PlayBundleKeyDefaults.memory,
      BundleKeys.diskSpace := PlayBundleKeyDefaults.diskSpace,
      conductrBundleLibVersion := Version.conductrBundleLib,
      libraryDependencies += Library.playConductrBundleLib(PlayVersion.current, scalaBinaryVersion.value, conductrBundleLibVersion.value),
      resolvers += Resolver.typesafeBintrayReleases
    )
}

/**
 * Mirrors the LagomVersion class of `play.core.PlayVersion`
 * By declaring the public methods from Lagom it is possible to "safely"
 * call the class via reflection.
 */
private object PlayVersion {

  import scala.language.reflectiveCalls

  val classLoader = this.getClass.getClassLoader

  // The method signature equals the signature of `play.core.PlayVersion`
  type PlayVersion = {
    def current: String
  }

  val current: String =
    withContextClassloader(classLoader) { loader =>
      Reflection.getSingletonObject[PlayVersion](loader, "play.core.PlayVersion$") match {
        case Failure(t)           => sys.error(s"The PlayVersion class can not be resolved. Error: ${t.getMessage}")
        case Success(playVersion) => playVersion.current
      }
    }
}