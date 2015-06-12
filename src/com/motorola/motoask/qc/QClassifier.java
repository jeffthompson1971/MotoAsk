package com.motorola.motoask.qc;
import com.motorola.motoask.Datastore;
import com.motorola.motoask.Constants;
import com.motorola.motoask.QuestionServlet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.api.services.prediction.*;
import com.google.gson.Gson;
import com.google.storage.*;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

public class QClassifier {
	String mQuestion;
	LinkedHashMap<String, Pattern> compRegex;	
	public static final String BUCKETNAME = "motoask";
	public static final String FILENAME = "cq_data.txt";		    
	
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
		// TODO
		// Update model on the fly (and store training example to file
		// in Cloud Storage for later use.
	}
	
	public void trainModel() throws IOException {
		FileService fileService = FileServiceFactory.getFileService();
		GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
		.setBucket(BUCKETNAME)
		.setKey(FILENAME)
		.setMimeType("text/html")
		.setAcl("public_read")
		.addUserMetadata("myfield1", "my field value");
		AppEngineFile writableFile =
				fileService.createNewGSFile(optionsBuilder.build());
		boolean lock = false;
		FileWriteChannel writeChannel =
				fileService.openWriteChannel(writableFile, lock);
		PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
		JSONArray acceptedQs  = Datastore.getQuestionsInState(Constants.ACCEPTED_STATE_NAME);
		Gson gson = new Gson();
		for (int i = 0; i < acceptedQs.length(); i++) {
			JSONObject qRecord = acceptedQs.getJSONObject(i);
			String q = qRecord.getString(QuestionServlet.PARAMETER_Q);
			String qClass = qRecord.getString(QuestionServlet.PARAMETER_Q_TOPICS);
			// Write one line at a time? Need Cloud Storage API.
     		out.println("\"" + qClass + "\",\"" + q + "\"");
		}
		// Close without finalizing and save the file path for writing later
		out.close();
		String path = writableFile.getFullPath();
		// Write more to the file in a separate request:
		writableFile = new AppEngineFile(path);
		// Lock the file because we intend to finalize it and
		// no one else should be able to edit it
		lock = true;
		writeChannel = fileService.openWriteChannel(writableFile, lock);
		// This time we write to the channel directly
		writeChannel.write(ByteBuffer.wrap
				("And miles to go before I sleep.".getBytes()));

		// Now finalize
		writeChannel.closeFinally();		
	}
	
	public static void main( String args[] ){
		
	}
}
