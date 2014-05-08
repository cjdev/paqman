package com.cj.paqman

class Service(val datas:Data, val authMechanism:AuthMechanism) {
    def getUserWithCreateIfNeeded(emailAddress:String) = {
        datas.users.get(emailAddress) match{
        case None => if(authMechanism.emailExists(emailAddress)){
            val user =  UserInfo(id=emailAddress, qualifications=Seq())
                    datas.users.put(emailAddress, user)
                    Some(user)
        }else{
            None
        }
        case Some(user)=> Some(user)
        }
    }
}