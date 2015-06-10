package com.motorola.motoask.qc;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.LinkedHashMap;
import com.google.api.services.prediction.*;

public class QClassifier {
	String mQuestion;
	
	public QClassifier(String question) {
		mQuestion = question;
	    LinkedHashMap<String, String> compRegex = new LinkedHashMap<String, String>();
	    compRegex.put("active_display", "(?i)(^|[^a-zA-Z0-9])(active[ -]*display|aod|always[ -]*on[ -]*display)($|[^a-zA-Z0-9])");
	    compRegex.put("assist",	"(?i)(^|[^a-zA-Z0-9])assist($|[^a-zA-Z0-9])");
	    compRegex.put("audio", "(?i)(^|[^a-zA-Z0-9])(audio|sound|music|songs?|speakers?|headphones?)($|[^a-zA-Z0-9])");
	    compRegex.put("battery", "(?i)(^|[^a-zA-Z0-9])battery($|[^a-zA-Z0-9])");
	    compRegex.put("bluetooth", "(?i)(^|[^a-zA-Z0-9])(bluetooth[0-9]?)($|[^a-zA-Z0-9])");
	    compRegex.put("display", "(?i)(^|[^a-zA-Z0-9])(display|screen)($|[^a-zA-Z0-9])");
	    compRegex.put("gps", "(?i)(^|[^a-zA-Z0-9])(gps)($|[^a-zA-Z0-9])");
	    compRegex.put("intro", "(?i)(^|[^a-zA-Z0-9])(intro|introduction|getting[ -]*started)($|[^a-zA-Z0-9])");
	    compRegex.put("memory",	"(?i)(^|[^a-zA-Z0-9])(memory|storage|sd|ram)($|[^a-zA-Z0-9])");
	    compRegex.put("motomaker", "(?i)(^|[^a-zA-Z0-9])moto *maker($|[^a-zA-Z0-9])");
	    compRegex.put("pictures", "(?i)(^|[^a-zA-Z0-9])(cameras?|lens|pictures?|photos?|gallery)($|[^a-zA-Z0-9])");
	    compRegex.put("shell", "(?i)(^|[^a-zA-Z0-9])((moto[ -]*)?shells?|back[ -]*covers?|rear[ -]*covers?|cases?)($|[^a-zA-Z0-9])");
	    compRegex.put("touchless_control", "(?i)(^|[^a-zA-Z0-9])touchless[ -]*controls?($|[^a-zA-Z0-9])");
	    compRegex.put("update",	"(?i)(^|[^a-zA-Z0-9])(upgrade[sd]?|update[sd]?)($|[^a-zA-Z0-9])");
	    compRegex.put("usb", "(?i)(^|[^a-zA-Z0-9])(usb[0-9]?)($|[^a-zA-Z0-9])");
	    compRegex.put("wifi", "(?i)(^|[^a-zA-Z0-9])wi-*fi($|[^a-zA-Z0-9])");
	}

	public String classify() {
		return "unknown";
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
