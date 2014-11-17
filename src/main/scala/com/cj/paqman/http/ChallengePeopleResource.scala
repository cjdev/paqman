package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Service
import com.cj.paqman.Jackson
import com.cj.paqman.ChallengePassedEvent

class ChallengePeopleResource (val data:Data, val service:Service) extends  HttpObject("/api/quals/{id}/challenges/{challengeId}/people"){
  override def post(r:Request) = {
    val qualId = r.path().valueFor("id")
    val challengeId = r.path().valueFor("challengeId")
    val emailAddress = Jackson.readJson[String](r.representation())
    data.qualifications.get(qualId) match {
      case None =>BAD_REQUEST()
      case Some(qual)=>
        val user = service.getUserWithCreateIfNeeded(emailAddress).get
        qual.hunks.find(_.hunkId == challengeId) match {
          case None => BAD_REQUEST(Text(s"NO SUCH CHALLENGE: $challengeId"))
          case Some(challenge)=>{
        	  val event = ChallengePassedEvent(qualId=qualId, challengeId=challengeId, challengeVersion=challenge.versionId, when=System.currentTimeMillis())
        			  
			  data.users.put(emailAddress, user.copy(events=user.events :+ event))
        			  
			  OK(Text("yo"))
          }
        }
        
        
        
    }
  }
}