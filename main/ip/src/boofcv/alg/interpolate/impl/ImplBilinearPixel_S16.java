/*
 * Copyright (c) 2011-2015, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.interpolate.impl;

import boofcv.alg.interpolate.BilinearPixel;
import boofcv.core.image.border.ImageBorder_I32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageType;


/**
 * <p>
 * Implementation of {@link BilinearPixel} for a specific image type.
 * </p>
 *
 * <p>
 * NOTE: This code was automatically generated using {@link GenerateImplBilinearPixel}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplBilinearPixel_S16 extends BilinearPixel<ImageSInt16> {

	public ImplBilinearPixel_S16() {
	}

	public ImplBilinearPixel_S16(ImageSInt16 orig) {

		setImage(orig);
	}
	@Override
	public float get_fast(float x, float y) {
		int xt = (int) x;
		int yt = (int) y;
		float ax = x - xt;
		float ay = y - yt;

		int index = orig.startIndex + yt * stride + xt;

		short[] data = orig.data;

		float val = (1.0f - ax) * (1.0f - ay) * (data[index] ); // (x,y)
		val += ax * (1.0f - ay) * (data[index + 1] ); // (x+1,y)
		val += ax * ay * (data[index + 1 + stride] ); // (x+1,y+1)
		val += (1.0f - ax) * ay * (data[index + stride] ); // (x,y+1)

		return val;
	}

	@Override
	public float get_border(float x, float y) {
		float xf = (float)Math.floor(x);
		float yf = (float)Math.floor(y);
		int xt = (int) xf;
		int yt = (int) yf;
		float ax = x - xf;
		float ay = y - yf;

		ImageBorder_I32 border = (ImageBorder_I32)this.border;

		float val = (1.0f - ax) * (1.0f - ay) * border.get(xt,yt); // (x,y)
		val += ax * (1.0f - ay) *  border.get(xt + 1, yt);; // (x+1,y)
		val += ax * ay *  border.get(xt + 1, yt + 1);; // (x+1,y+1)
		val += (1.0f - ax) * ay *  border.get(xt,yt+1);; // (x,y+1)

		return val;
	}

	@Override
	public float get(float x, float y) {
		if (x < 0 || y < 0 || x > width-1 || y > height-1)
			throw new IllegalArgumentException("Point is outside of the image "+x+" "+y);

		int xt = (int) x;
		int yt = (int) y;

		float ax = x - xt;
		float ay = y - yt;

		int index = orig.startIndex + yt * stride + xt;

		// allows borders to be interpolated gracefully by double counting appropriate pixels
		int dx = xt == width - 1 ? 0 : 1;
		int dy = yt == height - 1 ? 0 : stride;

		short[] data = orig.data;

		float val = (1.0f - ax) * (1.0f - ay) * (data[index] ); // (x,y)
		val += ax * (1.0f - ay) * (data[index + dx] ); // (x+1,y)
		val += ax * ay * (data[index + dx + dy] ); // (x+1,y+1)
		val += (1.0f - ax) * ay * (data[index + dy] ); // (x,y+1)

		return val;
	}

	@Override
	public ImageType<ImageSInt16> getImageType() {
		return ImageType.single(ImageSInt16.class);
	}

}
