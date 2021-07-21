package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.audio.Music
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

trait Boss extends Enemy {

  override val isKnockbackable = false
  override val isBoss = true

  val encounterMusic: Option[Music] = None

  var bossBattleStarted: Boolean = false

  val name: String

  val bossMusic: Option[Music]

  override def aggroOnCreature(otherCreature: Creature): Unit = {
    super.aggroOnCreature(otherCreature)

    if (!bossBattleStarted) {
      bossBattleStarted = true

      bossMusic.get.setVolume(0.1f)
      bossMusic.get.setLooping(true)
      bossMusic.get.play()

      GameSystem.bossLifeBar.onBossBattleStart(this)
      //mobSpawnPoint.blockade.active = true
      //Assets.monsterGrowlSound.play(0.1f)
    }
  }

  override def onDeath(): Unit = {
    super.onDeath()

    bossMusic.get.stop()
    if (GameSystem.bossLifeBar.boss == this)
      GameSystem.bossLifeBar.hide()
    //mobSpawnPoint.blockade.active = false
  }
}
