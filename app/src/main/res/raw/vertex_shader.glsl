uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;
attribute vec3 aPosition;
attribute vec4 aTexCoord;
varying vec2 vCoordinate;

void main(){
    gl_Position=uMVPMatrix*vec4(aPosition,1);
    vCoordinate = (uSTMatrix * aTexCoord).xy;
}