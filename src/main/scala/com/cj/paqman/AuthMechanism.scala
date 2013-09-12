package com.cj.paqman

trait AuthMechanism {
    def authenticateEmail(email:String, password:String):Option[AuthDetails]
    def emailExists(email:String):Boolean
}

case class AuthDetails