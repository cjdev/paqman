package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Session
import java.util.UUID
import com.cj.paqman.Paqman
import com.cj.paqman.AuthRequest
import com.cj.paqman.AuthMechanism
import com.cj.paqman.Service
import com.cj.paqman.PersonStatus
import com.cj.paqman.Jackson

class QualPeopleResource(val data:Data) extends HttpObject("/api/quals/{id}/people"){
          override def get(r:Request) = {
            val qualId = r.path().valueFor("id")
            data.qualifications.getHistory(qualId) match {
              case None =>BAD_REQUEST
              case Some(qual)=>{
                val result = data.users.toSeq.map(_.latest).flatMap{user=>
                  val maybeUserQualInfo = user.qualifications.find(_.id == qualId)
                  val hasPassedSomeChallenges = maybeUserQualInfo match {
                    case None=>false
                    case Some(userQualInfo)=>userQualInfo.passedChallenges.size>0
                  }
                  
                  val versionsPassed = qual.history.filter(user.hasPassed(_))
                  
                  val passedVersions = qual.history.filter(user.hasPassed(_))
                  
                  val wasCurrent = !passedVersions.isEmpty
                  val isCurrent = user.hasPassed(qual.latest)
                  println(s"user ${user} isCurrent=$isCurrent, wasCurrent=$wasCurrent hasPassedSomeChallenges=$hasPassedSomeChallenges passed $passedVersions")
                  
                  val challenges = qual.latest.hunks.filter(_.kind == "challenge")
                  val passedChallenges = challenges.filter{hunk=>
                    maybeUserQualInfo match {
                      case None=>false
                      case Some(userQualInfo)=> userQualInfo.passedChallenges.contains(hunk.id)
                    }
                  }
                  
                  val isAdministrator = qual.latest.administrator == user.id
                  
                  if(hasPassedSomeChallenges || isAdministrator){
                    Some(PersonStatus(email=user.id, isAdministrator=isAdministrator, isCurrent=isCurrent, wasCurrent=wasCurrent, 
                        hasPassedSomeChallenges=hasPassedSomeChallenges,
                        passedChallenges=passedChallenges.map(_.toHunkInfo),
                        challengesYetToDo=challenges.filterNot(passedChallenges.contains(_)).map(_.toHunkInfo)))
                  }else{
                      None
                  }
                }
                OK(Json(Jackson.generate(result)))
              }
            }
          }
        }