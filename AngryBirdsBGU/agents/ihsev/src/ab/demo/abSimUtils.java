/*****************************************************************************
 ** IHSEV AIBirds Agent 2014
 ** Copyright (c) 2015, Mihai Polceanu, CERV Brest France
 ** Contact: polceanu@enib.fr
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.demo;

import java.util.*;

public class abSimUtils
{
	private static abSimUtils instance;
	
	private List<SimulationWrapper> sims;
	
	public static abSimUtils getInstance()
	{
		if (instance == null) instance =  new abSimUtils();
		return instance;
	}
	
	public void addSimulation(SimulationWrapper sw)
	{
		if (!sims.contains(sw)) sims.add(sw);
	}
        
        public void abort()
        {
            for (int i=0; i<sims.size(); ++i)
            {
                sims.get(i).abort();
            }
        }
	
	public void clear()
	{
		for (int i=0; i<sims.size(); ++i)
		{
			sims.get(i).clean();
		}
		
		sims.clear();
	}
	
	public abSimulation getBestResult()
	{
		float maxPigsKilled = 0.0f;
		
		for (int i=0; i<sims.size(); ++i)
		{
			float tmpPigsKilled = sims.get(i).getSimulation().getPigsKilled();
			if (tmpPigsKilled > maxPigsKilled) maxPigsKilled = tmpPigsKilled;
		}
		
		List<SimulationWrapper> pigNumberCandidates = new ArrayList<SimulationWrapper>();
		for (int i=0; i<sims.size(); ++i)
		{
			float tmpPigsKilled = sims.get(i).getSimulation().getPigsKilled();
			if (tmpPigsKilled >= maxPigsKilled)
			{
				pigNumberCandidates.add(sims.get(i));
                                //System.out.println(sims.get(i).getSimulation().getShootingVector());
			}
		}
		/*
		for (int i=0; i<pigNumberCandidates.size(); ++i)
		{
			abSimulation tmpSim = pigNumberCandidates.get(i).getSimulation();
			System.out.println("first pig hit: "+tmpSim.getTimeKilledFirstPig());
		}
		*/
		abSimulation bestSimulation = null;
		
		if ((pigNumberCandidates.size() > 0) && (maxPigsKilled > 0))
		{
			long minKillTime = -1l;
			
			bestSimulation = pigNumberCandidates.get(0).getSimulation();
			minKillTime = bestSimulation.getTimeKilledFirstPig();
			
			for (int i=1; i<pigNumberCandidates.size(); ++i)
			{
				abSimulation tmpSim = pigNumberCandidates.get(i).getSimulation();
				if (tmpSim.getTimeKilledFirstPig() < minKillTime)
				{
					bestSimulation = tmpSim;
					minKillTime = tmpSim.getTimeKilledFirstPig();
				}
			}
		}
		else
		{
			int maxObjectsDestroyed = 0;
		
			if (pigNumberCandidates.size() > 0)
			{
				bestSimulation = sims.get(0).getSimulation();
				maxObjectsDestroyed = bestSimulation.getObjectsDestroyed();
				
				for (int i=1; i<sims.size(); ++i)
				{
					abSimulation tmpSim = sims.get(i).getSimulation();
					int tmpObjectsDestroyed = tmpSim.getObjectsDestroyed();
					
					if (tmpObjectsDestroyed > maxObjectsDestroyed)
					{
						bestSimulation = tmpSim;
						maxObjectsDestroyed = tmpObjectsDestroyed;
					}
				}
			}
		}
		System.out.println("maxPigsKilled = " + maxPigsKilled);
		
		return bestSimulation;
	}
	
	public boolean allFinished()
	{
		boolean allDone = true;
		for (int i=0; i<sims.size(); ++i)
		{
			allDone = allDone && sims.get(i).tryToStop();
		}
		
		return allDone;
	}
	
	private abSimUtils()
	{
		sims = new ArrayList<SimulationWrapper>();
	}
}
