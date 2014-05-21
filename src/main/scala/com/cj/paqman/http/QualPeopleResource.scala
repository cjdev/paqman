package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Service
import com.cj.paqman.Jackson

class QualPeopleResource(val data:Data, val service:Service) extends HttpObject("/api/quals/{id}/people"){
  override def get(r:Request) = {
    val qualId = r.path().valueFor("id")
    data.qualifications.getHistory(qualId) match {
      case None =>BAD_REQUEST
      case Some(qual)=>
        val result = data.users.toSeq().map(_.latest).flatMap{user=>

          val status = service.userStatus(qualId, user)

          if(status.hasPassedSomeChallenges || status.isAdministrator){
            Some(status)
          }else{
            None
          }
        }
        OK(Json(Jackson.generate(result)))
    }
  }
}