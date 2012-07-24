/*  Performance Worm: Visualisation of Expressive Musical Performance
	Copyright (C) 2001, 2006 by Simon Dixon

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along
	with this program (the file gpl.txt); if not, download it from
	http://www.gnu.org/licenses/gpl.txt or write to the
	Free Software Foundation, Inc.,
	51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package at.ofai.music.worm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import at.ofai.music.util.Format;

/** CLASS TempoInducer finds tempo (rate) but not beat times (phase) of
  * a performance.
  * It implements a real-time incremental tempo induction algorithm,
  * keeps track of multiple hypotheses, and allows switching between these
  * hypotheses by the user.
  * It assumes time is measured on a discrete scale (the timeBase),
  * where an amplitude (rms?) value is supplied for each time point and
  * the estimate of the tempo at this point is returned (method getTempo()).
  * Constants define the maximum, minimum and default tempos in terms of the
  * corresponding interbeat interval (IBI), and the length of time over which
  * tempo is induced (MEMORY).
  */
public class TempoInducer {

	double timeBase;
	double[] envelope;
	double[] slope;
	double[] peakTime;
	double[] peakSPL;
	double[] iois;
	double[] cluster;
	double[] clusterWgt;
	double[] newCluster;
	double[] newClusterWgt;
	double[] best;
	double[] bestWgt;
	boolean[] bestUsed;
	int peakHead;	// position of next peak (i.e. currently empty)
	int peakTail;	// position before first peak (i.e. currently empty)
	int bestCount;
	double tempo;
	int counter;
	double longTermSum;
	double recentSum;
	double[] prevAmp;
	static final double MEMORY = 8.0; 	// time in seconds used for IOI calcns
//	static final double MEMORY = 1000.0; //SD WAS 8 - changed for dance test
	static final double MIN_IOI = 0.1;
	static final double MAX_IOI = 2.5;
//	static final double MAX_IOI = 5;	//SD was 2.5 - changed for dance test
	static final double MIN_IBI = 0.200;		// 300 BPM  = 0.2
	static final double LOW_IBI = 0.400;		// 150 BPM  = 0.4
	static final double DEF_IBI = 0.800;		// 100 BPM  = 0.6
	static final double HI_IBI = 1.200;			//  75 BPM  = 0.8
	static final double MAX_IBI = 1.500;		//  40 BPM	= 1.5
//	static final double MIN_IBI = 0.100;
//	static final double LOW_IBI = 0.100;
//	static final double DEF_IBI = 0.800;		// 100 BPM  = 0.6
//	static final double HI_IBI = 5.000;
//	static final double MAX_IBI = 5.000;	//SD all changed for dance test
	static final double HYP_CHANGE_FACTOR = (1 - 0.4);	//WAS 1-.4
	static final double AMP_MEM_FACTOR = 0.95;
	static final double DECAY_BEST = 0.9;	// weight of best[0] decreases 10%/s
	static final double DECAY_OTHER = 0.8;	// weight of best[i] decreases 20%/s
	static final double RATIO_ERROR = 0.1;	// 1.9-2.1, 2.9-3.1, ...,7.9-8.1
	static final int CLUSTER_WIDTH = 8;			//WAS 4
	static final int CLUSTER_FACTOR = 30;		//WAS 10
	static final int CLUSTER_POINTS = 10;		//WAS 20
//	static final int CLUSTER_POINTS = 20;		//SD dance test WAS 10
	static final int REGRESSION_SIZE = 4;
	static final int SLOPE_POINTS = 15;
	static final int MID_POINT = SLOPE_POINTS / 2;
	static final int PEAK_POINTS = (int)MEMORY * 20;
	static final int OVERLAP = 4;
	int ioiPoints;
	public boolean onset;
	static Plot plot = null;
	static boolean plotFlag = false;
	double[] xplot, yplot, xplot2, yplot2, xplot3, yplot3;

	public TempoInducer(double tb) {
	//	System.err.println("Using infinite memory....");	//SD for dance test
		timeBase = tb;
		ioiPoints = (int)Math.ceil(MAX_IOI / tb) + 1;
		envelope = new double[SLOPE_POINTS];
		slope = new double[SLOPE_POINTS];
		peakTime = new double[PEAK_POINTS];
		peakSPL = new double[PEAK_POINTS];
		iois = new double[ioiPoints];
		cluster = new double[CLUSTER_POINTS];
		clusterWgt = new double[CLUSTER_POINTS];
		newCluster = new double[CLUSTER_POINTS];
		newClusterWgt = new double[CLUSTER_POINTS];
		best = new double[CLUSTER_POINTS];
		bestWgt = new double[CLUSTER_POINTS];
		bestUsed = new boolean[CLUSTER_POINTS];
		bestCount = 0;
		peakHead = 0;
		peakTail = PEAK_POINTS - 1;
		tempo = DEF_IBI;
		counter = 0;
		longTermSum = 0;
		recentSum = 0;
		prevAmp = new double[OVERLAP];
		xplot = new double[ioiPoints];
		for (int i = 0; i < ioiPoints; i++)
			xplot[i] = timeBase * i;
		yplot = new double[ioiPoints];
		xplot2 = new double[CLUSTER_POINTS];
		yplot2 = new double[CLUSTER_POINTS];
		xplot3 = new double[CLUSTER_POINTS];
		yplot3 = new double[CLUSTER_POINTS];
		if (plotFlag)
			makePlot();
	} // constructor

