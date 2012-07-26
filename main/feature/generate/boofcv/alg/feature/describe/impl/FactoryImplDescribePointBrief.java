/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.feature.describe.impl;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * @author Peter Abeles
 */
public class FactoryImplDescribePointBrief extends CodeGeneratorBase {

	AutoTypeImage imageType;

	@Override
	public void generate() throws FileNotFoundException {
		printClass(AutoTypeImage.F32);
		printClass(AutoTypeImage.U8);
	}

	private void printClass( AutoTypeImage imageType ) throws FileNotFoundException {
		this.imageType = imageType;
		className = "ImplDescribePointBrief_"+imageType.getAbbreviatedType();
		out = new PrintStream(new FileOutputStream(className + ".java"));
		printPreamble();
		printFunctions();

		out.print("}\n");
	}

	private void printPreamble() throws FileNotFoundException {
		setOutputFile(className);
		out.print("import boofcv.abst.filter.blur.BlurFilter;\n" +
				"import boofcv.alg.feature.describe.DescribePointBrief;\n" +
				"import boofcv.alg.feature.describe.brief.BriefDefinition_I32;\n" +
				"import boofcv.alg.feature.describe.brief.TupleDesc_B;\n" +
				"import boofcv.misc.BoofMiscOps;\n" +
				"import boofcv.struct.image.*;\n" +
				"import georegression.struct.point.Point2D_I32;\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Implementation of {@link DescribePointBrief} for a specific image type.\n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * WARNING: Do not modify.  Automatically generated by {@link FactoryImplDescribePointBrief}.\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" extends DescribePointBrief<"+imageType.getImageName()+"> {\n" +
				"\n" +
				"\tpublic "+className+"(BriefDefinition_I32 definition, BlurFilter<"+imageType.getImageName()+"> filterBlur) {\n" +
				"\t\tsuper(definition, filterBlur);\n" +
				"\t}\n\n");
	}

	public void printFunctions() {
		String bitwise = imageType.getBitWise();
		String sumType = imageType.getSumType();

		out.print("\t@Override\n" +
				"\tpublic boolean process( double X , double Y , TupleDesc_B feature )\n" +
				"\t{\n" +
				"\t\tint c_x = (int)X;\n" +
				"\t\tint c_y = (int)Y;\n"+
				"\n" +
				"\t\tif( !BoofMiscOps.checkInside(blur,c_x,c_y,definition.radius) )\n" +
				"\t\t\treturn false;\n" +
				"\n" +
				"\t\tBoofMiscOps.zero(feature.data,feature.data.length);\n" +
				"\n" +
				"\t\tint index = blur.startIndex + blur.stride*c_y + c_x;\n" +
				"\n" +
				"\t\tfor( int i = 0; i < definition.compare.length; i++ ) {\n" +
				"\t\t\t"+sumType+" valA = blur.data[index + offsetsA[i]]"+bitwise+";\n" +
				"\t\t\t"+sumType+" valB = blur.data[index + offsetsB[i]]"+bitwise+";\n" +
				"\n" +
				"\t\t\tif( valA < valB ) {\n" +
				"\t\t\t\tfeature.data[ i/32 ] |= 1 << (i % 32);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\treturn true;\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		FactoryImplDescribePointBrief app = new FactoryImplDescribePointBrief();
		app.generate();
	}

}
