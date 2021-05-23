package com.easternsauce.libgdxgame.creatures

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait Animated {
  protected val standStillImages: Array[TextureRegion] = new Array[TextureRegion](4)

  protected val walkAnimation: Array[Animation[TextureRegion]] = new Array[Animation[TextureRegion]](4)

  val animationTimer: EsTimer = EsTimer()

  var dirMap: Map[EsDirection.Value, Int] = _

  def setupTextures(
    atlas: TextureAtlas,
    regionName: String,
    textureWidth: Int,
    textureHeight: Int,
    animationFrameCount: Int,
    frameDuration: Float,
    dirMap: Map[EsDirection.Value, Int]
  ): Unit = {
    this.dirMap = dirMap

    val frames = new ListBuffer[TextureRegion]()

    val spriteTextureRegion: TextureAtlas.AtlasRegion = atlas.findRegion(regionName)

    for (i <- 0 until 4) {
      standStillImages(i) =
        new TextureRegion(spriteTextureRegion, 1 * textureWidth, i * textureHeight, textureWidth, textureHeight)
    }

    for (i <- 0 until 4) {
      for (j <- 0 until animationFrameCount) {
        frames += new TextureRegion(
          spriteTextureRegion,
          j * textureWidth,
          i * textureHeight,
          textureWidth,
          textureHeight
        )
      }
      walkAnimation(i) = new Animation[TextureRegion](frameDuration, frames.toArray: _*)
      frames.clear()
    }
  }

  def walkAnimationFrame(currentDirection: EsDirection.Value): TextureRegion = {
    walkAnimation(dirMap(currentDirection)).getKeyFrame(animationTimer.time, true)
  }

  def standStillImage(currentDirection: EsDirection.Value): TextureRegion = {
    standStillImages(dirMap(currentDirection))
  }

}
