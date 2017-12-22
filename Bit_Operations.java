package cs2420;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Helper methods for dealing with bytes and bits
 * 
 * @author H. James de St. Germain, modified by Ashton Schmidt and Andrew Worley
 *
 */
public final class Bit_Operations
{

	/**
	 * this is a "static" class (all methods are static), don't allow one to be created
	 */
	private Bit_Operations()
	{
		;
	}
	
	/**
	 * 
	 * Return a byte array representing the bitset data.
	 * 
	 * This method converts a bitset to a list of bytes. To complicate matters,
	 * because BitSets use LITTLE ENDIAN notation, we have to first convert the
	 * bits to BIG ENDIAN (the way we want the bits), and then output as bytes
	 * 
	 * Here is what that means:
	 * 
	 *   if our bit stream is as follows (1 1 0 0 0 0 0 0) and you just use
	 *   bitset.toByteArray, the output byte would be: (0 0 0 0 0 0 1 1)
	 * 
	 * To swap from little to big, you must reverse the order of the bits
	 * on a byte by byte basis.  Thus if you have the bit set:
	 *
	 *    0 1 2 3 4 5 6 7 8 9 a b c d e f
	 *    -------------------------------
	 *    1 0 1 1 0 1 1 1 0 0 0 0 0 1 1 0
	 *    
	 * which is two bytes
	 * 
	 *    0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7
	 *    ---------------   ---------------
	 *    1 0 1 1 0 1 1 1 | 0 0 0 0 0 1 1 0
	 *    
	 * we reverse the first byte and then the second giving us
	 * 
	 *    1 1 1 0 1 1 0 1 | 0 1 1 0 0 0 0 0
	 *    
	 * then when we dump the byteArray, these will be again reversed and we get what we expected
	 *    
	 * 
	 * tldr; For each 8 bits (byte) in the bit set, reverse the order of those bits.
	 * 
	 * @param bits - the bitset to convert from little endian to big endian
	 * 
	 * @return the new bitset
	 */

	public static byte[] get_bytes (BitSet bits) {
		BitSet result = new  BitSet(bits.length());
		
		for (int byte_start_index = 0; byte_start_index < bits.length(); byte_start_index += 8) {
			for (int bit_offset = 0; bit_offset < 8; bit_offset++) {
				int get_index = byte_start_index + bit_offset;
				int set_index = byte_start_index + (7 - bit_offset);
				
				if (bits.get(get_index)) {
					result.set(set_index);
				}
			}
		}
		
		return result.toByteArray();
	}

	/**
	 * convert an integer into 4 bytes
	 * 
	 * @param value - an integer value
	 * @return the four bytes that make up the integer
	 */
	public static byte[] convert_integer_to_bytes(int value) {
		ByteBuffer int_to_byte_converter = ByteBuffer.allocate(4);
		
		int_to_byte_converter.putInt(value);
		
		return int_to_byte_converter.array();
	}
	
	/**
	 * Get a single bit out of an 8 bit byte
	 * 
	 * remember the bit sequence for the number 7 is:  00000111
	 *                                      indexes:   01234567
	 * get_bit(7,0) --> false
	 * get_bit(7,1) --> false
	 * get_bit(7,2) --> false
	 * get_bit(7,3) --> false
	 * get_bit(7,4) --> false
	 * get_bit(7,5) --> true
	 * get_bit(7,6) --> true
	 * get_bit(7,7) --> true
	 * 
	 * @param the_byte 
	 * @param position - 0 to 7  with 0 being on the left and 7 being on the right
	 * @return the bit as a boolean
	 */
	public static boolean get_bit(byte the_byte, int position) {
		return ((0x1 << (7 - position)) & the_byte) != 0;
	}
	
	/**
	 * @param byte_input - positive byte value
	 * @return - permutation of 0s and 1s related to the byte argument
	 */
	public static String binary_String(byte byte_input, int show_bits) {
		String result = "";
			
		for (int position = 0; position < show_bits; position++) {
			if (position == 4) {
				result += "";//add space if desired space between 4 bits
			}
			//build result right to left
			if ((0x1 << (7 - position) & byte_input) != 0) {
				result += "1";
			} else {
				result += "0";
			}
		}

		return result;
	}
	
	/**
	 * @param byte_input - byte to be represented in decimal and binary
	 */
	public static void print_byte(byte byte_input, int show_bits) {
		String binary_string = binary_String(byte_input, show_bits);
		
		System.out.println("Decimal: "+ byte_input +"\tBinary: "+ binary_string);
	}

	/**
	 * Merge four bytes to create a single integer.
	 * 
	 * @param byte_0 - left most byte
	 * @param byte_1
	 * @param byte_2
	 * @param byte_3 - right most byte
	 * 
	 * @return - the integer value of the merged bytes
	 */
	public static int merge_four_bytes(byte byte_0, byte byte_1, byte byte_2, byte byte_3) {		
		int lhs = (Byte.toUnsignedInt(byte_0) << 8 | Byte.toUnsignedInt(byte_1));
		int rhs = (Byte.toUnsignedInt(byte_2) << 8 | Byte.toUnsignedInt(byte_3));
		int result = (lhs << 16 | rhs);
		
		return result;
	}

}
