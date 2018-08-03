package net.termer.twurl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.termer.twurl.Captcha;
import net.termer.twister.Twister;
import net.termer.twister.document.DocumentProcessor;
import net.termer.twister.document.HTMLDocumentResponse;
import net.termer.twister.module.ModulePriority;
import net.termer.twister.module.TwisterModule;
import net.termer.twister.utils.Config;
import net.termer.twister.utils.Method;
import net.termer.twister.utils.Writer;
import net.termer.twurl.handler.NewURLPostHandler;
import spark.Request;
import spark.Response;

public class Module implements TwisterModule {
	public static String[] BANNED_ALIASES = {"new", "about", "rules", "contact"};
	public static String DOMAIN = "";
	public static File REDIRECTS_FILE = null;
	
	private DocumentProcessor docPros = new DocumentProcessor() {
		public void process(HTMLDocumentResponse doc, Request req, Response res) {
			try {
				if(doc.getText().contains("%captcha")) {
					doc.replace("%captcha", Captcha.getCaptchaImageURL(req));
				}
				
				String error = "";
				if(req.queryParams("error") != null) {
					error = req.queryParams("error");
				}
				
				String success = "";
				if(req.queryParams("success") != null) {
					success = req.queryParams("success");
				}
				
				doc.replace("%error", error);
				doc.replace("%success", success);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	public String moduleName() {
		return "Twurl";
	}
	
	public double twiserVersion() {
		return 0.2;
	}
	
	public int modulePriority() {
		return ModulePriority.LOW;
	}
	
	public void initializeModule(Twister instance) {
		// Create config
		File config = new File("twurl.ini");
		
		if(!config.exists()) {
			try {
				Writer.print("# Paths users are not allowed to register\n"
						+ "# Separate paths with \",\"\n"
						+ "# For example: rules,about,somepath\n"
						+ "banned-paths: \n\n"
						+ "# The domains to bind this module to\n"
						+ "domain: localhost", config);
			} catch (IOException e) {
				System.err.println("Failed to write config file");
				e.printStackTrace();
			}
		}
		
		try {
			// Parse config
			HashMap<String,String> configMap = Config.parseConfig(config, ":", "#");
			String bannedPathsStr = configMap.get("banned-paths");
			if(bannedPathsStr.length()>0) {
				if(bannedPathsStr.contains(",")) {
					BANNED_ALIASES = bannedPathsStr.split(",");
				} else {
					BANNED_ALIASES = new String[] {bannedPathsStr};
				}
			}
			DOMAIN = configMap.get("domain");
			
			// Create redirects.ini if not present
			REDIRECTS_FILE = new File("domains/"+DOMAIN+"/redirects.ini");
			if(!REDIRECTS_FILE.exists()) {
				REDIRECTS_FILE.createNewFile();
			}
		} catch (IOException e) {
			System.err.println("Failed to load config");
			e.printStackTrace();
		}
		
		// Register RequestHandlers
		instance.addRequestHandler(DOMAIN, "/new/", new NewURLPostHandler(), Method.POST);
		
		// Register DocumentProcessors
		instance.addDocumentProcessor(DOMAIN, docPros);
	}
	
	public void shutdownModule() {
		Twister.current().removeRequestHandler(DOMAIN, "/new/", Method.POST);
		Twister.current().removeDocumentProcessor(DOMAIN, docPros);
	}
}
