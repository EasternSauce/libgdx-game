package com.easternsauce.libgdxgame.bossfight

import com.easternsauce.libgdxgame.creature.traits.Boss
import com.easternsauce.libgdxgame.system.GameSystem
import com.easternsauce.libgdxgame.util.EsBatch

class BossfightManager {

  var bossBattleActive: Boolean = false
  var boss: Option[Boss] = None
  val bossLifeBar = new BossLifeBar()

  def startBossfight(boss: Boss): Unit = {
    if (!bossBattleActive) {
      bossBattleActive = true

      if (boss.bossMusic.nonEmpty) {
        GameSystem.musicManager.playMusic(boss.bossMusic.get, 0.1f)
      }

      bossLifeBar.onBossBattleStart(boss)
      //mobSpawnPoint.blockade.active = true

      this.boss = Some(boss)
    }
  }

  def stopBossfight(): Unit = {
    if (bossBattleActive) {
      bossBattleActive = false
      bossLifeBar.hide()
      bossLifeBar.boss.bossMusic.get.stop()
      GameSystem.musicManager.stopMusic()

      boss = None
    }
  }

  def update(): Unit = {
    bossLifeBar.update()
  }

  def render(batch: EsBatch): Unit = {
    bossLifeBar.render(batch)
  }

}
