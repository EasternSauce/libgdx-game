package com.easternsauce.libgdxgame.ability.misc

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.ability.parameters.TimerParameters
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable.ListBuffer

trait ActiveAnimation {
  protected var abilityActiveAnimation: Animation[TextureRegion] = _

  val timerParameters: TimerParameters

  var loop = false

  def setupActiveAnimation(
    regionName: String,
    textureWidth: Int,
    textureHeight: Int,
    animationFrameCount: Int,
    frameDuration: Float,
    loop: Boolean = false
  ): Unit = {
    val frames = new ListBuffer[TextureRegion]()

    val spriteTextureRegion: TextureAtlas.AtlasRegion = Assets.atlas.findRegion(regionName)

    for (i <- 0 until animationFrameCount) {
      frames += new TextureRegion(spriteTextureRegion, i * textureWidth, 0, textureWidth, textureHeight)
    }

    abilityActiveAnimation = new Animation[TextureRegion](frameDuration, frames.toArray: _*)

    this.loop = loop

  }

  def currentActiveAnimationFrame: TextureRegion = {
    abilityActiveAnimation.getKeyFrame(timerParameters.abilityActiveAnimationTimer.time, loop)
  }
}
