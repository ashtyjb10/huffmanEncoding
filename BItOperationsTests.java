package cs2420;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Ashton Schmidt, Andrew Worley
 * 
 */
public class BItOperationsTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test_merge_four_bytes_byte_sequence_0000() {
		assertEquals(0, Bit_Operations.merge_four_bytes((byte) 0, (byte) 0,(byte) 0,(byte) 0));
	}
	
	@Test
	public void test_merge_four_bytes_byte_sequence_0001() {
		assertEquals(1, Bit_Operations.merge_four_bytes((byte) 0, (byte) 0,(byte) 0,(byte) 1));
	}

	@Test
	public void test_merge_four_bytes_byte_sequence_00210() {
		assertEquals(522, Bit_Operations.merge_four_bytes((byte) 0, (byte) 0,(byte) 2,(byte) 10));
	}
	
	@Test
	public void test_merge_four_bytes_byte_sequence_127127127() {
		assertEquals(2_139_062_143, Bit_Operations.merge_four_bytes((byte) 127, (byte) 127,(byte) 127,(byte) 127));
	}
	
	@Test
	public void test_merge_four_bytes_byte_sequence_128128128() {
		/*
		 * Expected negative due to signed binary
		 */
		assertEquals(-2_139_062_144, Bit_Operations.merge_four_bytes((byte) 128, (byte) 128,(byte) 128,(byte) 128));
	}
}
