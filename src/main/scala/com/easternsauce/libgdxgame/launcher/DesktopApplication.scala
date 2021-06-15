package com.easternsauce.libgdxgame.launcher

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}

object DesktopApplication {

  def main(arg: Array[String]): Unit = {

    val config = new Lwjgl3ApplicationConfiguration
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.setTitle("game")
    new Lwjgl3Application(GameSystem, config)
  }
}
