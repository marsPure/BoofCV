/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.interpolate.impl;

import gecv.alg.interpolate.InterpolateRectangle;
import gecv.struct.image.ImageFloat32;


/**
 * Nearest Neighbor interpolation for a rectangular region
 *
 * @author Peter Abeles
 */
public class NearestNeighborRectangle_F32 implements InterpolateRectangle<ImageFloat32>  {

	ImageFloat32 image;

	@Override
	public void setImage(ImageFloat32 image) {
		this.image = image;
	}

	@Override
	public ImageFloat32 getImage() {
		return image;
	}

	@Override
	public void region(float tl_x, float tl_y, ImageFloat32 dest) {

		int x = (int)tl_x;
		int y = (int)tl_y;

		if( x < 0 || y < 0 || x + dest.width > image.width || y + dest.height > image.height )
			throw new IllegalArgumentException("Out of bounds");

		for( int i = 0; i < dest.height; i++ ) {
			int indexSrc = image.startIndex + image.stride*(i+y)+x;
			int indexDst = dest.startIndex + dest.stride*i;

			System.arraycopy(image.data, indexSrc, dest.data, indexDst, dest.width);
		}

	}
}
