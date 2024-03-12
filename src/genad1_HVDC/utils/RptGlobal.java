package genad1_HVDC.utils;

import java.util.ArrayList;


public class RptGlobal {
	private static RptGlobal rptGlobal = null;
	private ArrayList<RptMap> rptMap;
	
	public static RptGlobal getInstance() {
		if (rptGlobal == null) rptGlobal = new RptGlobal();
		return rptGlobal;
	}
	
	RptGlobal() {
		rptMap = new ArrayList<RptMap>();
	}
	
	public ArrayList<RptMap> getRptMap() {
		return rptMap;
	}
}
