package genad1_HVDC.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Events extends Thread {

	Commons commons = new Commons();
	CommonsStr commonsStr = new CommonsStr();

	private RptVar sharedRptVar;

	private Boolean stoptag = false;
	private Boolean isrunning = false;
	private Boolean skipAndQuit = false;
	
	private String clientUniqueId = "";
	private String rptId = "";
	private String iedIp = "";
	private Integer iedPort = 0;
	private String sessionId = "";
	private String iedIndex = "";
	
	private String reportsName = "";
	private String clientIp = "";
	private Integer clientPort = 0;
	
	private String filePathRoot = commonsStr.filePathRoot;
	private String dataUploadDir = commonsStr.dataUploadDir;
	
	// 250M 파일을 생성하도록 요청시 2개의 채널에서 동시에 생성되기때문에
	// 중복요청하지않고 1개의 채널만 요청하기위해서 사용
	String EvtTransF_dateTime = "";
	
	private int channelCount = 0;
	
	
	public void run() {
		
		try {
			System.out.println("!!!!!=============== EVENT (" + reportsName + ")  =================!!!!!");
			System.out.println("Event thread id (" + this.getId() + ") started to listen");
			
			isrunning = true;
			String finalMsg = "";
			long requestTime = 0;
			
			while(!stoptag) {
				
				if (sharedRptVar.isRecvFinish() && sharedRptVar.isEmptyEventQueue()) {
					System.out.println("sharedRptVar.isRecvFinish() : " + sharedRptVar.isRecvFinish());
					System.out.println("sharedRptVar.isEmptyEventQueue() : " + sharedRptVar.isEmptyEventQueue());
					break;
				}
				
				if(!sharedRptVar.getIsAllowFileDownload()) {
					// 경과시간 측정
					long diffTime = (System.currentTimeMillis() - requestTime)/1000;
					
					// 경과시간이 30초이상일 경우 이벤트파일 다운로드 실행
					if(diffTime > 30) {
						System.out.println("250M파일 요청후 경과시간(s) ::: " + diffTime);
						sharedRptVar.setIsAllowFileDownload(true);
					}
				}
				
				// EventQueue에 데이터가 있고, 2개의 250M 파일이 Reports 데이터로 오면(getIsAllowFileDownload()==true)
				if (sharedRptVar.isEmptyEventQueue() || !sharedRptVar.getIsAllowFileDownload()) {
					if (sharedRptVar.isRecvFinish()) {
						stoptag = true; 
						System.out.println("[EVENT] stoptag = true");
					}
					continue; // ready for get
				}
				
				System.out.println("[EVENT] LinkedListQueue() Size : " + sharedRptVar.getCountEventQueue());
				finalMsg = sharedRptVar.getOneEventQueue();
				System.out.println("[EVENT] ****************** finalMsg ::: " + finalMsg);

//				if (finalMsg.indexOf("IED_RESPONSE_OK") > -1) continue;
				
				
				// make jsonObject to get information of report
				JsonParser jsonParser = new JsonParser();
				JsonObject jsonObject = (JsonObject) jsonParser.parse(finalMsg);
				JsonObject dataObj = jsonObject.get("response").getAsJsonObject();
				
				// value, reason
				JsonArray respValue = dataObj.get("value").getAsJsonArray();
				JsonArray reasons = dataObj.get("reason for inclusion").getAsJsonArray();
				
				JsonArray respDataRf = null;
				if (dataObj.has("data-reference")) respDataRf = dataObj.get("data-reference").getAsJsonArray();
				if (respDataRf != null) {
					
					for (int j = 0; j<respDataRf.size(); j++) {
						// TEMPLATE_IED_TEST/SPDC1$ST$RTTransF
						// TEMPLATE_IED_TEST/SPDC2$ST$EvtTransF
						String tmpDataRf = respDataRf.get(j).getAsString();
						String[] tmpRf2 = tmpDataRf.split("[$]"); // KEPCOALM/CINGGIO1, ST, Ind02, etc...
						String firstStr = tmpRf2[0];	// TEMPLATE_IED_TEST/SPDC1
						String fileLocation = firstStr.split("/")[1].toLowerCase(); // spdc1, spdc2
						String fileType = tmpRf2[2];	// RTTransF, EvtTransF, HighResTransF, TrendTransF
						String datatype_comment = "";
						
						// "EvtTransF" 일때만 진행
						if(fileType.equals("EvtTransF")) {
							
							String tempValue = respValue.get(j).getAsString();
							tempValue = tempValue.split(":::")[1];
							tempValue = tempValue.replace("{", "");
							tempValue = tempValue.replace("}", "");
							String[] valueArray = tempValue.split(",", -1);
//							String[] timeArray = valueArray[2].split("[.]");
//							String dateTime = timeArray[0];
							
							// Unix타임 변환
							long unixTime = Long.parseLong(valueArray[0]);
							Date date = new Date(unixTime*1000L);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
							sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
							sdf2.setTimeZone(TimeZone.getTimeZone("GMT+9"));
							
							String dateTime = sdf.format(date);
							String dataTime_directory = sdf2.format(date);
							
							String fileName = "";
							if(fileLocation.equals("spdc1")) {
								fileName = "00";
								fileLocation = "spdc0";
								datatype_comment = "61338";
								channelCount++;
							} else {
								fileName = "01";
								fileLocation = "spdc1";
								datatype_comment = "61440";
								channelCount++;
							}
							
							// RTTransF:실시간파일, EvtTransF:이벤트파일, HighResTransF:250M파일, TrendTransF:트랜드파일
							if(fileType.equals("RTTransF")) {
								fileName += "_01_" + dateTime + ".dat";
							} else if(fileType.equals("EvtTransF")) {
								fileName += "_02_" + dateTime + ".dat";
							} else if(fileType.equals("HighResTransF")) {
								fileName += "_03_" + dateTime + ".dat";
							} else {
								fileName += "_04_" + dateTime + ".dat";
							}
//							System.out.println("fileName ::: " + fileName);
							
							String reasonStr = reasons.get(j).getAsString();
							String reason = reasonStr.split("[:]")[1];
							
							// 폴더 생성
//							String uploadDir = dataUploadDir + "upload/" + iedIp + "/" + reportsName + "/" + fileLocation + "/";		//	/home/hvdcData/genad/upload/121.139.36.94/SPDCStat_brcb01/spdc0/
//							String completeDir = dataUploadDir + "complete/" + iedIp + "/" + reportsName + "/" + fileLocation + "/";	//	/home/hvdcData/genad/complete/121.139.36.94/SPDCStat_brcb01/spdc0/
//							
							// /home/hvdcData/pd/upload/121.139.36.94_102/RTTransF/20210827/
							String uploadDir = dataUploadDir + "upload/" + iedIp + "_" + iedPort + "/" + fileType + "/" + dataTime_directory + "/";	
							// /home/hvdcData/pd/complete/121.139.36.94_102/RTTransF/20210827/
							String completeDir = dataUploadDir + "complete/" + iedIp + "_" + iedPort + "/" + fileType + "/" + dataTime_directory + "/";
							
							File uploadDirFile = new File(uploadDir);
							File completeDirFile = new File(completeDir);
							
							if(!uploadDirFile.exists()) {
								uploadDirFile.mkdirs();
							}
							if(!completeDirFile.exists()) {
								completeDirFile.mkdirs();
							}
							
							
							// request block
							JsonObject requestObject = new JsonObject();
							
							// command block
							JsonObject commandObj = new JsonObject();
							commandObj.addProperty("commandtype", "select"); 
							commandObj.addProperty("datatype_fc", ""); 
							commandObj.addProperty("datatype", "");
							commandObj.addProperty("datatype_comment", ""); 
							commandObj.addProperty("key", "");
							commandObj.addProperty("value", "FILE_GET");
							
							String fileName_fullpath = filePathRoot + fileLocation + "/" + fileName;
							
							// control block
							JsonObject fileObj = new JsonObject();
							fileObj.addProperty("filename", fileName_fullpath);	//	/mnt/ADC_Raw/spdc0/01_01_20210809082758.dat
							fileObj.addProperty("file_command", "DOWNLOAD"); 
							fileObj.addProperty("file_size", "");
							
							requestObject.add("command", commandObj);
							requestObject.add("file_control", fileObj);
							requestObject.addProperty("client_unique_id", clientUniqueId);
							
							requestObject.addProperty("downloadPath", uploadDir);
							String receiveString = commons.socketConnection(iedIp, iedPort, clientIp, clientPort, requestObject, reportsName, "");
							
							if (receiveString != null && !receiveString.equals("")) {
//								System.out.println("receiveString ::: " + receiveString);
								
								jsonParser = new JsonParser();
								jsonObject = (JsonObject)jsonParser.parse(receiveString);
								String status = jsonObject.get("status").getAsString();
								// String responseSecurekey = jsonObject.get("securekey").getAsString();
								String message = jsonObject.get("message").getAsString();
								
								if(status.equals("Y")) {
									
									// 파일 이동
									File uploadFile = new File(uploadDir + fileName);		//	/home/hvdcData/pd/upload/121.139.36.94_102/RTTransF/20210827/01_01_20210809082758.dat
									File completeFile = new File(completeDir + fileName);	//	/home/hvdcData/pd/complete/121.139.36.94_102/RTTransF/20210827/01_01_20210809082758.dat
									uploadFile.renameTo(completeFile);
									
									System.out.println("EvtTransF_dateTime : " + EvtTransF_dateTime);
									System.out.println("dateTime : " + dateTime);
									
									// UnixTime이 다른파일인 경우(이전 spdc1과 spdc2의 이벤트파일이 모두 다운로드되었다고 판단하여 
									// 250M 파일생성을 요청함
									if(fileType.equals("EvtTransF") && !EvtTransF_dateTime.equals(dateTime)) {
										
										System.out.println("==========================================================================");
										System.out.println("   DataModel Update ( 250M 파일생성 : + " + dateTime + " )  ");
										System.out.println("==========================================================================");
										
										// TEMPLATE_IED_TEST/SPDC1.EvtReq.setVal
										String writeKkey = firstStr + ".EvtReq.setVal";
										
										// request block
										requestObject = new JsonObject();
										
										// command block
										commandObj = new JsonObject();
										commandObj.addProperty("commandtype", "update"); // update
										commandObj.addProperty("datatype_fc", "SP"); // 
										commandObj.addProperty("datatype", "2"); // 2:MMS_BOOLEAN,  4:MMS_INTEGER
										commandObj.addProperty("datatype_comment", datatype_comment); //  spdc1:61338,  spdc2:61440
										commandObj.addProperty("key", writeKkey); // 
										commandObj.addProperty("value", "1"); //   1 or 0 으로는 정상동작 하는데,   True or False 로는 정상동작하지않음.
										
										requestObject.add("command", commandObj);
										requestObject.addProperty("client_unique_id", clientUniqueId);
										
										receiveString = commons.socketConnection(iedIp, iedPort, clientIp, clientPort, requestObject, "NotReports", "");
										
										if (receiveString != null && !receiveString.equals("")) {
//											System.out.println("=======================================================");
//											System.out.println("receiveString : " + receiveString);
//											System.out.println("=======================================================");
											
											jsonParser = new JsonParser();
											jsonObject = (JsonObject)jsonParser.parse(receiveString);
											status = jsonObject.get("status").getAsString();
											String responseSecurekey = jsonObject.get("securekey").getAsString();
											message = jsonObject.get("message").getAsString();
											String from = "[ updateData - " + writeKkey + " ]";
											String data = "";
											
											if(status.equals("Y")) {
												data = jsonObject.get("response").getAsString();
//												System.out.println("DataModel Update response  : " + data);
												
												// 250M 파일을 생성하도록 요청시 2개의 채널에서 동시에 생성되기때문에
												// 중복요청하지않고 1개의 채널만 요청하기위해서 사용
												EvtTransF_dateTime = dateTime;
												
												// Event파일과 250M파일의 이름을 동일하게 생성하기위해 사용
												sharedRptVar.setEvent_datetime(dateTime);
												
												// 경과시간 측정
												requestTime = System.currentTimeMillis();
												
												// 2022.03.17
												// 임시로 추가 ( 차후에 삭제 필요 )
												// 제나드 장비가 1번채널만을 Event를 보내오기때문에 추가
												sharedRptVar.setIsAllowFileDownload(false); // 2개의 250M 파일이 Reports 데이터로 오면 true로 변경
												
											} else {
												System.out.println("Event DataModel Update message  : " + message);
											}
											
										} else {
											System.out.println("Event DataModel Update message : receiveString is empty");
										}
									}
									// Event 파일 spdc1, spdc2 를 모두 다운로드 받은경우
									else {
										System.out.println("----- channelCount : " + channelCount);
										System.out.println("----- setIsAllowFileDownload : false (spdc1, spdc2 모두 Event 파일을 다운로드 받음)");
										
										channelCount = 0;
										sharedRptVar.setIsAllowFileDownload(false); // 2개의 250M 파일이 Reports 데이터로 오면 true로 변경
									}
									
								} else {
									System.out.println("Event FILE_GET message (" + reportsName + ")  : " + message);
								}
								
							} else {
								System.out.println("Event FILE_GET message (" + reportsName + ")  : receiveString is empty");
							}
						}
					}
				}
			}
			
		} catch(Exception e) {
			System.out.println("--- normal Error(Events)");
			e.printStackTrace();
		}
		
		System.out.println("Events Thread(" + this.getId() + ") is stopped!!");
		stoptag = false;
		isrunning = false;
	}
	
	
	public void setRptVar(RptVar sharedRptVar) {
		this.sharedRptVar = sharedRptVar;
	}
	public Boolean getStoptag() {
		return stoptag;
	}
	public void setStoptag(Boolean stoptag) {
		this.stoptag = stoptag;
	}
	public Boolean getIsrunning() {
		return isrunning;
	}
	public void setIsrunning(Boolean isrunning) {
		this.isrunning = isrunning;
	}
	public Boolean getSkipAndQuit() {
		return skipAndQuit;
	}
	public void setSkipAndQuit(Boolean skipAndQuit) {
		this.skipAndQuit = skipAndQuit;
	}
	public String getClientUniqueId() {
		return clientUniqueId;
	}
	public void setClientUniqueId(String clientUniqueId) {
		this.clientUniqueId = clientUniqueId;
	}
	public String getRptId() {
		return rptId;
	}
	public void setRptId(String rptId) {
		this.rptId = rptId;
	}
	public String getIedIp() {
		return iedIp;
	}
	public void setIedIp(String iedIp) {
		this.iedIp = iedIp;
	}
	public Integer getIedPort() {
		return iedPort;
	}
	public void setIedPort(Integer iedPort) {
		this.iedPort = iedPort;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getIedIndex() {
		return iedIndex;
	}
	public void setIedIndex(String iedIndex) {
		this.iedIndex = iedIndex;
	}
	public String getReportsName() {
		return reportsName;
	}
	public void setReportsName(String reportsName) {
		this.reportsName = reportsName;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public Integer getClientPort() {
		return clientPort;
	}
	public void setClientPort(Integer clientPort) {
		this.clientPort = clientPort;
	}
}


