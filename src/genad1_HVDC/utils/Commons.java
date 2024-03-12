package genad1_HVDC.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Commons {
	private RptGlobal rptglobal = RptGlobal.getInstance();
	
	
	// 24자리의 connectionid 생성
	public String generateConnectionid(String userId) {
		Random random = new Random();
		StringBuffer buf = new StringBuffer();
		
		for(int i=0; i<8; i++) {
			if(random.nextBoolean()) {
				buf.append((char)(random.nextInt(26) + 65));
			} else {
				buf.append(random.nextInt(10));
			}
		}
		
		String connectionid = userId + buf.toString();
		int diff = 24 - connectionid.length();
		
		for(int i=0; i<diff; i++) {
			connectionid = "0" + connectionid;
		}
		
		return connectionid;
	}

	
	// associate 인증
	public String socketConnection2(Map authMap, String ip, int port, String clientIp, int clientPort, String _commandtype, String _datatype, String _datatype_comment, String _datatype_fc, String _key, String _value, String _clientUniqueId) throws Exception {
		
		String duration = "30";					// 
		String sKey = "T0N1M2T3E4C5H6";			// securekey 만들때 사용할 키
		
		Socket _socket = new Socket(clientIp, clientPort);
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		
		System.out.println("clientUniqueId : " + _clientUniqueId);
		System.out.println("commandtype : " + _commandtype);
		System.out.println("datatype : " + _datatype);
		System.out.println("datatype_comment : " + _datatype_comment);
		System.out.println("datatype_fc : " + _datatype_fc);
		System.out.println("key : " + _key);
		System.out.println("value : " + _value);
		
		// 보낼 메시지
		JsonObject sendObject = new JsonObject();
		JsonObject requestObject = new JsonObject();
		JsonObject commandObject = new JsonObject();
		commandObject.addProperty("commandtype", _commandtype);
		commandObject.addProperty("datatype", _datatype);
		commandObject.addProperty("datatype_comment", _datatype_comment);
		commandObject.addProperty("datatype_fc", _datatype_fc);
		commandObject.addProperty("key", _key);
		commandObject.addProperty("value", _value);
		commandObject.addProperty("ap_title", (String)authMap.get("ap_title"));
		commandObject.addProperty("ae_qualifier", (String)authMap.get("ae_qualifier"));
		commandObject.addProperty("pdusize", (String)authMap.get("pdusize"));
		commandObject.addProperty("psel", (String)authMap.get("psel"));
		commandObject.addProperty("ssel", (String)authMap.get("ssel"));
		commandObject.addProperty("tsel", (String)authMap.get("tsel"));
		
		requestObject.add("command", commandObject);
		requestObject.addProperty("client_unique_id", _clientUniqueId);
		
		// 예 - md5(client_unique_id + type + 보안키 + key + value)
		String securekey = _clientUniqueId + _commandtype + sKey + _key + _value;
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(securekey.getBytes());
		byte[] msgStr = md.digest();
//			System.out.println("msgStr[] : " + Arrays.toString(msgStr));
		
		StringBuffer sb = new StringBuffer();
		for(byte byteTmp : msgStr) {
			sb.append(Integer.toString((byteTmp&0xff) + 0x100, 16).substring(1));
		}
		securekey = sb.toString();
		requestObject.addProperty("securekey", securekey);
		requestObject.addProperty("ied_ip", ip);
		requestObject.addProperty("ied_port", port);
		requestObject.addProperty("duration", duration);
		
		sendObject.add("request", requestObject);
		
		System.out.println("--- sendObject(associate) : " + sendObject.toString());
		
		bufferedWriter.write(sendObject.toString());
//				bufferedWriter.newLine();
		bufferedWriter.flush();
		
		String receiveString = bufferedReader.readLine();
		
		System.out.println("--- receiveString(associate) : " + receiveString);
		
		bufferedWriter.close();
		bufferedReader.close();
		_socket.close();
		
		return receiveString;
	}
	
	
	public String socketConnection(String ip, int port, String clientIp, int clientPort, String _commandtype, String _datatype, String _datatype_comment, String _datatype_fc, String _key, String _value, String _clientUniqueId) throws Exception {
		
		String duration = "30";					// 
		String sKey = "T0N1M2T3E4C5H6";			// securekey 만들때 사용할 키
		
		Socket _socket = new Socket(clientIp, clientPort);
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		
		System.out.println("clientUniqueId : " + _clientUniqueId);
		System.out.println("commandtype : " + _commandtype);
		System.out.println("datatype : " + _datatype);
		System.out.println("datatype_comment : " + _datatype_comment);
		System.out.println("datatype_fc : " + _datatype_fc);
		System.out.println("key : " + _key);
		System.out.println("value : " + _value);
		
		// 보낼 메시지
		JsonObject sendObject = new JsonObject();
		JsonObject requestObject = new JsonObject();
		JsonObject commandObject = new JsonObject();
		commandObject.addProperty("commandtype", _commandtype);
		commandObject.addProperty("datatype", _datatype);
		commandObject.addProperty("datatype_comment", _datatype_comment);
		commandObject.addProperty("datatype_fc", _datatype_fc);
		commandObject.addProperty("key", _key);
		commandObject.addProperty("value", _value);
		
		requestObject.add("command", commandObject);
		requestObject.addProperty("client_unique_id", _clientUniqueId);
		
		// 예 - md5(client_unique_id + type + 보안키 + key + value)
		String securekey = _clientUniqueId + _commandtype + sKey + _key + _value;
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(securekey.getBytes());
		byte[] msgStr = md.digest();
//		System.out.println("msgStr[] : " + Arrays.toString(msgStr));
		
		StringBuffer sb = new StringBuffer();
		for(byte byteTmp : msgStr) {
			sb.append(Integer.toString((byteTmp&0xff) + 0x100, 16).substring(1));
		}
		securekey = sb.toString();
		requestObject.addProperty("securekey", securekey);
		requestObject.addProperty("ied_ip", ip);
		requestObject.addProperty("ied_port", port);
		requestObject.addProperty("duration", duration);
		
		sendObject.add("request", requestObject);

		System.out.println("--- sendObject(GET_BRCB_VALUES) : " + sendObject.toString());
		
		bufferedWriter.write(sendObject.toString());
//			bufferedWriter.newLine();
		bufferedWriter.flush();
		
		String receiveString = bufferedReader.readLine();
		
		System.out.println("--- receiveString(GET_BRCB_VALUES) : " + receiveString);
		
		bufferedWriter.close();
		bufferedReader.close();
		_socket.close();
		
		return receiveString;
	}
	
	
	@SuppressWarnings("resource")
	public String socketConnection(String ip, int port, String clientIp, int clientPort, JsonObject _object, String reportsName, String event_datetime) throws Exception {
		System.out.println("===============[ Unified Controller Called ]===============");

		String client_ip = "172.30.1.15";// "192.168.219.160"; // "172.30.1.15";	//
		int client_port = 8030;				//
		String duration = "30";				//
		String sKey = "T0N1M2T3E4C5H6"; 	// securekey 만들때 사용할 키
		
		Socket _socket = new Socket(clientIp, clientPort);
		
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

		String securekey = _object.get("client_unique_id").getAsString()
				+ _object.getAsJsonObject("command").get("commandtype").getAsString() + sKey
				+ _object.getAsJsonObject("command").get("key").getAsString()
				+ _object.getAsJsonObject("command").get("value").getAsString();
		
		String rptId = _object.getAsJsonObject("command").get("key").getAsString();	

		JsonObject sendObject = new JsonObject();
		JsonObject requestObject = _object.getAsJsonObject();

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(securekey.getBytes());
		byte[] msgStr = md.digest();

		StringBuffer sb = new StringBuffer();
		for (byte byteTmp : msgStr) {
			sb.append(Integer.toString((byteTmp & 0xff) + 0x100, 16).substring(1));
		}
		securekey = sb.toString();
//		System.out.println("request hexStr : " + securekey);

		requestObject.addProperty("securekey", securekey);
		requestObject.addProperty("ied_ip", ip);
		requestObject.addProperty("ied_port", port);
		requestObject.addProperty("duration", duration);

		sendObject.add("request", requestObject);

		boolean fileFlag = requestObject.has("file_control");
		boolean brcb_set_ena = _object.getAsJsonObject("command").get("value").getAsString().equals("BRCB_SET_ENA");
		boolean urcb_set_ena = _object.getAsJsonObject("command").get("value").getAsString().equals("URCB_SET_ENA");
		
		boolean set_rpt_ena = false;
		if (_object.has("reporting") && _object.getAsJsonObject("reporting").has("report_setRptEna"))
			set_rpt_ena = _object.getAsJsonObject("reporting").get("report_setRptEna").getAsString().equals("1");
		
		boolean reportFlag = ((brcb_set_ena || urcb_set_ena) && set_rpt_ena);
//		boolean reportFlag = ((_object.getAsJsonObject("command").get("value").getAsString().equals("BRCB_SET_ENA")
//				|| _object.getAsJsonObject("command").get("value").getAsString().equals("URCB_SET_ENA"))
//				&& _object.getAsJsonObject("reporting").get("report_setRptEna").getAsString().equals("1"));

		String receiveString = null;
		
		// for File
		FileOutputStream fos = null;
		String filePath = "";
		String fileName = "";
		String filevalue = "";
		
		File file_handler = null; // = new File(filePath);
		boolean dirMade = false; //file_handler.mkdirs();
		File ofilePath = null;
		String fullpath = "";
		
		if (fileFlag) {
			// FILE 처리
			JsonObject fileControlObject = requestObject.getAsJsonObject("file_control"); // _object.getAsJsonObject("file_control").getAsJsonObject();
			fileName = fileControlObject.get("filename").getAsString();
			filevalue = _object.getAsJsonObject("command").get("value").getAsString();
			
			if (filevalue.equals("FILE_DIR")) {
				
			} else if (filevalue.equals("FILE_GET")) {
				if (fileName.equals("")) { // incorrect
					bufferedWriter.close();
					bufferedReader.close();
					_socket.close();
					return "";
				}
				
				boolean isWindows = false;
				String tmpFileName = fileName;
				if (tmpFileName.contains("\\")) {
//					System.out.println("tmpFileName : ~2`123`12`123`12`12`12");
//					System.out.println(tmpFileName);
					tmpFileName = tmpFileName.replace("\\", "/");
//					System.out.println(tmpFileName);
					// tmpFileName = tmpFileName.replaceAll("\\", "/");
					isWindows = true;
				}
				
				filePath = requestObject.get("downloadPath").getAsString(); // for linux
				
				requestObject.remove("downloadPath");
				
				String filename = fileName.equals("") == true ? "NO_FILE_NAME.err" : fileName;
				fileControlObject.addProperty("filename", filename);
				System.out.println("[FILE_GET]=========== filename (" + reportsName + ")  : " + filename);
				
				String tmpPath = "";
				String[] depth = null;
				
				tmpPath = filename;
				if (isWindows)
					depth = tmpFileName.split("/");
				else
					depth = tmpPath.split("/");
				filename = depth[depth.length - 1];
				
				// 250M 파일일 경우에는 파일이름을 바꿔줌.
				if(filePath.contains("HighResTransF")) {
					String[] filename_arr = filename.split("_");
					filename = filename_arr[0] + "_" + filename_arr[1] + "_" + event_datetime + ".dat";
				}
				
//				System.out.println("================================================== filename : " + filename);
				fullpath = filePath+"/"+filename;
			} else if (filevalue.equals("FILE_SET")) {
				if (fileName.equals("")) { // incorrect
					bufferedWriter.close();
					bufferedReader.close();
					_socket.close();
					return "";
				}
				// C:\\IEC61850\\uploadFiles\\sample.txt
				// if format is C:\IEC61850\\uploadFiles\sample.txt ?
				//                           ↑ Cut here.
				String filename = fileName.equals("") == true ? "NO_FILE_NAME.err" : fileName;
				String uploadFilename = filename.substring(fileName.lastIndexOf("\\") + 1);
				fileControlObject.addProperty("filename", uploadFilename);
				ofilePath = new File(filename);
				if (ofilePath.exists()) {
					long L = ofilePath.length();
					System.out.println("file " + filename + " size is " +L+ "bytes");
					fileControlObject.addProperty("file_size", L); // todo
				} else {
					System.out.println("original filepath is " + fileName + ", and filename : " + filename);
					fileControlObject.addProperty("file_size", 0); // todo
				}
			} else if (filevalue.equals("FILE_DEL")) {
				if (fileName.equals("")) { // incorrect
					bufferedWriter.close();
					bufferedReader.close();
					_socket.close();
					return "";
				}
			} else {
				System.out.println("Error Msg _value is incorrect.1 _value : " + filevalue);
			}
			// ofilePath = null;
			
			// fileControlObject.addProperty("file_command", _file_command);
			requestObject.add("file_control", fileControlObject);
			sendObject.add("request", requestObject);
		}
// 잠시 주석처리 jongsu
//		System.out.println("sendObject (" + reportsName + ")  : " + sendObject.toString());
		bufferedWriter.write(sendObject.toString());
		bufferedWriter.flush();

		if (reportFlag) {
			System.out.println("REPORT FLAG (" + reportsName + ") !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			// _socket.setSoTimeout(1000);
			StringBuilder sbuilder = new StringBuilder();
			int countsofBlock = 0;
			while(true) {
				char readChar = (char) bufferedReader.read();
				sbuilder.append(readChar);
				if (readChar == '{') countsofBlock++;
				else if (readChar == '}') {
					countsofBlock--;
					if (countsofBlock == 0) {
						receiveString = sbuilder.toString();
						break;
					}
				}
			}
			// receiveString = bufferedReader.readLine();
			
			System.out.println("[BRCB_SET_ENA] receiveString (" + reportsName + ")  ::: " + receiveString);
			// for RptEna Check
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = (JsonObject)jsonParser.parse(receiveString);
			String status = jsonObject.get("status").getAsString();
			
			if(status.equals("Y")) {
				
				// it is acceptable
				RptMap _rpt = null; // KeyPair<UID, IED_IP, IED_PORT>, Reports 객체 연결
				boolean match = false;

				for (RptMap iter : rptglobal.getRptMap()) { // 전역 변수에서 매칭되는 rpt가 있는지 확인
//					System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//					System.out.println("_object.get(\"client_unique_id\").getAsString() : " + _object.get("client_unique_id").getAsString());
//					System.out.println("rptId : " + rptId);
					
					if (iter.matches(_object.get("client_unique_id").getAsString(), ip, port, rptId)) { // 만약 매칭되는 게 있다면, 그 객체를
						// _rpt에 저장
						_rpt = iter;
						match = true;
						break;
					}
				}
				if (match != true) { // 전역 변수에 없는 조합이라면, _rpt를 생성한 후, 추가 (이 때, Reports 객체는 자동 생성)
					System.out.println("no match rptmap");
//					System.out.println("ip : " + ip);
//					System.out.println("port : " + port);
//					System.out.println("report Id : " + rptId);
					
					_rpt = new RptMap();
//					_rpt.setup(_object.get("client_unique_id").getAsString(), ip, port, rptId);
					rptglobal.getRptMap().add(_rpt);
				}
				
				Events myevent = _rpt.getEvents(); // 
				Reports myreport = _rpt.getReports(); // 해당 조합의 Report를 받아옴
				ReportReceiver myrptrecv = _rpt.getReportRecv();
				
//				System.out.println("=====================End of Report Process");
				// end of report process
				
				myevent = new Events();
				myreport = new Reports();
				myrptrecv = new ReportReceiver();
				
				_rpt.setEvents(myevent);
				_rpt.setReports(myreport);
				_rpt.setReportRecv(myrptrecv);
				
				_rpt.setup(_object.get("client_unique_id").getAsString(), ip, port, rptId, clientIp, clientPort, reportsName);

				if (myrptrecv.getSocket() == null || myrptrecv.getSocket().isClosed())
					myrptrecv.setSocket(_socket);
//				System.out.println("=====================Socket Setted");
				if (!myrptrecv.isRunning()) {
					System.out.println("--- myrptrecv status : " + myrptrecv.getId() + ", " + myrptrecv.getName() + ", "
							+ myrptrecv.getSocket() + "[" + myrptrecv.getSocket().isConnected() + "]" + "myrptrecv status : "
							+ myrptrecv.getState());
					myrptrecv.start();
					myreport.start();
					myevent.start();
				}
			}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			// System.out.println("111============== receivceString : " + receiveString);
			// report process
		} else if(fileFlag) {
			if (filevalue.equals("FILE_SET")) {
				System.out.println("FILESET START");
				OutputStream out = _socket.getOutputStream();
				try {
					FileInputStream readFile = new FileInputStream(ofilePath);
					byte[] buffer = new byte[1024];
					try {
						while (true) {
							int resultofread = readFile.read(buffer);
							if (resultofread == -1) break;
							out.write(buffer, 0, resultofread);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (readFile != null) readFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			char[] tmpReceiveString = new char[1024];
			int j = 0;
			int jsonObjdepth = 0;
			int tmps = 0;
		
			// RECEIVE RESULT STRING
			receiveString = "";
			
			if (filevalue.equals("FILE_GET")) { // FILE 처리
				
				while((tmps = bufferedReader.read()) != -1) {
					tmpReceiveString[j] = (char)tmps;
					if (tmpReceiveString[j] == '{') jsonObjdepth++;
					else if (tmpReceiveString[j] == '}') jsonObjdepth--;
					j++;
					if (jsonObjdepth == 0) break;
				}
				tmpReceiveString[j] = 0;
				// json data 짜르기
				
				receiveString = String.valueOf(tmpReceiveString).trim();
				
				// 잠시 주석처리 jongsu
//				System.out.println("[" + filevalue + "] 받은 메시지 ( " + reportsName + ", " + fileName + " )  : " + receiveString);
				
				JsonParser jsonParser = new JsonParser();
				JsonObject jsonObject = (JsonObject)jsonParser.parse(receiveString);
				String status = jsonObject.get("status").getAsString();
				
				if(status.equals("Y")) {
					file_handler = new File(fullpath);
					System.out.println("file fullpath : " + fullpath);
					fos = new FileOutputStream(file_handler);
				
					long fileL = 0L; // own file size
					long fileDL = 0L; // download file Length
	
					// 신과장님이 file_size 구하는 절차가 복잡하여 주석처리하고 새로구현
//					int startidx = receiveString.indexOf("file_size") + "file_size\": \"".length();
//					char checklength[] = receiveString.substring(startidx).toCharArray();
//					
//					System.out.println("---> " + new String(checklength));
//	
//					int i;
//					for (i = 0; i < checklength.length; i++) {
//						if (checklength[i] == '\"') {
//							break;
//						}
//						fileL = fileL*10 + (checklength[i] - '0');
//					} // get File Size
					
					
					fileL = jsonObject.get("file_size").getAsLong();
					
					System.out.println("---> fileL : " + fileL);
					
					InputStream in = _socket.getInputStream();
					byte[] buffer = new byte[1024 * 1024 * 2];
					try {
	
						while(fileDL < fileL) {
							int resultofread = in.read(buffer);
							System.out.println("---> resultofread : " + resultofread);
							if (resultofread == -1) {
								break;
							}
							fileDL += resultofread;
							fos.write(buffer, 0, resultofread);
						}
						System.out.println("---> fileDL : " + fileDL);
						
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (fileL != fileDL) {
						System.out.println("fileL is : " + fileL + " and fileDL is : " + fileDL + ", unmatched different : " + (fileL - fileDL));
	
						bufferedWriter.close();
						bufferedReader.close();
						_socket.close();
						return "";
					} else {
						// System.out.println("fileL is : " + fileL + " and fileDL is : " + fileDL + ", matched!!!");
					}
				}
			} else {
				receiveString = bufferedReader.readLine();
				System.out.println("[" + filevalue + "] 받은 메시지 (" + reportsName + ", " + fileName + ")  : " + receiveString);
			}
			
			bufferedWriter.close();
			bufferedReader.close();
			_socket.close();
			
		} else {
			
			receiveString = bufferedReader.readLine();
			System.out.println("[else] 받은 메시지 (" + reportsName + ")  : " + receiveString);
			boolean reportOff = ((brcb_set_ena || urcb_set_ena) && !set_rpt_ena);
//					|| _object.getAsJsonObject("command").get("value").getAsString().equals("URCB_SET_ENA"))
//					&& _object.getAsJsonObject("reporting").get("report_setRptEna").getAsString().equals("0"));
			if (reportOff) {
				// report process
				RptMap _rpt = null; // KeyPair<UID, IED_IP, IED_PORT>, Reports 객체 연결
				boolean match = false;

				for (RptMap iter : rptglobal.getRptMap()) { // 전역 변수에서 매칭되는 rpt가 있는지 확인
					if (iter.matches(_object.get("client_unique_id").getAsString(), ip, port, rptId)) { // 만약 매칭되는 게 있다면, 그 객체를
																									// _rpt에 저장
						_rpt = iter;
						match = true;
						break;
					}
				}
				if (match != true) {
					 System.out.println("왜 매칭이 안되냐, 기존 거는 있어야지.");
					bufferedWriter.close();
					bufferedReader.close();
					_socket.close();
					return receiveString;
				}
				System.out.println("found rpt object for remove." + rptglobal.getRptMap() + ", target : " + _rpt);
				ReportReceiver myreport = _rpt.getReportRecv(); // 해당 조합의 ReportRecv를 받아옴
				// end of report process

				myreport.setstoptag();
				Thread.sleep(1000);
				myreport.interrupt();
				rptglobal.getRptMap().remove(_rpt);
				System.out.println("rpt object removed." + rptglobal.getRptMap());
				System.out.println("rptglobal removed rpt object?" + rptglobal.getRptMap().contains(_rpt));
			}
			bufferedWriter.close();
			bufferedReader.close();
			_socket.close();
		}
		return receiveString;
	}
	

}

