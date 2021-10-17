package com.easternsauce.libgdxgame.util

trait CreatureInfo {
  val standardAbilities: List[String] = List("slash", "shoot_arrow", "thrust")
  val additionalAbilities: List[String]
}
