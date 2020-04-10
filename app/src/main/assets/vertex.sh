uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;

attribute vec4 aColor;
varying vec4 vColor;

void main()
{
   gl_Position = uMVPMatrix * vec4(aPosition,1);
   vTextureCoord = aTextureCoord;
   vColor = aColor;
}