	void makePlot() {
		System.out.println("makePlot() " + (plot == null));//DEBUG
		if (plot == null)
			plot = new Plot();
		else
			plot.clear();
		plot.addPlot(xplot, yplot, java.awt.Color.blue, PlotPanel.IMPULSE);
		plot.addPlot(xplot2, yplot2, java.awt.Color.green);
		plot.addPlot(xplot3, yplot3, java.awt.Color.red);
		plot.setTitle("Tempo Tracking Histogram and Clusters");
	} // constructor

	public double getTempo(double amp) {
		int i;
		counter++;
		for (i = 0; i < SLOPE_POINTS - 1; i++) {
			envelope[i] = envelope[i+1];
			slope[i] = slope[i+1];
		}
		envelope[i] = amp;
		for (int j = 0; j < OVERLAP; j++) { // overlap windows
			envelope[i] += prevAmp[j];
			if (j == OVERLAP - 1)
				prevAmp[j] = amp;
			else
				prevAmp[j] = prevAmp[j+1];
		}
		longTermSum += amp;
		if (recentSum == 0)
			recentSum = amp;
		else
			recentSum = AMP_MEM_FACTOR * recentSum + (1 - AMP_MEM_FACTOR) * amp;
		slope[i] = getSlope();	// slope of points up to i (i.e. env[i-3 : i])
		for (i = 0; i < SLOPE_POINTS; i++)
			if ((i != MID_POINT) && (slope[MID_POINT] <= slope[i]))
				break;
		// System.out.println(i + " " + Format.d(envelope[MID_POINT], 5) + " " +
		// 		Format.d(slope[MID_POINT],5) + " " +
		// 		Format.d(longTermSum / (counter*4*REGRESSION_SIZE*timeBase),3));
		// System.out.println(i + " " + Format.d(1000*recentSum,3) + " " +
		// 							Format.d(1000*longTermSum / counter, 3));
		if ((i == SLOPE_POINTS) &&
				(envelope[MID_POINT] > recentSum / 5) &&
				(slope[MID_POINT] > recentSum /
								(2 * REGRESSION_SIZE * timeBase))) {
			// onset = true;	// click on onsets (for debugging)
			addPeak(timeBase * (counter - MID_POINT), envelope[MID_POINT]);
		} else
			onset = false;
		return tempo;
	} // getTempo
	
	public void switchLevels(boolean faster) {
		for (int i = 1; i < bestCount; i++)
			if ((best[i] < best[0]) == faster) {
				double tmp = best[0];
				best[0] = best[i];
				best[i] = tmp;
				break;
			}
	} // switchLevels()
	
	static int next(int p) {
		return (p == PEAK_POINTS - 1)? 0: p + 1;
	} // next()

	static int prev(int p) {
		return (p == 0)? PEAK_POINTS - 1: p - 1;
	} // prev()

	static int top(int p) {		// Required nearness of IOI's for clustering
		return p + p / CLUSTER_FACTOR + CLUSTER_WIDTH; // was 2*0.010=20ms (3pt)
	} // top()

