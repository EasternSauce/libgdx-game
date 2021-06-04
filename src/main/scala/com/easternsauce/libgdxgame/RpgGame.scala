package com.easternsauce.libgdxgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.graphics.Texture
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.saving.SavefileManager
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.util.Random

class RpgGame extends Game {

  var worldBatch: EsBatch = _
  var hudBatch: EsBatch = _

  var savefileManager: SavefileManager = _

  override def create(): Unit = {
    worldBatch = new EsBatch()
    hudBatch = new EsBatch()
    RpgGame.manager = new AssetManager()

    savefileManager = new SavefileManager()

    AssetPaths.sounds.foreach(RpgGame.manager.load(_, classOf[Sound]))
    AssetPaths.textures.foreach(RpgGame.manager.load(_, classOf[Texture]))
    AssetPaths.music.foreach(RpgGame.manager.load(_, classOf[Music]))

    RpgGame.manager.finishLoading()

    setScreen(new PlayScreen(this))

  }

}

object RpgGame {
  val Random: Random = new Random()

  var manager: AssetManager = _

  val VWidth = 1650
  val VHeight = 864
  val PPM = 32

  val WindowWidth = 1360
  val WindowHeight = 720

  val equipmentTypes = Map(0 -> "weapon", 1 -> "helmet", 2 -> "body", 3 -> "gloves", 4 -> "ring", 5 -> "boots")
  val equipmentTypeIndices: Map[String, Int] = for ((k, v) <- equipmentTypes) yield (v, k)

  val TiledMapCellSize: Float = 2f
}
