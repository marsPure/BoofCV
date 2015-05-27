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

package boofcv.alg.shapes.polygon;

import boofcv.abst.distort.FDistort;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.alg.misc.GImageMiscOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import boofcv.testing.BoofTesting;
import georegression.geometry.UtilLine2D_F64;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.metric.Distance2D_F64;
import georegression.struct.affine.Affine2D_F64;
import georegression.struct.line.LineGeneral2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.shapes.Polygon2D_F64;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.transform.ConvertTransform_F64;
import georegression.transform.affine.AffinePointOps_F64;
import org.junit.Test;

import java.util.Random;

import static java.lang.Math.max;
import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
@SuppressWarnings("unchecked")
public class TestRefinePolygonLineToImage {

	Random rand = new Random(234);

	int width = 400, height = 500;
	ImageSingleBand work; // original image before homography has been applied
	ImageSingleBand image; // image after homography applied

	int x0 = 200, y0 = 160;
	int x1 = 260, y1 = 400; // that's exclusive
	int xx1 = x1-1, yy1 = y1-1; // inclusive location, this is where the estimate will actually end

	int white = 200;

	Class imageTypes[] = new Class[]{ImageUInt8.class,ImageFloat32.class};

	/**
	 * Give it a shape which is too small and see if it fails
	 */
	@Test
	public void fit_tooSmall() {
		final boolean black = true;

		Polygon2D_F64 input = new Polygon2D_F64(5,5, 5,6, 6,6, 6,5);

		for (Class imageType : imageTypes) {
			setup(new Affine2D_F64(), black, imageType);

			RefinePolygonLineToImage alg = createAlg(input.size(),black, imageType);

			Polygon2D_F64 output = new Polygon2D_F64(input.size());
			alg.initialize(image);
			assertFalse(alg.refine(input, output));
		}
	}

	/**
	 * Makes sure it can handle sub-images
	 */
	@Test
	public void fit_subimage() {
		final boolean black = true;

		Polygon2D_F64 input = new Polygon2D_F64(x0,y0 , x0,yy1, xx1,yy1, xx1,y0);

		for (Class imageType : imageTypes) {
			setup(new Affine2D_F64(), black, imageType);

			RefinePolygonLineToImage alg = createAlg(input.size(),black, imageType);

			Polygon2D_F64 output = new Polygon2D_F64(4);
			alg.initialize(image);
			assertTrue(alg.refine(input, output));

			// do it again with a sub-image
			Polygon2D_F64 output2 = new Polygon2D_F64(4);
			image = BoofTesting.createSubImageOf_S(image);
			alg.initialize(image);
			assertTrue(alg.refine(input, output2));

			assertTrue(UtilPolygons2D_F64.isIdentical(output, output2, 1e-8));
		}
	}

	/**
	 * Fit a square which is alligned to the image axis.  It should get a nearly perfect fit.
	 * Initial conditions are be a bit off.
	 */
	@Test
	public void alignedSquare() {
		Polygon2D_F64 original = new Polygon2D_F64(x0,y0 , x0,yy1, xx1,yy1, xx1,y0);

		for (Class imageType : imageTypes) {
			for (int i = 0; i < 2; i++) {
				boolean black = i == 0;

				setup(new Affine2D_F64(), black, imageType);

				RefinePolygonLineToImage alg = createAlg(original.size(),black, imageType);

				for (int j = 0; j < 20; j++) {
					Polygon2D_F64 input = original.copy();
					addNoise(input,2);

					Polygon2D_F64 output = new Polygon2D_F64(original.size());
					alg.initialize(image);
					assertTrue(alg.refine(input, output));

					assertTrue(original.isIdentical(output, 0.01));
				}
			}
		}
	}

	/**
	 * Perfect initial guess.
	 */
	@Test
	public void fit_perfect_affine() {
		// distorted and undistorted views
		Affine2D_F64 affines[] = new Affine2D_F64[2];
		affines[0] = new Affine2D_F64();
		affines[1] = new Affine2D_F64(1.3,0.05,-0.15,0.87,0.1,0.6);
		ConvertTransform_F64.convert(new Se2_F64(0,0,0.2),affines[0]);

		for (Class imageType : imageTypes) {
			for (Affine2D_F64 a : affines) {
//				System.out.println(imageType+"  "+a);
				fit_perfect_affine(true, a, imageType);
				fit_perfect_affine(false, a, imageType);
			}
		}
	}

