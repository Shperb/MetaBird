/*****************************************************************************
 ** IHSEV AIBirds Agent 2014
 ** Copyright (c) 2015, Mihai Polceanu, CERV Brest France
 ** Contact: polceanu@enib.fr
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.demo;

import org.jbox2d.common.Color3f;
import org.jbox2d.common.IViewportTransform;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.callbacks.DebugDraw;

public class DummyDebugDraw extends DebugDraw
{
        
        protected int m_drawFlags;
        protected final IViewportTransform viewportTransform;

        public DummyDebugDraw(IViewportTransform viewport)
		{
			super(viewport);
			viewportTransform = viewport;
        }

        public void setFlags(int flags) {
                m_drawFlags = flags;
        }

        public int getFlags() {
                return m_drawFlags;
        }

        public void appendFlags(int flags) {
                m_drawFlags |= flags;
        }

        public void clearFlags(int flags) {
                m_drawFlags &= ~flags;
        }

        /**
         * Draw a closed polygon provided in CCW order.  This implementation
         * uses {@link #drawSegment(Vec2, Vec2, Color3f)} to draw each side of the
         * polygon.
         * @param vertices
         * @param vertexCount
         * @param color
         */
        public void drawPolygon(Vec2[] vertices, int vertexCount, Color3f color){
                if(vertexCount == 1){
                        //drawSegment(vertices[0], vertices[0], color);
                        return;
                }
                
                for(int i=0; i<vertexCount-1; i+=1){
                        //drawSegment(vertices[i], vertices[i+1], color);
                }
                
                if(vertexCount > 2){
                        //drawSegment(vertices[vertexCount-1], vertices[0], color);
                }
        }
        
        public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color3f argColor) {}

        /**
         * Draw a solid closed polygon provided in CCW order.
         * @param vertices
         * @param vertexCount
         * @param color
         */
        public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {}

        /**
         * Draw a circle.
         * @param center
         * @param radius
         * @param color
         */
        public void drawCircle(Vec2 center, float radius, Color3f color) {}
        
        /**
         * Draw a solid circle.
         * @param center
         * @param radius
         * @param axis
         * @param color
         */
        public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {}
        
        /**
         * Draw a line segment.
         * @param p1
         * @param p2
         * @param color
         */
        public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {}

        /**
         * Draw a transform.  Choose your own length scale
         * @param xf
         */
        public void drawTransform(Transform xf) {}

        /**
         * Draw a string.
         * @param x
         * @param y
         * @param s
         * @param color
         */
        public void drawString(float x, float y, String s, Color3f color) {}
        
        public void drawString(Vec2 pos, String s, Color3f color) {
          //drawString(pos.x, pos.y, s, color);
        }
                
        public IViewportTransform getViewportTranform(){
                return viewportTransform;
        }
        
        /**
         * @param x
         * @param y
         * @param scale
         * @see IViewportTransform#setCamera(float, float, float)
         */
        public void setCamera(float x, float y, float scale){
                //viewportTransform.setCamera(x,y,scale);
        }
        
        
        /**
         * @param argScreen
         * @param argWorld
         * @see org.jbox2d.common.IViewportTransform#getScreenToWorld(org.jbox2d.common.Vec2, org.jbox2d.common.Vec2)
         */
        public void getScreenToWorldToOut(Vec2 argScreen, Vec2 argWorld) {
                //viewportTransform.getScreenToWorld(argScreen, argWorld);
        }

        /**
         * @param argWorld
         * @param argScreen
         * @see org.jbox2d.common.IViewportTransform#getWorldToScreen(org.jbox2d.common.Vec2, org.jbox2d.common.Vec2)
         */
        public void getWorldToScreenToOut(Vec2 argWorld, Vec2 argScreen) {
                //viewportTransform.getWorldToScreen(argWorld, argScreen);
        }
        
        /**
         * Takes the world coordinates and puts the corresponding screen
         * coordinates in argScreen.
         * @param worldX
         * @param worldY
         * @param argScreen
         */
        public void getWorldToScreenToOut(float worldX, float worldY, Vec2 argScreen){
                //argScreen.set(worldX,worldY);
                //viewportTransform.getWorldToScreen(argScreen, argScreen);
        }
        
        /**
         * takes the world coordinate (argWorld) and returns
         * the screen coordinates.
         * @param argWorld
         */
        public Vec2 getWorldToScreen(Vec2 argWorld){
                Vec2 screen = new Vec2();
                //viewportTransform.getWorldToScreen( argWorld, screen);
                return screen;
        }
        
        /**
         * Takes the world coordinates and returns the screen
         * coordinates.
         * @param worldX
         * @param worldY
         */
        public Vec2 getWorldToScreen(float worldX, float worldY){
                Vec2 argScreen = new Vec2(worldX, worldY);
                //viewportTransform.getWorldToScreen( argScreen, argScreen);
                return argScreen;
        }
        
        /**
         * takes the screen coordinates and puts the corresponding 
         * world coordinates in argWorld.
         * @param screenX
         * @param screenY
         * @param argWorld
         */
        public void getScreenToWorldToOut(float screenX, float screenY, Vec2 argWorld){
                argWorld.set(screenX,screenY);
                //viewportTransform.getScreenToWorld(argWorld, argWorld);
        }
        
        /**
         * takes the screen coordinates (argScreen) and returns
         * the world coordinates
         * @param argScreen
         */
        public Vec2 getScreenToWorld(Vec2 argScreen){
                Vec2 world = new Vec2();
                //viewportTransform.getScreenToWorld(argScreen, world);
                return world;
        }
        
        /**
         * takes the screen coordinates and returns the
         * world coordinates.
         * @param screenX
         * @param screenY
         */
        public Vec2 getScreenToWorld(float screenX, float screenY){
                Vec2 screen = new Vec2(screenX, screenY);
                //viewportTransform.getScreenToWorld( screen, screen);
                return screen;
        }
}