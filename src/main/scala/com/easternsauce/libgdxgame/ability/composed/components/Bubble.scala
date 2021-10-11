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
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

case class Bubble(
  override val creatureId: String,
  override val mainAbility: Ability,
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters =
    AnimationParameters(textureWidth = 64, textureHeight = 64, activeRegionName = "bubble", activeFrameCount = 2),
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
  type Self = Bubble

  override lazy val activeTime: Float = 1.5f
  override lazy val channelTime: Float = 0.6f

  val loopTime = 0.2f

  // TODO: workaround: change user data when ability is modified
  if (bodyParameters.b2Body.nonEmpty && bodyParameters.b2Body.get != null) {
    bodyParameters.b2Body.get.setUserData(this)
  }

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = None

  def start(): Self = {
    timerParameters.channelTimer.restart()

    this
      .modify(_.started)
      .setTo(true)
      .modify(_.state)
      .setTo(AbilityState.Channeling)
  }

  override def onUpdateActive(creature: Creature): Self = {
    val component0: Bubble = this
    val component1: Bubble = if (component0.started) {
      component0.state match {
        case AbilityState.Channeling =>
          val component1_1: Bubble = if (component0.timerParameters.channelTimer.time > component0.channelTime) {
            component0.onActiveStart(creature)
          } else component0
          component1_1
        case AbilityState.Active =>
          val component1_2: Bubble =
            if (
              component0.bodyParameters.b2Body.nonEmpty && component0.timerParameters.activeTimer.time >= component0.activeTime
            ) {
              component0.bodyParameters.b2Body.get.getWorld.destroyBody(component0.bodyParameters.b2Body.get)
              component0
                .modify(_.bodyParameters.b2Body)
                .setTo(None)
            } else component0
          val component1_3: Bubble = if (component1_2.timerParameters.activeTimer.time > component1_2.activeTime) {
            // on active stop
            component1_2
              .modify(_.state)
              .setTo(AbilityState.Inactive)
          } else component1_2

          val component1_4: Bubble = if (component1_3.bodyParameters.b2Body.nonEmpty) {
            component1_3.bodyParameters.b2Body.get
              .setLinearVelocity(
                component1_3.dirVector.x * component1_3.componentParameters.speed,
                component1_3.dirVector.y * component1_3.componentParameters.speed
              )
            component1_3
          } else component1_3

          component1_4

        case _ => component0
      }
    } else component0

    component1
  }

  private def onActiveStart(creature: Creature): Self = {
    //Assets.sound(Assets.explosionSound).play(0.01f)

    timerParameters.abilityActiveAnimationTimer.restart()

    timerParameters.activeTimer.restart()

    val body = initBody(creature, componentParameters.startX, componentParameters.startY)
    val dirVector = if (creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
      creature.facingVector.cpy
    } else {
      new Vector2(1.0f, 0.0f)
    }

    this
      .modify(_.state)
      .setTo(AbilityState.Active)
      .modify(_.bodyParameters.b2Body)
      .setTo(body)
      .modify(_.dirVector)
      .setTo(dirVector)
  }

  def initBody(creature: Creature, x: Float, y: Float): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body = Some(creature.area.get.world.createBody(bodyDef))
    body.get.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(componentParameters.radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.get.createFixture(fixtureDef)

    body
  }

  override def render(creature: Creature, batch: EsBatch): Self = {
    if (state == AbilityState.Active) {
      val spriteWidth = 64
      val scale = componentParameters.radius * 2 / spriteWidth
      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)

      batch.spriteBatch.draw(
        image,
        bodyParameters.b2Body.get.getPosition.x - componentParameters.radius,
        bodyParameters.b2Body.get.getPosition.y - componentParameters.radius,
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
    if (!(creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }

    this
  }

}
