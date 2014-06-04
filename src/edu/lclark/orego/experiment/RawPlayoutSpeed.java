package edu.lclark.orego.experiment;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.move.SimpleRandom;
import edu.lclark.orego.score.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;

public final class RawPlayoutSpeed {

	public static void main(String[] args) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		Board board = new Board(19);
		Scorer scorer = new ChinesePlayoutScorer(board, 7.5);
		Feature f = new Conjunction(
				new NotEyeLike(board),
				new Disjunction(
						OnThirdOrFourthLine.forWidth(board.getCoordinateSystem().getWidth()),
						new NearAnotherStone(board)));
		SimpleRandom mover = new SimpleRandom(board, f);
//		SimpleRandom mover = new SimpleRandom(board, new NotEyeLike(board));
		final int runs = 100000;
		long total = 0;
		int[] wins = new int[3];
		for (int run = 0; run < runs; run++) {
			long before = System.nanoTime();
			board.clear();
			do {
				short p = mover.selectAndPlayOneMove(random);
//				System.out.println(board.toString(p));
//				System.out.println(board);
			} while (board.getPasses() < 2);
			wins[scorer.winner().index()]++;
			long after = System.nanoTime();
			total += (after - before);
//			System.out.println((after - before) * 1.0 / board.getTurn());
		}
		System.out.println((runs / 1000.0) / (total / 1000000000.0) + " kpps");
		System.out.println("Black wins: " + wins[BLACK.index()]);
		System.out.println("White wins: " + wins[WHITE.index()]);
		System.out.println("Ties: " + wins[VACANT.index()]);
	}

}