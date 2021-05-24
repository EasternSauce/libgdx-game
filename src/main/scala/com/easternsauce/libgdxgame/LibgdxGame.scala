package com.easternsauce.libgdxgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.util.Random

class LibgdxGame extends Game {

  var batch: EsBatch = _
  var manager: AssetManager = _

  override def create(): Unit = {
    batch = new EsBatch()
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
