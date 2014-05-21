package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Jackson
import com.cj.paqman.UserQualStatus
import com.cj.paqman.SessionInfo

class SessionResource (data:Data) extends HttpObject("/api/sessions/{id}"){
    override def get(r:Request)={
      val id = r.path().valueFor("id")
      data.sessions.get(id) match {
        case None => NOT_FOUND()
        case Some(session) =>
          data.users.get(session.email) match {
                case Some(user) =>
                  val myQuals = user.qualifications.map{q=>
                  val qual = data.qualifications.get(q.id).get
                    if(user.id == qual.administrator){
                      UserQualStatus(
                        id=q.id,
                        isQualified = true,
                        challengesMet = qual.hunks.filter(_.kind == "challenge").map(_.id).toSet)
                    }else{
                      UserQualStatus(
                        id=q.id,
                        isQualified=q.hasPassed(data.qualifications),
                        challengesMet=q.passedChallenges)
                    }
                  }
                  OK(Json(Jackson.generate(SessionInfo(email = session.email, qualifications = myQuals))))
                case None => INTERNAL_SERVER_ERROR
            }
      }
    }
}