package com.motorola.motoask.qc;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.api.services.prediction.*;
import com.google.storage.*;


public class QClassifier {
	String mQuestion;
	LinkedHashMap<String, Pattern> compRegex;	
	
	public QClassifier(String question) {
		mQuestion = question;
	
		// This really should only be done statically (once for the whole class).
	    compRegex = new LinkedHashMap<String, Pattern>();
	    compRegex.put("active_display", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(active[ -]*display|aod|always[ -]*on[ -]*display)($|[^a-zA-Z0-9])"));
	    compRegex.put("assist",	Pattern.compile("(?i)(^|[^a-zA-Z0-9])assist($|[^a-zA-Z0-9])"));
	    compRegex.put("audio", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(audio|sound|music|songs?|speakers?|headphones?)($|[^a-zA-Z0-9])"));
	    compRegex.put("battery", Pattern.compile("(?i)(^|[^a-zA-Z0-9])battery($|[^a-zA-Z0-9])"));
	    compRegex.put("bluetooth", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(bluetooth[0-9]?)($|[^a-zA-Z0-9])"));
	    compRegex.put("display", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(display|screen)($|[^a-zA-Z0-9])"));
	    compRegex.put("gps", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(gps)($|[^a-zA-Z0-9])"));
	    compRegex.put("intro", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(intro|introduction|getting[ -]*started)($|[^a-zA-Z0-9])"));
	    compRegex.put("memory",	Pattern.compile("(?i)(^|[^a-zA-Z0-9])(memory|storage|sd|ram)($|[^a-zA-Z0-9])"));
	    compRegex.put("motomaker", Pattern.compile("(?i)(^|[^a-zA-Z0-9])moto *maker($|[^a-zA-Z0-9])"));
	    compRegex.put("pictures", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(cameras?|lens|pictures?|photos?|gallery)($|[^a-zA-Z0-9])"));
	    compRegex.put("shell", Pattern.compile("(?i)(^|[^a-zA-Z0-9])((moto[ -]*)?shells?|back[ -]*covers?|rear[ -]*covers?|cases?)($|[^a-zA-Z0-9])"));
	    compRegex.put("touchless_control", Pattern.compile("(?i)(^|[^a-zA-Z0-9])touchless[ -]*controls?($|[^a-zA-Z0-9])"));
	    compRegex.put("update",	Pattern.compile("(?i)(^|[^a-zA-Z0-9])(upgrade[sd]?|update[sd]?)($|[^a-zA-Z0-9])"));
	    compRegex.put("usb", Pattern.compile("(?i)(^|[^a-zA-Z0-9])(usb[0-9]?)($|[^a-zA-Z0-9])"));
	    compRegex.put("wifi", Pattern.compile("(?i)(^|[^a-zA-Z0-9])wi-*fi($|[^a-zA-Z0-9])"));
	}

	public QClassification classify() {
		Set regexSet = compRegex.entrySet();
		Iterator it = regexSet.iterator();

		int winnerMatches = 0;
		int totalMatches = 0;
		String bestClass = "unknown";
		while (it.hasNext())
		{
		    Map.Entry<String, Pattern> me = (Map.Entry)it.next();
		    String candidateClass = me.getKey();
		    Pattern regex = me.getValue();
		    Matcher m = regex.matcher(mQuestion);
		    int matchCount = 0;
		    while (m.find()) {
		        matchCount++;
		        totalMatches++;
		    } 
		    if (matchCount > winnerMatches) {
		    	bestClass = candidateClass;
		    }
		}
		
		double confidence = 1.0;
		if (! bestClass.equals("unknown")) {
			confidence = (double)winnerMatches/(double)totalMatches;
		}
		QClassification classy = new QClassification(bestClass, confidence);
		return classy; // San Diego
	}
	
	public void learn(String question, String classification) {
		// Append training example to file in Cloud Storage
	}
	
	public void cram(String question, String classification) {
		// Update model on the fly (and store training example to file
		// in Cloud Storage for later use.
	}
	
	public void trainModel() {
	    	
	}
	
	public static void main( String args[] ){
		
	}
}
