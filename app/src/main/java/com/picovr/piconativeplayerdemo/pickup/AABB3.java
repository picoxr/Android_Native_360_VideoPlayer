package com.picovr.piconativeplayerdemo.pickup;

import android.opengl.Matrix;

import com.picovr.piconativeplayerdemo.utils.MatrixUtil;

public class AABB3 {
	Vector3f min;
	Vector3f max;

	public AABB3() {
		min = new Vector3f();
		max = new Vector3f();
		empty();
	}

	public AABB3(float[] vertices) {
		min = new Vector3f();
		max = new Vector3f();
		empty();
		for (int i = 0; i < vertices.length; i += 3) {
			this.findMinMax(vertices[i], vertices[i + 1], vertices[i + 2]);
		}
	}

	/**
	 * init the AABB bounding box min and max value
	 */
	public void empty() {
		min.x = min.y = min.z = Float.POSITIVE_INFINITY;
		max.x = max.y = max.z = Float.NEGATIVE_INFINITY;
	}

	/**
	 * set the AABB bounding box min and max x,y,z value
	 * @param p the vector {x,y,z}
	 */
	public void findMinMax(Vector3f p) {
		if (p.x < min.x) {
			min.x = p.x;
		}
		if (p.x > max.x) {
			max.x = p.x;
		}
		if (p.y < min.y) {
			min.y = p.y;
		}
		if (p.y > max.y) {
			max.y = p.y;
		}
		if (p.z < min.z) {
			min.z = p.z;
		}
		if (p.z > max.z) {
			max.z = p.z;
		}
	}

	/**
	 * set the AABB bounding box min and max x,y,z value
	 * @param p the vector {x,y,z}
	 */
	public void findMinMax(float x, float y, float z) {
		if (x < min.x) {
			min.x = x;
		}
		if (x > max.x) {
			max.x = x;
		}
		if (y < min.y) {
			min.y = y;
		}
		if (y > max.y) {
			max.y = y;
		}
		if (z < min.z) {
			min.z = z;
		}
		if (z > max.z) {
			max.z = z;
		}
	}

	public Vector3f[] getAllCorners() {
		Vector3f[] result = new Vector3f[8];
		for (int i = 0; i < 8; i++) {
			result[i] = getCorner(i);
		}
		return result;
	}

	public Vector3f getCorner(int i) {
		if (i < 0 || i > 7) {
			return null;
		}
		return new Vector3f(((i & 1) == 0) ? max.x : min.x, ((i & 2) == 0) ? max.y : min.y, ((i & 4) == 0) ? max.z : min.z);
	}

	public AABB3 setToTransformedBox(float[] m) {
		Vector3f[] va = this.getAllCorners();
		float[] transformedCorners = new float[24];
		float[] tmpResult = new float[4];
		int count = 0;
		for (int i = 0; i < va.length; i++) {
			float[] point = new float[] { va[i].x, va[i].y, va[i].z, 1 };
			Matrix.multiplyMV(tmpResult, 0, m, 0, point, 0);
			transformedCorners[count++] = tmpResult[0];
			transformedCorners[count++] = tmpResult[1];
			transformedCorners[count++] = tmpResult[2];
		}
		MatrixUtil.logMatrix("AABB3 : setToTransformedBox ", transformedCorners);
		return new AABB3(transformedCorners);
	}

	public float getXSize() {
		return max.x - min.x;
	}

	public float getYSize() {
		return max.y - min.y;
	}

	public float getZSize() {
		return max.z - min.z;
	}

	public Vector3f getSize() {
		return max.minus(min);
	}

	public Vector3f getCenter() {
		return (min.add(max)).multiK(0.5f);
	}

	/**
	 * ray intersect estimate
	 * @param rayStart ray start coordinate
	 * @param rayDir ray direction vector
	 * @param returnNormal
	 * @return
	 */
	public float rayIntersect(Vector3f rayStart, Vector3f rayDir, Vector3f returnNormal) {
		final float kNoIntersection = Float.POSITIVE_INFINITY;
		boolean inside = true;
		float xt, xn = 0.0f;

		if (rayStart.x < min.x) {
			xt = min.x - rayStart.x;
			if (xt > rayDir.x) {
				return kNoIntersection;
			}
			xt /= rayDir.x;
			inside = false;
			xn = -1.0f;
		} else if (rayStart.x > max.x) {
			xt = max.x - rayStart.x;
			if (xt < rayDir.x) {
				return kNoIntersection;
			}
			xt /= rayDir.x;
			inside = false;
			xn = 1.0f;
		} else {
			xt = -1.0f;
		}

		float yt, yn = 0.0f;
		if (rayStart.y < min.y) {
			yt = min.y - rayStart.y;
			if (yt > rayDir.y) {
				return kNoIntersection;
			}
			yt /= rayDir.y;
			inside = false;
			yn = -1.0f;
		} else if (rayStart.y > max.y) {
			yt = max.y - rayStart.y;
			if (yt < rayDir.y) {
				return kNoIntersection;
			}
			yt /= rayDir.y;
			inside = false;
			yn = 1.0f;
		} else {
			yt = -1.0f;
		}

		float zt, zn = 0.0f;
		if (rayStart.z < min.z) {
			zt = min.z - rayStart.z;
			if (zt > rayDir.z) {
				return kNoIntersection;
			}
			zt /= rayDir.z;
			inside = false;
			zn = -1.0f;
		} else if (rayStart.z > max.z) {
			zt = max.z - rayStart.z;
			if (zt < rayDir.z) {
				return kNoIntersection;
			}
			zt /= rayDir.z;
			inside = false;
			zn = 1.0f;
		} else {
			zt = -1.0f;
		}
		if (inside) {
			if (returnNormal != null) {
				returnNormal = rayDir.multiK(-1);
				returnNormal.normalize();
			}
			return 0.0f;
		}
		int which = 0;
		float t = xt;
		if (yt > t) {
			which = 1;
			t = yt;
		}
		if (zt > t) {
			which = 2;
			t = zt;
		}
		switch (which) {
		case 0: {
			float y = rayStart.y + rayDir.y * t;
			if (y < min.y || y > max.y) {
				return kNoIntersection;
			}
			float z = rayStart.z + rayDir.z * t;
			if (z < min.z || z > max.z) {
				return kNoIntersection;
			}
			if (returnNormal != null) {
				returnNormal.x = xn;
				returnNormal.y = 0.0f;
				returnNormal.z = 0.0f;
			}
		}
			break;
		case 1: {
			float x = rayStart.x + rayDir.x * t;
			if (x < min.x || x > max.x) {
				return kNoIntersection;
			}
			float z = rayStart.z + rayDir.z * t;
			if (z < min.z || z > max.z) {
				return kNoIntersection;
			}
			if (returnNormal != null) {
				returnNormal.x = 0.0f;
				returnNormal.y = yn;
				returnNormal.z = 0.0f;
			}
		}
			break;
		case 2: {
			float x = rayStart.x + rayDir.x * t;
			if (x < min.x || x > max.x) {
				return kNoIntersection;
			}
			float y = rayStart.y + rayDir.y * t;
			if (y < min.y || y > max.y) {
				return kNoIntersection;
			}
			if (returnNormal != null) {
				returnNormal.x = 0.0f;
				returnNormal.y = 0.0f;
				returnNormal.z = zn;
			}
		}
			break;
		}
		return t;
	}
}
