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

package boofcv.factory.fiducial;

import boofcv.abst.calib.ConfigChessboard;
import boofcv.abst.calib.ConfigSquareGrid;
import boofcv.abst.fiducial.CalibrationFiducialDetector;
import boofcv.abst.fiducial.FiducialDetector;
import boofcv.abst.fiducial.SquareBinary_to_FiducialDetector;
import boofcv.abst.fiducial.SquareImage_to_FiducialDetector;
import boofcv.abst.filter.binary.InputToBinary;
import boofcv.alg.fiducial.DetectFiducialSquareBinary;
import boofcv.alg.fiducial.DetectFiducialSquareImage;
import boofcv.alg.shapes.polygon.BinaryPolygonConvexDetector;
import boofcv.factory.filter.binary.FactoryThresholdBinary;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.struct.image.ImageSingleBand;

/**
 * Factory for creating fiducial detectors which implement {@link FiducialDetector}.
 *
 * @author Peter Abeles
 */
public class FactoryFiducial {

	/**
	 * Fast detector for square binary based fiducials.  The speed increase comes from using a simple
	 * threshold and is not lighting invariant.
	 *
	 * @param config Description of the fiducial.  Can't be null.
	 * @param binaryThreshold Threshold for binary image.
	 * @param imageType Type of image it's processing
	 * @return FiducialDetector
	 */
	public static <T extends ImageSingleBand>
	FiducialDetector<T> squareBinaryFast( ConfigFiducialBinary config,
										  int binaryThreshold ,
										  Class<T> imageType ) {

		config.squareDetector.clockwise = false;

		InputToBinary<T> binary = FactoryThresholdBinary.globalFixed(binaryThreshold,true,imageType);
		BinaryPolygonConvexDetector<T> squareDetector = FactoryShapeDetector.polygon(binary,config.squareDetector,imageType);

		DetectFiducialSquareBinary<T> alg = new DetectFiducialSquareBinary<T>(
				squareDetector,imageType);
		alg.setAmbiguityThreshold(config.ambiguousThreshold);

		return new SquareBinary_to_FiducialDetector<T>(alg,config.targetWidth);
	}

	/**
	 * Robust detector for square image based fiducials.  Lighting invariant detector
	 *
	 * @param config Description of the fiducial.  Can't be null.
	 * @param thresholdRadius Size of the radius used for adaptive thresholding.  For 640x480 image try radius of 6.
	 * @param imageType Type of image it's processing
	 * @return FiducialDetector
	 */
	public static  <T extends ImageSingleBand>
	FiducialDetector<T> squareBinaryRobust( ConfigFiducialBinary config,
											int thresholdRadius,
											Class<T> imageType ) {

		config.squareDetector.clockwise = false;

		InputToBinary<T> binary = FactoryThresholdBinary.adaptiveSquare(thresholdRadius, 0, true, imageType);
		BinaryPolygonConvexDetector<T> squareDetector = FactoryShapeDetector.polygon(binary,config.squareDetector,imageType);

		DetectFiducialSquareBinary<T> alg = new DetectFiducialSquareBinary<T>(
				squareDetector,imageType);
		alg.setAmbiguityThreshold(config.ambiguousThreshold);

		return new SquareBinary_to_FiducialDetector<T>(alg,config.targetWidth);
	}

	/**
	 * Fast detector for square image based fiducials.  The speed increase comes from using a simple
	 * threshold and is not lighting invariant.
	 *
	 * @param config Description of the fiducial.  Can't be null.
	 * @param binaryThreshold Threshold for binary image.
	 * @param imageType Type of image it's processing
	 * @return FiducialDetector
	 */
	public static  <T extends ImageSingleBand>
	SquareImage_to_FiducialDetector<T> squareImageFast( ConfigFiducialImage config,
														int binaryThreshold,
														Class<T> imageType ) {

		config.squareDetector.clockwise = false;

		InputToBinary<T> binary = FactoryThresholdBinary.globalFixed(binaryThreshold, true, imageType);
		BinaryPolygonConvexDetector<T> squareDetector = 
				FactoryShapeDetector.polygon(binary,config.squareDetector,imageType);
		DetectFiducialSquareImage<T> alg = new DetectFiducialSquareImage<T>(
				squareDetector,config.maxErrorFraction,imageType);

		return new SquareImage_to_FiducialDetector<T>(alg);
	}

	/**
	 * Robust detector for square image based fiducials.  Lighting invariant detector
	 *
	 * @param config Description of the fiducial.  Can't be null.
	 * @param thresholdRadius Size of the radius used for adaptive thresholding.  For 640x480 image try radius of 6.
	 * @param imageType Type of image it's processing
	 * @return FiducialDetector
	 */
	public static  <T extends ImageSingleBand>
	SquareImage_to_FiducialDetector<T> squareImageRobust( ConfigFiducialImage config,
														  int thresholdRadius,
														  Class<T> imageType ) {

		config.squareDetector.clockwise = false;

		InputToBinary<T> binary = FactoryThresholdBinary.adaptiveSquare(thresholdRadius, 0, true, imageType);
		BinaryPolygonConvexDetector<T> squareDetector =
				FactoryShapeDetector.polygon(binary,config.squareDetector,imageType);
		
		DetectFiducialSquareImage<T> alg = new DetectFiducialSquareImage<T>(
				squareDetector,config.maxErrorFraction,imageType);

		return new SquareImage_to_FiducialDetector<T>(alg);
	}

	/**
	 * Wrapper around chessboard calibration detector.
	 *
	 * @param config Description of the chessboard.
	 * @param imageType Type of image it's processing
	 * @return FiducialDetector
	 */
	public static <T extends ImageSingleBand>
	CalibrationFiducialDetector<T> calibChessboard( ConfigChessboard config, Class<T> imageType) {
		return new CalibrationFiducialDetector<T>(config,imageType);
	}

	/**
	 * Wrapper around square-grid calibration detector.
	 *
	 * @param config Description of the chessboard.
	 * @param imageType Type of image it's processing
	 * @return FiducialDetector
	 */
	public static <T extends ImageSingleBand>
	CalibrationFiducialDetector<T> calibSquareGrid( ConfigSquareGrid config, Class<T> imageType) {
		return new CalibrationFiducialDetector<T>(config,imageType);
	}
}
