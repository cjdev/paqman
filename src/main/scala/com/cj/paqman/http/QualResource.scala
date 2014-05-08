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
import com.cj.paqman.QualDto

class QualResource (datas:Data) extends HttpObject("/api/quals/{id}"){
  override def get(r:Request) = {
    val id = r.path().valueFor("id")
    val maybeQual = datas.qualifications.get(id)
    maybeQual match {
      case Some(qual) => {
        val proctors = datas.users.toSeq.map(_.latest).filter(_.hasPassed(qual)).map(_.id) :+ qual.administrator
        val dto = new QualDto(qual, proctors)
        OK(Json(Jackson.generate(dto)))
      }
      case None => NOT_FOUND
    }
  }
}