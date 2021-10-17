package com.easternsauce.libgdxgame.animation

import com.badlogic.gdx.graphics.g2d.{TextureRegion, Animation => GdxAnimation, TextureAtlas => GdxTextureAtlas}
import com.easternsauce.libgdxgame.ability.misc.parameters.AnimationParameters
import com.easternsauce.libgdxgame.system.Assets

case class Animation(
  regionName: String,
  textureWidth: Int,
  textureHeight: Int,
  animationFrameCount: Int,
  frameDuration: Float
) {

  private val animation: GdxAnimation[TextureRegion] = {
    val spriteTextureRegion: GdxTextureAtlas.AtlasRegion = Assets.atlas.findRegion(regionName)

    val frames =
      for (i <- 0 until animationFrameCount)
        yield new TextureRegion(spriteTextureRegion, i * textureWidth, 0, textureWidth, textureHeight)

    new GdxAnimation[TextureRegion](frameDuration, frames: _*)

  }

  def currentFrame(time: Float, loop: Boolean): TextureRegion = {
    animation.getKeyFrame(time, loop)
  }

}

object Animation {
  def activeAnimationFromParameters(animationParameters: AnimationParameters, activeTime: Float): Animation = {
    Animation(
      regionName = animationParameters.activeRegionName,
      textureWidth = animationParameters.textureWidth,
      textureHeight = animationParameters.textureHeight,
      animationFrameCount = animationParameters.activeFrameCount,
      frameDuration = activeTime / animationParameters.activeFrameCount
    )
  }

  def channelAnimationFromParameters(animationParameters: AnimationParameters, channelTime: Float): Animation = {
    Animation(
      regionName = animationParameters.channelRegionName,
      textureWidth = animationParameters.textureWidth,
      textureHeight = animationParameters.textureHeight,
      animationFrameCount = animationParameters.channelFrameCount,
      frameDuration = channelTime / animationParameters.channelFrameCount
    )
  }
}
