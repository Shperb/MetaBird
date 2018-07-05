/*****************************************************************************
 ** IHSEV AIBirds Agent 2014
 ** Copyright (c) 2015, Mihai Polceanu, CERV Brest France
 ** Contact: polceanu@enib.fr
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.demo;

import javax.swing.*;
import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.*;
import org.jbox2d.testbed.framework.TestbedSetting.SettingType;
import org.jbox2d.testbed.framework.TestbedController.UpdateBehavior;

public class SimulationWrapper
{
	private TestbedModel model;
	private abSimulation simulation;
	private SimulationController controller;
	
	private boolean debug_mode = false;
	
	public SimulationWrapper(abSimulation sim)
	{
		this(sim, false);
	}
	
	public SimulationWrapper(abSimulation sim, boolean debug)
	{
		int simulationFrequency = 20; //hz, normal is 60hz
		
		debug_mode = debug;
		model = new TestbedModel();
		model.getSettings().getSetting("Hz").value = simulationFrequency;
		model.getSettings().getSetting("SubStepping").enabled = true;
		model.getSettings().getSetting("Pos Iters").value = 50;
		model.getSettings().getSetting("Vel Iters").value = 50;
		float timeRatio = ((float)simulationFrequency)/60.0f;
		//System.out.println("timeRatio = " + timeRatio);
		simulation = sim;
		
		simulation.setFinishTimeRatio(timeRatio);
		model.addTest(simulation);
		if (!debug_mode)
		{
			model.setDebugDraw(new DummyDebugDraw(null));
			simulation.init(model);
			model.setCurrTestIndex(0);
		}
		
		if (!debug_mode)
		{
			controller = new SimulationController(model);
			controller.playTest(0);
			controller.start();
		}
		else
		{
			TestbedPanel panel = new TestPanelJ2D(model);
			JFrame testbed = new TestbedFrame(model, panel, UpdateBehavior.UPDATE_CALLED);
			testbed.setVisible(true);
			testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}
	
	public void addSimulation(abSimulation sim)
	{
		model.addTest(sim);
	}
	
	public SimulationController getController()
	{
		return controller;
	}
	
	public abSimulation getSimulation()
	{
		return simulation;
	}
	
	public boolean tryToStop()
	{
            if (controller == null) return true; //debug mode stuff

            if (simulation.isFinished())
            {
                    controller.stop();
            }

            return (!controller.isAnimating());
	}
        
        public void abort()
        {
            if (controller == null) return;
            
            simulation.abort();

            if (simulation.isFinished())
            {
                controller.stop();
            }
            else
            {
                System.out.println("Simulation didn't terminate cleanly.");
            }
        }
	
	public void clean()
	{
		model.clearTestList();
		controller = null;
		model = null;
		simulation = null;
	}
}
