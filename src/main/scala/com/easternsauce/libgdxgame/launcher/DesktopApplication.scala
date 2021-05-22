package com.easternsauce.libgdxgame.launcher

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.easternsauce.libgdxgame.LibgdxGame

object DesktopApplication {

  val windowWidth: Int = 1280
  val windowHeight: Int = 720

  def main(arg: Array[String]): Unit = {

    val config = new Lwjgl3ApplicationConfiguration
    config.setWindowedMode(windowWidth, windowHeight)
    new Lwjgl3Application(new LibgdxGame(), config)
  }
}
