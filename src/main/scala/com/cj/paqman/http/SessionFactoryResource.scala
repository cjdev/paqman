package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Session
import java.util.UUID
import com.cj.paqman.AuthRequest
import com.cj.paqman.AuthMechanism
import com.cj.paqman.Service
import com.cj.paqman.Jackson

class SessionFactoryResource(val datas:Data, val authMechanism:AuthMechanism, val service:Service) extends HttpObject("/api/sessions"){
    override def post(r:Request)= {
        val request = Jackson.readJson[AuthRequest](r.representation())
        val result = authMechanism.authenticateEmail(request.email, request.password)
        result match {
            case Some(userInfo) => service.getUserWithCreateIfNeeded(request.email) match {
                case None => UNAUTHORIZED
                case Some(user)=>
                    val sessionId = UUID.randomUUID().toString
                            datas.sessions.put(sessionId, Session(request.email))
                            OK(Json("""{
                                    "token":"""" + sessionId + """"
                                    }"""))
            }
            case None => UNAUTHORIZED()
        }
    }
}