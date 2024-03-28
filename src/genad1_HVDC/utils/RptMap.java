package genad1_HVDC.utils;

import java.util.Map;

public class RptMap {
	private String UID;
	private String IEDIP;
	private int IEDPORT;
	private String rptId;
	
//	private Events events;
	private Reports reports;
	private ReportReceiver reportRecv;
	private RptVar rptVar;
	
	public String getUID() {
		return UID;
	}

	public void setUID(String uID) {
		UID = uID;
	}

	public String getIEDIP() {
		return IEDIP;
	}

	public void setIEDIP(String iEDIP) {
		IEDIP = iEDIP;
	}

	public int getIEDPORT() {
		return IEDPORT;
	}

	public void setIEDPORT(int iEDPORT) {
		IEDPORT = iEDPORT;
	}
	
	public String getRptId() {
		return rptId;
	}

	public void setRptId(String rptId) {
		this.rptId = rptId;
	}

	public RptMap() {
		UID = "";
		IEDIP = "";
		IEDPORT = 0;
		rptId = "";
		// reports = new Reports();
		// rptVar = new RptVar();
	}
	
	public synchronized void setup(String uid, String ip, int i, String rid, String clientIp, int clientPort, String reportsName, Map _eeNameMap) {
		UID = uid;
		IEDIP = ip;
		IEDPORT = i;
		rptId = rid;
		
//		events.setClientUniqueId(UID);
//		events.setRptId(rid);
//		events.setIedIp(ip);
//		events.setIedPort(i);
//		events.setClientIp(clientIp);
//		events.setClientPort(clientPort);
//		events.setReportsName(reportsName);
		
		reports.setClientUniqueId(UID);
		reports.setRptId(rid);
		reports.setIedIp(ip);
		reports.setIedPort(i);
		reports.setClientIp(clientIp);
		reports.setClientPort(clientPort);
		reports.setReportsName(reportsName);
		
		reportRecv.setRptId(rid);
		
		rptVar = new RptVar(_eeNameMap);
		
//		events.setRptVar(rptVar);
		reports.setRptVar(rptVar);
		reportRecv.setRptVar(rptVar);
	}
	
	public boolean matches(String uid, String ip, int port) {
		return UID.equals(uid) && IEDIP.equals(ip) && (IEDPORT == port);
	}
	
	public boolean matches(String uid, String ip, int port, String rid) {
		return UID.equals(uid) && IEDIP.equals(ip) && (IEDPORT == port) && rptId.equals(rid);
	}
	
	public boolean matches(RptMap target) {
		return matches(target.getUID(), target.getIEDIP(), target.getIEDPORT(), target.getRptId());
	}
	
	public Reports getReports() {
		return reports;
	}
	
//	public Events getEvents() {
//		return events;
//	}
	
	public synchronized ReportReceiver getReportRecv() {
		return reportRecv;
	}
	
	public synchronized RptVar getRptVar() {
		return rptVar;
	}
	
//	public synchronized void setEvents(Events _events) {
//		if (events != null)
//			events.interrupt();
//		events = _events;
//	}
	
	public synchronized void setReports(Reports rpt) {
		if (reports != null)
			reports.interrupt();
		reports = rpt;
	}
	
	public synchronized void setReportRecv(ReportReceiver reportReciever) {
		if (reportRecv != null)
			reportRecv.interrupt();
		reportRecv = reportReciever;
	}

}
