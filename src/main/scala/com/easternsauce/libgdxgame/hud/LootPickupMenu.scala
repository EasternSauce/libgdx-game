package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.items.{Item, LootPile}
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class LootPickupMenu(game: RpgGame) {

  val lootPiles: ListBuffer[LootPile] = ListBuffer()

  val scheduledToRemove: ListBuffer[(Item, LootPile)] = ListBuffer()

  def render(batch: EsBatch): Unit = {
    if (visible) {
      RpgGame.defaultFont.setColor(Color.ORANGE)

      val mouseX: Float = game.mousePositionWindowScaled.x
      val mouseY: Float = game.mousePositionWindowScaled.y

      var i = 0
      for {
        lootPile <- lootPiles
        item <- lootPile.itemList
      } {

        val x = RpgGame.WindowWidth / 2 - 100f
        val y = 150f - i * 30f

        val rect = new Rectangle(x - 25f, y - 20f, 300f, 25)

        batch.shapeDrawer.filledRectangle(rect, Color.DARK_GRAY)
        RpgGame.defaultFont.setColor(Color.WHITE)
        RpgGame.defaultFont.draw(batch.spriteBatch, "> " + item.name, x, y)

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

  def handleMouseClicked(): Unit = {
    val mouseX: Float = game.mousePositionWindowScaled.x
    val mouseY: Float = game.mousePositionWindowScaled.y

    var i = 0
    for {
      lootPile <- lootPiles
      item <- lootPile.itemList
    } {

      val x = RpgGame.WindowWidth / 2 - 100f
      val y = 150f - i * 30f
      val rect = new Rectangle(x - 25f, y - 20f, 300f, 25)

      if (rect.contains(mouseX, mouseY)) {
        val success = game.player.tryPickUpItem(item)
        if (success) scheduledToRemove += (item -> lootPile)
      }
      i += 1
    }

    scheduledToRemove.foreach {
      case (item, lootPile) =>
        if (lootPile.itemList.size == 1) {
          //          lootPile match { TODO sounds
          //            case _: Treasure => Assets.chestOpeningSound.play(0.1f)
          //            case _: LootPile => Assets.coinBagSound.play(0.3f)
          //          }
          val world = lootPile.b2body.getWorld
          world.destroyBody(lootPile.b2body)
        }

        lootPile.itemList -= item
        item.lootPile = None

        lootPile.area.lootPileList -= lootPile
    }
  }

  def visible: Boolean = lootPiles.nonEmpty && !game.inventoryWindow.visible
}
