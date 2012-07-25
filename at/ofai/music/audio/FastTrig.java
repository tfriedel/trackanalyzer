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
package at.ofai.music.audio;

public class FastTrig {

	
    private static final int           SIZE                 = 1024;
    private static final float        STRETCH            = (float) Math.PI;
    // Output will swing from -STRETCH to STRETCH (default: Math.PI)
    // Useful to change to 1 if you would normally do "atan2(y, x) / Math.PI"

    // Inverse of SIZE
    private static final int        EZIS            = -SIZE;
    private static final float[]    ATAN2_TABLE_PPY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_PPX    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_PNY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_PNX    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NPY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NPX    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NNY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NNX    = new float[SIZE + 1];

    static
    {
        for (int i = 0; i <= SIZE; i++)
        {
            float f = (float)i / SIZE;
            ATAN2_TABLE_PPY[i] = (float)(StrictMath.atan(f) * STRETCH / StrictMath.PI);
            ATAN2_TABLE_PPX[i] = STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_PNY[i] = -ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_PNX[i] = ATAN2_TABLE_PPY[i] - STRETCH * 0.5f;
            ATAN2_TABLE_NPY[i] = STRETCH - ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_NPX[i] = ATAN2_TABLE_PPY[i] + STRETCH * 0.5f;
            ATAN2_TABLE_NNY[i] = ATAN2_TABLE_PPY[i] - STRETCH;
            ATAN2_TABLE_NNX[i] = -STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
        }
    }

    /**
     * ATAN2
     */

    public static final float aTan2Lookup(float y, float x)
    {
        if (x >= 0)
        {
            if (y >= 0)
            {
                if (x >= y)
                    return ATAN2_TABLE_PPY[(int)(SIZE * y / x + 0.5)];
                else
                    return ATAN2_TABLE_PPX[(int)(SIZE * x / y + 0.5)];
            }
            else
            {
                if (x >= -y)
                    return ATAN2_TABLE_PNY[(int)(EZIS * y / x + 0.5)];
                else
                    return ATAN2_TABLE_PNX[(int)(EZIS * x / y + 0.5)];
            }
        }
        else
        {
            if (y >= 0)
            {
                if (-x >= y)
                    return ATAN2_TABLE_NPY[(int)(EZIS * y / x + 0.5)];
                else
                    return ATAN2_TABLE_NPX[(int)(EZIS * x / y + 0.5)];
            }
            else
            {
                if (x <= y) // (-x >= -y)
                    return ATAN2_TABLE_NNY[(int)(SIZE * y / x + 0.5)];
                else
                    return ATAN2_TABLE_NNX[(int)(SIZE * x / y + 0.5)];
            }
        }
    }

	/**
	 * Fast approximation of 1.0 / sqrt(x). See <a
	 * href="http://www.beyond3d.com/content/articles/8/">http://www.beyond3d.com/content/articles/8/</a>
	 *
	 * @param x Positive value to estimate inverse of square root of
	 * @return Approximately 1.0 / sqrt(x)
	 *
	 */
	public static double invSqrt(double x) {
		double xhalf = 0.5 * x;
		long i = Double.doubleToRawLongBits(x);
		i = 0x5FE6EB50C7B537AAL - (i >> 1);
		x = Double.longBitsToDouble(i);
		x = x * (1.5 - xhalf * x * x);
		return x;
	}

	public static double aTan2(double y, double x) {
		double coeff_1 = Math.PI / 4d;
		double coeff_2 = 3d * coeff_1;
		double abs_y = Math.abs(y);
		double angle;
		if (x >= 0d) {
			double r = (x - abs_y) / (x + abs_y);
			angle = coeff_1 - coeff_1 * r;
		} else {
			double r = (x + abs_y) / (abs_y - x);
			angle = coeff_2 - coeff_1 * r;
		}
		return y < 0d ? -angle : angle;
	}

	/**
	 * Approximation of arctangent. Slightly faster and substantially less
	 * accurate than
	 *  {@link Math#atan2(double, double)}.
	 *
	 */
	public static double fast_atan2(double y, double x) {
		double d2 = x * x + y * y;

		// Bail out if d2 is NaN, zero or subnormal
		if (Double.isNaN(d2)
				|| (Double.doubleToRawLongBits(d2) < 0x10000000000000L)) {
			return Double.NaN;
		}

		// Normalise such that 0.0 <= y <= x
		boolean negY = y < 0.0;
		if (negY) {
			y = -y;
		}
		boolean negX = x < 0.0;
		if (negX) {
			x = -x;
		}
		boolean steep = y > x;
		if (steep) {
			double t = x;
			x = y;
			y = t;
		}

		// Scale to unit circle (0.0 <= y <= x <= 1.0)
		double rinv = invSqrt(d2); // rinv ≅ 1.0 / hypot(x, y)
		x *= rinv; // x ≅ cos θ
		y *= rinv; // y ≅ sin θ, hence θ ≅ asin y

		// Hack: we want: ind = floor(y * 256)
		// We deliberately force truncation by adding floating-point numbers whose
		// exponents differ greatly.  The FPU will right-shift y to match exponents,
		// dropping all but the first 9 significant bits, which become the 9 LSBs
		// of the resulting mantissa.
		// Inspired by a similar piece of C code at
		// http://www.shellandslate.com/computermath101.html
		double yp = FRAC_BIAS + y;
		int ind = (int) Double.doubleToRawLongBits(yp);

		// Find φ (a first approximation of θ) from the LUT
		double φ = ASIN_TAB[ind];
		double cφ = COS_TAB[ind]; // cos(φ)

		// sin(φ) == ind / 256.0
		// Note that sφ is truncated, hence not identical to y.
		double sφ = yp - FRAC_BIAS;
		double sd = y * cφ - x * sφ; // sin(θ-φ) ≡ sinθ cosφ - cosθ sinφ

		// asin(sd) ≅ sd + ⅙sd³ (from first 2 terms of Maclaurin series)
		double d = (6.0 + sd * sd) * sd * ONE_SIXTH;
		double θ = φ + d;

		// Translate back to correct octant
		if (steep) {
			θ = Math.PI * 0.5 - θ;
		}
		if (negX) {
			θ = Math.PI - θ;
		}
		if (negY) {
			θ = -θ;
		}

		return θ;
	}
	private static final double ONE_SIXTH = 1.0 / 6.0;
	private static final int FRAC_EXP = 8; // LUT precision == 2 ** -8 == 1/256
	private static final int LUT_SIZE = (1 << FRAC_EXP) + 1;
	private static final double FRAC_BIAS =
			Double.longBitsToDouble((0x433L - FRAC_EXP) << 52);
	private static final double[] ASIN_TAB = new double[LUT_SIZE];
	private static final double[] COS_TAB = new double[LUT_SIZE];

	static {
		/*
		 * Populate trig tables
		 */
		for (int ind = 0; ind < LUT_SIZE; ++ind) {
			double v = ind / (double) (1 << FRAC_EXP);
			double asinv = Math.asin(v);
			COS_TAB[ind] = Math.cos(asinv);
			ASIN_TAB[ind] = asinv;
		}
	}
}
