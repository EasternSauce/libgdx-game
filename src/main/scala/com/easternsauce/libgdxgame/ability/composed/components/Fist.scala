package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{
  AnimationParameters,
  BodyParameters,
  ComponentParameters,
  TimerParameters
}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens.ModifyPimp

case class Fist(
  override val creatureId: String,
  override val mainAbility: Ability,
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(
    textureWidth = 40,
    textureHeight = 80,
    activeRegionName = "fist_slam",
    activeFrameCount = 5,
    channelRegionName = "fist_slam_windup",
    channelFrameCount = 5
  ),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val dirVector: Vector2 = new Vector2(0, 0)
) extends AbilityComponent(
      creatureId = creatureId,
      mainAbility = mainAbility,
      state = state,
      started = started,
      componentParameters = componentParameters,
      timerParameters = timerParameters,
      animationParameters = animationParameters,
      bodyParameters = bodyParameters,
      dirVector = dirVector
    ) {
  type Self = Fist

  override lazy val activeTime: Float = 0.2f
  override lazy val channelTime: Float = 0.4f

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = Some(
    Animation.channelAnimationFromParameters(animationParameters, channelTime)
  )

  def start(): Self = {

    timerParameters.channelTimer.restart()
    timerParameters.abilityChannelAnimationTimer.restart()

    this
      .modify(_.started)
      .setTo(true)
      .modify(_.state)
      .setTo(AbilityState.Channeling)
  }

  override def onUpdateActive(creature: Creature): Self = {
    //TODO: refactor
    this
//    modifyIf(started) {
//      state match {
//        case AbilityState.Channeling =>
//          modifyIf(timerParameters.channelTimer.time > channelTime) {
//
//            Assets.sound(Assets.glassBreakSound).play(0.1f)
//            timerParameters.abilityActiveAnimationTimer.restart()
//            timerParameters.activeTimer.restart()
//            val body = initBody(componentParameters.startX, componentParameters.startY)
//
//            this
//              .modify(_.state)
//              .setTo(AbilityState.Active)
//              .modify(_.bodyParameters.body)
//              .setTo(body)
//          }
//        case AbilityState.Active =>
//          this
//            .modifyIf(timerParameters.activeTimer.time > activeTime) {
//              this
//                .modify(_.state)
//                .setTo(AbilityState.Inactive)
//            }
//            .modifyIf(!bodyParameters.destroyed && timerParameters.activeTimer.time >= 0.2f) {
//              bodyParameters.body.get.getWorld.destroyBody(bodyParameters.body.get)
//              this
//                .modify(_.bodyParameters.destroyed)
//                .setTo(true)
//                .modify(_.bodyParameters.body)
//                .setTo(Some(null))
//            }
//            .modifyIf(timerParameters.activeTimer.time > activeTime) {
//              // on active stop
//              this
//                .modify(_.state)
//                .setTo(AbilityState.Inactive)
//            }
//        case _ => this
//      }
//    }
  }

  def initBody(creature: Creature, x: Float, y: Float): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val body = creature.area.get.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(componentParameters.radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)

    Some(body)
  }

  override def render(creature: Creature, batch: EsBatch): Self = {

    if (state == AbilityState.Channeling) {

      val image = channelAnimation.get.currentFrame(time = timerParameters.channelTimer.time, loop = true)

      val scale = componentParameters.radius * 2f / image.getRegionWidth

      val shift = componentParameters.radius
      batch.spriteBatch.draw(
        image,
        componentParameters.startX - shift,
        componentParameters.startY - shift,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0.0f
      )
    }
    if (state == AbilityState.Active) {

      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)

      val scale = componentParameters.radius * 2f / image.getRegionWidth

      val shift = componentParameters.radius

      batch.spriteBatch.draw(
        image,
        componentParameters.startX - shift,
        componentParameters.startY - shift,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0.0f
      )
    }

    this
  }

  override def onCollideWithCreature(creature: Creature): Self = {
    if (!(creature.isEnemy && creature.isEnemy) && creature.isAlive && timerParameters.activeTimer.time < 0.15f) {
      if (!creature.isImmune) creature.takeLifeDamage(100f, immunityFrames = true)
    }

    this
  }

}