	// Updates tempo based on new peak
	void addPeak(double t, double dB) {
		java.util.Arrays.fill(iois, 0);
		// add new peak
		peakTime[peakHead] = t;
		peakSPL[peakHead] = dB;
		peakHead = next(peakHead);
		if (peakHead == peakTail)
			System.err.println("Overflow: too many peaks");
		// delete old peaks
		int loPtr = next(peakTail);			// first peak
		while (t - peakTime[loPtr] > MEMORY) {
			peakTail = loPtr;
			loPtr = next(peakTail);
		}
		// get all IOIs
		int hiPtr;
		for ( ; loPtr != peakHead; loPtr = next(loPtr))
			for (hiPtr = next(loPtr); hiPtr != peakHead; hiPtr = next(hiPtr)) {
				double ioi = peakTime[hiPtr] - peakTime[loPtr];
				if (ioi >= MAX_IOI)
					break;
				iois[(int)Math.rint(ioi / timeBase)] += // 1 +	// better??
								Math.sqrt(peakSPL[hiPtr] * peakSPL[loPtr]);
			}
		for (int i = 0; i < iois.length; i++)
			yplot[i] = iois[i];	// copy values before they are destroyed
		// System.out.println("x = [");
		// for (int p = 0; p < 200; p++)
		// 	System.out.println(Format.d(1000*iois[p], 3));
		// System.out.println("];");
		// make clusters (with width defined by top())
		int clusterCount = 0;
		for ( ; clusterCount < CLUSTER_POINTS; clusterCount++) {
			double sum = 0;
			double max = 0;
			int maxIndex = 0;
			hiPtr = (int)(MIN_IOI / timeBase);			// ignore < 100ms
			loPtr = hiPtr;
			while (hiPtr < ioiPoints) {	// find window with greatest average
				if (hiPtr >= top(loPtr))
					sum -= iois[loPtr++];
				else {
					sum += iois[hiPtr++];
					if (sum / (top(loPtr) - loPtr) > max) {
						max = sum / (top(loPtr) - loPtr);
						maxIndex = loPtr;
					}
				}
			}
			if (max == 0)
				break;
			hiPtr = top(maxIndex);
			if (hiPtr > ioiPoints)
				hiPtr = ioiPoints;
			sum = 0;
			double weights = 0;
			for (loPtr = maxIndex; loPtr < hiPtr; loPtr++) {
				sum += loPtr * iois[loPtr];
				weights += iois[loPtr];
				iois[loPtr] = 0;	// use each value once
			}
			cluster[clusterCount] = sum / weights * timeBase; // Weighted av (s)
			clusterWgt[clusterCount] = max;
			// System.out.println(Format.d(cluster[clusterCount], 3) + "   " +
			// 				Format.d(clusterWgt[clusterCount], 3) + "   " +
			// 				Format.d(weights, 3) + "   " + maxIndex);
		}
		// re-weight clusters using related clusters
		for (int i = 0; i < clusterCount; i++) {
			newCluster[i] = cluster[i] * clusterWgt[i];
			newClusterWgt[i] = clusterWgt[i];
			for (int j = 0; j < clusterCount; j++) {
				if (i != j) {
					int ratio = getRatio(cluster[i], cluster[j]);
					// newCluster = sum[(val{*|/}rat)*wgt/rat] / sum[wgt/rat] 
					if (ratio > 0) {
						newCluster[i] += cluster[j] * clusterWgt[j];
						newClusterWgt[i] += clusterWgt[j] / ratio;
					} else if (ratio < 0) {
						newCluster[i] += cluster[j] * clusterWgt[j] / 
											(ratio * ratio);
						newClusterWgt[i] += clusterWgt[j] / -ratio;
					}
				}
			}
			newCluster[i] /= newClusterWgt[i];
		}
		for (int i = 0; i < CLUSTER_POINTS; i++) {
			if (i < clusterCount) {
				xplot2[i] = cluster[i];
				yplot2[i] = clusterWgt[i] * 3;
				xplot3[i] = newCluster[i];
				yplot3[i] = newClusterWgt[i] * 1.5;
			} else
				xplot2[i] = yplot2[i] = xplot3[i] = yplot3[i] = 0;
		}
		if (plotFlag) {
			if (plot == null)
				makePlot();
			plot.update();
		} else if (plot != null) {
			plot.close();
			plot = null;
		}
		// update best clusters; smooth over time
		double dt = t - peakTime[prev(prev(peakHead))];
		// System.out.println("CURRENT IOI = " + Format.d(dt,3));
		//if (false)	//SD for dance tests
		for (int i = 0; i < bestCount; i++) {
			bestUsed[i] = false;
			if (i != 0)
				bestWgt[i] *= Math.pow(DECAY_OTHER, dt); // memory decay 20%/s
			else
				bestWgt[i] *= Math.pow(DECAY_BEST, dt);  // memory decay 10%/s
			if (best[i] < LOW_IBI)		// penalise if too fast ...
				bestWgt[i] *= 1 - Math.pow((LOW_IBI - best[i]) /
											(2 * (LOW_IBI - MIN_IBI)), 3);
			else if (best[i] > HI_IBI)	// ... or too slow
				bestWgt[i] *= 1 - Math.pow((best[i] - HI_IBI) /
											(2 * (MAX_IBI - HI_IBI)), 3);
		}
		for (int i = 0; i < clusterCount; i++) {
			if ((newCluster[i] < MIN_IBI) || (newCluster[i] > MAX_IBI))
				continue;
			double dMax = newCluster[i]/CLUSTER_FACTOR + CLUSTER_WIDTH*timeBase;
			double dMin = dMax / 2;	// NEW:"/2"; don't allow values too far away
			int index = -1;
			for (int j = 0; j < bestCount; j++) {	// find the nearest match
				double diff = Math.abs(newCluster[i] - best[j]);
				if (diff < dMin) {
					dMin = diff;
					index = j;
				}
			}
			if (index >= 0) {						// match found; update best
				if (bestUsed[index])
					continue;
				// update is equivalent to exp.-decaying memory used in paint()
				best[index] += (newCluster[i] - best[index]) *HYP_CHANGE_FACTOR;
				//	* newClusterWgt[i] * (1 - dMin / dMax) / bestWgt[index];
				bestWgt[index] += newClusterWgt[i] * (1 - dMin / dMax);
				// bestUsed[index] = true;
			} else if (bestCount < CLUSTER_POINTS) {	// not full yet; add
				best[bestCount] = newCluster[i];
				bestWgt[bestCount++] = newClusterWgt[i];
			} else if (bestWgt[bestCount-1] < newClusterWgt[i]) { // best full;
				best[bestCount-1] = newCluster[i];				// add if better
				bestWgt[bestCount-1] = newClusterWgt[i];
			}
		}
		for (int i = 0; i < bestCount; i++)		// merge clusters
			for (int j = i + 1; j < bestCount; j++)
				if (Math.abs(best[i] - best[j]) <
										CLUSTER_WIDTH * timeBase / 2) {
					best[i] = (best[i] * bestWgt[i] +
									best[j] * bestWgt[j]) /
									(bestWgt[i] + bestWgt[j]);
					bestWgt[i] += bestWgt[j];
					for (int k = j + 1; k < bestCount; k++) {
						best[k-1] = best[k];
						bestWgt[k-1] = bestWgt[k];
					}
					bestCount--;
					j--;
				}
		boolean change = true;		// bubble sort, since almost sorted
		while (change) {
			change = false;
			for (int i = bestCount-1; i > 0; i--)	// sort by weight
				if (bestWgt[i] > bestWgt[i-1]) {
					change = true;
					double tmp = bestWgt[i];
					bestWgt[i] = bestWgt[i - 1];
					bestWgt[i - 1] = tmp;
					tmp = best[i];
					best[i] = best[i - 1];
					best[i - 1] = tmp;
				}
		}
		if (bestCount > 0) {
			// int maxIndex = 0;
			// for (int i = 1; i < clusterCount; i++)
			// 	if (newClusterWgt[i] > newClusterWgt[maxIndex])
			// 		maxIndex = i;
			tempo = best[0];
			// System.out.println("Best: " + Format.d(best[0], 3));
		}
	} // addPeak()

