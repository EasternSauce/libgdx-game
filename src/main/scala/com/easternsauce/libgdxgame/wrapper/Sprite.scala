package com.easternsauce.libgdxgame.wrapper

import com.badlogic.gdx.graphics.g2d.{Batch, TextureRegion, Sprite => GdxSprite}

case class Sprite() {
  private var sprite: GdxSprite = new GdxSprite()

  def setRegion(region: TextureRegion): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.setRegion(region)
    newSprite
  }

  def setPosition(x: Float, y: Float): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.setPosition(x, y)
    newSprite
  }

  def setOrigin(x: Float, y: Float): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.setOrigin(x, y)
    newSprite
  }

  def setColor(r: Float, g: Float, b: Float, a: Float): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.setColor(r, g, b, a)
    newSprite
  }

  def setRotation(degrees: Float): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.setRotation(degrees)
    newSprite
  }

  def setBounds(x: Float, y: Float, width: Float, height: Float): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.setBounds(x, y, width, height)
    newSprite
  }

  def draw(batch: Batch): Sprite = {
    val newSprite = Sprite()
    newSprite.sprite = new GdxSprite(sprite)
    newSprite.sprite.draw(batch)
    newSprite
  }

  def width: Float = sprite.getWidth

  def height: Float = sprite.getHeight

}
