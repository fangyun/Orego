package edu.lclark.orego.sgf;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class SgfParserTest {

	SgfParser parser;
	Board board;
	CoordinateSystem coords;
	
	@Before
	public void setUp() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		parser = new SgfParser(coords);
	}
	
	/** Delegate method to call at on coords. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testSgfToPoint() {
		// Test conversion of sgf (and human-readable strings) to ints
		assertEquals(at("e15"), parser.sgfToPoint("ee"));
		assertEquals(at("t1"), parser.sgfToPoint("ss"));
		assertEquals(at("a19"), parser.sgfToPoint("aa"));
		assertEquals(at("t19"), parser.sgfToPoint("sa"));
		assertEquals(at("a1"), parser.sgfToPoint("as"));
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void testSgfToMoves(){
		List<List<Short>> games = parser.parseGamesFromFile(new File("SgfTestFiles/19/1977-02-27.sgf"), 500);
		assertEquals(1, games.size());
		List<Short> game = games.get(0);
		assertEquals(180, game.size());
		assertEquals(coords.at("R16"), (short)game.get(0));
		assertEquals(coords.at("N11"), (short)game.get(179));
	}

}