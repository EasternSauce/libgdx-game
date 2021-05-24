package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.easternsauce.libgdxgame.creature.Creature

class SwordAttack(val creature: Creature) extends MeleeAttack {
  override var scale: Float = _
  override var attackRange: Float = _
  override protected var aimed: Boolean = _
  override protected var width: Float = _
  override protected var height: Float = _
  override protected var knockbackPower: Float = _
  override protected val cooldownTime: Float = 0f
  override protected val activeTime: Float = 0f
  override protected val channelTime: Float = 0f
  override protected var abilityActiveAnimation: Animation[TextureRegion] = _
  override protected var abilityWindupAnimation: Animation[TextureRegion] = _
}
