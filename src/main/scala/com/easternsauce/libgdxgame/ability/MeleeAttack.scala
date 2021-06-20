package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Polygon, Rectangle, Vector2}
import com.easternsauce.libgdxgame.ability.traits.{ActiveAnimation, Attack, PhysicalHitbox, WindupAnimation}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

trait MeleeAttack extends Attack with PhysicalHitbox with ActiveAnimation with WindupAnimation {
  var attackRange: Float
  var hitbox: Option[AttackHitbox] = None
  var toRemoveBody = false

  var bodyActive = false
  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected var aimed: Boolean
  protected var spriteWidth: Int
  protected var spriteHeight: Int
  protected def width: Float = spriteWidth.toFloat / Constants.PPM
  protected def height: Float = spriteHeight.toFloat / Constants.PPM
  protected var knockbackVelocity: Float
  override protected val isAttack = true

  protected val baseChannelTime: Float
  protected val baseActiveTime: Float

  override protected def activeTime: Float = baseActiveTime
  override protected def channelTime: Float = baseChannelTime / attackSpeed

  def attackSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackSpeed.get
    else 1.4f

  def attackScale: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackScale.get
    else 1.4f

  override def onActiveStart(): Unit = {
    super.onActiveStart()

    abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(15f)

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
    poly.setScale(attackScale, attackScale)

    hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    if (creature.area.nonEmpty) initHitboxBody(creature.area.get.world, hitbox.get)

    bodyActive = true

    toRemoveBody = false
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
          attackScale,
          attackScale,
          theta
        )
      }
    }

    if (state == AbilityState.Channeling) renderFrame(currentWindupAnimationFrame)
    if (state == AbilityState.Active) renderFrame(currentActiveAnimationFrame)

  }

  override def onChannellingStart(): Unit = {
    super.onChannellingStart()

    creature.attackVector = creature.facingVector.cpy()
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
    poly.setScale(attackScale, attackScale)

    hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

  }

  override def update(): Unit = {
    super.update()

    if (b2Body.nonEmpty && toRemoveBody) {
      b2Body.get.getWorld.destroyBody(b2Body.get)
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
        b2Body.get.setTransform(hitbox.get.x, hitbox.get.y, 0f)
      }
    }

  }

  override def onStop(): Unit = {
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
        otherCreature.takeLifeDamage(
          damage = creature.weaponDamage,
          immunityFrames = true,
          dealtBy = Some(creature),
          attackKnockbackVelocity = knockbackVelocity,
          sourceX = creature.pos.x,
          sourceY = creature.pos.y
        )
        val random = GameSystem.randomGenerator.nextFloat()

        if (creature.isWeaponEquipped && random < creature.currentWeapon.template.poisonChance.get) {
          otherCreature.effect("poisoned").applyEffect(10f)
          otherCreature.poisonTickTimer.restart()
        }
      }
    }
  }
}

case class AttackHitbox private (var x: Float, var y: Float, polygon: Polygon)
