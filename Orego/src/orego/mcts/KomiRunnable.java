package orego.mcts;

import orego.policy.Policy;

public class KomiRunnable extends McRunnable {
	
	public KomiRunnable(DynamicKomiPlayer player, Policy policy) {
		super(player, policy);
	}
	
	/**
	 * Performs runs and incorporate them into player's search tree until this
	 * thread is interrupted.
	 */
	@Override
	public void run() {
		boolean limitPlayouts = getPlayer().getMillisecondsPerMove() <= 0;
		int playouts = 0;
		int limit = getPlayer().getPlayoutLimit();
		while ((limitPlayouts && playouts < limit)
				|| (!limitPlayouts & getPlayer().shouldKeepRunning())) {
			performMcRun();
			playouts++;
			if (playouts % 1000 == 0 ) {
				DynamicKomiPlayer player = (DynamicKomiPlayer) getPlayer();
				player.valueSituationalCompensation();
			}
		}
	}
}
