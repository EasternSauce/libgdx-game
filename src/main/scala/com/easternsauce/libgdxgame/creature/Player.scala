package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.other.DashAbility
import com.easternsauce.libgdxgame.ability.parameters.SoundParameters
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.system.{Assets, GameSystem}
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}
import com.softwaremill.quicklens.ModifyPimp

case class Player(
  override val id: String,
  override val area: Option[Area],
  override val b2Body: Option[Body],
  override val standardAbilities: Map[String, Ability],
  override val additionalAbilities: Map[String, Ability]
) extends Creature(id = id, area = area, b2Body = b2Body) {
  override type Self = Player

  override val creatureWidth = 1.85f
  override val creatureHeight = 1.85f

  override val maxLife = 200f

  override val isPlayer: Boolean = true

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.painSound))

  override val walkSound: Option[Sound] = Some(Assets.sound(Assets.runningSound))

  var onSpawnPointId: Option[String] = None

  var respawning: Boolean = false
  val respawnTimer: EsTimer = EsTimer()

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "male1",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  override val defaultAdditionalAbilities: Map[String, Ability] =
    Map({
      DashAbility(
        creature = this,
        soundParameters =
          SoundParameters(activeSound = Some(Assets.sound(Assets.flybySound)), activeSoundVolume = Some(0.2f))
      ).asMapEntry
    })

  override def calculateFacingVector(): Unit = {
    val mouseX = Gdx.input.getX
    val mouseY = Gdx.input.getY

    val centerX = Gdx.graphics.getWidth / 2f
    val centerY = Gdx.graphics.getHeight / 2f

    // we need to reverse y due to mouse coordinates being in different system
    facingVector = new Vector2(mouseX - centerX, (Gdx.graphics.getHeight - mouseY) - centerY).nor()
  }

  override def onDeath(): Unit = {
    super.onDeath()

    respawnTimer.restart()
    respawning = true
    sprinting = false

  }

  def onRespawn(): Unit = {
    GameSystem.bossfightManager.stopBossfight()
  }

  def interact(): Unit = {
    if (onSpawnPointId.nonEmpty) {
      playerSpawnPoint = Some(area.get.playerSpawns.filter(_.id == onSpawnPointId.get).head)
      playerSpawnPoint.get.onRespawnSet()
    }
  }

  override def calculateWalkingVector(): Unit = {
    val dirs: List[EsDirection.Value] = GameSystem.playerMovementDirections

    val vector = new Vector2(0f, 0f)

    dirs.foreach {
      case EsDirection.Up    => vector.y += 1f
      case EsDirection.Down  => vector.y -= 1f
      case EsDirection.Left  => vector.x -= 1f
      case EsDirection.Right => vector.x += 1f
    }

    vector.nor()

    walkingVector = vector

  }

  def copy(
    id: String = id,
    area: Option[Area] = area,
    b2Body: Option[Body] = b2Body,
    standardAbilities: Map[String, Ability] = standardAbilities,
    additionalAbilities: Map[String, Ability] = additionalAbilities
  ): Self =
    Player(
      id = id,
      area = area,
      b2Body = b2Body,
      standardAbilities = standardAbilities,
      additionalAbilities = additionalAbilities
    )._temp_copyVars(this)

  init()
}

object Player {
  def apply(
    id: String,
    area: Option[Area] = None,
    b2Body: Option[Body] = None,
    standardAbilities: Map[String, Ability] = Map(),
    additionalAbilities: Map[String, Ability] = Map()
  ): Player = {
    val creature0 = new Player(id, area, b2Body, standardAbilities, additionalAbilities)
    val creature1 = if (standardAbilities.isEmpty) {
      creature0
        .modify(_.standardAbilities)
        .setTo(creature0.defaultStandardAbilities)
    } else creature0
    val creature2 = if (additionalAbilities.isEmpty) {
      creature1
        .modify(_.additionalAbilities)
        .setTo(creature1.defaultAdditionalAbilities)
    } else creature1

    creature2
  }
}
