package com.motorola.motoask;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;


@Entity(name = "UserData")
@Unindex
public class UserDataEntity {
	
  @Id
  private String regId;
  
  @Index
  private String userId;
  
  @Index
  private String userEmail;
  private String userName;
  private String imageUrl;
  private String deviceInfo;
  
  public UserDataEntity setRegId(String id){
	  regId = id;
	  return this;
  }
  public UserDataEntity setUserId(String id){
	  userId = id;
	  return this;
  }
  public UserDataEntity setEmail(String email){
	  userEmail = email;
	  return this;
  }
  public UserDataEntity setName(String name){
	  userName = name;
	  return this;
  }
  public UserDataEntity setImageUrl(String url){
	  imageUrl = url;
	  return this;
  }
  public UserDataEntity setDeviceInfo(String devInfo){
	  deviceInfo = devInfo;
	  return this;
  }
  
  public String getRegId(){
	  return regId;
  }
  public String getUserId(){
	  return userId;
  }
  public String getEmail(){
	  return userEmail;
  }
  public String getUserName(){
	  return userName;
  }
  public String getImageUrl(){
	  return imageUrl;
  }
  public String getDeviceInfo(){
	  return deviceInfo;
  }
}
