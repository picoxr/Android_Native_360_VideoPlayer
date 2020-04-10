package com.picovr.piconativeplayerdemo;

import android.util.Log;

import java.util.ArrayList;

public class ObjVertex {
    private int verticesCount;
    private float[] vertices;
    private float[] uvs;
    private float[] normals;

    public ObjVertex(ArrayList<Float> res_vertices, ArrayList<Float> res_uvs, ArrayList<Float> res_normals) {
        int size=res_vertices.size();
        vertices = new float[size];
        for(int i=0;i<size;i++)
        {
            vertices[i]=res_vertices.get(i);
        }
        verticesCount = size/3;
        Log.i("lhc","vertices " + size);

        size = res_uvs.size();
        uvs = new float[size];
        for (int i=0;i<size;i++) {
            uvs[i] = res_uvs.get(i);
        }
        Log.i("lhc","uvs " + size);

        size = res_normals.size();
        normals = new float[size];
        for (int i=0;i < size;i++) {
            normals[i] = res_normals.get(i);
        }
        Log.i("lhc","normals " + size);
    }

    public int getVerticesCount() {
        return verticesCount;
    }

    public float[] getVertices(){
        return vertices;
    }

    public float[] getUvs(){
        return uvs;
    }

    public float[] getNormals(){
        return normals;
    }
}
