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

        val x = Constants.WindowWidth / 2 - 100f
        val y = 150f - i * 30f

        val rect = new Rectangle(x - 25f, y - 20f, 300f, 25)

        batch.shapeDrawer.filledRectangle(rect, Color.DARK_GRAY)
        Fonts.defaultFont.draw(batch.spriteBatch, "> " + item.name, x, y, Color.WHITE)

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

  def pickUpItemClick(): Unit = {
    val mouseX: Float = mousePositionWindowScaled.x
    val mouseY: Float = mousePositionWindowScaled.y

    var i = 0
    for {
      lootPile <- lootPiles
      item <- lootPile.itemList
    } {

      val x = Constants.WindowWidth / 2 - 100f
      val y = 150f - i * 30f
      val rect = new Rectangle(x - 25f, y - 20f, 300f, 25)

      if (rect.contains(mouseX, mouseY)) {
        val success = player.tryPickUpItem(item)
        if (success) scheduledToRemove += (item -> lootPile)
      }
      i += 1
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

  def visible: Boolean = lootPiles.nonEmpty && !inventoryWindow.visible
}
