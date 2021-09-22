package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.ability.parameters.TimerParameters
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable.ListBuffer

trait WindupAnimation {
  protected var abilityWindupAnimation: Animation[TextureRegion] = _

  val timerParameters: TimerParameters

  def setupWindupAnimation(
    regionName: String,
    textureWidth: Int,
    textureHeight: Int,
    animationFrameCount: Int,
    frameDuration: Float
  ): Unit = {
    val frames = new ListBuffer[TextureRegion]()

    val spriteTextureRegion: TextureAtlas.AtlasRegion = Assets.atlas.findRegion(regionName)

    for (i <- 0 until animationFrameCount) {
      frames += new TextureRegion(spriteTextureRegion, i * textureWidth, 0, textureWidth, textureHeight)
    }

    abilityWindupAnimation = new Animation[TextureRegion](frameDuration, frames.toArray: _*)

  }

  def currentWindupAnimationFrame: TextureRegion = {
    abilityWindupAnimation.getKeyFrame(timerParameters.abilityWindupAnimationTimer.time, false)
  }
}
