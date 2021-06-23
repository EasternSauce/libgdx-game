package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.items.{Item, LootPile}
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.system.{Assets, Constants, Fonts}
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class LootPickupMenu {

  val lootPiles: ListBuffer[LootPile] = ListBuffer()

  val scheduledToRemove: ListBuffer[(Item, LootPile)] = ListBuffer()

  def render(batch: EsBatch): Unit = {
    if (visible) {
      val mouseX: Float = mousePositionWindowScaled.x
      val mouseY: Float = mousePositionWindowScaled.y

      var i = 0
      for {
        lootPile <- lootPiles
        item <- lootPile.itemList
      } {

        val rect: Rectangle = optionRect(i)

        batch.shapeDrawer.filledRectangle(rect, Color.DARK_GRAY)
        val text = "> " + item.name + (if (item.quantity > 1) " (" + item.quantity + ")" else "")
        Fonts.defaultFont.draw(batch.spriteBatch, text, optionPosX(i), optionPosY(i), Color.WHITE)

        if (rect.contains(mouseX, mouseY)) batch.shapeDrawer.rectangle(rect, Color.RED)

        i += 1
      }
    }
  }

  def showLootPile(lootPile: LootPile): Unit = {
    lootPiles += lootPile
  }

  def hideLootPile(lootPile: LootPile): Unit = {
    lootPiles -= lootPile
  }

  def pickUpOptionRects: IndexedSeq[Rectangle] = {
    for {
      i <- lootPiles.flatMap(_.itemList).indices
      x = optionPosX(i)
      y = optionPosY(i)
      rect = new Rectangle(x - 25f, y - 20f, 300f, 25)
    } yield rect
  }

  def pickUpItemClick(): Unit = {
    val mouseX: Float = mousePositionWindowScaled.x
    val mouseY: Float = mousePositionWindowScaled.y

    val itemOptions = lootPiles.flatMap(_.itemList)

    for {
      item <- itemOptions
    } {
      val i = itemOptions.indexOf(item)

      val rect: Rectangle = optionRect(i)

      if (rect.contains(mouseX, mouseY)) {
        val success = player.tryPickUpItem(item)
        if (success) {
          val lootPile = item.lootPile.get
          if (lootPile.isTreasure && !treasureLootedList.contains(currentArea.get.id -> lootPile.treasureId.get))
            treasureLootedList += (currentArea.get.id -> lootPile.treasureId.get)
          scheduledToRemove += (item -> lootPile)
        }
      }
    }

    scheduledToRemove.foreach {
      case (item, lootPile) =>
        Assets.sound(Assets.coinBagSound).play(0.3f)

        if (lootPile.itemList.size == 1) {
          val world = lootPile.b2Body.getWorld
          world.destroyBody(lootPile.b2Body)

          lootPile.area.lootPileList -= lootPile
        }

        lootPile.itemList -= item
        item.lootPile = None
    }
    scheduledToRemove.clear()
  }

  private def optionRect(i: Int): Rectangle = {
    val x = optionPosX(i)
    val y = optionPosY(i)
    new Rectangle(x - 25f, y - 20f, 300f, 25)
  }

  private def optionPosX(i: Int): Float = {
    Constants.WindowWidth / 2 - 100f
  }

  private def optionPosY(i: Int): Float = {
    150f - i * 30f
  }

  def visible: Boolean = lootPiles.nonEmpty && !inventoryWindow.visible
}
