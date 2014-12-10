package com.cj.paqman

class MockRecord[T](val history:T *) extends Record[T] {
    def latest=history.last
}