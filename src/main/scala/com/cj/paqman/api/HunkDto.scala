package com.cj.paqman.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown=true)
case class HunkDto(
        name:String, 
	  	url:String,
	  	kind:String,
	  	description:String, 
	  	isSignificantEdit:Boolean)
