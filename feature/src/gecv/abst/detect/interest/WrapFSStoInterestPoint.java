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

package gecv.abst.detect.interest;

import gecv.alg.detect.interest.FeatureScaleSpace;
import gecv.struct.feature.ScalePoint;
import gecv.struct.gss.GaussianScaleSpace;
import gecv.struct.image.ImageBase;
import jgrl.struct.point.Point2D_I32;

import java.util.List;


/**
 * Wrapper around {@link gecv.alg.detect.interest.FeatureScaleSpace} for {@link InterestPointDetector}.
 *
 * @author Peter Abeles
 */
public class WrapFSStoInterestPoint<T extends ImageBase, D extends ImageBase> implements InterestPointDetector<T>{

	FeatureScaleSpace<T,D> detector;
	List<ScalePoint> location;
	GaussianScaleSpace<T,D> ss;

	public WrapFSStoInterestPoint(FeatureScaleSpace<T,D> detector,
								  GaussianScaleSpace<T,D> ss ) {
		this.detector = detector;
		this.ss = ss;
	}

	@Override
	public void detect(T input) {
		ss.setImage(input);

		detector.detect(ss);

		location = detector.getInterestPoints();
	}

	@Override
	public int getNumberOfFeatures() {
		return location.size();
	}

	@Override
	public Point2D_I32 getLocation(int featureIndex) {
		return location.get(featureIndex);
	}

	@Override
	public double getScale(int featureIndex) {
		return location.get(featureIndex).scale;
	}

	@Override
	public double getOrientation(int featureIndex) {
		throw new IllegalArgumentException("Not supported");
	}

	@Override
	public boolean hasScale() {
		return true;
	}

	@Override
	public boolean hasOrientation() {
		return false;
	}
}