package com.easternsauce.libgdxgame.ability.parameters

case class AnimationParameters(
  textureWidth: Int = 0,
  textureHeight: Int = 0,
  activeRegionName: String = "",
  channelRegionName: String = "",
  channelFrameCount: Int = 0,
  activeFrameCount: Int = 0
)
