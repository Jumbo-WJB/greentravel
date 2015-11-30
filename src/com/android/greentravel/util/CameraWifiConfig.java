package com.android.greentravel.util;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

/**
 * 摄像头零配置 
 * 摄像头没有配置网络时，客户端发送广播 （携带 客户端ip、ssid、ssid密码） 当摄像头接收到后 会根据ssid和密码联网 并 返回 摄像头编号
 *
 */
public class CameraWifiConfig {
	
	private final String TAG= "CameraWifiConfig";
	private final short port = 5154;
	private String pwd;
	private String ssid;
	private String ip;
	private boolean run;
	private ServerSocket receiveSocket; //接收摄像头返回的 编号信息
	private MulticastSocket sendSocket; //发送广播（携带 客户端ip、ssid、ssid密码）
	private SendThread sendThread;
	private ReceiveThread receiveThread;
	private Handler mCallback= null;
	
	public CameraWifiConfig(Handler callback) {
		mCallback= callback;
	}

	public void startConfig() {
		run = true;
		if(sendSocket== null){
			try {
				sendSocket = new MulticastSocket();
			} catch (Exception e) {
				Log.e(TAG, " init sendSocket error: "+e);
				e.printStackTrace();
			}
		}
		if(receiveSocket== null){
			try {
				receiveSocket= new ServerSocket(port);
			} catch (Exception e) {
				Log.e(TAG, " init receiveSocket error: "+e);
				e.printStackTrace();
			}
		}
		if(sendThread== null){
			sendThread = new SendThread();
			sendThread.start();
			Log.d(TAG, " start send search camera thread ");
		}
		
		if(receiveThread== null){
			receiveThread = new ReceiveThread();
			receiveThread.start();
			Log.d(TAG, " start recevice camera thread ");
		}
		
	}

	public void stopConfig() {
		run = false;
		if (sendThread != null) {
			try {
				sendThread.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
			sendThread = null;
			Log.d(TAG, " stop send search camera thread ");
		}
		if (receiveThread != null) {
			try {
				receiveThread.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
			receiveThread = null;
			Log.d(TAG, " stop recevice camera thread ");
		}
		if(sendSocket!= null){
			sendSocket.close();
			sendSocket= null;
		}
		if(receiveSocket!= null){
			try {
				receiveSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			receiveSocket= null;
		}
	}
	
	private class SendThread extends Thread {

		

		public void run() {
			
//			Log.d(TAG, "---localIp="+ip+"---wifiSSID="+ssid+"---wifiPwd="+pwd);
			byte ac[] = convert(ip, port);
			Object[] aobj = new Object[3];
			String s = new String(Base64.encode(ac, Base64.NO_WRAP));
			String s1 = new String(Base64.encode(ssid.getBytes(),Base64.NO_WRAP));
			String s2 = new String(Base64.encode(pwd.getBytes(),Base64.NO_WRAP));
			
			aobj[0] = s;
			aobj[1] = s1;
			aobj[2] = s2;
			String s3 = String.format("%s&%s&%s", aobj);
			int i = 0x7f & (new Random()).nextInt();
			if (i > 124)
				i = 124;
			do {
				if(sendSocket== null){
					return;
				}
				if (!run) {
					sendSocket.close();
					return;
				}
				try {
					
					broadcastIP(s3, i);
					System.out.println("--------send---------"); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (true);
		}
		
		
		private void broadcastIP(String s, int i) throws Exception {
			byte abyte0[] = new byte[4];
			Object aobj[] = new Object[2];
			aobj[0] = Integer.valueOf(i);
			aobj[1] = Integer.valueOf(s.length());
			String s1 = String.format("224.125.%d.%d", aobj);
			DatagramPacket datagrampacket = new DatagramPacket(abyte0,
					abyte0.length, InetAddress.getByName(s1), port);
			sendSocket.send(datagrampacket);
			int j = 0;
			do {
				if (j >= s.length())
					return;
				if (j % 4 == 0)
					sendSocket.send(datagrampacket);
				Object aobj1[] = new Object[3];
				aobj1[0] = Integer.valueOf(i);
				aobj1[1] = Integer.valueOf(j);
				aobj1[2] = Integer.valueOf(s.toCharArray()[j]);
				String s2 = String.format("224.%d.%d.%d", aobj1);
				DatagramPacket datagrampacket1 = new DatagramPacket(abyte0,
						abyte0.length, InetAddress.getByName(s2), port);
				sendSocket.send(datagrampacket1);
				j++;
				Thread.sleep(10L);
			} while (true);
		}
		
		
		
		/**
		 * ip、port转6个字节的数组
		 */
		private byte[] convert(String ip,short port){
			InetAddress iaddr;
			ByteArrayOutputStream bout = null;
			try {
				byte[] bytes = ByteBuffer.allocate(2).putShort(port).array();
				iaddr = InetAddress.getByName(ip);
				bout = new ByteArrayOutputStream();
				bout.write(iaddr.getAddress()); //write ip  
				bout.write(bytes);	 //write port		
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return bout.toByteArray();
		}
		
	}
	

	
	private class ReceiveThread extends Thread {
		
		@Override
		public void run() {
			
			
			// 服务器无穷的循环等待客户端的请求
			while (true) {
				if(receiveSocket== null){
					return;
				}
				try {
					if (!run) {
						receiveSocket.close();
						return;
					}
					Socket socket = receiveSocket.accept();
					new ReceiveHandleThread(socket).start();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//处理接收到的数据
	private class ReceiveHandleThread extends Thread{
		
		private Socket socket= null;
		public ReceiveHandleThread(Socket socket){
			this.socket= socket;
		}
		
		
		@Override
		public void run() {
			InputStreamReader in= null;
			BufferedReader br= null;
			try {
				in = new InputStreamReader(socket.getInputStream());
				br = new BufferedReader(in);
				String info= null;
				while(!((info=br.readLine())==null)){  
					Log.d(TAG, "----- receive camera info ---------"+info);  
					if(info.startsWith("uuid:")){
						String uid= info.replace("uuid:", "");
						Message msg= new Message();
						msg.what= Constant.MSG_CONNEC_RESULT;
						msg.obj= uid;
						mCallback.sendMessage(msg);
					}
	            } 
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(in!= null){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(br!= null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(socket!= null){
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	};



	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/*public boolean isRun() {
		return run;
	}*/


	

	
	
	
	
	
}
