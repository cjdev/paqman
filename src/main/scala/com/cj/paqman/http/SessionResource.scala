package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Jackson
import com.cj.paqman.UserQualStatus
import com.cj.paqman.SessionInfo
import com.cj.paqman.Service

class SessionResource (data:Data, service:Service) extends HttpObject("/api/sessions/{id}"){
    override def get(r:Request)={
      val id = r.path().valueFor("id")
      data.sessions.get(id) match {
        case None => NOT_FOUND()
        case Some(session) =>
          data.users.get(session.email) match {
                case Some(user) =>
                  val myQuals = data.qualifications.toSeq.map{qualRecord=>
                    val qual = qualRecord.latest
                    UserQualStatus(
                      id=qual.id,
                      isQualified=service.userIsCurrent(user, qualRecord),
                      challengesMet=Set()/* TODO: Figure out what to do here q.passedChallenges*/)
                  }
                  
                  OK(Json(Jackson.generate(SessionInfo(email = session.email, qualifications = myQuals))))
                case None => INTERNAL_SERVER_ERROR
            }
      }
    }
}