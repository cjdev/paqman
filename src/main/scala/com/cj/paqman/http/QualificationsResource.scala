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
import com.cj.paqman.QualSummary
import com.cj.paqman.Qual
import com.cj.paqman.Qual

class QualificationsResource (datas:Data, service:Service) extends HttpObject("/api/quals"){
  override def get(r:Request) = {
    
    val summaries = datas.qualifications.toSeq().map(_.latest).map{qual=>
      QualSummary(id = qual.id, name=qual.name, description = qual.description, ref= "/api/quals/" + qual.id)
    }
    OK(Json(Jackson.generate(summaries)))
  }
  override def post(r:Request) = {
    val input = Jackson.readJson[Qual](r.representation())
    val aQualExistsWithThisNameAlready = datas.qualifications.toSeq.map(_.latest).find(_.name == input.name).isDefined
    
    if(aQualExistsWithThisNameAlready || !service.authMechanism.emailExists(input.administrator)){
      BAD_REQUEST
    }else{
      val qual = Qual(
                  id = UUID.randomUUID().toString(), 
                  name=input.name, 
                  description = input.description, 
                  administrator = input.administrator, 
                  hunks = Seq())
                  
      datas.qualifications.put(qual.id, qual);
      CREATED(Location("/api/quals/" + qual.id))
    }
  }
}