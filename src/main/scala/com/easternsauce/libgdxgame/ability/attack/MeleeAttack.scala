package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Polygon, Rectangle, Vector2}
import com.easternsauce.libgdxgame.ability.misc._
import com.easternsauce.libgdxgame.ability.parameters.AbilityParameters
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

trait MeleeAttack extends Ability with PhysicalHitbox with ActiveAnimation with WindupAnimation {
  val attackRange: Float
  val hitbox: Option[AttackHitbox]
  val toRemoveBody: Boolean = false

  val bodyActive = false
  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected val aimed: Boolean
  protected val spriteWidth: Int
  protected val spriteHeight: Int
  protected def width: Float = spriteWidth.toFloat / Constants.PPM
  protected def height: Float = spriteHeight.toFloat / Constants.PPM
  protected val knockbackVelocity: Float
  override protected val isAttack = true

  protected val baseChannelTime: Float
  protected val baseActiveTime: Float

  override protected lazy val activeTime: Float = baseActiveTime
  override protected lazy val channelTime: Float = baseChannelTime / attackSpeed

  def attackSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackSpeed.get
    else 1.4f

  def attackScale: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackScale.get
    else 1.4f

  override def onActiveStart(): AbilityParameters = {
    super.onActiveStart()

    timerParameters.abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(15f)

    val attackVector = creature.attackVector

    val normalizedAttackVector =
      if (attackVector.len() > 0f) {
        new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      } else attackVector

    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    val attackShiftX = normalizedAttackVector.x * attackRange
    val attackShiftY = normalizedAttackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(attackScale, attackScale)

    val hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    if (creature.area.nonEmpty) initHitboxBody(creature.area.get.world, hitbox.get)

    AbilityParameters(hitbox = Some(hitbox), toRemoveBody = Some(false), bodyActive = Some(true))
  }

  override def render(batch: EsBatch): AbilityParameters = {
    // TODO: remove side effect
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

    AbilityParameters()

  }

  override def onChannellingStart(): AbilityParameters = {
    super.onChannellingStart()

    creature.attackVector = creature.facingVector.cpy()
    timerParameters.abilityWindupAnimationTimer.restart()
    creature.isAttacking = true

    val attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    val normalizedAttackVector =
      if (attackVector.len() > 0f) {
        new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      } else attackVector

    val attackShiftX = normalizedAttackVector.x * attackRange
    val attackShiftY = normalizedAttackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(attackScale, attackScale)

    val hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    AbilityParameters(hitbox = Some(hitbox))

  }

  override def update(): AbilityParameters = {
    val params = super.update()

    if (b2Body.nonEmpty && toRemoveBody) {
      b2Body.get.getWorld.destroyBody(b2Body.get)

      params.copy(toRemoveBody = Some(false), bodyActive = Some(false))
    } else
      params
  }

  override def updateHitbox(): AbilityParameters = {
    if (hitbox.nonEmpty) {

      val attackVector = creature.attackVector

      val normalizedAttackVector =
        if (attackVector.len() > 0f) {
          new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
        } else attackVector

      val attackShiftX = normalizedAttackVector.x * attackRange
      val attackShiftY = normalizedAttackVector.y * attackRange

      val newHitbox = Some(
        AttackHitbox(attackShiftX + creature.pos.x, attackShiftY + creature.pos.y, hitbox.get.polygon)
      )

      // TODO: remove sideeffect
      if (bodyActive) {
        b2Body.get.setTransform(hitbox.get.x, hitbox.get.y, 0f)
      }

      AbilityParameters(hitbox = Some(newHitbox))
    } else
      AbilityParameters()

  }

  override def onStop(): AbilityParameters = {
    creature.isAttacking = false

    // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash
    if (state == AbilityState.Active)
      AbilityParameters(toRemoveBody = Some(true))
    else
      AbilityParameters()
  }

  override def onCollideWithCreature(otherCreature: Creature): AbilityParameters = {

    println("collision!!!")
    // TODO: remove sideeffect
    if (!(creature.isEnemy && otherCreature.isEnemy)) {
      println("colliding, state = " + state)
      if (
        creature != otherCreature
        //&& state == AbilityState.Active TODO: ??? collision box created before ability is active?
        && !otherCreature.isImmune
      ) {
        println("inside")
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
          otherCreature.activateEffect("poisoned", 10f)
          otherCreature.poisonTickTimer.restart()
        }
      }
    }

    AbilityParameters()
  }
}

case class AttackHitbox private (x: Float, y: Float, polygon: Polygon)
