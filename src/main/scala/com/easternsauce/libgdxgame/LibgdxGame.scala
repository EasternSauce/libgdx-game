package com.easternsauce.libgdxgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.util.Random

class LibgdxGame extends Game {

  var worldBatch: EsBatch = _
  var hudBatch: EsBatch = _

  override def create(): Unit = {
    worldBatch = new EsBatch()
    hudBatch = new EsBatch()
    LibgdxGame.manager = new AssetManager()

    LibgdxGame.manager.load(AssetPaths.downArrowTexture, classOf[Texture])

    LibgdxGame.manager.finishLoading()

    setScreen(new PlayScreen(this))

  }
}

object LibgdxGame {
  val Random: Random = new Random()

  var manager: AssetManager = _

  val VWidth = 1650
  val VHeight = 864
  val PPM = 32

  val WindowWidth = 1360
  val WindowHeight = 720

  val equipmentTypes = Map(0 -> "weapon", 1 -> "helmet", 2 -> "body", 3 -> "gloves", 4 -> "ring", 5 -> "boots")
  val equipmentTypeIndices = for ((k, v) <- equipmentTypes) yield (v, k)

}
