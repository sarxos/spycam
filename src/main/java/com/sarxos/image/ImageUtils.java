package com.sarxos.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.jhlabs.image.PixelUtils;


public class ImageUtils {

	public static BufferedImage diff(BufferedImage a, BufferedImage b, int threshold) {
		return diff(a, b, threshold, 0);
	}

	public static BufferedImage diff(BufferedImage a, BufferedImage b, int threshold, int padding) {

		final int white = Color.WHITE.getRGB();
		BufferedImage c = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

		int w = a.getWidth();
		int h = a.getHeight();

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {

				int rgb = PixelUtils.combinePixels(a.getRGB(i, j), b.getRGB(i, j), PixelUtils.DIFFERENCE);

				int cr = (rgb & 0x00ff0000) >> 16;
				int cg = (rgb & 0x0000ff00) >> 8;
				int cb = (rgb & 0x000000ff);

				int max = Math.max(Math.max(cr, cg), cb);

				if (max > threshold) {

				}

				c.setRGB(i, j, max < threshold ? 0 : white);

			}
		}

		return c;
	}

}
