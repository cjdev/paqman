package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Jackson
import com.cj.paqman.QualDto
import com.cj.paqman.Service

class QualResource (datas:Data, service:Service) extends HttpObject("/api/quals/{id}"){
  override def get(r:Request) = {
    val id = r.path().valueFor("id")
    val maybeQual = datas.qualifications.getHistory(id)
    maybeQual match {
      case Some(record) =>
        val qual = record.latest
        val proctors = datas.users.toSeq().map(_.latest).filter(service.userHasPassed(_, record)).map(_.id) :+ qual.administrator
        val dto = new QualDto(qual, proctors)
        OK(Json(Jackson.generate(dto)))
      case None => NOT_FOUND
    }
  }
}