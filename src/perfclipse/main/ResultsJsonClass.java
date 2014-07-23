package perfclipse.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

//import javax.json.Json;

import perfclipse.perforations.PerforatedLoop;

//import org.json.simple.JSONObject;

public class ResultsJsonClass {
	
	private static JSONObject results = new JSONObject();
	
	public static boolean addResults(Results orig, Results perf) {
		DateFormat df = new SimpleDateFormat("yyyyMMddhhmm");
		String date = df.format(System.currentTimeMillis());
		
		try {
			JSONObject original = new JSONObject();
			original.put("Elapsed Time", orig.ElapsedTime);
			original.put("SpeedUp", orig.Speedup);
			original.put("Quality of Service", orig.QualityOfService);
		
			original.put("Perforated Loops", getLoops(orig.PerforatedLoops));
			
			JSONObject perforated = new JSONObject();
			original.put("Elapsed Time", perf.ElapsedTime);
			original.put("SpeedUp", perf.Speedup);
			original.put("Quality of Service", perf.QualityOfService);
			original.put("Perforated Loops", getLoops(perf.PerforatedLoops));
			
			JSONObject combined = new JSONObject();
			combined.put("Original Run", original);
			combined.put("Perforated Run", perforated);
			
			results.put(date, combined);
			return writeToFile();
			
		} catch (JSONException e) {
			return false;
		}
		
	}
	
	private static boolean writeToFile() {
		StringWriter out = new StringWriter();
		try {
			results.write(out);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String filename = "PerforationMultiLoopResults.json";
		File resultsOut = new File(filename);
		
		try {
			if (!resultsOut.exists()) {
				resultsOut.createNewFile();
			}
			FileWriter fw = new FileWriter(resultsOut, true);
			fw.write(out.toString());
			fw.close();
			return true;
		} catch (IOException e) {
			return false;
		}

		
	}

	private static ArrayList<String> getLoops(List<PerforatedLoop> loops) {
		ArrayList<String> array = new ArrayList<String>();
		for (PerforatedLoop loop: loops) {
			array.add(loop.getClassName() + ": " + loop.getName());
		}
		return array;
	}
	
	public ResultsJsonClass() {
		JSONObject obj = new JSONObject();

	      try {
			obj.put("name", "foo");
			obj.put("num", new Integer(100));
	      obj.put("balance", new Double(1000.21));
	      obj.put("is_vip", new Boolean(true));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      

	      System.out.print(obj);
	}

}
