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

package boofcv.alg.shapes.corner;

import boofcv.struct.PointGradient_F64;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCornerFitFunction_F64 {
	@Test
	public void basicTest() {
		FastQueue<PointGradient_F64> points = new FastQueue<PointGradient_F64>(PointGradient_F64.class,true);

		points.grow().set(1,1,-1,1);

		CornerFitFunction_F64 alg = new CornerFitFunction_F64();
		alg.setPoints(points.toList());

		double a = alg.process(new double[]{ 6 ,6});
		double b = alg.process(new double[]{-1,-1});
		double c = alg.process(new double[]{-1, 3});
		double d = alg.process(new double[]{-2, 0});

		assertEquals(0,a,1e-6);
		assertEquals(0,b,1e-6);
		assertEquals(2, c, 1e-6);
		assertTrue(0 < d);
		assertTrue(d < c);

		points.grow().set(2,2,-1,1);

		a = alg.process(new double[]{6,6});
		b = alg.process(new double[]{-1,-1});
		c = alg.process(new double[]{ 9,3});
		d = alg.process(new double[]{-2, 0});

		assertEquals(a, b, 1e-8);
		assertTrue(0 < d && d < c);
	}

	/**
	 * Test the case where the corner point is exactly in the same position as a pixel
	 */
	@Test
	public void convergeOnTopOfPoint() {
		fail("implement");
	}
}
