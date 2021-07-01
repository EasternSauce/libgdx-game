package com.easternsauce.libgdxgame.ability.music

import com.badlogic.gdx.audio.Music

class MusicManager {

  var currentMusic: Option[Music] = None

  def playMusic(music: Music, volume: Float): Unit = {
    stopMusic()
    music.setVolume(volume)
    music.setLooping(true)
    music.play()
    currentMusic = Some(music)
  }

  def stopMusic(): Unit = {
    if (currentMusic.nonEmpty) currentMusic.get.stop()
  }
}
