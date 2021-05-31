package com.easternsauce.libgdxgame.ability.traits

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.util.EsTimer

import scala.collection.mutable.ListBuffer

trait WindupAnimation {
  protected var abilityWindupAnimation: Animation[TextureRegion] = _
  protected val abilityWindupAnimationTimer: EsTimer = EsTimer()

  def setupWindupAnimation(
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
      frames += new TextureRegion(spriteTextureRegion, i * textureWidth, 0, textureWidth, textureHeight)
    }

    abilityWindupAnimation = new Animation[TextureRegion](frameDuration, frames.toArray: _*)

  }

  def currentWindupAnimationFrame: TextureRegion = {
    abilityWindupAnimation.getKeyFrame(abilityWindupAnimationTimer.time, false)
  }
}
