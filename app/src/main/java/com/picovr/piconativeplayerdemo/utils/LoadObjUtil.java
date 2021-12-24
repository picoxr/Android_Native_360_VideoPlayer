package com.picovr.piconativeplayerdemo.utils;

import android.content.Context;
import android.util.Log;

import com.picovr.piconativeplayerdemo.model.ObjVertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LoadObjUtil {

    public static ObjVertex loadFromSystem(String fPath) {
        Log.i("lhc", "loadFromSystem");
        ObjVertex objVertex = null;
        File objFile = new File(fPath);
        try {
            Log.i("lhc", "loadFromSystem " + objFile.exists() + " " + objFile.canRead());
            if (objFile.exists() && objFile.canRead()) {
                InputStream in = new FileInputStream(objFile);
                objVertex = getObjVertex(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return objVertex;
    }

    public static ObjVertex loadFromAssets(String fName, Context context) {
        Log.i("lhc", "loadFromAssets");
        ObjVertex objVertex = null;
        try {
            InputStream in = context.getAssets().open(fName);
            objVertex = getObjVertex(in);

        } catch (Exception e) {
            Log.e("lhc", "load error");
            e.printStackTrace();
        }
        return objVertex;
    }

    private static ObjVertex getObjVertex(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        ObjVertex objVertex = null;
        ArrayList<Float> vec_vertices = new ArrayList<>();
        ArrayList<Float> res_vertices = new ArrayList<>();
        ArrayList<Float> vec_uvs = new ArrayList<>();
        ArrayList<Float> res_uvs = new ArrayList<>();
        ArrayList<Float> vec_normals = new ArrayList<>();
        ArrayList<Float> res_normals = new ArrayList<>();
        String temps;
        while ((temps = br.readLine()) != null) {
            String[] tempsa = temps.split("[ ]+");
            if (tempsa[0].trim().equals("v")) {
                vec_vertices.add(Float.parseFloat(tempsa[1]));
                vec_vertices.add(Float.parseFloat(tempsa[2]));
                vec_vertices.add(Float.parseFloat(tempsa[3]));
            } else if (tempsa[0].trim().equals("vt")) {
                vec_uvs.add(Float.parseFloat(tempsa[1]));
                vec_uvs.add(1 - Float.parseFloat(tempsa[2]));
            } else if (tempsa[0].trim().equals("vn")) {
                vec_normals.add(Float.parseFloat(tempsa[1]));
                vec_normals.add(Float.parseFloat(tempsa[2]));
                vec_normals.add(Float.parseFloat(tempsa[3]));
            } else if (tempsa[0].trim().equals("f")) {
                int length = tempsa.length;
                for (int i = 1; i < length - 2; i++) {
                    String[] temp = tempsa[i].split("/");
                    setMatrix(vec_vertices, res_vertices, vec_uvs, res_uvs, vec_normals, res_normals, temp);
                    temp = tempsa[i + 1].split("/");
                    setMatrix(vec_vertices, res_vertices, vec_uvs, res_uvs, vec_normals, res_normals, temp);
                    temp = tempsa[i + 2].split("/");
                    setMatrix(vec_vertices, res_vertices, vec_uvs, res_uvs, vec_normals, res_normals, temp);
                }
            }
        }

        objVertex = new ObjVertex(res_vertices, res_uvs, res_normals);
        return objVertex;
    }

    private static void setMatrix(ArrayList<Float> vec_vertices, ArrayList<Float> res_vertices, ArrayList<Float> vec_uvs, ArrayList<Float> res_uvs, ArrayList<Float> vec_normals, ArrayList<Float> res_normals, String[] temp) {
        int hold = Integer.parseInt(temp[0]) - 1;
        res_vertices.add(vec_vertices.get(3 * hold));
        res_vertices.add(vec_vertices.get(3 * hold + 1));
        res_vertices.add(vec_vertices.get(3 * hold + 2));

        hold = Integer.parseInt(temp[1]) - 1;
        res_uvs.add(vec_uvs.get(2 * hold));
        res_uvs.add(vec_uvs.get(2 * hold + 1));

        hold = Integer.parseInt(temp[2]) - 1;
        res_normals.add(vec_normals.get(3 * hold));
        res_normals.add(vec_normals.get(3 * hold + 1));
        res_normals.add(vec_normals.get(3 * hold + 2));
    }
}
