package com.easternsauce.libgdxgame.launcher

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.easternsauce.libgdxgame.LibgdxGame

object DesktopApplication {

  def main(arg: Array[String]): Unit = {

    val config = new Lwjgl3ApplicationConfiguration
    config.setWindowedMode(LibgdxGame.WindowWidth, LibgdxGame.WindowHeight)
    new Lwjgl3Application(new LibgdxGame(), config)
  }
}
