package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

case class Goblin(override val id: String, override val area: Option[Area] = None, override val b2Body: Option[Body] = None)
    extends Enemy(id = id, area = area, b2Body = b2Body) {
  override type Self = Goblin

  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 190f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.evilYellingSound))

  override val dropTable = Map(
    "ironSword" -> 0.03f,
    "poisonDagger" -> 0.07f,
    "healingPowder" -> 0.3f,
    "steelArmor" -> 0.03f,
    "steelGreaves" -> 0.05f,
    "steelGloves" -> 0.05f,
    "steelHelmet" -> 0.05f
  )

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "goblin",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.25f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  def copy(id: String = id, area: Option[Area] = area, b2Body: Option[Body] = b2Body): Self = Goblin(id = id, area = area, b2Body = b2Body)

  init()
}
