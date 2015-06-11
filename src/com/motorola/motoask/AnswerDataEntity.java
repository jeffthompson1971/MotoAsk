
package com.motorola.motoask;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;


@Entity(name = "AnswerData")
@Unindex
public class AnswerDataEntity {
	
  @Id
  private Long aId;
  
  @Index
  private Long qId;
  
  @Index
  private String userId;
    
  @Index
  private String userEmail;
  private String a;
  
  public AnswerDataEntity setAnswerId(Long id){
	  aId = id;
	  return this;
  }
  public AnswerDataEntity setQuestionId(Long id){
	  qId = id;
	  return this;
  }

  public AnswerDataEntity setUserId(String id){
	  userId = id;
	  return this;
  }
  public AnswerDataEntity setEmail(String email){
	  userEmail = email;
	  return this;
  }
  public AnswerDataEntity setAInfo(String info){
	  a = info;
	  return this;
  }
  
  public Long getAnswerId(){
	  return aId;
  }
  public Long getQuestionId(){
	  return qId;
  }

  public String getUserId(){
	  return userId;
  }
  public String getEmail(){
	  return userEmail;
  }
  public String getAInfo(){
	  return a;
  }
}
