package com.cj.paqman

trait DataInterface {
    val qualifications:DatabaseInterface[Qual]
    val users:DatabaseInterface[UserInfo]
    val sessions:DatabaseInterface[Session]
}