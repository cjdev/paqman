package com.cj.paqman

import java.util.UUID
import com.cj.paqman.api.HunkDto

case class Qual(id:String = UUID.randomUUID().toString, name:String, description:String, hunks:Seq[HunkVersion], administrator:String) {
}

case class HunkReplacementInfo(
    isSignificantEdit:Boolean,
    replacesId:String)

case class HunkVersion(
    hunkId:String,
    versionId:String,
    kind:String,
    name:String,
    url:String = "",
    description:String = "",
    whenAdded:Long,
    isSignificantEdit:Boolean
){
  def withUpdatesFromDto(dto:HunkDto, now:Long) = copy(
		  	versionId=UUID.randomUUID().toString, 
		  	name=dto.name, 
		  	url = dto.url, 
		  	description = dto.description, 
		  	whenAdded = now, 
		  	isSignificantEdit = dto.isSignificantEdit)
  def toHunkInfo=HunkInfo(id=hunkId, name=name)
  def toDto() = HunkDto(name=name, kind=kind, description=description, url=url, isSignificantEdit=isSignificantEdit)
}