package edu.lclark.orego.time;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.CopiableStructureFactory;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;
import edu.lclark.orego.mcts.SearchNode;

public class ExitingTimeManagerTest {
	
	private Player player;
	
	private ExitingTimeManager manager;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		// TODO Should we set a smaller transposition table size?
		player = new PlayerBuilder().threads(1).timeManagement().build();
		coords = player.getBoard().getCoordinateSystem();
		manager = (ExitingTimeManager)player.getTimeManager();
		player.setTimeManager(manager);
	}

	@Test
	public void testCompareRest() {
		player.setRemainingTime(10);
		SearchNode root = player.getRoot();
		assertNotEquals(0, manager.getTime());
		root.update(coords.at("a5"), 1000, 1000);
		assertEquals(0, manager.getTime(), .01);
	}
	
	@Test
	public void testRollover(){
		player.setRemainingTime(10);
		SearchNode root = player.getRoot();
		assertNotEquals(0, manager.getTime());
		
		root.update(coords.at("a5"), 1000, 1000);
		assertEquals(0, manager.getTime(), .01);
		assertNotEquals(0, manager.getRollover());
		for(short p : coords.getAllPointsOnBoard()){
			if(p != coords.at("a5")){
				root.update(p, 1000, 1000);
			}
		}
		while(manager.getTime() != 0){
			manager.getTime();
		}
		assertEquals(0, manager.getRollover());
	}

}