	void showTime() {
		System.out.println("Time = " +
						  Format.d(timeBase * (counter - MID_POINT), 3) + "\n");
	} // showTime()

	void saveHist() { //SD for dance test
		try {
			Format.init(1,1,3,false);
			FileWriter outfile = new FileWriter(new File("worm-dance.tmp"));
			for (int i = 0; i < CLUSTER_POINTS; i++)
				outfile.write(// Format.d(cluster[i], 3) + " : " +
							//	Format.d(clusterWgt[i], 3) + "     " +
							//	Format.d(newCluster[i], 3) + " : " +
							//	Format.d(newClusterWgt[i], 3) + "     " +
								((i < bestCount)? (Format.d(best[i], 3) + "   "+
									Format.d(bestWgt[i], 3)) : "") +
								"\n");
			outfile.write("\n");
			outfile.close();
			System.exit(0);
		} catch (IOException e) {
			System.err.println("Exception in saveHist(): " + e);
		}
	} // saveHist()

	int getRatio(double a, double b) {
		double r = a / b;
		if (r < 1)
			r = -1 / r;
		int round = (int)Math.rint(r);
		if ((Math.abs((r - round) / r) < RATIO_ERROR) &&
					(Math.abs(round) >= 2) && (Math.abs(round) <= 8))
			return round;
		return 0;
	} // getRatio()

	// Uses an n-point linear regression to estimate the slope of envelope
	double getSlope() {
		int start = SLOPE_POINTS - REGRESSION_SIZE;
		double sx = 0, sxx = 0, sy = 0, sxy = 0;
		for (int i = 0; i < REGRESSION_SIZE; i++) {
			sx += i;
			sxx += i * i;
			sy += envelope[start+i];
			sxy += i * envelope[start+i];
		}
		return (4 * sxy - sx * sy) / (4 * sxx - sx * sx) / timeBase;
	} // getSlope()

} // class TempoInducer
