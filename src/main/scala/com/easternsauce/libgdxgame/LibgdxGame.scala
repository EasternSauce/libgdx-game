package com.easternsauce.libgdxgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.util.Random

class LibgdxGame extends Game {

  var batch: EsBatch = _

  override def create(): Unit = {
    batch = new EsBatch()
    LibgdxGame.manager = new AssetManager()

    LibgdxGame.manager.load(AssetPaths.downArrowTexture, classOf[Texture])

    LibgdxGame.manager.finishLoading()

    setScreen(new PlayScreen(this))

  }
}

object LibgdxGame {
  val Random: Random = new Random()

  var manager: AssetManager = _

  val VWidth = 1536
  val VHeight = 864
  val PPM = 32
}
