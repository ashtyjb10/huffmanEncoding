package cs2420;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author Ashton Schmidt, Andrew Worley
 * 
 */
public class NodeTests {

	Node left_child = new Node("a", 11);
	Node right_child = new Node("b", 11);
	Node left_child_par_2 = new Node("c", 11);
	Node right_child_par2 = new Node("d", 11);
	Node parent_1 = new Node("124",left_child, right_child);
	Node parent_2 = new Node("123",left_child_par_2, right_child_par2); 
	Node grandparent =new Node ("185", parent_1, parent_2);
	
	@Test
	public void test_get_symbol() 
	{
		assertEquals("a", left_child.get_symbol());
		assertEquals("b", right_child.get_symbol());
		assertEquals("124", parent_1.get_symbol());
	}
	@Test
	public void test_to_string() 
	{
		assertEquals("a, Freq:11", left_child.toString());
		assertEquals("b, Freq:11", right_child.toString());
		assertEquals("124, Freq:22", parent_1.toString());
	}
	@Test
	public void test_is_leaf()
	{
		assertTrue(left_child.leaf());
		assertTrue(right_child.leaf());
		assertFalse(parent_1.leaf());
	}
	@Test
	public void test_set_and_get_parent()
	{
		 parent_1.set_parent(grandparent);
		 assertFalse(grandparent.leaf());
		 assertFalse(parent_1.leaf());
		 assertEquals("185, Freq:44", parent_1.get_parent().toString());
		 assertEquals(null, grandparent.get_parent());
	}
	@Test
	public void test_parents_left()
	{
		parent_1.set_parent(grandparent);
		right_child.set_parent(parent_1);
		assertEquals(parent_1.toString(), parent_1.parents_left().toString());
		assertEquals(left_child.toString(), right_child.parents_left().toString());
	}
	@Test
	public void test_get_freq()
	{
		assertEquals(11, left_child.get_frequency());
		assertEquals(11, right_child.get_frequency());

	}
	@Test
	public void test_inc_freq()
	{
		left_child.increment_frequency();
		right_child.increment_frequency();
		assertEquals(12, left_child.get_frequency());
		assertEquals(12, right_child.get_frequency());
	}
	@Test
	public void test_get_sym()
	{
		String result = grandparent.get_symbol("00");
		assertEquals("a", result);

		result = grandparent.get_symbol("01");
		assertEquals("b", result);
	}
	@Test
	public void test_compare()
	{
		assertTrue( parent_1.compareTo(parent_2) < 0); //124 to 123
		assertTrue(parent_2.compareTo(parent_1) > 0);
		assertTrue(left_child.compareTo(right_child) < 0); // 11: 11, a:b
	}
	
	
	
	
	

}
