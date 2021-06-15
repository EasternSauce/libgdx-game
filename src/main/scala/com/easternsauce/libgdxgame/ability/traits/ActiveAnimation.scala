package com.easternsauce.libgdxgame.ability.traits

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsTimer

import scala.collection.mutable.ListBuffer

trait ActiveAnimation {
  protected var abilityActiveAnimation: Animation[TextureRegion] = _
  protected val abilityActiveAnimationTimer: EsTimer = EsTimer()

  def setupActiveAnimation(
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

    abilityActiveAnimation = new Animation[TextureRegion](frameDuration, frames.toArray: _*)

  }

  def currentActiveAnimationFrame: TextureRegion = {
    abilityActiveAnimation.getKeyFrame(abilityActiveAnimationTimer.time, false)
  }
}
