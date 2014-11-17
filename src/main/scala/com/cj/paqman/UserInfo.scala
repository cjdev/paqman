package com.cj.paqman

case class ChallengePassedEvent(qualId:String, challengeId:String, challengeVersion:String, when:Long)
case class UserInfo (id:String, events:Seq[ChallengePassedEvent])
