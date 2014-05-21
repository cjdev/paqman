package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Service
import com.cj.paqman.Jackson
import com.cj.paqman.QualificationInfo

class ChallengePeopleResource (val data:Data, val service:Service) extends  HttpObject("/api/quals/{id}/challenges/{challengeId}/people"){
  override def post(r:Request) = {
    val qualId = r.path().valueFor("id")
    val challengeId = r.path().valueFor("challengeId")
    val emailAddress = Jackson.readJson[String](r.representation())
    data.qualifications.get(qualId) match {
      case None =>BAD_REQUEST()
      case Some(qual)=>
        val user = service.getUserWithCreateIfNeeded(emailAddress).get
        def isThis(q:QualificationInfo) = q.id == qualId
        
        val qualInfo:QualificationInfo = user.qualifications.find(isThis).getOrElse(new QualificationInfo(id=qualId))
        val otherQuals = user.qualifications.filterNot(isThis)
        
        val passedChallenges = qualInfo.passedChallenges + challengeId
        val updatedQual = qualInfo.copy(passedChallenges = passedChallenges)
        data.users.put(emailAddress, user.copy(qualifications=otherQuals :+ updatedQual))
        OK(Text("yo"))
    }
  }
}