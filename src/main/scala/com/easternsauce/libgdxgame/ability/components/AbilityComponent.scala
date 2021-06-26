package com.easternsauce.libgdxgame.ability.components

import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.traits.{ActiveAnimation, WindupAnimation}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait AbilityComponent extends ActiveAnimation with WindupAnimation {

  protected val activeTimer: EsTimer = EsTimer()
  protected val channelTimer: EsTimer = EsTimer()
  protected val activeTime: Float
  protected val channelTime: Float

  var state: AbilityState
  var started: Boolean
  var body: Body
  var destroyed: Boolean

  def onUpdateActive(): Unit

  def render(batch: EsBatch): Unit

  def onCollideWithCreature(creature: Creature): Unit

}
