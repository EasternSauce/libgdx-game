package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AnimatedWalk {
  this: Creature =>

  protected val standStillImages: Array[TextureRegion] = new Array[TextureRegion](4)

  protected val walkAnimation: Array[Animation[TextureRegion]] = new Array[Animation[TextureRegion]](4)

  val animationTimer: EsTimer = EsTimer()

  var dirMap: Map[EsDirection.Value, Int] = _

  def setupAnimation(
    regionName: String,
    textureWidth: Int,
    textureHeight: Int,
    animationFrameCount: Int,
    frameDuration: Float,
    neutralStanceFrame: Int,
    dirMap: Map[EsDirection.Value, Int]
  ): Unit = {
    this.dirMap = dirMap

    val frames = new ListBuffer[TextureRegion]()

    val spriteTextureRegion: TextureAtlas.AtlasRegion = Assets.atlas.findRegion(regionName)

    for (i <- 0 until 4) {
      standStillImages(i) = new TextureRegion(
        spriteTextureRegion,
        neutralStanceFrame * textureWidth,
        i * textureHeight,
        textureWidth,
        textureHeight
      )
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
