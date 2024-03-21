package genad1_HVDC.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.springframework.transaction.annotation.Isolation;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//import kr.co.mems.main.model.DataModelDetailVO;
//import kr.co.mems.main.model.DataSetsVO;
//import kr.co.mems.main.model.IedInfoVO;
//import kr.co.mems.main.model.ReportsVO;
//import kr.co.mems.main.service.MainService;
//import kr.co.mems.report.controller.ReportController;
//import kr.co.mems.report.model.ReportRequestVO;
//import kr.co.mems.report.model.RptVar;
//import kr.co.mems.utils.BeanUtils;
//import kr.co.mems.utils.Commons;

public class Reports extends Thread {
	
	Commons commons = new Commons();
	CommonsStr commonsStr = new CommonsStr();

	private RptVar sharedRptVar;

//	private ReportController reportController;
	private Commons checkLib;
//	private MainService mainService;
	
	private Boolean stoptag = false;
	private Boolean isrunning = false;
	private Boolean skipAndQuit = false;
	
	private Boolean HighResTransF_spdc1 = false;
	private Boolean HighResTransF_spdc2 = false;
	
	private String mqttIp = "172.30.1.14"; // mqtt.tnmtech.com // 172.30.1.14
	private Integer mqttPort = 1883;
	private String protocolUrl = "tcp";
	
	private String clientUniqueId = "";
	private String rptId = "";
	private String iedIp = "";
	private Integer iedPort = 0;
	private String sessionId = "";
	private String iedIndex = "";
	
	private String HighResTransF_filename = "";
	private String event_datetime = "";
	private String reportsName = "";
	private String clientIp = "";
	private Integer clientPort = 0;
	
	private String filePathRoot = commonsStr.filePathRoot;
	private String dataUploadDir = commonsStr.dataUploadDir;
	
	public void setRptVar(RptVar rptVar) {
		sharedRptVar = rptVar;
	}
	
