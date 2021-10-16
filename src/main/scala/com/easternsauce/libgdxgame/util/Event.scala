package com.easternsauce.libgdxgame.util


trait Event

case class EntityUpdate[T](id: String, update: T => T) extends Event