	public void fit_perfect_affine(boolean black, Affine2D_F64 affine, Class imageType) {
		setup(affine, black, imageType);

		RefinePolygonLineToImage alg = createAlg(4,black, imageType);

		Polygon2D_F64 input = new Polygon2D_F64(4);
		AffinePointOps_F64.transform(affine,new Point2D_F64(x0,y0),input.get(0));
		AffinePointOps_F64.transform(affine,new Point2D_F64(x0,yy1),input.get(1));
		AffinePointOps_F64.transform(affine,new Point2D_F64(xx1,yy1),input.get(2));
		AffinePointOps_F64.transform(affine,new Point2D_F64(xx1,y0),input.get(3));

		Polygon2D_F64 expected = input.copy();
		Polygon2D_F64 found = new Polygon2D_F64(4);

		alg.initialize(image);
		assertTrue(alg.refine(input, found));

		// input shouldn't be modified
		assertTrue(expected.isIdentical(input,0));
		// should be close to the expected
		assertTrue(expected.isIdentical(found,0.25));

		// do it again with a sub-image to see if it handles that
		image = BoofTesting.createSubImageOf_S(image);
		alg.initialize(image);
		assertTrue(alg.refine(input, found));
		assertTrue(expected.isIdentical(input,0));
		assertTrue(expected.isIdentical(found,0.25));
	}

	/**
	 * Fit the quad with a noisy initial guess
	 */
	@Test
	public void fit_noisy_affine() {
		// distorted and undistorted views
		Affine2D_F64 affines[] = new Affine2D_F64[2];
		affines[0] = new Affine2D_F64();
		affines[1] = new Affine2D_F64(1.3,0.05,-0.15,0.87,0.1,0.6);
		ConvertTransform_F64.convert(new Se2_F64(0,0,0.2),affines[0]);

		for (Class imageType : imageTypes) {
			for (Affine2D_F64 a : affines) {
//				System.out.println(imageType+"  "+a);
				fit_noisy_affine(true, a, imageType);
				fit_noisy_affine(false, a, imageType);
			}
		}
	}

	public void fit_noisy_affine(boolean black, Affine2D_F64 affine, Class imageType) {
		setup(affine, black, imageType);

		RefinePolygonLineToImage alg = createAlg(4,black, imageType);

		Polygon2D_F64 input = new Polygon2D_F64(4);
		AffinePointOps_F64.transform(affine,new Point2D_F64(x0,y0),input.get(0));
		AffinePointOps_F64.transform(affine,new Point2D_F64(x0,yy1),input.get(1));
		AffinePointOps_F64.transform(affine,new Point2D_F64(xx1,yy1),input.get(2));
		AffinePointOps_F64.transform(affine,new Point2D_F64(xx1,y0),input.get(3));

		Polygon2D_F64 expected = input.copy();
		Polygon2D_F64 found = new Polygon2D_F64(4);

		for (int i = 0; i < 10; i++) {
			// add some noise
			input.set(expected);
			addNoise(input, 2);

			alg.initialize(image);
			assertTrue(alg.refine(input, found));

			// should be close to the expected
			double before = computeMaxDistance(input,expected);
			double after = computeMaxDistance(found,expected);

			assertTrue(after<before);
			assertTrue(expected.isIdentical(found,0.5));
		}
	}

	private void addNoise(Polygon2D_F64 input, double spread) {
		for (int i = 0; i < input.size(); i++) {
			Point2D_F64 v = input.get(i);
			v.x += rand.nextDouble()*spread - spread/2.0;
			v.y += rand.nextDouble()*spread - spread/2.0;
		}
	}

	private RefinePolygonLineToImage createAlg( int numSides , boolean black, Class imageType) {
		InterpolatePixelS interp = FactoryInterpolation.createPixelS(0, 255, TypeInterpolate.BILINEAR, imageType);
		return new RefinePolygonLineToImage(numSides,black,interp);
//		return new RefineQuadrilateralToImage(2,20,2,10,0.01,black,interp);
	}

