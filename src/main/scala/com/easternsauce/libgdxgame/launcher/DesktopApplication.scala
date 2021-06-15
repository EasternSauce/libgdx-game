package com.easternsauce.libgdxgame.launcher

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.easternsauce.libgdxgame.GameSystem
import com.easternsauce.libgdxgame.GameSystem._

object DesktopApplication {

  def main(arg: Array[String]): Unit = {

    val config = new Lwjgl3ApplicationConfiguration
    config.setWindowedMode(WindowWidth, WindowHeight)
    config.setTitle("game")
    new Lwjgl3Application(GameSystem, config)
  }
}
