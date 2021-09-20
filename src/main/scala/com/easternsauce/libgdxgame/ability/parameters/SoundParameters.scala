package com.easternsauce.libgdxgame.ability.parameters

import com.badlogic.gdx.audio.Sound

case class SoundParameters(
  activeSound: Option[Sound] = None,
  activeSoundVolume: Option[Float] = None,
  channelSound: Option[Sound] = None,
  channelSoundVolume: Option[Float] = None
)
