package com.easternsauce.libgdxgame.util

case class StateUpdate[+A](state: A, events: List[Event] = List()) {
  def map[B](f: A => B): StateUpdate[B] = StateUpdate(f(state), events)
  def flatMap[B](f: A => StateUpdate[B]): StateUpdate[B] =
    f(state) match { case StateUpdate(s, es) => StateUpdate(s, events ++ es) }

  def unsafeEvents: List[Event] = events
  def updateEvents(newEvents: List[Event]): StateUpdate[A] = StateUpdate(state, events ++ newEvents)
}
