package com.easternsauce.libgdxgame.area.traits

import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.area.{AreaGate, TerrainTile}
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.projectile.Arrow

trait CollisionDetection {

  val game: RpgGame

  def createContactListener(world: World): Unit = {
    val contactListener: ContactListener = new ContactListener {
      override def beginContact(contact: Contact): Unit = {

        val objA = contact.getFixtureA.getBody.getUserData
        val objB = contact.getFixtureB.getBody.getUserData

        def onContactStart(pair: (AnyRef, AnyRef)): Unit = {
          pair match { // will run onContact twice for same type objects!
            case (creature: Creature, areaGate: AreaGate) =>
              areaGate.activate(creature)
            case (creature: Creature, ability: Ability) =>
              ability.onCollideWithCreature(creature)
//            case (creature: Creature, abilityComponent: AbilityComponent) =>
//              abilityComponent.onCollideWithCreature(creature)
            case (creature: Creature, arrow: Arrow) =>
              arrow.onCollideWithCreature(creature)
            case (areaTile: TerrainTile, arrow: Arrow) =>
              arrow.onCollideWithTerrain(areaTile)
            case _ =>
          }
        }

        onContactStart(objA, objB)
        onContactStart(objB, objA)
      }

      override def endContact(contact: Contact): Unit = {
        val objA = contact.getFixtureA.getBody.getUserData
        val objB = contact.getFixtureB.getBody.getUserData

        def onContactEnd(pair: (AnyRef, AnyRef)): Unit = {
          pair match { // will run onContact twice for same type objects!
            case (creature: Creature, _: AreaGate) =>
              creature.passedGateRecently = false
            case _ =>
          }
        }

        onContactEnd(objA, objB)
        onContactEnd(objB, objA)
      }

      override def preSolve(contact: Contact, oldManifold: Manifold): Unit = {}

      override def postSolve(contact: Contact, impulse: ContactImpulse): Unit = {}
    }

    world.setContactListener(contactListener)
  }
}
