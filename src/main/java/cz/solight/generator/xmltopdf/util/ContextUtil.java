/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.solight.generator.xmltopdf.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import cz.solight.generator.xmltopdf.service.OfferPdfGenerator;

import name.berries.wicket.util.app.AppConfigProvider;
import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

/**
 * @author vit
 */
public class ContextUtil
{
	private static final Logger log = LoggerFactory.getLogger(ContextUtil.class);

	/** Image paths for logo and wave. */
	private static final String IMG_LOGO_OFFER = "/webapp/img/offer-logo.png";
	/** Image paths for logo and wave. */
	private static final String IMG_VLNKA_OFFER = "/webapp/img/offer-vlnka.png";
	/** Image paths for logo and wave. */
	private static final String IMG_LOGO_PS = "/webapp/img/product-sheet-logo.png";
	/** Image paths for logo and wave. */
	private static final String IMG_VLNKA_PS = "/webapp/img/product-sheet-vlnka.png";

	// Cache to store Base64 strings so we don't read disk on every request
	private static final Map<String, String> FONT_CACHE = new ConcurrentHashMap<>();

	// Enum to keep definitions clean and mapping to filenames strict
	private enum ExoFont
	{
		/** */
		BLACK("Exo2-Black.ttf", "Exo2_Black"),
		/** */
		BOLD("Exo2-Bold.ttf", "Exo2_Bold"),
		/** */
		BOLD_CONDENSED("Exo2-BoldCondensed.ttf", "Exo2_BoldCondensed"),
		/** */
		BOLD_ITALIC("Exo2-BoldItalic.ttf", "Exo2_BoldItalic"),
		/** */
		EXTRA_BOLD("Exo2-ExtraBold.ttf", "Exo2_ExtraBold"),
		/** */
		EXTRA_BOLD_ITALIC("Exo2-ExtraBoldItalic.ttf", "Exo2_ExtraBoldItalic"),
		/** */
		EXTRA_LIGHT("Exo2-ExtraLight.ttf", "Exo2_ExtraLight"),
		/** */
		EXTRA_LIGHT_ITALIC("Exo2-ExtraLightItalic.ttf", "Exo2_ExtraLightItalic"),
		/** */
		LIGHT_CONDENSED("Exo2-LightCondensed.ttf", "Exo2_LightCondensed"),
		/** */
		REGULAR("Exo2-Regular.ttf", "Exo2_Regular"),
		/** */
		REGULAR_CONDENSED("Exo2-RegularCondensed.ttf", "Exo2_RegularCondensed"),
		/** */
		SEMI_BOLD_CONDENSED("Exo2-SemiBoldCondensed.ttf", "Exo2_SemiBoldCondensed");

		final String fileName;
		final String contextKey;

		ExoFont(String fileName, String contextKey)
		{
			this.fileName = fileName;
			this.contextKey = contextKey;
		}
	}

	/**
	 * Adds common values to the context.
	 *
	 * @param context
	 */
	public static void addCommonValues(HashMap<String, Object> context)
	{
		context.put("baseUrl", AppConfigProvider.getDefaultConfiguration().getString(ConfigKey.APP_BASE_URL));
		context.put("baseImagesUrl", AppConfigProvider.getDefaultConfiguration().getString(ConfigKey.APP_BASE_IMAGES_URL));

		// Base64 encoded images for header/footer templates (Playwright doesn't load external
		// images)
		context.put("offerLogoBase64", loadImageAsBase64DataUrl(IMG_LOGO_OFFER));
		context.put("offerVlnkaBase64", loadImageAsBase64DataUrl(IMG_VLNKA_OFFER));
		context.put("psLogoBase64", loadImageAsBase64DataUrl(IMG_LOGO_PS));
		context.put("psVlnkaBase64", loadImageAsBase64DataUrl(IMG_VLNKA_PS));

		populateFontContext(context);

	}

