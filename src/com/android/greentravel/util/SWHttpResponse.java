package com.android.greentravel.util;

import org.json.JSONObject;

public class SWHttpResponse {
	public int statusCode; // http 状态码
//	public String body; // 消息体
	public JSONObject bodyJson;   //消息提的json对象
	public int errorCode= -1;  //消息体中的  errorCode
	/*public String oisToken; // ois token, 从 http 消息头得到
	public String epgsToken; // epgs token， 从 http 消息头得到
	public String ois; // ois 返回 ois 服务器列表， 从 http 消息头得到
	public String epgs; // ois 返回的epgs， 从 http 消息头得到
	public int userType; // 登录后 ois 返回的用户类型（0:个人， 1:行业， 2:操作员 ）， 从 http 消息头得到
*/}