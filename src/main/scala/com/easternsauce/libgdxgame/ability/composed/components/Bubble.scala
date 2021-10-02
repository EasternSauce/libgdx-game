package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState, Modification}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, ComponentParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

case class Bubble(
  mainAbility: Ability,
  override val componentParameters: ComponentParameters = ComponentParameters(),
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters =
    AnimationParameters(textureWidth = 64, textureHeight = 64, activeRegionName = "bubble", activeFrameCount = 2),
  override val state: AbilityState = AbilityState.Inactive,
  override val started: Boolean = false,
  override val body: Option[Body] = None,
  override val destroyed: Boolean = false,
  override val dirVector: Vector2 = new Vector2(1, 0)
) extends AbilityComponent
    with Modification {
  type Self = Bubble

  override lazy val activeTime: Float = 1.5f
  override lazy val channelTime: Float = 0.6f

  val loopTime = 0.2f

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = None

  def start(): Bubble = {
    channelTimer.restart()

    this
      .modify(_.started)
      .setTo(true)
      .modify(_.state)
      .setTo(AbilityState.Channeling)
  }

  override def onUpdateActive(): Bubble = {
    modifyIf(started) {
      state match {
        case AbilityState.Channeling =>
          if (channelTimer.time > channelTime) {
            onActiveStart()
          } else this
        case AbilityState.Active =>
          //TODO: use this: person.modify(_.address.street.name).setToIf(shouldChangeAddress)("3 00 Ln.")
          val component: Bubble =
            this
              .modifyIf(!destroyed && activeTimer.time >= activeTime) {
                body.get.getWorld.destroyBody(body.get)
                this.modify(_.destroyed).setTo(true)
              }
              .modifyIf(activeTimer.time > activeTime) {
                // on active stop
                this.modify(_.state).setTo(AbilityState.Inactive)
              }

          if (!destroyed) {
            body.get.setLinearVelocity(dirVector.x * componentParameters.speed, dirVector.y * componentParameters.speed)
          }

          component

        case _ => this
      }
    }

  }

  private def onActiveStart(): Bubble = {
    //Assets.sound(Assets.explosionSound).play(0.01f)

    timerParameters.abilityActiveAnimationTimer.restart()

    activeTimer.restart()

    val body = initBody(componentParameters.startX, componentParameters.startX)
    val dirVector = if (mainAbility.creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
      mainAbility.creature.facingVector.cpy
    } else {
      new Vector2(1.0f, 0.0f)
    }

    this
      .modify(_.state)
      .setTo(AbilityState.Active)
      .modify(_.body)
      .setTo(body)
      .modify(_.dirVector)
      .setTo(dirVector)
  }

  def initBody(x: Float, y: Float): Option[Body] = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body = Some(mainAbility.creature.area.get.world.createBody(bodyDef))
    body.get.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(componentParameters.radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.get.createFixture(fixtureDef)

    body
  }

  override def render(batch: EsBatch): Bubble = {
    if (state == AbilityState.Active) {
      val spriteWidth = 64
      val scale = componentParameters.radius * 2 / spriteWidth
      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)
      batch.spriteBatch.draw(
        image,
        body.get.getPosition.x - componentParameters.radius,
        body.get.getPosition.y - componentParameters.radius,
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

  override def onCollideWithCreature(creature: Creature): Bubble = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }

    this
  }

}
