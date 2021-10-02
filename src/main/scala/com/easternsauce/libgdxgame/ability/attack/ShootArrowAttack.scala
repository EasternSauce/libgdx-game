package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{
  AnimationParameters,
  BodyParameters,
  SoundParameters,
  TimerParameters
}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class ShootArrowAttack private (
  override val creature: Creature,
  override val state: AbilityState = AbilityState.Inactive,
  override val onCooldown: Boolean = false,
  override val soundParameters: SoundParameters = SoundParameters(),
  override val timerParameters: TimerParameters = TimerParameters()
) extends Ability {
  type Self = ShootArrowAttack

  override val id: String = "shoot_arrow"

  override protected lazy val channelTime: Float = 0.85f
  override protected lazy val activeTime: Float = 0.1f
  override protected val cooldownTime = 0.8f

  override protected val isAttack = true

  override def onChannellingStart(): Self = {
    super.onChannellingStart()

    // TODO: remove side effects
    creature.isAttacking = true

    Assets.sound(Assets.bowPullSound).play(0.1f)

    this
  }

  override def onActiveStart(): Self = {
    val result = super.onActiveStart().asInstanceOf[Self]

    // TODO: remove side effects

    Assets.sound(Assets.bowReleaseSound).play(0.1f)

    creature.attackVector = creature.facingVector.cpy()

    val arrowList: ListBuffer[Arrow] = creature.area.get.arrowList
    val tiles: TiledMap = creature.area.get.map
    val areaCreatures: mutable.Map[String, Creature] =
      creature.area.get.creaturesMap

    if (!creature.facingVector.equals(new Vector2(0.0f, 0.0f))) {
      val arrowStartX = creature.pos.x
      val arrowStartY = creature.pos.y

      val arrow: Arrow = Arrow(
        arrowStartX,
        arrowStartY,
        creature.area.get,
        creature.facingVector,
        arrowList,
        tiles,
        areaCreatures,
        this.creature
      )
      arrowList += arrow
    }

    creature.takeStaminaDamage(20f)

    result
  }

  override def copy(
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    state: AbilityState,
    onCooldown: Boolean,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    copy(state = state, onCooldown = onCooldown, soundParameters = soundParameters, timerParameters = timerParameters)

}
