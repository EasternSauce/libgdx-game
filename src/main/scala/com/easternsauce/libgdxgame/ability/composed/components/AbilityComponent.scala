package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

trait AbilityComponent {

  protected val activeTimer: EsTimer = EsTimer()
  protected val channelTimer: EsTimer = EsTimer()
  val activeTime: Float
  val channelTime: Float
  val startTime: Float

  var state: AbilityState
  var started: Boolean
  var body: Body
  var destroyed: Boolean

  def onUpdateActive(): Unit

  def render(batch: EsBatch): Unit

  def onCollideWithCreature(creature: Creature): Unit

  def start(): Unit

  def totalTime: Float = startTime + channelTime + activeTime + 0.05f // with buffer

}
