package com.easternsauce.libgdxgame.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.graphics.g2d.TextureAtlas

object Assets {
  val area1DataLocation = "assets/areas/area1"
  val area2DataLocation = "assets/areas/area2"
  val area3DataLocation = "assets/areas/area3"

  val attackSound = "assets/sounds/swoosh.wav"
  val painSound = "assets/sounds/pain.wav"
  val arrowWhizzSound = "assets/sounds/arrow-whizz.wav"
  val bloodSquirtSound = "assets/sounds/blood-squirt.wav"
  val boneClickSound = "assets/sounds/bone-click.wav"
  val boneCrushSound = "assets/sounds/bone-crush.wav"
  val bowPullSound = "assets/sounds/bow-pull.wav"
  val bowReleaseSound = "assets/sounds/bow-release.wav"
  val darkLaughSound = "assets/sounds/dark-laugh.wav"
  val dogBarkSound = "assets/sounds/dogbark.wav"
  val dogWhineSound = "assets/sounds/dogwhine.wav"
  val evilYellingSound = "assets/sounds/evil-yelling.wav"
  val explosionSound = "assets/sounds/explosion.wav"
  val flybySound = "assets/sounds/flyby.wav"
  val glassBreakSound = "assets/sounds/glass-break.wav"
  val gruntSound = "assets/sounds/grunt.wav"
  val monsterGrowlSound = "assets/sounds/monster-growl.wav"
  val punchSound = "assets/sounds/punch.wav"
  val roarSound = "assets/sounds/roar.wav"
  val runningSound = "assets/sounds/running.wav"
  val strongPunchSound = "assets/sounds/strong-punch.wav"
  val swooshSound = "assets/sounds/swoosh.wav"
  val chestOpeningSound = "assets/sounds/chest-opening.wav"
  val coinBagSound = "assets/sounds/coinbag.wav"
  val matchIgniteSound = "assets/sounds/match-ignite.wav"
  val appleCrunchSound = "assets/sounds/apple-crunch.wav"
  val boneRattleSound = "assets/sounds/bone-rattle.wav"

  val sounds = List(
    attackSound,
    painSound,
    arrowWhizzSound,
    bloodSquirtSound,
    boneClickSound,
    boneCrushSound,
    bowPullSound,
    bowReleaseSound,
    darkLaughSound,
    dogBarkSound,
    dogWhineSound,
    evilYellingSound,
    explosionSound,
    flybySound,
    glassBreakSound,
    gruntSound,
    monsterGrowlSound,
    punchSound,
    roarSound,
    runningSound,
    strongPunchSound,
    swooshSound,
    chestOpeningSound,
    coinBagSound,
    matchIgniteSound,
    appleCrunchSound,
    boneRattleSound
  )

  val abandonedPlainsMusic = "assets/music/abandoned_plains.wav"
  val fireDemonMusic = "assets/music/fire_demon.wav"

  val music = List(abandonedPlainsMusic, fireDemonMusic)

  val youngSerifFont = "assets/font/YoungSerif-Regular.ttf"

  private var assetManager: AssetManager = _
  var atlas: TextureAtlas = _

  def loadAssets(): Unit = {
    atlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

    assetManager = new AssetManager()

    Assets.sounds.foreach(assetManager.load(_, classOf[Sound]))
    Assets.music.foreach(assetManager.load(_, classOf[Music]))

    assetManager.finishLoading()
  }

  def sound(path: String): Sound = {
    assetManager.get(path, classOf[Sound])
  }

  def music(path: String): Music = {
    assetManager.get(path, classOf[Music])
  }
}
