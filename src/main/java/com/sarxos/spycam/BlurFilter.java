package com.sarxos.spycam;

import com.jhlabs.image.BoxBlurFilter;


/**
 * Custom blur filter.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class BlurFilter extends BoxBlurFilter {

	public BlurFilter(int power) {
		super(power, power, 1);
		setPremultiplyAlpha(false);
	}

}
