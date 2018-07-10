/*****************************************************************************
 ** IHSEV AIBirds Agent 2014
 ** Copyright (c) 2015, Mihai Polceanu, CERV Brest France
 ** Contact: polceanu@enib.fr
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.demo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jbox2d.testbed.framework.*;

/**
 * This class contains most control logic for the testbed and the update loop. It also watches the
 * model to switch tests and populates the model with some loop statistics.
 * 
 * @author Daniel Murphy
 */
public class SimulationController implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(TestbedController.class);

	public static final int DEFAULT_FPS = 60;

	private long startTime;
	private long frameCount;
	private int targetFrameRate;
	private float frameRate = 0;
	private boolean animating = false;
	private Thread animator;

	private final TestbedModel model;

	public SimulationController(TestbedModel argModel)
	{
		model = argModel;
		setFrameRate(DEFAULT_FPS);
		animator = new Thread(this, "Testbed");
	}

	protected void loopInit()
	{
		if (model.getCurrTest() != null)
		{
			model.getCurrTest().init(model);
		}
	}

	protected void update()
	{
		if (model.getCurrTest() != null)
		{
			model.getCurrTest().update();
		}
	}

	public void resetTest()
	{
		model.getCurrTest().reset();
	}

	public void saveTest()
	{
		model.getCurrTest().save();
	}

	public void loadTest()
	{
		model.getCurrTest().load();
	}

	public void playTest(int argIndex)
	{
		if (argIndex == -1)
		{
			return;
		}
		while (!model.isTestAt(argIndex))
		{
			if (argIndex + 1 < model.getTestsSize())
			{
				argIndex++;
			}
			else
			{
				return;
			}
		}
		model.setCurrTestIndex(argIndex);
	}

	public void setFrameRate(int fps)
	{
		if (fps <= 0)
		{
			throw new IllegalArgumentException("Fps cannot be less than or equal to zero");
		}
		targetFrameRate = fps;
		frameRate = fps;
	}

	public int getFrameRate()
	{
		return targetFrameRate;
	}

	public float getCalculatedFrameRate()
	{
		return frameRate;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public long getFrameCount()
	{
		return frameCount;
	}

	public boolean isAnimating()
	{
		return animating;
	}

	public synchronized void start()
	{
		if (animating != true)
		{
			frameCount = 0;
			animator.start();
		}
		else
		{
			log.warn("Animation is already animating.");
		}
	}

	public synchronized void stop()
	{
		animating = false;
	}

	public void run()
	{
		long beforeTime, afterTime, updateTime, timeDiff, sleepTime, timeSpent;
		float timeInSecs;
		beforeTime = startTime = updateTime = System.nanoTime();
		sleepTime = 0;

		animating = true;
		loopInit();
		while (animating)
		{
			timeSpent = beforeTime - updateTime;
			if (timeSpent > 0)
			{
				timeInSecs = timeSpent * 1.0f / 1000000000.0f;
				updateTime = System.nanoTime();
				frameRate = (frameRate * 0.9f) + (1.0f / timeInSecs) * 0.1f;
				model.setCalculatedFps(frameRate);
			}
			else
			{
				updateTime = System.nanoTime();
			}

			//if(panel.render()) {
			update();
			//panel.paintScreen();        
			//}
			frameCount++;

			afterTime = System.nanoTime();

			timeDiff = afterTime - beforeTime;
			sleepTime = (1000000000 / targetFrameRate - timeDiff) / 1000000;
			if (sleepTime > 0)
			{
				try
				{
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException ex)
				{
				}
			}

			beforeTime = System.nanoTime();
		} // end of run loop
	}
}