	/**
	 * Optimize a line with a perfect initial guess
	 */
	@Test
	public void optimize_line_perfect() {
		for (Class imageType : imageTypes) {
			optimize_line_perfect(true, imageType);
			optimize_line_perfect(false, imageType);
		}
	}

	public void optimize_line_perfect(boolean black, Class imageType) {
		setup(null, black, imageType);

		RefinePolygonLineToImage alg = createAlg(4,black, imageType);

		Quadrilateral_F64 input = new Quadrilateral_F64(x0,y0,x0,y1,x1,y1,x1,y0);
		LineGeneral2D_F64 found = new LineGeneral2D_F64();

		alg.interpolate.setImage(image);
		alg.optimize(input.a, input.b, found);

		assertTrue(Distance2D_F64.distance(found, input.a) <= 1e-4);
		assertTrue(Distance2D_F64.distance(found, input.b) <= 1e-4);
	}

	private double computeMaxDistance(Polygon2D_F64 expected, Polygon2D_F64 found ) {
		double a = 0;
		for (int i = 0; i < expected.size(); i++) {
			a = max(a,expected.get(i).distance(found.get(i)));
		}

		return a;
	}

	/**
	 * Simple case where it samples along a line on a perfect rectangle
	 */
	@Test
	public void computePointsAndWeights() {
		for (Class imageType : imageTypes) {
			computePointsAndWeights(true, imageType);
			computePointsAndWeights(false, imageType);
		}
	}

	public void computePointsAndWeights( boolean black , Class imageType ) {
		setup(null,black,imageType);

		RefinePolygonLineToImage alg = createAlg(4,black, imageType);

		float H = y1-y0-10;

		alg.interpolate.setImage(image);
		alg.center.set(10, 12);
		alg.computePointsAndWeights(0, H, x0, y0 + 5, -1, 0);

		int radius = alg.sampleRadius;
		int N = radius*2+1;

		for (int i = 0; i < alg.lineSamples; i++) {
			for (int j = 0; j < N; j++) {
				int index = i*N+j;
				if( j == radius ) {
					assertEquals(white,alg.weights[index],1e-8);
				} else {
					assertEquals(0,alg.weights[index],1e-8);
				}

				double x = x0 - 10 - (radius-j);
				double y = y0 + 5 + H*i/(alg.lineSamples -1) - 12;
				Point2D_F64 p = (Point2D_F64)alg.samplePts.get(index);
				assertEquals(x,p.x,1e-4);
				assertEquals(y,p.y,1e-4);
			}
		}
	}

	@Test
	public void convert() {
		Polygon2D_F64 orig = new Polygon2D_F64(10,20,30,21,19.5,-10,8,-8);

		LineGeneral2D_F64[] lines = new LineGeneral2D_F64[4];
		lines[0] = UtilLine2D_F64.convert(orig.getLine(0,null),(LineGeneral2D_F64)null);
		lines[1] = UtilLine2D_F64.convert(orig.getLine(1,null),(LineGeneral2D_F64)null);
		lines[2] = UtilLine2D_F64.convert(orig.getLine(2,null),(LineGeneral2D_F64)null);
		lines[3] = UtilLine2D_F64.convert(orig.getLine(3,null),(LineGeneral2D_F64)null);

		Polygon2D_F64 found = new Polygon2D_F64(4);
		assertTrue(RefinePolygonLineToImage.convert(lines, found));

		assertTrue(orig.isIdentical(found, 1e-8));
	}

	private void setup( Affine2D_F64 affine, boolean black , Class imageType ) {
		work = GeneralizedImageOps.createSingleBand(imageType,width,height);
		image = GeneralizedImageOps.createSingleBand(imageType,width,height);

		int bg = black ? white : 0;
		int fg = black ? 0 : white;
		GImageMiscOps.fill(work, bg);
		GImageMiscOps.fillRectangle(work, fg, x0, y0, x1 - x0, y1 - y0);

		if( affine != null ) {
			new FDistort(work, image).border(bg).affine(affine).apply();
		} else {
			image.setTo(work);
		}

//		BufferedImage out = ConvertBufferedImage.convertTo(image, null, true);
//		ShowImages.showWindow(out,"Renered");
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

}