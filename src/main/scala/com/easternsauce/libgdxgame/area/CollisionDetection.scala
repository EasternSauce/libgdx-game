package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.physics.box2d.{Contact, ContactImpulse, ContactListener, Manifold}

trait CollisionDetection extends PhysicalTerrain {

  def createContactListener(): Unit = {
    val contactListener: ContactListener = new ContactListener {
      override def beginContact(contact: Contact): Unit = {

        val objA = contact.getFixtureA.getBody.getUserData
        val objB = contact.getFixtureB.getBody.getUserData

        def onContactStart(pair: (AnyRef, AnyRef)): Unit = {
//          pair match { // will run onContact twice for same type objects!
//            case (creature: Creature, areaGate: AreaGate) =>
//              if (!creature.passedGateRecently) {
//                onPassedAreaGate(areaGate, creature)
//              }
//            case (creature: Creature, ability: Ability) =>
//              ability.onCollideWithCreature(creature)
//            case (creature: Creature, abilityComponent: AbilityComponent) =>
//              abilityComponent.onCollideWithCreature(creature)
//            case (creature: Creature, arrow: Arrow) =>
//              arrow.onCollideWithCreature(creature)
//            case (areaTile: AreaTile, arrow: Arrow) =>
//              arrow.onCollideWithTerrain(areaTile)
//            case _ =>
//          }
        }

        onContactStart(objA, objB)
        onContactStart(objB, objA)
      }

      override def endContact(contact: Contact): Unit = {
        val objA = contact.getFixtureA.getBody.getUserData
        val objB = contact.getFixtureB.getBody.getUserData

        def onContactEnd(pair: (AnyRef, AnyRef)): Unit = {
//          pair match { // will run onContact twice for same type objects!
//            case (creature: Creature, _: AreaGate) =>
//              creature.passedGateRecently = false
//            case _ =>
//          }
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
