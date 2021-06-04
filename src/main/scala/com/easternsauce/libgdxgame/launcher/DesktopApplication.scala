package com.easternsauce.libgdxgame.launcher

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.easternsauce.libgdxgame.RpgGame

object DesktopApplication {

  def main(arg: Array[String]): Unit = {

    val config = new Lwjgl3ApplicationConfiguration
    config.setWindowedMode(RpgGame.WindowWidth, RpgGame.WindowHeight)
    new Lwjgl3Application(new RpgGame(), config)
  }
}
