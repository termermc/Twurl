package net.termer.twurl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.github.cage.Cage;
import com.github.cage.GCage;

import net.termer.twister.utils.StringFilter;
import spark.Request;

/**
 * Class to generate and check captchas
 * @author termer
 * @since 1.0
 */
public class Captcha {
	
	/**
	 * Returns a new captcha image as a PNG data URL
	 * @param req the user's request
	 * @return the new captcha image as PNG data URL
	 * @throws IOException if creating the image fails
	 * @since 1.0
	 */
	public static String getCaptchaImageURL(Request req) throws IOException {
		// Get captcha text
		int length = new Random().nextInt(5)+5;
		String text = StringFilter.generateString(length);
		req.session(true).attribute("captcha", text);
		
		// Generate image
		Cage cage = new GCage();
		BufferedImage img = cage.drawImage(text);
		
		// Create data URL
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		return "data:image/png;base64," +
		    DatatypeConverter.printBase64Binary(baos.toByteArray());
	}
	
	/**
	 * Checks whether the provided text is matches the captcha
	 * @param text the text
	 * @param req the user's request
	 * @return whether the provided text is correct
	 * @since 1.0
	 */
	public static boolean isCorrect(String text, Request req) {
		boolean correct = false;
		
		if(text != null && req.session(true).attribute("captcha") != null) {
			correct = StringFilter.same(text, (String)req.session().attribute("captcha"));
		}
		
		// Clear captcha string
		req.session().attribute("captcha", null);
		
		return correct;
	}
}