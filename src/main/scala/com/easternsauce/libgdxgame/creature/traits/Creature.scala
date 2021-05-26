package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.ability.SwordAttack
import com.easternsauce.libgdxgame.ability.traits.{Ability, Attack}
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.effect.Effect
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.{EsBatch, EsDirection, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait Creature extends Sprite with PhysicalBody with AnimatedEntity {

  val screen: PlayScreen

  val isEnemy: Boolean = false
  val isPlayer: Boolean = false

  val id: String

  val creatureWidth: Float
  val creatureHeight: Float

  var currentDirection: EsDirection.Value = EsDirection.Down

  var isWalkAnimationActive = false
  val timeSinceMovedTimer: EsTimer = EsTimer()

  val initX: Float
  val initY: Float

  val directionalSpeed = 30f

  var area: Option[Area] = None

  var maxHealthPoints = 100f
  var healthPoints: Float = maxHealthPoints
  val maxStaminaPoints = 100f
  var staminaPoints: Float = maxStaminaPoints

  var isAttacking = false

  val totalArmor = 100f // TODO
  val knockbackable = false // TODO
  var knockbackVector = new Vector2() //TODO

  var onGettingHitSound: Sound = _ // TODO

  val weaponDamage = 50f // TODO

  var attackVector: Vector2 = new Vector2(0f, 0f)

  var facingVector: Vector2 = new Vector2(0f, 0f)

  var staminaOveruse = false
  val staminaOveruseTimer: EsTimer = EsTimer()

  var abilityList: mutable.ListBuffer[Ability] = ListBuffer()
  var attackList: mutable.ListBuffer[Attack] = ListBuffer()

  protected val effectMap: mutable.Map[String, Effect] = mutable.Map()

  var swordAttack: SwordAttack = _

  def pos: Vector2 = b2Body.getPosition

  def isImmune: Boolean = effect("immune").isActive

  def alive: Boolean = {
    true // TODO
  }

  def initParams(mass: Float): Unit = {
    this.mass = mass
  }

  def onUpdateStart(): Unit = {
//    isMoving = false TODO
//
//    totalDirections = 0
//
//    knockbackSpeed = knockbackPower * Gdx.graphics.getDeltaTime
//
//    movingDir.x = 0
//    movingDir.y = 0
//
//    currentMaxVelocity = this.baseSpeed
//
//    if (isAttacking) currentMaxVelocity = currentMaxVelocity / 2
//    else if (sprinting && staminaPoints > 0) {
//      currentMaxVelocity = currentMaxVelocity * 1.75f
//      staminaDrain += Gdx.graphics.getDeltaTime
//    }
  }

  def update(): Unit = {
//    if (isAlive) {
//      onUpdateStart()

//      performActions()
//
//      controlMovement()
//      processMovement()
//
    setFacingDirection()
//
//      regenerate()
//    }

    for (effect <- effectMap.values) {
      effect.update()
    }

    for (ability <- abilityList) {
      ability.update()
    }

    currentAttack.update()

//    if (staminaDrain >= 0.3f) {
//      takeStaminaDamage(11f)
//
//      staminaDrain = 0.0f
//    }
//
//    if (
//      GameSystem.cameraFocussedCreature.nonEmpty
//        && this == GameSystem.cameraFocussedCreature.get
//    ) {
//      GameSystem.adjustCamera(this)
//    }
//
//    if (toSetBodyNonInteractive) {
//      fixture.setSensor(true)
//      body.setType(BodyDef.BodyType.StaticBody)
//      toSetBodyNonInteractive = false
//    }

    if (isWalkAnimationActive) setRegion(walkAnimationFrame(currentDirection))
    else setRegion(standStillImage(currentDirection))

    setPosition(pos.x - getWidth / 2f, pos.y - getHeight / 2f)

    if (isWalkAnimationActive && timeSinceMovedTimer.time > 0.25f) isWalkAnimationActive = false
  }

  def setFacingDirection(): Unit = {}

  def defineStandardAbilities(): Unit = {
    swordAttack = new SwordAttack(this)

    attackList += swordAttack
  }

  def currentAttack: Ability = {
//    if (equipmentItems.contains(0)) {
//      equipmentItems(0).itemType.attackType match {
//        case Sword   => swordAttack
//        case Bow     => bowAttack
//        case Trident => tridentAttack
//        case _       => throw new RuntimeException("Unrecognized attack type")
//      }
//    } else {
//      unarmedAttack
//    } TODO
    swordAttack
  }

  protected def defineEffects(): Unit = {
    effectMap.put("immune", Effect())
    effectMap.put("immobilized", Effect())
    effectMap.put("staminaRegenStopped", Effect())
    effectMap.put("poisoned", Effect())
  }

  def onDeath(): Unit = {
    isWalkAnimationActive = false

    for (ability <- abilityList) {
      ability.forceStop()
    }
    currentAttack.forceStop()
  }

  def takeHealthDamage(
    damage: Float,
    immunityFrames: Boolean,
    knockbackPower: Float = 0,
    sourceX: Float = 0,
    sourceY: Float = 0
  ): Unit = {
    if (alive) {
      val beforeHP = healthPoints

      val actualDamage = damage * 100f / (100f + totalArmor)

      if (healthPoints - actualDamage > 0) healthPoints -= actualDamage
      else healthPoints = 0f

      if (beforeHP != healthPoints && healthPoints == 0f) onDeath()

      if (immunityFrames) { // immunity frames on hit
        effect("immune").applyEffect(0.75f)
        // stagger on hit
        effect("immobilized").applyEffect(0.35f)
      }

      if (knockbackable) {
        knockbackVector = new Vector2(pos.x - sourceX, pos.y - sourceY).nor()

        b2Body.applyLinearImpulse(
          new Vector2(knockbackVector.x * knockbackPower, knockbackVector.y * knockbackPower),
          b2Body.getWorldCenter,
          true
        )
      }

      onGettingHitSound.play(0.1f)
    }
  }

  def takeStaminaDamage(staminaDamage: Float): Unit = {
    if (staminaPoints - staminaDamage > 0) staminaPoints -= staminaDamage
    else {
      staminaPoints = 0f
      staminaOveruse = true
      staminaOveruseTimer.restart()
    }
  }

  def moveInDirection(dirs: List[EsDirection.Value]): Unit = {

    timeSinceMovedTimer.restart()

    if (!isWalkAnimationActive) {
      animationTimer.restart()
      isWalkAnimationActive = true
    }

    val vector = new Vector2()

    currentDirection = dirs.last

    dirs.foreach {
      case EsDirection.Up    => vector.y += directionalSpeed
      case EsDirection.Down  => vector.y -= directionalSpeed
      case EsDirection.Left  => vector.x -= directionalSpeed
      case EsDirection.Right => vector.x += directionalSpeed
    }

    val horizontalCount = dirs.count(EsDirection.isHorizontal)
    val verticalCount = dirs.count(EsDirection.isVertical)

    if (horizontalCount < 2 && verticalCount < 2) {
      if (horizontalCount > 0 && verticalCount > 0) {
        sustainVelocity(new Vector2(vector.x / Math.sqrt(2).toFloat, vector.y / Math.sqrt(2).toFloat))
      } else {
        sustainVelocity(vector)
      }
    }
  }

  def assignToArea(area: Area): Unit = {
    if (this.area.isEmpty) {
      this.area = Some(area)
      initCircularBody(area.world, initX, initY, creatureWidth / 2f)

      area.creatureMap += (id -> this)
    } else {
      val oldArea = this.area.get

      oldArea.world.destroyBody(b2Body)
      oldArea.creatureMap -= id

      this.area = Some(area)
      initCircularBody(area.world, initX, initY, creatureWidth / 2f)

      area.creatureMap += (id -> this)
    }

  }

  def effect(effectName: String): Effect = {
    effectMap.get(effectName) match {
      case Some(effect) => effect
      case _ =>
        throw new RuntimeException("tried to access non-existing effect: " + effectName)
    }
  }

  def renderAbilities(batch: EsBatch): Unit = {
    for (ability <- abilityList) {
      ability.render(batch)
    }
    currentAttack.render(batch)
  }
}
