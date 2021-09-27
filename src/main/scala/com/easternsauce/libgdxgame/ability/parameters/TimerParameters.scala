package com.easternsauce.libgdxgame.ability.parameters

import com.easternsauce.libgdxgame.util.EsTimer

case class TimerParameters(
  activeTimer: EsTimer = EsTimer(),
  channelTimer: EsTimer = EsTimer(),
  abilityChannelAnimationTimer: EsTimer = EsTimer(),
  abilityActiveAnimationTimer: EsTimer = EsTimer()
)
