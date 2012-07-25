package orego.response;

import orego.core.Board;
import orego.core.Coordinates;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class AverageResponsePlayer extends ResponsePlayer {

	public static void main(String[] args) {
		ResponsePlayer p = new ResponsePlayer();
		try {
			p.setProperty("policy", "Escape:Pattern:Capture");
			p.setProperty("threads", "2");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	@Override
	protected int findAppropriateMove(Board board, int history1, int history2,
			MersenneTwisterFast random, boolean isFinalMove) {
		int colorToPlay = board.getColorToPlay();
		int bestMove = Coordinates.PASS;
		double bestSum = 0;
		boolean skipOne = false;
		boolean skipTwo = false;

		// pick table based on threshold values
		RawResponseList twoList = (RawResponseList) getResponses().get(
				levelTwoEncodedIndex(history2, history1, colorToPlay));
		RawResponseList oneList = (RawResponseList) getResponses().get(
				levelOneEncodedIndex(history1, colorToPlay));
		RawResponseList zeroList = (RawResponseList) getResponses().get(
				levelZeroEncodedIndex(colorToPlay));
		skipOne = (oneList==null);
		skipTwo = (twoList==null);
		// TODO Should we include data from lists with very few updates?
		IntSet vacantPoints = board.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			int divisor = 1;
			int currMove = vacantPoints.get(i);
			double currWinRate = zeroList.getWinRate(currMove);
			if (!skipOne) {
				currWinRate += oneList.getWinRate(currMove);
				divisor++;
				if (!skipTwo) {
					currWinRate += twoList.getWinRate(currMove);
					divisor++;
				}
			}
			double average = currWinRate / divisor;
			if (average > bestSum) {
				if (board.isFeasible(currMove) && board.isLegal(currMove)) {
					bestSum = average;
					bestMove = currMove;
				}
			}
		}
		return bestMove;
	}

}