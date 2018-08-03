package net.termer.twurl.handler;

import java.net.URL;
import java.util.Set;

import net.termer.twister.Twister;
import net.termer.twister.handler.RequestHandler;
import net.termer.twister.utils.Config;
import net.termer.twister.utils.StringFilter;
import net.termer.twurl.Captcha;
import net.termer.twurl.Module;
import spark.Request;
import spark.Response;

public class NewURLPostHandler implements RequestHandler {
	private Response resp = null;
	
	public String handle(Request req, Response res) {
		resp = res;
		
		if(Captcha.isCorrect(req.queryParams("captcha"), req)) {
			String url = req.queryParams("url");
			if(url == null) {
				error("You must enter a url");
			} else {
				try {
					// Check whether the URL is valid
					new URL(url);
					
					boolean aliasOk = true;
					
					String alias = req.queryParams("alias");
					if(alias.length()<1) {
						alias = StringFilter.generateString(5);
					} else {
						if(alias.length()<21) {
							if(StringFilter.acceptableString(alias)) {
								// Get redirect aliases
								Set<String> aliases = Config.parseConfig(Module.REDIRECTS_FILE, ">", "#").keySet();
								if(aliases.contains(alias)) {
									aliasOk = false;
									error("The alias you requested is already taken");
								}
							} else {
								aliasOk = false;
								error("Aliases may only contain letters, numbers, and underscores");
							}
						} else {
							aliasOk = false;
							error("Aliases may be a maximum of 20 characters long");
						}
					}
					
					if(aliasOk) {
						Config.addField(Module.REDIRECTS_FILE, "/"+alias, url, ">", "#");
						Twister.current().reloadConfigurations();
						success("Your URL is now shortened at "+Module.DOMAIN+"/"+alias);
					}
				} catch(Exception e) {
					error("The provided URL is invalid!");
				}
			}
		} else {
			error("You did not solve the captcha correctly");
		}
		return "";
	}
	
	private void error(String msg) {
		resp.redirect("?error="+StringFilter.encodeURIComponent(msg));
	}
	private void success(String msg) {
		resp.redirect("?success="+StringFilter.encodeURIComponent(msg));
	}
}
