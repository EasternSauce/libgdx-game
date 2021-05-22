package com.easternsauce.libgdxgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.easternsauce.libgdxgame.screens.PlayScreen

import scala.util.Random

class LibgdxGame extends Game {

  var batch: SpriteBatch = _
  var manager: AssetManager = _

  override def create(): Unit = {
    batch = new SpriteBatch()
    manager = new AssetManager()

    // load

    manager.finishLoading()

    setScreen(new PlayScreen(this))

  }
}

object LibgdxGame {
  val Random: Random = new Random()

  val VWidth = 1536
  val VHeight = 864
  val PPM = 32
}
