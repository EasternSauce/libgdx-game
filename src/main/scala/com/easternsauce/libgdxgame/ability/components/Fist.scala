package com.easternsauce.libgdxgame.ability.components

import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.AbilityState
import com.easternsauce.libgdxgame.ability.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

class Fist(val mainAbility: Ability, val startTime: Float, posX: Float, posY: Float, radius: Float)
    extends AbilityComponent {

  override protected val activeTime: Float = 0.2f
  override protected val channelTime: Float = 0.4f

  override var state: AbilityState = AbilityState.Inactive
  override var started: Boolean = false
  override var body: Body = _
  override var destroyed: Boolean = false

  val activeSpriteWidth = 40
  val activeSpriteHeight = 80
  val windupSpriteWidth = 40
  val windupSpriteHeight = 40
  val numOfFrames = 5

  setupActiveAnimation(
    regionName = "fist_slam",
    textureWidth = activeSpriteWidth,
    textureHeight = activeSpriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = activeTime / numOfFrames
  )

  setupWindupAnimation(
    regionName = "fist_slam_windup",
    textureWidth = windupSpriteWidth,
    textureHeight = windupSpriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = channelTime / numOfFrames
  )

  def start(): Unit = {
    started = true
    state = AbilityState.Channeling
    channelTimer.restart()
    abilityWindupAnimationTimer.restart()
  }

  override def onUpdateActive(): Unit = {
    if (started) {
      if (state == AbilityState.Channeling) {
        if (channelTimer.time > channelTime) {
          state = AbilityState.Active
          Assets.sound(Assets.glassBreakSound).play(0.1f)
          abilityActiveAnimationTimer.restart()
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
      val image = currentWindupAnimationFrame

      println("windup height: " + image.getRegionHeight)

      val scale = radius * 2f / image.getRegionWidth

      val shift = radius
      batch.spriteBatch.draw(
        image,
        posX - shift,
        posY - shift,
        0,
        0,
        image.getRegionWidth,
        image.getRegionHeight,
        scale,
        scale,
        0.0f
      )
    }
    if (state == AbilityState.Active) {

      val image = currentActiveAnimationFrame

      val scale = radius * 2f / image.getRegionWidth

      val shift = radius

      batch.spriteBatch.draw(
        image,
        posX - shift,
        posY - shift,
        0,
        0,
        image.getRegionWidth,
        image.getRegionHeight,
        scale,
        scale,
        0.0f
      )
    }
  }

  override def onCollideWithCreature(creature: Creature): Unit = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.alive && activeTimer.time < 0.15f) {
      if (!creature.immune) creature.takeLifeDamage(50f, immunityFrames = true)
    }
  }

}