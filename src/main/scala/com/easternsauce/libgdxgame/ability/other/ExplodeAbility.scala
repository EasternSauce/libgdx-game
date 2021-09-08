package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState, ActiveAnimation}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

class ExplodeAbility private (val creature: Creature) extends Ability with ActiveAnimation {
  val id: String = "explode"
  protected val cooldownTime: Float = 0.8f

  protected var explosionRange: Float = 10f

  val spriteWidth: Int = 64
  val spriteHeight: Int = 64
  val numOfFrames: Int = 21

  override protected val isStoppable: Boolean = false

  var b2Body: Body = _
  var bodyCreated = false

  override protected def activeTime: Float = 0.9f

  override protected def channelTime: Float = 1.3f

  setupActiveAnimation(
    regionName = "explosion",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = activeTime / numOfFrames
  )

  override protected def onActiveStart(): Unit = {
    super.onActiveStart()

    abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(25f)
    creature.takeLifeDamage(700f, immunityFrames = false, Some(creature), 0, 0, 0)
    Assets.sound(Assets.explosionSound).play(0.07f)

    initBody(creature.pos.x, creature.pos.y)
    bodyCreated = true
  }

  override protected def onUpdateActive(): Unit = {
    if (bodyCreated && activeTimer.time > 0.1f) {
      destroyBody(creature.area.get.world)
    }
  }

  override def render(batch: EsBatch): Unit = {
    super.render(batch)

    def renderFrame(image: TextureRegion): Unit = {

      val scale = explosionRange * 2 / image.getRegionWidth

      val scaledWidth = explosionRange * 2
      val scaledHeight = explosionRange * 2

      batch.spriteBatch.draw(
        image,
        creature.pos.x - scaledWidth / 2f,
        creature.pos.y - scaledHeight / 2f,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0f
      )

    }

    if (state == AbilityState.Active) {
      renderFrame(currentActiveAnimationFrame)
    }

  }

  def initBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    b2Body = creature.area.get.world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(explosionRange)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2Body.createFixture(fixtureDef)
  }

  def destroyBody(world: World): Unit = {
    if (bodyCreated) {
      world.destroyBody(b2Body)
      bodyCreated = false
    }
  }

  override def onCollideWithCreature(otherCreature: Creature): Unit = {
    super.onCollideWithCreature(creature)

    if (!(creature.isEnemy && otherCreature.isEnemy) && otherCreature.isAlive) { // mob can't hurt a mob?
      if (!otherCreature.isImmune) otherCreature.takeLifeDamage(700f, immunityFrames = true, Some(creature), 0, 0, 0)
    }
  }

}

object ExplodeAbility {
  def apply(abilityCreature: Creature): ExplodeAbility = {
    new ExplodeAbility(abilityCreature)
  }
}
