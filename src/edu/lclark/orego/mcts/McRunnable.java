package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfSuggester;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.feature.ShapeSuggester;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.patterns.ShapeTable;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.PlayoutScorer;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

/**
 * Players use this class to perform multiple Monte Carlo runs in different
 * threads.
 */
public final class McRunnable implements Runnable {

	/** The board on which this McRunnable plays its moves. */
	private final Board board;

	private final CoordinateSystem coords;

	/** @see #getFancyHashes() */
	private final long[] fancyHashes;

	/** Moves not passing this filter should never be played. */
	private final Predicate filter;

	/** Keeps track of moves played. */
	private final HistoryObserver historyObserver;

	/** Counts stones for fast mercy cutoffs of playouts. */
	private final StoneCountObserver mercyObserver;

	/** Generates moves beyond the tree. */
	private final Mover mover;

	/**
	 * Used by RaveNode.recordPlayout. It is stored here rather than in RaveNode
	 * to avoid creating millions of ShortSets.
	 */
	private final ShortSet playedPoints;

	/** The Player that launches the thread wrapped around this McRunnable. */
	private final Player player;

	/** Number of playouts completed. */
	private long playoutsCompleted;

	/** Random number generator. */
	private final MersenneTwisterFast random;

	/** Determines winners of playouts. */
	private final PlayoutScorer scorer;

	/** An array of suggesters used for updating bias. */
	private final Suggester[] suggesters;

	/** An array of weights for each suggester used for updating bias. */
	private final int[] weights;

	public McRunnable(Player player, CopiableStructure stuff) {
		LgrfTable table = null;
		try {
			table = stuff.get(LgrfTable.class);
		} catch (final IllegalArgumentException e) {
			// If we get here, we're not using LGRF
		}
		final CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		coords = board.getCoordinateSystem();
		Suggester[] tempSuggesters = copy.get(Suggester[].class);
		ShapeTable shapeTable = null;
		ShapeSuggester shape = null;
		try {
			shapeTable = stuff.get(ShapeTable.class);
			shape = stuff.get(ShapeSuggester.class);
			shape.setTable(shapeTable);
		} catch (final IllegalArgumentException e) {
			// If we get here, we're not using shape
		}
		int[] tempWeights = copy.get(int[].class);
		if(shape != null){
			Suggester[] suggestersWithShape = new Suggester[tempSuggesters.length+1];
			int[] weightsWithShape = new int[tempWeights.length + 1];
			for(int i = 0; i<tempSuggesters.length; i++){
				suggestersWithShape[i] = tempSuggesters[i];
				weightsWithShape[i] = tempWeights[i];
			}
			suggestersWithShape[tempSuggesters.length] = shape; 
			tempSuggesters = suggestersWithShape;
			weightsWithShape[tempSuggesters.length] = 20; 
			tempWeights = weightsWithShape;
		}
		
		
		suggesters = tempSuggesters;
		
		weights = tempWeights;
		this.player = player;
		random = new MersenneTwisterFast();
		mover = copy.get(Mover.class);
		if (table != null) {
			final LgrfSuggester lgrf = copy.get(LgrfSuggester.class);
			lgrf.setTable(table);
		}
		scorer = copy.get(ChinesePlayoutScorer.class);
		mercyObserver = copy.get(StoneCountObserver.class);
		historyObserver = copy.get(HistoryObserver.class);
		filter = copy.get(Predicate.class);
		fancyHashes = new long[coords.getMaxMovesPerGame() + 1];
		playedPoints = new ShortSet(coords.getFirstPointBeyondBoard());
	}

	/**
	 * Accepts (plays on on this McRunnable's own board) the given move.
	 *
	 * @see edu.lclark.orego.core.Board#play(int)
	 */
	public void acceptMove(short p) {
		final Legality legality = board.play(p);
		assert legality == OK : "Legality " + legality + " for move "
				+ coords.toString(p) + "\n" + board;
		fancyHashes[board.getTurn()] = board.getFancyHash();
	}

	/** Copies data from that (the player's real board) to the local board. */
	public void copyDataFrom(Board that) {
		board.copyDataFrom(that);
		fancyHashes[board.getTurn()] = board.getFancyHash();
	}

	/** Returns the board associated with this runnable. */
	public Board getBoard() {
		return board;
	}

	/**
	 * Returns the sequence of fancy hashes for search nodes visited during this
	 * run. Only the elements between the real board's turn (inclusive) and this
	 * McRunnable's turn (exclusive) are valid.
	 */
	public long[] getFancyHashes() {
		return fancyHashes;
	}

	public HistoryObserver getHistoryObserver() {
		return historyObserver;
	}

	/**
	 * @return the playedMoves
	 */
	public ShortSet getPlayedPoints() {
		return playedPoints;
	}

	/** @return the player associated with this runnable */
	public Player getPlayer() {
		return player;
	}

	/** Returns the number of playouts completed by this runnable. */
	public long getPlayoutsCompleted() {
		return playoutsCompleted;
	}

	/** Returns the random number generator associated with this runnable. */
	public MersenneTwisterFast getRandom() {
		return random;
	}

	/** Returns the list of suggesters used for updating biases. */
	public Suggester[] getSuggesters() {
		return suggesters;
	}

	/** Returns the current turn number on this runnable's board. */
	public int getTurn() {
		return board.getTurn();
	}

	/**
	 * Returns the weights associated with each suggester used for updating
	 * bias.
	 */
	public int[] getWeights() {
		return weights;
	}

	/** Returns true if p passes this McRunnable's filter. */
	public boolean isFeasible(short p) {
		return filter.at(p);
	}

	/**
	 * Performs a single Monte Carlo run and incorporates it into player's
	 * search tree. The player should generate moves to the frontier of the
	 * known tree and then return. The McRunnable performs the actual playout
	 * beyond the tree, then calls incorporateRun on the player.
	 *
	 * @return The winning color, although this is only used in tests.
	 */
	public Color performMcRun() {
		copyDataFrom(player.getBoard());
		player.descend(this);
		Color winner;
		if (board.getPasses() == 2) {
			winner = scorer.winner();
		} else {
			winner = playout();
		}
		player.updateTree(winner, this);
		playoutsCompleted++;
		return winner;
	}

	/**
	 * Plays moves to the end of the game and returns the winner: BLACK, WHITE,
	 * or (in rare event of a tie or a playout canceled because it hits the
	 * maximum number of moves) VACANT.
	 */
	public Color playout() {
		do {
			if (board.getTurn() >= coords.getMaxMovesPerGame()) {
				// Playout ran out of moves, probably due to superko
				return VACANT;
			}
			if (board.getPasses() < 2) {
				selectAndPlayOneMove();
			}
			if (board.getPasses() >= 2) {
				// Game ended
				return scorer.winner();
			}
			final Color mercyWinner = mercyObserver.mercyWinner();
			if (mercyWinner != null) {
				// One player has far more stones on the board
				return mercyWinner;
			}
		} while (true);
	}

	/**
	 * Performs runs and incorporate them into player's search tree until this
	 * thread is interrupted.
	 */
	@Override
	public void run() {
		while (getPlayer().shouldKeepRunning()) {
			performMcRun();
		}
	}

	private short selectAndPlayOneMove() {
		return mover.selectAndPlayOneMove(random);
	}

}
