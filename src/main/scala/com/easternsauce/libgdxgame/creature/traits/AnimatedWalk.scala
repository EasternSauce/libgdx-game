package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable.ListBuffer

trait AnimatedWalk {
  this: Creature =>

  var standStillImages: Array[TextureRegion] = new Array[TextureRegion](4)

  var walkAnimation: Array[Animation[TextureRegion]] = new Array[Animation[TextureRegion]](4)

  var animationTimer: EsTimer = EsTimer()

  var dirMap: Map[EsDirection.Value, Int] = _

  var animationParams: AnimationParams

  protected def setupAnimation(): Creature = {
    dirMap = animationParams.dirMap

    val frames = new ListBuffer[TextureRegion]()

    val spriteTextureRegion: TextureAtlas.AtlasRegion = Assets.atlas.findRegion(animationParams.regionName)

    for (i <- 0 until 4) {
      standStillImages(i) = new TextureRegion(
        spriteTextureRegion,
        animationParams.neutralStanceFrame * animationParams.textureWidth,
        i * animationParams.textureHeight,
        animationParams.textureWidth,
        animationParams.textureHeight
      )
    }

    for (i <- 0 until 4) {
      for (j <- 0 until animationParams.animationFrameCount) {
        frames += new TextureRegion(
          spriteTextureRegion,
          j * animationParams.textureWidth,
          i * animationParams.textureHeight,
          animationParams.textureWidth,
          animationParams.textureHeight
        )
      }
      walkAnimation(i) = new Animation[TextureRegion](animationParams.frameDuration, frames.toArray: _*)
      frames.clear()
    }

    this
  }

  def walkAnimationFrame(currentDirection: EsDirection.Value): TextureRegion = {
    walkAnimation(dirMap(currentDirection)).getKeyFrame(animationTimer.time, true)
  }

  def standStillImage(currentDirection: EsDirection.Value): TextureRegion = {
    standStillImages(dirMap(currentDirection))
  }

}

case class AnimationParams(
  regionName: String,
  textureWidth: Int,
  textureHeight: Int,
  animationFrameCount: Int,
  frameDuration: Float,
  neutralStanceFrame: Int,
  dirMap: Map[EsDirection.Value, Int]
)
