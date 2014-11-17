package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import java.util.UUID
import com.cj.paqman.Jackson
import com.cj.paqman.HunkVersion
import com.cj.paqman.api.HunkDto

class HunksResource (datas:Data) extends HttpObject("/api/quals/{id}/hunks"){
          
  private def randomUUID() = UUID.randomUUID().toString()
  
  override def post(r:Request) = {
	val now = System.currentTimeMillis()
    val id = r.path().valueFor("id")
    val maybeQual = datas.qualifications.get(id)
    maybeQual match {
      case Some(qual) =>
        val dto = Jackson.readJson[HunkDto](r.representation())
        val defaultHunk = HunkVersion(
					    hunkId=randomUUID,
					    versionId=randomUUID,
					    kind=dto.kind,
					    name="",
					    whenAdded = System.currentTimeMillis(),
					    isSignificantEdit = true)
		val sanitizedDto = dto.copy(isSignificantEdit = true) // the first edit should always be significant
        val newHunk = defaultHunk.withUpdatesFromDto(sanitizedDto, now)
        						 

        val updatedQual = qual.copy(hunks = qual.hunks :+ newHunk)
        datas.qualifications.put(id, updatedQual)
        CREATED(Location(s"/api/quals/$id/hunks/${newHunk.hunkId}"))
      case None => NOT_FOUND
    }
  }
}
