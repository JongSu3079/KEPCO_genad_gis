package genad1_HVDC.utils;

public class CommonsStr {
	
	// 개발용 MacBook
//	public String clientIp = "172.30.1.14";
//	public String clientPort = "8201";
//	public String clientFilePath = "/home/tnmtech/libiec61850-1.4.1/mems_hmi_server/iec61850_client_server/";
//	public String IedConfigFile = "/home/tnmtech/libiec61850-1.4.1/mems_hmi_server/iec61850_client_server/genad_stat_ied.txt";
//	public String dataUploadDir = "/home/hvdcData/genad/";
	
	
	// 테스트 실서버 (203.243.17.250)(192.168.192.50)
//	public String clientIp = "192.168.192.50";
//	public String clientPort = "8201";
	public String clientFilePath = "/home/KEPCO_iec61850/iec61850_client_server/";
//	public String IedConfigFile = "/home/KEPCO_iec61850/iec61850_client_server/genad_stat_ied.txt";
	public String dataUploadDir = "/home/KEPCO_iec61850/kepcoData/gis/pd";
	
//	public String clientFileName = "start_stat_61850.sh"; // 실행파일 -> iec61850_client_server 
//	public String clientAliveCommand = "ps -ef | grep iec61850_client_server | grep " + clientPort + " | grep -v grep";
//	public String filePathRoot = "/mnt/ADC_Raw/";
	public String filePathRoot = "C:/MU";
	
	public String RealTime_SumFile = "/home/KEPCO_iec61850/RealTime_SumFile.sh";
}
