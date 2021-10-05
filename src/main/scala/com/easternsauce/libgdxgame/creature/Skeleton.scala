package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

case class Skeleton(override val id: String, override val area: Option[Area] = None)
    extends Enemy(id = id, area = area) {
  override type Self = Skeleton

  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 160f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.boneClickSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.boneRattleSound))

  override val dropTable = Map(
    "ringmailGreaves" -> 0.1f,
    "leatherArmor" -> 0.05f,
    "hideGloves" -> 0.1f,
    "leatherHelmet" -> 0.1f,
    "woodenSword" -> 0.1f,
    "healingPowder" -> 0.5f
  )

  override val animationParams: AnimationParams = {
    AnimationParams(
      regionName = "skeleton",
      textureWidth = 64,
      textureHeight = 64,
      animationFrameCount = 9,
      frameDuration = 0.05f,
      neutralStanceFrame = 0,
      dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
    )
  }

  def copy(id: String = id, area: Option[Area] = area): Self = Skeleton(id = id, area = area)

  init()
}
