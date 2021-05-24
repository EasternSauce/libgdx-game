package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.util.EsTimer

import scala.collection.mutable.ListBuffer

trait AbilityActiveAnimation {
  protected var abilityActiveAnimation: Animation[TextureRegion]
  protected val abilityActiveAnimationTimer: EsTimer = EsTimer()

  def setupActiveAnimation(
    atlas: TextureAtlas,
    regionName: String,
    textureWidth: Int,
    textureHeight: Int,
    animationFrameCount: Int,
    frameDuration: Float
  ): Unit = {
    val frames = new ListBuffer[TextureRegion]()

    val spriteTextureRegion: TextureAtlas.AtlasRegion = atlas.findRegion(regionName)

    for (i <- 0 until animationFrameCount) {
      frames += new TextureRegion(spriteTextureRegion, i * textureWidth, textureHeight, textureWidth, textureHeight)
    }

    abilityActiveAnimation = new Animation[TextureRegion](frameDuration, frames.toArray: _*)

  }

  def currentActiveAnimationFrame: TextureRegion = {
    abilityActiveAnimation.getKeyFrame(abilityActiveAnimationTimer.time, false)
  }
}