	/**
	 * Populates the Velocity context with Base64 encoded font strings.
	 *
	 * @param context
	 */
	private static void populateFontContext(Map<String, Object> context)
	{
		for (ExoFont font : ExoFont.values())
		{
			String base64Content = FONT_CACHE.computeIfAbsent(font.fileName, ContextUtil::loadAndEncodeFont);
			if (base64Content != null)
			{
				context.put(font.contextKey, base64Content);
			}
		}
	}

	private static String loadAndEncodeFont(String fileName)
	{
		String resourcePath = "templates/fonts/" + fileName;

		try (InputStream inputStream = OfferPdfGenerator.class.getResourceAsStream(resourcePath))
		{
			if (inputStream == null)
			{
				log.error("Font resource not found: {}", resourcePath);
				return null;
			}
			byte[] fontBytes = inputStream.readAllBytes(); // Java 9+ method
			return Base64.getEncoder().encodeToString(fontBytes);
		}
		catch (IOException e)
		{
			log.error("Failed to load font: {}", fileName, e);
			return null;
		}
	}

	/**
	 * Generates an EAN barcode image and returns it as a base64 data URL. Supports EAN-13 (13
	 * digits) and EAN-8 (8 digits).
	 *
	 * @param ean
	 *            the EAN code string
	 * @return base64 data URL string, or empty string if generation fails
	 */
	public static String generateEanBarcodeBase64(String ean)
	{
		if (ean == null || ean.isBlank())
		{
			return "";
		}

		var trimmed = ean.trim();

		try
		{
			BarcodeFormat format;
			if (trimmed.length() == 13)
			{
				format = BarcodeFormat.EAN_13;
			}
			else if (trimmed.length() == 8)
			{
				format = BarcodeFormat.EAN_8;
			}
			else
			{
				log.warn("Unsupported EAN length {}: '{}'", trimmed.length(), trimmed);
				return "";
			}

			var bitMatrix = new MultiFormatWriter().encode(trimmed, format, 250, 50);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);


			// int barcodeWidth = 250;
			// int barcodeHeight = 50;
			// int textHeight = 18;
			// int totalHeight = barcodeHeight + textHeight;
			//
			// var bitMatrix = new MultiFormatWriter().encode(trimmed, format, barcodeWidth,
			// barcodeHeight);
			// BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
			//
			// var image = new BufferedImage(barcodeWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
			// Graphics2D g = image.createGraphics();
			// g.setColor(Color.WHITE);
			// g.fillRect(0, 0, barcodeWidth, totalHeight);
			// g.drawImage(barcodeImage, 0, 0, null);
			//
			// g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// g.setColor(Color.BLACK);
			// g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			// var fm = g.getFontMetrics();
			// int textX = (barcodeWidth - fm.stringWidth(trimmed)) / 2;
			// g.drawString(trimmed, textX, barcodeHeight + fm.getAscent());
			// g.dispose();

			var baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			var base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
			return "data:image/png;base64," + base64;
		}
		catch (Exception e)
		{
			log.error("Failed to generate barcode for EAN: {}", trimmed, e);
			return "";
		}
	}

	/**
	 * Loads an image from the classpath and converts it to a base64 data URL.
	 *
	 * @param resourcePath
	 *            the classpath resource path
	 * @return base64 data URL string
	 */
	private static String loadImageAsBase64DataUrl(String resourcePath)
	{
		try (InputStream is = OfferPdfGenerator.class.getResourceAsStream(resourcePath))
		{
			if (is == null)
			{
				log.warn("Image not found: {}", resourcePath);
				return "";
			}
			var bytes = is.readAllBytes();
			var base64 = Base64.getEncoder().encodeToString(bytes);
			var mimeType = resourcePath.endsWith(".png") ? "image/png" : "image/jpeg";
			return "data:" + mimeType + ";base64," + base64;
		}
		catch (IOException e)
		{
			log.error("Failed to load image: {}", resourcePath, e);
			return "";
		}
	}
}

