package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState, ActiveAnimation}
import com.easternsauce.libgdxgame.ability.parameters.{SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

case class ExplodeAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  body: Option[Body] = None,
  bodyCreated: Boolean = false
) extends Ability(creature = creature, state = state, onCooldown = onCooldown, timerParameters = timerParameters)
    with ActiveAnimation {
  override val id: String = "explode"
  override protected val cooldownTime: Float = 0.8f

  protected val explosionRange: Float = 10f

  val spriteWidth: Int = 64
  val spriteHeight: Int = 64
  val numOfFrames: Int = 21

  override protected val isStoppable: Boolean = false

  override protected lazy val activeTime: Float = 0.9f

  override protected lazy val channelTime: Float = 1.3f

  setupActiveAnimation(
    regionName = "explosion",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = activeTime / numOfFrames
  )

  override def onActiveStart(): ExplodeAbility = {
    super.onActiveStart()

    timerParameters.abilityActiveAnimationTimer.restart()

    // TODO: side effects
    creature.takeStaminaDamage(25f)
    creature.takeLifeDamage(700f, immunityFrames = false, Some(creature), 0, 0, 0)
    Assets.sound(Assets.explosionSound).play(0.07f)

    initBody(creature.pos.x, creature.pos.y)
  }

  override def onUpdateActive(): ExplodeAbility = {
    val activeTimer = timerParameters.activeTimer

    if (bodyCreated && activeTimer.time > 0.1f) {
      destroyBody(creature.area.get.world)
    } else
      copy()
  }

  override def render(batch: EsBatch): ExplodeAbility = {
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

    // TODO: remove side effect
    if (state == AbilityState.Active) {
      renderFrame(currentActiveAnimationFrame)
    }

    copy()
  }

  def initBody(x: Float, y: Float): ExplodeAbility = {
    // TODO: side effects

    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val b2Body = creature.area.get.world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(explosionRange)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2Body.createFixture(fixtureDef)

    copy(body = Some(b2Body), bodyCreated = true)
  }

  def destroyBody(world: World): ExplodeAbility = {
    if (bodyCreated) {
      // TODO: sideeffect?
      if (body.nonEmpty) world.destroyBody(body.get)
      copy(bodyCreated = false) // TODO: change to bodyDestroyed = true?
    } else
      copy()
  }

  override def onCollideWithCreature(otherCreature: Creature): ExplodeAbility = {
    // TODO: side effect
    if (!(creature.isEnemy && otherCreature.isEnemy) && otherCreature.isAlive) { // mob can't hurt a mob?
      if (!otherCreature.isImmune) otherCreature.takeLifeDamage(700f, immunityFrames = true, Some(creature), 0, 0, 0)
    }

    copy()
  }

}
