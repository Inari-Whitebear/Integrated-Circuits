package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;

public interface IConfigurableDelay {
	public int getConfigurableDelay(Vec2i pos, ICircuit parent);

	public void setConfigurableDelay(Vec2i pos, ICircuit parent, int delay);
}