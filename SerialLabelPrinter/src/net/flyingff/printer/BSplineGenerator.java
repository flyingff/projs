package net.flyingff.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class BSplineGenerator {
	private List<float[]> points = new ArrayList<>();
	private final int k;
	
	/**
	 * 构造一个生成器
	 * @param k 阶数
	 */
	public BSplineGenerator(int k) {
		this.k = k;
	}
	public BSplineGenerator() { this(3); }
	
	public BSplineGenerator addPoint(float[] pt) {
		points.add(pt); return this;
	}
	public BSplineGenerator addPoint(float[] pt, int weight) {
		for(int i = 0; i < weight; i++) {
			points.add(pt);
		}
		return this;
	}
	public BSplineGenerator addAllPoints(float[]... pts) { points.addAll(Arrays.asList(pts)); return this; }
	public BSplineGenerator addAllPoints(Collection<float[]> pts) { points.addAll(pts); return this; }
	public BSplineGenerator clearPoints() { points.clear(); return this; }
	
	public BSpline build() {
		return new BSpline(points, k);
	}
	
	public static final class BSpline {
		private double[] controlVector;
		private float[][] pts;
		private int N;
		/** 次数 */
		private int P;
		private float minT, maxT, tRange;
		private boolean even = false;
		
		private BSpline(List<float[]> ptList, int k) {
			this.pts = ptList.toArray(new float[0][0]);
			this.P = k - 1;
			this.N = ptList.size();
			this.controlVector = new double[N + P + 1];
			for(int i = 0; i <= P; i++) {
				controlVector[i] = 0;
				controlVector[N + i] = 1;
			}
			
			double invX = 1.0 / (N - P);
			for(int i = P + 1; i <= N; i++) {
				controlVector[i] = (i - P) * invX;
			}
			
			/* evenly B spline control vector
			for(int i = 0; i < controlVector.length; i++) {
				controlVector[i] = (double)i / (controlVector.length - 1);
			}
			*/
			minT = (float)(k) / controlVector.length;
			maxT = 1 - minT;
			tRange = maxT - minT;
		}
		public void modifyControlVector(Consumer<double[]> modifier) {
			double[] copy = controlVector.clone();
			modifier.accept(copy);
			for(int i = 1; i < controlVector.length; i++) {
				if(copy[i - 1] > copy[i]) {
					throw new RuntimeException("Controller vector not in ascending order.");
				}
			}
			System.arraycopy(copy, 0, controlVector, 0, copy.length);
		}
		private double divide(double x, double y) {
			if(y == 0) { y = 1; }
			return x / y;
		}
		
		private double F(int i, int p, float u) {
			double ui = controlVector[i];
			double ui1 = controlVector[i + 1];
			if(p <= 0) {
				return ui <= u && (u < ui1 || (i + P + 2 == controlVector.length && u == ui1)) ? 1 : 0;
			} else {
				double uip = controlVector[i + p];
				double uip1 = controlVector[i + p + 1];
				
				return divide(u    - ui , uip  - ui ) * F(i    , p - 1, u)
					 + divide(uip1 - u	, uip1 - ui1) * F(i + 1, p - 1, u);
			}
		}
		
		public float[] sample(float t) {
			if(t < 0 || t > 1) throw new IllegalArgumentException("Sample arg 't' should be in range of [0, 1], but: " + t);
			
			float[] pt = new float[2];
			int i = 0;
			
			// sum with weight
			for(float[] p : pts) {
				double f = F(i++, P, t);
				// ignore if weight is zero
				if(f > 0) {
					pt[0] += p[0] * f;
					pt[1] += p[1] * f;
				}
			}
			
			return pt;
		}
		public BSpline makeEvenly() {
			modifyControlVector(v->{
				for(int i = 0; i < v.length; i++) {
					v[i] = i / (v.length - 1.0);
				}
			});
			even = true;
			return this;
		}
		public List<float[]> sample(int n) {
			List<float[]> sampleResult = new ArrayList<>(n + 1);
			if(even) {
				for(int i = 0; i <= n; i++) {
					sampleResult.add(sample(((float)i / n) * tRange + minT));
				}
			} else {
				for(int i = 0; i <= n; i++) {
					sampleResult.add(sample(((float)i / n)));
				}
			}
			return sampleResult;
		}
	}
	
}
