package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.LgrfTable;

/** Builds a player. */
@SuppressWarnings("hiding")
public final class PlayerBuilder {

	private int biasDelay;

	private int gestation;

	private double komi;

	private int msecPerMove;

	private int threads;

	private int width;

	private boolean rave;

	private boolean lgrf2;

	public PlayerBuilder() {
		// Default values
		biasDelay = 1;
		gestation = 1;
		komi = 7.5;
		threads = 2;
		msecPerMove = 1000;
		width = 19;
	}

	public PlayerBuilder biasDelay(int biasDelay) {
		this.biasDelay = biasDelay;
		return this;
	}

	public PlayerBuilder boardWidth(int width) {
		this.width = width;
		return this;
	}

	/** Creates the Player. */
	public Player build() {
		CopiableStructure copyStructure = lgrf2 ? CopiableStructureFactory.lgrfWithPriors(width,
				komi) : CopiableStructureFactory.useWithPriors(width, komi);
		Player result = new Player(threads, copyStructure);
		Board board = result.getBoard();
		CoordinateSystem coords = board.getCoordinateSystem();
		TranspositionTable table;
		if (rave) {
			table = new TranspositionTable(new RaveNodeBuilder(coords),
					coords);
			result.setTreeDescender(new RaveDescender(board, table, biasDelay));
		} else {
			table = new TranspositionTable(new SimpleSearchNodeBuilder(coords),
					coords);
			result.setTreeDescender(new UctDescender(board, table, biasDelay));
		}
		TreeUpdater updater;
		if(lgrf2){
			updater = new LgrfUpdater(new SimpleTreeUpdater(board, table, gestation), copyStructure.get(LgrfTable.class));
		}else{
			updater = new SimpleTreeUpdater(board, table, gestation);
		}
		result.setTreeUpdater(updater);
		result.setMsecPerMove(msecPerMove);
		return result;
	}

	public PlayerBuilder gestation(int gestation) {
		this.gestation = gestation;
		return this;
	}

	public PlayerBuilder komi(double komi) {
		this.komi = komi;
		return this;
	}

	public PlayerBuilder lgrf2() {
		lgrf2 = true;
		return this;
	}

	public PlayerBuilder msecPerMove(int msec) {
		this.msecPerMove = msec;
		return this;
	}

	public PlayerBuilder rave() {
		rave = true;
		return this;
	}

	public PlayerBuilder threads(int threads) {
		this.threads = threads;
		return this;
	}

}
