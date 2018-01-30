/*****************************************************************************
 ** IHSEV AIBirds Agent 2014
 ** Copyright (c) 2015, Mihai Polceanu, CERV Brest France
 ** Contact: polceanu@enib.fr
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.demo;

import ab.vision.ABShape;
import java.util.List;
import org.jbox2d.common.Vec2;

public class Rec2D
{
	public Rec2D()
	{
	
	}
	
	public Rec2D(float w, float h)
	{
		width = w;
		height = h;
	}
	
	public Rec2D(Rec2D rec)
	{
		this.height = rec.height;
		this.width = rec.width;
		this.x = rec.x;
		this.y = rec.y;
		this.angle = rec.angle;
		
		this.shape = rec.shape;
	}
	
	public float height = 0.0f;
	public float width = 0.0f;
	public float x = 0.0f;
	public float y = 0.0f;
	public float angle = 0.0f;
	
	public ABShape shape = ABShape.Rect; //default
}
