package com.easternsauce.libgdxgame.ability.misc.templates

import com.easternsauce.libgdxgame.ability.attack.{ShootArrowAttack, SlashAttack, ThrustAttack}
import com.easternsauce.libgdxgame.ability.composed._
import com.easternsauce.libgdxgame.ability.other.{DashAbility, ExplodeAbility}

object AbilityFactory {
  def ability(abilityId: String, creatureId: String): Ability = {
    abilityId match {
      case ShootArrowAttack.id   => ShootArrowAttack(creatureId)
      case SlashAttack.id        => SlashAttack(creatureId)
      case ThrustAttack.id       => ThrustAttack(creatureId)
      case BubbleAbility.id      => BubbleAbility(creatureId)
      case FistSlamAbility.id    => FistSlamAbility(creatureId)
      case IceShardAbility.id    => IceShardAbility(creatureId)
      case MeteorCrashAbility.id => MeteorCrashAbility(creatureId)
      case MeteorRainAbility.id  => MeteorRainAbility(creatureId)
      case DashAbility.id        => DashAbility(creatureId)
      case ExplodeAbility.id     => ExplodeAbility(creatureId)
      case other                 => throw new RuntimeException("ability id not found: " + other)
    }
  }
}
