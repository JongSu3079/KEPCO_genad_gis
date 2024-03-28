package genad1_HVDC.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import genad1_HVDC.queue.LinkedListQueue;

public class ReportReceiver extends Thread {

	private RptVar sharedRptVar;
	
	private Boolean stoptag = false;
	private Boolean isrunning = false;
	private Socket socket = null;
	private int retryCount = 0;
	
	private int countJSONmarker = 0;
	private String response = null;
	private String wholeCopy = null;
	private String finalMsg = null;
	
	private int MAX_BUFFER_SIZE = 1024 * 1024 * 2; // 2 MB

	private String rptId = "";
	
	public ReportReceiver() {
		stoptag = false;
		isrunning = false;
		socket = null;
		retryCount = 0;
		
		countJSONmarker = 0;
		response = null;
		wholeCopy = null;
		finalMsg = null;
	}
	
	public String getRptId() {
		return rptId;
	}

	public void setRptId(String rptId) {
		this.rptId = rptId;
	}

	public void setRptVar(RptVar rptVar) {
		sharedRptVar = rptVar;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void setSocket(Socket socket) {
		System.out.println("setSocket() ::: " + socket + "[" + socket.isConnected() + "]");
		this.socket = socket;
	}

	public Boolean isRunning() {
		return isrunning;
	}
	
	public void setstoptag() {
		stoptag = true;
	}

	public void run() {
		try {
			System.out.println("!!!!!=============== REPORT RECIEVER ( rptId : " + rptId + " ) (" + this.getId() + ") =================!!!!!");
			System.out.println("ReportRecv id (" + this.getId() + ") started to listen");
			
			socket.setSoTimeout(0);
			isrunning = true;
			
			String wholeMsg = "";
			
			while (true) {
				if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
					System.out.println("Socket is closed Report Thread("+this.getId()+") will be closed");
				}
//				System.out.println("Report Obj start get inputstream with socket("+socket+") ( stoptag : " + stoptag + " ) ( rptId : " + rptId + " )");
				
				byte[] buffer = new byte[MAX_BUFFER_SIZE];
				if (stoptag) {
					socket.setSoTimeout(1000);
				}
				
				int socReadStatus = socket.getInputStream().read(buffer);
				
				if (socReadStatus <= 0) {
//					System.out.println("Buffer has " + new String(buffer) + ", size : " + buffer.length);
					System.out.println("Buffer has size : " + buffer.length);
					System.out.println("Socket Thread returned irregular input stream size : " + socReadStatus);
					
					// stoptag = true;
					if (stoptag) {
						sharedRptVar.recvFinish();
						SocketTimeoutException e = new SocketTimeoutException();
						throw e;
					} else {
						System.out.println("retryCount : " + retryCount + " ( rptId : " + rptId + " )");
						if (retryCount > 5) {
							sharedRptVar.recvFinish();
							SocketTimeoutException e = new SocketTimeoutException();
							throw e;
						}
						retryCount++;
					}
				}

				response = new String(buffer).trim(); // trimming
				wholeMsg = wholeMsg + response;
				wholeCopy = wholeMsg;
				
//				System.out.println("----------------- wholeMsg -----------------------------------");
//				System.out.println(wholeMsg);
//				System.out.println("----------------------------------------------------");

				// for pruning or extending
				finalMsg = "";
				byte[] wholeBuffer = wholeMsg.getBytes();
				
				int startidxforsub = 0;
				countJSONmarker = 0;
				
				for (int i = 0; i < wholeMsg.length(); i++) {
					byte ch = wholeBuffer[i];
					
					if (ch == '{') countJSONmarker++;
					else if (ch == '}') {
						countJSONmarker--;
						// else continue;
						if (countJSONmarker == 0) {
							finalMsg = wholeCopy.substring(startidxforsub, i+1);
							startidxforsub = i+1;
							
							// _reports.add(finalMsg);							
//							sharedRptVar.addReport(finalMsg);
							
							// EvtTransF, TrendTransF, RTTransF
							if(finalMsg.contains("EvtTransF") || finalMsg.contains("TrendTransF") || finalMsg.contains("RTTransF")) {
								sharedRptVar.addReportQueue(finalMsg);
							}
							
//							System.out.println("rpt added to sharedRptVar Report");
//							System.out.println("----------- finalMsg (" + startidxforsub + ") (rptId : " + rptId + ") --------------------");
//							System.out.println(finalMsg);
//							System.out.println("----------------------------------------------------");
						}
					}
				}
				if (countJSONmarker != 0) {
					wholeMsg = wholeMsg.substring(startidxforsub);
				} else {
					wholeMsg = "";
				}
			}
		} catch (Exception e) {
			System.out.println("--- normal Error(ReportReceiver)");
			e.printStackTrace();
		} finally {
			try {
				if (socket != null) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("ReportRecv Thread("+this.getId()+") is stopped!!!");
		sharedRptVar.recvFinish();
		stoptag = false;
		isrunning = false;
	}

}
