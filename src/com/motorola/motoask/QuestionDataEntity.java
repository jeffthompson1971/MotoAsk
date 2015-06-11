
package com.motorola.motoask;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;


@Entity(name = "QuestionData")
@Unindex
public class QuestionDataEntity {
	
  @Id
  private Long qId;
  
  @Index
  private String userId;
    
  @Index
  private String userEmail;
  private String q;
  private String qDetails;
  private String qTopics;
  
  public QuestionDataEntity setQuestionId(Long id){
	  qId = id;
	  return this;
  }
  public QuestionDataEntity setUserId(String id){
	  userId = id;
	  return this;
  }
  public QuestionDataEntity setEmail(String email){
	  userEmail = email;
	  return this;
  }
  public QuestionDataEntity setQInfo(String info){
	  q = info;
	  return this;
  }
  public QuestionDataEntity setQDetails(String details){
	  qDetails = details;
	  return this;
  }
  public QuestionDataEntity setQTopics(String topics){
	  qTopics = topics;
	  return this;
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
  public String getQInfo(){
	  return q;
  }
  public String getQDetails(){
	  return qDetails;
  }
  public String getQTopics(){
	  return qTopics;
  }
}
