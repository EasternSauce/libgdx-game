package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Polygon, Rectangle, Vector2}
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.ability.traits.{ActiveAnimation, Attack, PhysicalHitbox, WindupAnimation}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

trait MeleeAttack extends Attack with PhysicalHitbox with ActiveAnimation with WindupAnimation {
  var scale: Float
  var attackRange: Float
  var hitbox: Option[AttackHitbox] = None
  var toRemoveBody = false

  var bodyActive = false
  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected var aimed: Boolean
  protected var spriteWidth: Int
  protected var spriteHeight: Int
  protected def width: Float = spriteWidth.toFloat / PPM
  protected def height: Float = spriteHeight.toFloat / PPM
  protected var knockbackPower: Float
  override protected val isAttack = true

  override def onActiveStart(): Unit = {
    super.onActiveStart()

    abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(15f)

    if (abilitySound.nonEmpty) abilitySound.get.play(0.1f)

    var attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    if (attackVector.len() > 0f) {
      attackVector = new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
    }

    val attackShiftX = attackVector.x * attackRange
    val attackShiftY = attackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(scale, scale)

    hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    if (creature.area.nonEmpty) initHitboxBody(creature.area.get.world, hitbox.get)

    bodyActive = true

    toRemoveBody = false
  }

  override def onUpdateActive(): Unit = {
    super.onUpdateActive()

  }

  override def render(batch: EsBatch): Unit = {
    super.render(batch)

    def renderFrame(image: TextureRegion): Unit = {
      val attackVector = creature.attackVector
      val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

      if (hitbox.nonEmpty) {
        batch.spriteBatch.draw(
          image,
          hitbox.get.x,
          hitbox.get.y - height / 2,
          0,
          height / 2,
          width,
          height,
          scale,
          scale,
          theta
        )
      }
    }

    if (state == AbilityState.Channeling) renderFrame(currentWindupAnimationFrame)
    if (state == AbilityState.Active) renderFrame(currentActiveAnimationFrame)

  }

  override def onChannellingStart(): Unit = {
    creature.attackVector = creature.facingVector
    abilityWindupAnimationTimer.restart()
    creature.isAttacking = true

    var attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    if (attackVector.len() > 0f) {
      attackVector = new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
    }

    val attackShiftX = attackVector.x * attackRange
    val attackShiftY = attackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(scale, scale)

    hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

  }

  override def update(): Unit = {
    super.update()

    if (b2body.nonEmpty && toRemoveBody) {
      b2body.get.getWorld.destroyBody(b2body.get)
      toRemoveBody = false
      bodyActive = false
    }
  }

  override def updateHitbox(): Unit = {
    super.updateHitbox()

    if (hitbox.nonEmpty) {

      var attackVector = creature.attackVector

      if (attackVector.len() > 0f) {
        attackVector = new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      }

      val attackShiftX = attackVector.x * attackRange
      val attackShiftY = attackVector.y * attackRange

      hitbox.get.x = attackShiftX + creature.pos.x
      hitbox.get.y = attackShiftY + creature.pos.y

      if (bodyActive) {
        b2body.get.setTransform(hitbox.get.x, hitbox.get.y, 0f)
      }
    }

  }

  override def onStop() {
    super.onStop()

    creature.isAttacking = false

    if (state == AbilityState.Active)
      toRemoveBody = true // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash

  }

  override def onCollideWithCreature(otherCreature: Creature): Unit = {
    super.onCollideWithCreature(otherCreature)

    if (!(creature.isEnemy && otherCreature.isEnemy)) {
      if (creature != otherCreature && state == AbilityState.Active && !otherCreature.immune) {
        otherCreature.takeHealthDamage(
          damage = creature.weaponDamage,
          immunityFrames = true,
          dealtBy = Some(creature),
          knockbackPower = knockbackPower,
          sourceX = creature.pos.x,
          sourceY = creature.pos.y
        )
      }
    }
  }
}

case class AttackHitbox private (var x: Float, var y: Float, polygon: Polygon)
