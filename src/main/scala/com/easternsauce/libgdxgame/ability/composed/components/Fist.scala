package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

class Fist(
  val mainAbility: Ability,
  val startTime: Float,
  posX: Float,
  posY: Float,
  radius: Float,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(
    textureWidth = 40,
    textureHeight = 80,
    activeRegionName = "fist_slam",
    activeFrameCount = 5,
    channelRegionName = "fist_slam_windup",
    channelFrameCount = 5
  )
) extends AbilityComponent {

  override lazy val activeTime: Float = 0.2f
  override lazy val channelTime: Float = 0.4f

  override var state: AbilityState = AbilityState.Inactive
  override var started: Boolean = false
  override var body: Body = _
  override var destroyed: Boolean = false

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = Some(
    Animation.channelAnimationFromParameters(animationParameters, channelTime)
  )

  def start(): Unit = {
    started = true
    state = AbilityState.Channeling
    channelTimer.restart()
    timerParameters.abilityChannelAnimationTimer.restart()
  }

  override def onUpdateActive(): Unit = {
    if (started) {
      if (state == AbilityState.Channeling) {
        if (channelTimer.time > channelTime) {
          state = AbilityState.Active
          Assets.sound(Assets.glassBreakSound).play(0.1f)
          timerParameters.abilityActiveAnimationTimer.restart()
          activeTimer.restart()
          initBody(posX, posY)
        }
      }
      if (state == AbilityState.Active) {
        if (activeTimer.time > activeTime) {
          state = AbilityState.Inactive
        }
        if (!destroyed && activeTimer.time >= 0.2f) {
          body.getWorld.destroyBody(body)
          destroyed = true
        }
        if (activeTimer.time > activeTime) {
          // on active stop
          state = AbilityState.Inactive
        }
      }
    }
  }

  def initBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    body = mainAbility.creature.area.get.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)
  }

  override def render(batch: EsBatch): Unit = {

    if (state == AbilityState.Channeling) {

      val image = channelAnimation.get.currentFrame(time = timerParameters.channelTimer.time, loop = true)

      val scale = radius * 2f / image.getRegionWidth

      val shift = radius
      batch.spriteBatch.draw(
        image,
        posX - shift,
        posY - shift,
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

      val scale = radius * 2f / image.getRegionWidth

      val shift = radius

      batch.spriteBatch.draw(
        image,
        posX - shift,
        posY - shift,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0.0f
      )
    }
  }

  override def onCollideWithCreature(creature: Creature): Unit = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive && activeTimer.time < 0.15f) {
      if (!creature.isImmune) creature.takeLifeDamage(100f, immunityFrames = true)
    }
  }

}