	public Reports() {
		// 서비스 Bean을 가져온다! 이렇게 사용.
//		reportController = (ReportController) BeanUtils.getBean("ReportController");
//		checkLib = (Commons) BeanUtils.getBean("Commons");
//		mainService = (MainService) BeanUtils.getBean("MainService");
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	@Override
	public void run() {
		
		try {
			System.out.println("!!!!!=============== REPORT (" + reportsName + ")  =================!!!!!");
			System.out.println("Report id (" + this.getId() + ") started to listen");
			System.out.println("Report id ::: " + rptId);
			
			isrunning = true;
			// if (!stoptag) break;
			int rn = 0;
			
			String finalMsg = "";
			
			while (!stoptag) {
				// System.out.println("alived!! isRecvFinish : " + sharedRptVar.isRecvFinish() + ", isMax : " + sharedRptVar.isMax());
				if (sharedRptVar.isRecvFinish() && sharedRptVar.isEmptyQueue()) {
					System.out.println("sharedRptVar.isRecvFinish() : " + sharedRptVar.isRecvFinish());
					System.out.println("sharedRptVar.isEmptyQueue() : " + sharedRptVar.isEmptyQueue());
					break;
				}
				if (skipAndQuit) {
					System.out.println("skipAndQuit");
					break;
				}
				if (sharedRptVar.isEmptyQueue()) {
					if (sharedRptVar.isRecvFinish()) {
						stoptag = true; 
						System.out.println("[REPORT] stoptag = true");
					}
					continue; // ready for get
				}
				
				finalMsg = sharedRptVar.getOneRptQueue();
//				System.out.println("[REPORT] ****************** rptId ::: " + rptId + "   finalMsg ::: " + finalMsg);
				
				// 실시간 데이터(RTTransF)가 아닌경우에만 로그기록
//				if(finalMsg.contains("EvtTransF") || finalMsg.contains("HighResTransF") || finalMsg.contains("TrendTransF")) {
					System.out.println("[REPORT] LinkedListQueue() Size : " + sharedRptVar.getCountReportsQueue());
					System.out.println("[REPORT] ****************** finalMsg ::: " + finalMsg);
//				}
				
				if (finalMsg.indexOf("IED_RESPONSE_OK") > -1) continue;
				
				// 이벤트 레포트가 포함되어 있을경우 Event 큐에 추가
//				if (finalMsg.indexOf("EvtTransF") > -1) {
//					int eventQueueCount = sharedRptVar.getCountEventQueue();
//					System.out.println("[EVENT ADD] EventQueueCount : " + eventQueueCount);
//					
//					// 이벤트큐의 size가 10개 미만이면 저장
//					if(eventQueueCount < 10) {
//						sharedRptVar.addEventQueue(finalMsg);
//					}
//				}
				
				try {
					
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
							
							// 이벤트 레포트일 경우에 skip (Event 큐에서 파일 다운로드함)
							// 실시간 레포트일 경우에 skip
//							if(fileType.equals("EvtTransF") || fileType.equals("RTTransF")) {
//							if(fileType.equals("EvtTransF")) {
//								continue;
//							}
							
							String tempValue = respValue.get(j).getAsString();
							tempValue = tempValue.split(":::")[1];
							tempValue = tempValue.replace("{", "");
							tempValue = tempValue.replace("}", "");
							String[] valueArray = tempValue.split(",", -1);
//							String[] timeArray = valueArray[2].split("[.]");
//							String dateTime = timeArray[0];
							
							// Unix타임 변환
//							long unixTime = Long.parseLong(valueArray[0]);
//							Date date = new Date(unixTime*1000L);
//							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//							SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
//							sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
//							sdf2.setTimeZone(TimeZone.getTimeZone("GMT+9"));
//							
//							String dateTime = sdf.format(date);
//							String dataTime_directory = sdf2.format(date);
//							
//							String fileName = "";
//							if(fileLocation.equals("spdc1")) {
//								fileName = "00";
//								fileLocation = "spdc0";
//								datatype_comment = "61338";
//							} else {
//								fileName = "01";
//								fileLocation = "spdc1";
//								datatype_comment = "61440";
//							}
//							
//							// RTTransF:실시간파일, EvtTransF:이벤트파일, HighResTransF:250M파일, TrendTransF:트랜드파일
//							if(fileType.equals("RTTransF")) {
//								fileName += "_01_" + dateTime + ".dat";
//							} else if(fileType.equals("EvtTransF")) {
//								fileName += "_02_" + dateTime + ".dat";
//							} else if(fileType.equals("HighResTransF")) {
//								
//								// Event파일과 250M파일의 이름을 동일하게 생성하기위해 사용
//								event_datetime = sharedRptVar.getEvent_datetime();
//								// 폴더
//								dataTime_directory = event_datetime.substring(0, 8);
//								
//								// 로컬에 저장하기위해서 사용
//								HighResTransF_filename = fileName + "_03_" + event_datetime + ".dat";
//								// IED에서 가져오기위해서 사용
//								fileName += "_03_" + dateTime + ".dat";
//								
//							} else {
//								fileName += "_04_" + dateTime + ".dat";
//							}
////							System.out.println("fileName ::: " + fileName);
//							
//							String reasonStr = reasons.get(j).getAsString();
//							String reason = reasonStr.split("[:]")[1];
							
							// 폴더 생성
//							String uploadDir = dataUploadDir + "upload/" + iedIp + "/" + reportsName + "/" + fileLocation + "/";		//	/home/hvdcData/genad/upload/121.139.36.94/SPDCStat_brcb01/spdc0/
//							String completeDir = dataUploadDir + "complete/" + iedIp + "/" + reportsName + "/" + fileLocation + "/";	//	/home/hvdcData/genad/complete/121.139.36.94/SPDCStat_brcb01/spdc0/
//							
							// 폴더 생성
							// /home/hvdcData/pd/upload/121.139.36.94_102/RTTransF/20210827/
//							String uploadDir = dataUploadDir + "upload/" + iedIp + "_" + iedPort + "/" + fileType + "/" + dataTime_directory + "/";	
//							// /home/hvdcData/pd/complete/121.139.36.94_102/RTTransF/20210827/
//							String completeDir = dataUploadDir + "complete/" + iedIp + "_" + iedPort + "/" + fileType + "/" + dataTime_directory + "/";
//							
//							File uploadDirFile = new File(uploadDir);
//							File completeDirFile = new File(completeDir);
//							
//							if(!uploadDirFile.exists()) {
//								uploadDirFile.mkdirs();
//							}
//							if(!completeDirFile.exists()) {
//								completeDirFile.mkdirs();
//							}
							
							
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
							
//							String fileName_fullpath = filePathRoot + fileLocation + "/" + fileName;
							String fileName_fullpath = "C:\\MU\\G101_01\\EVENT\\2024\\03\\20\\K_J9999_GLU101_CH01_CBOP_9999001_22_20240320143400.dat";
							String uploadDir = "/home/KEPCO_iec61850/upload/gis/";
							
							// control block
							JsonObject fileObj = new JsonObject();
							fileObj.addProperty("filename", fileName_fullpath);	//	/mnt/ADC_Raw/spdc0/01_01_20210809082758.dat
							fileObj.addProperty("file_command", "DOWNLOAD"); 
							fileObj.addProperty("file_size", "");
							
							requestObject.add("command", commandObj);
							requestObject.add("file_control", fileObj);
							requestObject.addProperty("client_unique_id", clientUniqueId);
							
							requestObject.addProperty("downloadPath", uploadDir);
							String receiveString = commons.socketConnection(iedIp, iedPort, clientIp, clientPort, requestObject, reportsName, event_datetime);
							
							if (receiveString != null && !receiveString.equals("")) {
//								System.out.println("receiveString ::: " + receiveString);
								
								jsonParser = new JsonParser();
								jsonObject = (JsonObject)jsonParser.parse(receiveString);
								String status = jsonObject.get("status").getAsString();
								// String responseSecurekey = jsonObject.get("securekey").getAsString();
								String message = jsonObject.get("message").getAsString();
								
								if(status.equals("Y")) {
									
//									if(fileType.equals("HighResTransF")) {
//										
//										System.out.println("==========================================================================");
//										System.out.println("   250M 파일 다운로드 ( " + fileLocation + " )");
//										System.out.println("==========================================================================");
//										
//										if(fileLocation.equals("spdc0")) {
//											HighResTransF_spdc1 = true;
//										} else if(fileLocation.equals("spdc1")) {
//											HighResTransF_spdc2 = true;
//										}
//										
//										// spdc1, spdc2 모두 250M 파일을 다운로드 받으면 다음 Event 파일을 다운로드 받도록 true 입력
//										if(HighResTransF_spdc1 == true && HighResTransF_spdc2 == true) {
//											sharedRptVar.setIsAllowFileDownload(true);
//											System.out.println("----- setIsAllowFileDownload : true (spdc1, spdc2 모두 250M 파일을 다운로드 받음)");
//											HighResTransF_spdc1 = false;
//											HighResTransF_spdc2 = false;
//										}
//										
//										// 250M 파일일 경우에 이름변경
//										fileName = HighResTransF_filename;
//									}
									
									// 실시간 파일인 경우 ( 실시간 파일은 5초간격으로 파일을 합친 후 complete 폴더로 이동 )
//									if(fileType.equals("RTTransF")) {
//										
//										// 바로전 5초 파일들을 묶기 ( 0~4  or  5~9 )
//										Runtime runtime = Runtime.getRuntime();
//										// ex) /home/HVDC_iec61850/RealTime_SumFile.sh /home/HVDC_iec61850/hvdcData/pd/ 1.241.156.210_102 20211201 00_01_20211201154958.dat
//										String command = commonsStr.RealTime_SumFile + " " + dataUploadDir + " " + iedIp + "_" + iedPort + " " + dataTime_directory + " " + fileName;
//										Process process = runtime.exec(command);
//										
//									} else {
										// 파일 이동
//										File uploadFile = new File(uploadDir + fileName);		//	/home/hvdcData/pd/upload/121.139.36.94_102/RTTransF/20210827/01_01_20210809082758.dat
//										File completeFile = new File(completeDir + fileName);	//	/home/hvdcData/pd/complete/121.139.36.94_102/RTTransF/20210827/01_01_20210809082758.dat
//										uploadFile.renameTo(completeFile);
//									}
									
								} else {
									System.out.println("FILE_GET message (" + reportsName + ")  : " + message);
								}
								
							} else {
								System.out.println("FILE_GET message (" + reportsName + ")  : receiveString is empty");
							}
						}
					}
					
				} catch(Exception e) {
					System.out.println("--- normal Error(Reports while inner)");
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			System.out.println("--- normal Error(Reports)");
			e.printStackTrace();
		}
		System.out.println("Report Thread("+this.getId()+") is stopped!!!");
		stoptag = false;
		isrunning = false;
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

	public String getRptId() {
		return this.rptId;
	}
	
	public void setRptId(String rptId) {
		this.rptId = rptId;
	}
	
	public String getClientUniqueId() {
		return this.clientUniqueId;
	}
	
	public void setClientUniqueId(String clientUniqueId) {
		this.clientUniqueId = clientUniqueId;
	}

	public Boolean isRunning() {
		return isrunning;
	}
	
	public void setstoptag() {
		stoptag = true;
	}
}
