package com.android.greentravel.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class SWInterface {

	
	
	public final static String TAG = "SWInterface";  
	public final static String METHOD_HEART = "/ois/user/query";  // 心跳
	public final static String METHOD_SETCAMNAME = "/ois/terminal/camera/name";  // 修改摄像头名称
	public final static String METHOD_BIND = "/ois/user/bind";  // 绑定
	public final static String METHOD_FORWARD = "/ois/forward";  // 转发给摄像头的命令  (云台，对讲，布防等 都需要ois转发给摄像头)
	
	
	
	
	
	public static SWHttpResponse heart(Parameter parameter){
		String currentUser= parameter.get("currentUser");
		if(currentUser== null || currentUser.trim().equals("")){
			Log.w(TAG, "---invalid heart, currentUser ="+currentUser);
			return new SWHttpResponse();
		}
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_HEART;
		String body= "{" +
				"\"user_id\":\""+currentUser+"\"," +
				"\"peer_id\":\""+parameter.get("mac")+"\"}";
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	} 
	
	public static SWHttpResponse setCameraName(Parameter parameter, String camera_id, String camera_name){
		
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_SETCAMNAME;
		String body= "{" +
				"\"camera_id\":\""+camera_id+"\"," +
				"\"camera_name\":\""+camera_name+"\"}";
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	
	public static SWHttpResponse bind(Parameter parameter, String user_id, String camera_id){
		
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_BIND;
		String body= "{" +
				"\"terminal_id\":\""+parameter.get("mac")+"\"," +
				"\"user_id\":\""+user_id+"\", " +
				"\"camera_id\":\""+camera_id+"\"}";
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	public static SWHttpResponse move(Parameter parameter, String camera_id, String move_direction){
		
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "move" + "', " +
				"'value':'" + move_direction + "'}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	//获取视频清晰度
	public static int getResolution(Parameter parameter, String camera_id){
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "get_resolution" + "'}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		SWHttpResponse response= http.executePost();
		int resolution= -1;
		if(response.statusCode== 200 && response.errorCode== 0){
			try {
				resolution= response.bodyJson.getInt("value");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resolution;
	}
	
	public static boolean setResolution(Parameter parameter, String camera_id, int resolution){
		boolean success= false;
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "'," +
				"'action':'" + "set_resolution" + "'," +
				"'value':" + resolution + "}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		SWHttpResponse response= http.executePost();
		if(response.statusCode== 200 && response.errorCode== 0){
			success= true;
		}
		return success;
	}
	
	//获取布防区域
	public static int[] getDetection(Parameter parameter, String camera_id){
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "get_detection" + "'}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		SWHttpResponse response= http.executePost();
		int[] area= null;
		if(response.statusCode== 200 && response.errorCode== 0){
			try {
				JSONObject value= response.bodyJson.getJSONObject("value");
				int detection_switch= value.getInt("switch");
				if(1== detection_switch){
					JSONArray array= value.getJSONArray("area");
					if(array.length()> 0){
						JSONObject areaObj= array.getJSONObject(0);
						area= new int[]{areaObj.getInt("x"), areaObj.getInt("y"), areaObj.getInt("w"), areaObj.getInt("h")}; 
						Log.d(TAG, "--monitioninfo--"+Arrays.toString(area));
					}else{
						area= new int[]{};
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return area;
	}
	
	
	//开启关闭布防区域
	public static SWHttpResponse setDetection(Parameter parameter, String camera_id, int detection_switch){
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "set_detection" + "', " +
				"'value': {" +
					"'mode':" + 1 + "," +
					"'switch':" + detection_switch + 
				"}}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	
	public static SWHttpResponse delAllDetection(Parameter parameter, String camera_id){
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "delall_detection" + "', " +
				"'value': {" +
					"'mode':" + 1 +
				"}}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	public static SWHttpResponse addDetection(Parameter parameter, String camera_id, int[] monitioninfo){
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "add_detection" + "', " +
				"'value': {" +
					"'mode':" + 1 + "," +
					"'area':{'x': "+monitioninfo[0]+", 'y':"+monitioninfo[1]+", 'w': "+monitioninfo[2]+", 'h': "+monitioninfo[3]+"}"  +
				"}}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	
	public static boolean setFlip(Parameter parameter, String camera_id){
		boolean success= false;
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("mac") + "'," +
				"'dest':'" + camera_id + "', " +
				"'action':'" + "flip" + "'" + 
				"}";
		body= body.replaceAll("'", "\"");
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		SWHttpResponse response= http.executePost();
		if(response.statusCode== 200 && response.errorCode== 0){
			success= true;
		}
		return success;
	}
	
	
	public static SWHttpResponse talk(Parameter parameter, String camera_id, String audio){
		String url= "http://"+parameter.get("ois_ip")+":"+parameter.get("ois_port")+ METHOD_FORWARD; 
		String body= "{" +
				"'src':'" + parameter.get("ois_ip") + "'," +
				"'dest':'" + camera_id + "'," +
				"'action':'" + "talk" + "'," + 
				"'value':'" + audio + "'" +
				"}";
		body= body.replaceAll("'", "\"");   
		
		/*byte[] b= audio.getBytes();
		byte[] d= Base64.decode(b, Base64.DEFAULT);
		byte[] data= new byte[160];
		ByteArrayInputStream byteIn= new ByteArrayInputStream(d);
		DataInputStream dataIn= new DataInputStream(byteIn);
		try {
			int _pts= dataIn.readInt();
			int _len= dataIn.readInt();
			dataIn.read(data);
			dataIn.close();
			byteIn.close();
			System.out.println("--333==="+_pts+"--"+_len);
			String aa= "--333==";
			for(byte bb: data){
				aa+=(bb+",");
			}
			System.out.println(aa); 
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		
		
		
		Map<String, String> header= postHeader();
		SWHttp http= new SWHttp(url, body, header);
		return http.executePost();
	}
	
	private static Map<String, String> postHeader(){
		
		Map<String, String> map= new HashMap<String, String>();
		map.put("Content-Type", "application/json");
		return map;
	}

}
