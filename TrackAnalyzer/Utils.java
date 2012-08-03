/*************************************************************************

  Copyright 2012 Thomas Friedel

  This file is part of TrackAnalyzer.

  TrackAnalyzer is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  TrackAnalyzer is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with TrackAnalyzer.  If not, see <http://www.gnu.org/licenses/>.

*************************************************************************/
package TrackAnalyzer;

import java.util.ArrayList;

public class Utils {

    public static ArrayList<Double> doubleArrayAsList(double[] array) {
        ArrayList<Double> result = new ArrayList<Double>(array.length);
        for (double f : array) {
            result.add(Double.valueOf(f));
        }
        return result;
    }

    public static double[] doubleArrayListToPrimitive(ArrayList<Double> doubleList) {

        double[] doubleArray = new double[doubleList.size()];

        for (int i = 0; i < doubleList.size(); i++) {
            Double f = doubleList.get(i);
            doubleArray[i] = (f != null ? f : Double.NaN); // Or whatever default you want.
        }
        return doubleArray;
    }

    public static ArrayList<Float> floatArrayAsList(float[] array) {
        ArrayList<Float> result = new ArrayList<Float>(array.length);
        for (float f : array) {
            result.add(Float.valueOf(f));
        }
        return result;
    }

    public static float[] floatArrayListToPrimitive(ArrayList<Float> floatList) {

        float[] floatArray = new float[floatList.size()];

        for (int i = 0; i < floatList.size(); i++) {
            Float f = floatList.get(i);
            floatArray[i] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        return floatArray;
    }

    public static ArrayList<ArrayList<Float>> newFloatArrayList2D(int a, int b) {
        ArrayList<ArrayList<Float>> data = new ArrayList<ArrayList<Float>>(a);
        for (int i = 0; i < a; i++) {
            ArrayList<Float> array = new ArrayList<Float>(b);
            for (int j = 0; j < b; j++) {
                array.add((float) 0.0);
            }
            data.add(array);
        }
        return data;
    }

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
