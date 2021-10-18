package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.misc.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.parameters.{AnimationParameters, BodyParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.ability.misc.templates.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.system.GameSystem.areaMap
import com.easternsauce.libgdxgame.util.AbilityInfo

import scala.collection.mutable.ListBuffer

case class ShootArrowAttack private (
  override val creatureId: String,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
  override val dirVector: Vector2 = new Vector2(0f, 0f)
) extends Ability(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      timerParameters = timerParameters,
      soundParameters = soundParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    ) {
  type Self = ShootArrowAttack

  override val id: String = ShootArrowAttack.id

  override protected lazy val channelTime: Float = 0.85f
  override protected lazy val activeTime: Float = 0.1f
  override protected val cooldownTime = 0.8f

  override protected val isAttack = true

  override def onChannellingStart(): Self = {
    super.onChannellingStart()

    // TODO: remove side effects
    modifyCreature(creature => { creature.isAttacking = true; creature })

    Assets.sound(Assets.bowPullSound).play(0.1f)

    this
  }

  override def onActiveStart(): Self = {
    val result = super.onActiveStart().asInstanceOf[Self]

    // TODO: remove side effects

    Assets.sound(Assets.bowReleaseSound).play(0.1f)

    modifyCreature(creature => { creature.attackVector = creature.facingVector.cpy(); creature })

    val area = areaMap(creature.params.areaId.get)
    val arrowList: ListBuffer[Arrow] = area.arrowList
    val tiles: TiledMap = area.map
    val areaCreatures: List[Creature] =
      area.creaturesMap

    if (!creature.facingVector.equals(new Vector2(0.0f, 0.0f))) {
      val arrowStartX = creature.pos.x
      val arrowStartY = creature.pos.y

      val arrow: Arrow = Arrow(arrowStartX, arrowStartY, area, creature.facingVector, arrowList, tiles, this.creature)
      arrowList += arrow
    }

    modifyCreature(creature => { creature.takeStaminaDamage(20f); creature })

    result
  }

  override def copy(
    creatureId: String,
    state: AbilityState,
    onCooldown: Boolean,
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    ShootArrowAttack(
      creatureId = creatureId,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    )
}

object ShootArrowAttack extends AbilityInfo {
  override val id = "shoot_arrow"
}
