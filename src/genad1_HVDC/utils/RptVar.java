package genad1_HVDC.utils;

import java.util.ArrayList;

import genad1_HVDC.queue.LinkedListQueue;

public class RptVar {
	private ArrayList<String> _reports;
	private int cursor;
	
	private volatile boolean recvFin = false;
	// private boolean rptFin = false;
	
	// 2개의 250M 레포트가  왔을때(getIsAllowFileDownload()==true)
	private Boolean isAllowFileDownload = true;
	
	// report 데이터 저장
	LinkedListQueue<String> queue;
	
	// event report 데이터 저장
	LinkedListQueue<String> eventQueue;
	
	// Event파일과 250M파일의 이름을 동일하게 생성하기위해 사용
	private String event_datetime = "";
	

	public RptVar() {
		_reports = new ArrayList<String>();
		queue = new LinkedListQueue<>();
		eventQueue = new LinkedListQueue<>();
		cursor = 0;
		recvFin = false;
	}
	
	public String getEvent_datetime() {
		return event_datetime;
	}

	public void setEvent_datetime(String event_datetime) {
		this.event_datetime = event_datetime;
	}

	public synchronized void recvFinish() {
		recvFin = true;
	}
	
	public synchronized boolean isRecvFinish() {
		return recvFin;
	}
	
	public synchronized void setIsAllowFileDownload(boolean vlaue) {
		isAllowFileDownload = vlaue;
	}
	
	public synchronized boolean getIsAllowFileDownload() {
		return isAllowFileDownload;
	}
	
	public synchronized boolean isReadyForRpt() {
		
		return !(isFirstRun() || isMax());
	}
	
	public synchronized boolean isMax() {
		return cursor == _reports.size(); // 0 == 0, 
	}

	public synchronized Boolean isFirstRun() {
		return _reports.isEmpty();
	}
	
	public synchronized void addReport(String report) {
		_reports.add(report);
	}
	
	public synchronized ArrayList<String> getAll() { // 현재까지 저장되어 있는 모든 데이터를 받기
		cursor = _reports.size() - 1;
		return _reports;
	}
	
	public synchronized ArrayList<String> getRange(int startidx, int endidx) { // 레포트 중에서 영역으로 받기
		cursor = endidx;
		ArrayList<String> returnData = new ArrayList<String>();
		for (int idx = startidx; idx < endidx; idx++)
			returnData.add(_reports.get(idx));
		return returnData;
	}
	
	public synchronized String getLastest() {
		return _reports.get(_reports.size() - 1);
	}
	
	public synchronized String getOneRpt() {
		String oneRpt = "";
		if (isMax()) oneRpt = "max";
		else {
			oneRpt = _reports.get(cursor);
			cursor++;
		}
		return oneRpt;
	}
	
	public synchronized ArrayList<String> getFromCursor() {
		ArrayList<String> returnData = new ArrayList<String>();
		int lastidx = _reports.size();
		for (int idx = cursor; idx < lastidx; idx++) {
			returnData.add(_reports.get(idx));
		}
		cursor = lastidx;
		return returnData;
	}
	
	public synchronized int getCursor() { // Controller에서 데이터를 받을 때, 현재까지 받은 부분을 확인하기 위해 필요한 함수
		return cursor;
	}
	
	public synchronized int getReportsCount() { // 현재 저장되어 있는 레포트 갯수
		return _reports.size();
	}

	// [ Reports Queue]
	public synchronized void addReportQueue(String report) {
		queue.offer(report);
	}
	public synchronized String getOneRptQueue() {
		return queue.poll();
	}
	public synchronized int getCountReportsQueue() {
		return queue.getSize();
	}
	public synchronized boolean isEmptyQueue() {
		return (queue.getSize() == 0);
	}
	
	// [ Event Queue]
	public synchronized void addEventQueue(String event) {
		eventQueue.offer(event);
	}
	public synchronized String getOneEventQueue() {
		return eventQueue.poll();
	}
	public synchronized int getCountEventQueue() {
		return eventQueue.getSize();
	}
	public synchronized boolean isEmptyEventQueue() {
		return (eventQueue.getSize() == 0);
	}
	public synchronized String getOneEventQueueNotDelete() {
		return eventQueue.peek();
	}

}
