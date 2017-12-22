package cs2420;

import static cs2420.Bit_Operations.*;
import static cs2420.Utility.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Original Huffman code by Erin Parker, April 2007 Adapted by H. James
 *         de St. Germain to words as symbols 2008 Heavily Updated by H. James
 *         de St. Germain 2017. Class updated for assignment by Andrew Worley and Ashton Schmidt 
 * 
 *         Implements file compression and decompression using Huffman's
 *         algorithm.
 * 
 *         Instead of compressing on just characters, we treat words as symbols
 *         as well. To see what the best levels of compression are, we choose
 *         the "N" most frequent words (modified by their length) and use these
 *         along with characters as symbols for compression.
 */
public class HuffmanTreeUsingWords {
	/*
	 * allows us to tag the end of a file with a special character
	 * 
	 * otherwise it's not clear how to end the bit stream output of compression
	 * (i.e., bit stream needs to print a full byte at end, but we may only have
	 * a partial byte)
	 */
	final static String EOF = "EOF";

	/*
	 * For encoding, how many words to treat as symbols
	 */
	int WORD_COUNT;

	/*
	 * For a verbose account of what is going on, set these to true.
	 */
	static final boolean VERBOSE_ENCODING_TREE = false;
	static final boolean VERBOSE_FILE_SIZE = false;
	static final boolean VERBOSE_PRINT_SYMBOL_BITS = false;
	static final boolean VERBOSE_PRINT_TREE = false;

	
	/*
	 * The root of the Huffman tree
	 */
	private Node root;

	/**
	 * Constructor for an empty tree
	 * 
	 * @param words_as_symbols_count - take the top N words and use as symbols
	 */
	public HuffmanTreeUsingWords(int words_as_symbols_count) {
		this.WORD_COUNT = words_as_symbols_count;

		this.root = null;
	}

	/**
	 * Generates a compressed version of the input file.
	 * 
	 * 1) read the file, counting all the symbols 
	 * 2) create the huffman tree based on frequency counts 
	 * 3) compress the data into a binary file
	 * 
	 * @param infile - input file of (uncompressed) data
	 * @param outfile - output file of compressed data
	 */
	public void compress_file(File infile, File outfile) {
		List<String> ordered_list_of_symbols = new ArrayList<>();//will hold symbols from file in order of appearance
		Hashtable<String, Node> top_words;//will only hold top N (by frequency) words in file
		Hashtable<String, Node> all_symbols;//all symbols in file, no order (quick access)
		ArrayList<Character> buffer = read_file(infile);//all symbols from file

		top_words = compute_most_common_word_symbols(buffer, this.WORD_COUNT);
		all_symbols = compute_remaining_single_character_symbols(buffer, top_words, ordered_list_of_symbols);

		create_tree(all_symbols.values());//all symbols contains top words post compute_remaining_single_character_symbols
		compress_data(outfile, all_symbols, ordered_list_of_symbols);//build file header and data stream
	}

	/**
	 * Generates a decompressed version of the input file.
	 * 
	 * 1) Read the encoding information (how to reconstruct the tree) from the
	 * front of the file. 2) Build the Huffman tree (exactly as it was for
	 * compression) 3) read the bits from the compressed file one by one until a
	 * bit sequence finds a leaf in the huffman tree. report the symbol in the
	 * leaf and start again.
	 * 
	 * @param infile - Path to input file (of compressed data)
	 * @param outfile - output file of decompressed data
	 */
	public void decompress_file(Path path, File outfile) {
		try {
			//have to read the file as bytes and then process bits inside of those bytes
			byte[] bytes = Files.readAllBytes(path);
			ByteBuffer byte_buffer = ByteBuffer.wrap(bytes);
			Hashtable<String, Node> symbols = new Hashtable<>();//symbols contains all data, header and data stream
			
			if (byte_buffer.remaining() == 0) {//check for empty files pre-decompression
				System.err.println("File contains no data");
				throw new IOException();
			}

			symbols = read_file_header_with_symbol_frequencies(byte_buffer); // builds symbol frequency table

			if (VERBOSE_FILE_SIZE) {
				System.out.println("");
				System.out.printf("\tHeader Size in Bytes:   %10d\n", byte_buffer.position());
				System.out.printf("\tBody Size in Bytes:     %10d\n", byte_buffer.remaining());
				System.out.printf("\tTotal Size in Bytes:    %10d\n\n", bytes.length);
			}
			
			this.root = create_tree(symbols.values());//create header
			decompress_data(this.root, byte_buffer, outfile);//decode symbols from remaining bytes in byte_buffer
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Write the compressed file, including the encoding information and the
	 * compressed data.
	 * 
	 * @param file - the file to write the data to
	 * @param ordered_list_of_symbols - the symbols created by parsing the input file
	 * 
	 * @throws IOException - if something goes wrong...
	 */
	private static void compress_data(File file,
									  Hashtable<String, Node> symbols, 
									  List<String> ordered_list_of_symbols) {
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			//only need symbols, order does not matter (header used for tree creation)
			byte[] file_header = build_file_header(symbols.values());
			//order matters, for appearance of symbols in stream
			byte[] symbol_bit_stream = build_compressed_bit_stream(ordered_list_of_symbols, symbols);

			out.write(file_header);//add header first
			out.write(symbol_bit_stream);

			if (VERBOSE_FILE_SIZE) {
				System.out.printf("Header Size:   %10d bytes\n", file_header.length);
				System.out.printf("Encoding Size: %10d bytes\n", symbol_bit_stream.length);
				System.out.printf("Total Size:    %10d bytes\n", (file_header.length + symbol_bit_stream.length));
			}

		} catch (IOException e) {
			throw new RuntimeException("Error: could not read file");
		}
	}

	/**
	 * Read the compressed data bit stream, translating it into characters, and
	 * writing the uncompressed values into "out"
	 * 
	 * @param bits - the compressed bit stream that needs to be turned back into real characters and words
	 * @param outfile - the file to put this data into
	 * @throws IOException - if something goes wrong
	 */
	public static void decompress_data(Node huffman_root, //public for testing
										ByteBuffer bits, 
										File outfile) throws IOException {
		List<String> symbol_list = convert_bitstream_to_original_symbols(bits, huffman_root);
		write_data(outfile, symbol_list);
	}

	/**
	 * The compressed file has two parts: 
	 * a) the HEADER - symbol frequency count
	 * b) the DATA - compressed symbol bit stream
	 * 
	 * Here we read the HEADER and build the list of symbols (and counts)
	 * 
	 * 1) read four bytes (representing the number of characters in the symbol
	 * 2) read that many groups of bytes (each is a character in the symbol) 
	 * 3) read four bytes (representing the symbol's frequency)
	 * 
	 * repeat from 1, unless we read a zero, then the encoding table is complete
	 * 
	 * @param file_bytes - the bytes from the compressed file (which start with the header information)
	 * 
	 * @return the hashtable containing all the symbol nodes
	 */
	public static Hashtable<String, Node> read_file_header_with_symbol_frequencies(ByteBuffer file_bytes) {//public for testing
		if (VERBOSE_ENCODING_TREE) {
			System.out.println("\n---------------- Reading encoding tree information  -----------------");
		}
		
		Hashtable<String, Node> word_symbols = new Hashtable<>();
		//get our first symbols length
		int symbol_length = Bit_Operations.merge_four_bytes(file_bytes.get(), 
															file_bytes.get(), 
															file_bytes.get(), 
															file_bytes.get());
		
		/*
		 * Note: only symbols contained in 0 - 128 will be read
		 * 
		 * L = length of symbol
		 * S = symbol
		 * F = frequency
		 * 
		 * byte allocation [0,0,0,0][0][0,0,0,0]
		 * 				  	   L	 S	   F 
		 */
		while (symbol_length != 0) {
			String symbol = "";

			while(symbol_length > 0) {//from the symbol length get each byte
				symbol += (char) file_bytes.get();
				symbol_length--;
			}
			
			//after each byte we can expect the next 4 bytes to be for the frequency count
			int symbol_frequency = Bit_Operations.merge_four_bytes(file_bytes.get(), 
																   file_bytes.get(), 
																   file_bytes.get(), 
																   file_bytes.get());
			
			word_symbols.put(symbol, new Node(symbol, symbol_frequency));
			
			//below, get the next symbols symbol length
			symbol_length = Bit_Operations.merge_four_bytes(file_bytes.get(), 
															file_bytes.get(), 
															file_bytes.get(), 
															file_bytes.get());
		}

		if (VERBOSE_ENCODING_TREE) {
			System.out.println("\n\tRead encoding table. Size:  " + file_bytes.position() + " bytes");
		}

		return word_symbols;
	}

	/**
	 * transfer all the data from the file into an array list
	 * 
	 * @param infile - file we are compressing
	 * 
	 * @return the ArrayList representation of the file (ordered of course)
	 */
	public static ArrayList<Character> read_file(File infile) {
		final int End_Of_Stream = -1;
		ArrayList<Character> buffer = new ArrayList<Character>(1000);

		try (FileReader reader = new FileReader(infile)) {

			while (true) {
				int character_code = reader.read();

				if (character_code == End_Of_Stream) {
					break;
				}

				buffer.add((char) character_code);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error: reading the file.");
		}

		return buffer;
	}

	/**
	 * To build the Huffman tree (for compression), we must compute the list of
	 * symbols from the file.
	 * 
	 * Algorithm:
	 * 
	 * 1) counts how often all the words appear 
	 * 2) keep the N most common words
	 *
	 * @param infile - input file
	 * @param count - find the top N (count) words
	 * 
	 * WARNING: modifies the symbol hashtable
	 * 
	 * @return an ordered list of the symbols as they appear in the file
	 * 
	 */
	public static Hashtable<String, Node> compute_most_common_word_symbols(ArrayList<Character> buffer, int count) {
		Hashtable<String, Node> histogram = new Hashtable<>();

		if (count == 0) {//if count is zero, do not compute any top words
			return histogram;
		}
		
		PriorityQueue<Node> top_words = new PriorityQueue<>(Collections.reverseOrder());
		String word = "";

		for (Character the_char : buffer) {
			/*
			 * Create words by checking if each char is a letter
			 * When a non letter is encountered then add the word we have
			 */
			if (Character.isLetter(the_char)) {
				word += the_char;
			} else if (word.length() > 0){//only add words
				increment(word, histogram);
				word = "";
			}
		}
		
		top_words.addAll(histogram.values());//put all words into PQ for sorting
		histogram.clear();//dump all non top count words
		
		//fill hash table with top N words (by frequency)
		for (int word_count = 0; word_count < count; word_count++) {
			Node temp_node = top_words.poll();
			
			if (temp_node == null) {
				break;
			} else {
				histogram.put(temp_node.get_symbol(), temp_node);
				
			}
		}
		
		return histogram;
	}

	/**
	 * count all the symbols in the file including single characters and
	 * predefined "word" symbols
	 * 
	 * @param buffer - the file's characters
	 * @param word_symbols - the words that have already been identified as most common
	 *                       and are to be used as symbols
	 * @param ordered_list_of_symbols - RETURNS an array list of all symbols in the file.
	 * 
	 * @return the final symbol table representing the huffman nodes (not
	 *         connected yet) for the symbols in the file.
	 */
	public static Hashtable<String, Node> compute_remaining_single_character_symbols(ArrayList<Character> buffer,
																					 Hashtable<String, Node> word_symbols, 
																					 List<String> ordered_list_of_symbols) {
		Hashtable<String, Node> all_symbols = new Hashtable<>();
		String current_symbol = "";

		all_symbols.putAll(word_symbols);

		for (Character ch : buffer) {
			if (Character.isLetter(ch)) {//build up all the words
				current_symbol += ch;
			} else {// a non letter (thus marking the division between possible word symbols
				// 1) if we have started to build a word
				if (current_symbol.length() > 0) {
					// if it is not a TOP word
					if (!word_symbols.containsKey(current_symbol)) {
						// add all it's letters to the symbols table and the ordered list
						for (int i = 0; i < current_symbol.length(); i++) {
							increment("" + current_symbol.charAt(i), all_symbols);
							ordered_list_of_symbols.add("" + current_symbol.charAt(i));
						}
					} else {// just add the word to the ordered list
						ordered_list_of_symbols.add(current_symbol);
					}
				}

				// 2) account for the current character (non-letter)
				increment("" + ch, all_symbols);
				ordered_list_of_symbols.add("" + ch);

				// start over building another word
				current_symbol = "";
			}
		}
		
		//clean up check if file did not end in a non-letter
		if (current_symbol.length() > 0) {
			// if it is not a TOP word
			if (!word_symbols.containsKey(current_symbol)) {
				// add all it's letters to the symbols table and the ordered list
				for (int i = 0; i < current_symbol.length(); i++) {
					increment("" + current_symbol.charAt(i), all_symbols);
					ordered_list_of_symbols.add("" + current_symbol.charAt(i));
				}
			} else {// just add the word to the ordered list
				ordered_list_of_symbols.add(current_symbol);
			}
		}

		// add end of file at end of symbols
		all_symbols.put(EOF, new Node(EOF, 1));
		ordered_list_of_symbols.add(EOF);

		return all_symbols;
	}

	/**
	 * given a list of bits (and the root of the huffman tree) create a list of
	 * symbols
	 * 
	 * DECOMPRESSION Pseudocode
	 * 
	 * For each bit in the bit stream (the compressed file, after the header),
	 * code += get the next bit, if code forms path from root of huffman tree to
	 * leaf we have a symbol, save it reset code to empty, end
	 * 
	 * @param bit_stream - all the bits representing the symbols in the file
	 * @param root - the root of the huffman tree
	 * 
	 * @return the reconstructed list of symbols
	 */
	public static List<String> convert_bitstream_to_original_symbols(ByteBuffer bit_stream, Node root) {
		if (VERBOSE_PRINT_SYMBOL_BITS) {
			System.out.println("\n------------- Converting bit sequences back into symbols -------------------");
		}
		
		byte curr_byte = 0;
		String code = "";
		List<String> symbols = new ArrayList<>();
		
		while(bit_stream.remaining() > 0) {
			curr_byte = bit_stream.get();
			
			//check each bit per byte
			for (int bit_index = 0; bit_index < 8; bit_index++) {
				 if (Bit_Operations.get_bit(curr_byte, bit_index)) {
					 code += "1";
				 } else {
					 code += "0";
				 }
				 
				 String word = root.get_symbol(code);//check every bit sequence
				 
				//if we found a symbol from the current bit sequence, print it, then clear
				 if (word != null && word.equals("EOF")) {//do not write EOF to file
					 break;
				 } else if (word != null) {
					 symbols.add(Utility.convert_printable_symbol(word));//check for special symbols
					 code = "";
				 }
			}
		}
		
		return symbols;		
	}

	/**
	 * COMPRESSION - write the symbol frequencies
	 * 
	 * 1) Writes the symbols and frequency information to the output file, 
	 *   o)This allows the Huffman tree to be reconstructed at the time of
	 *     decompression.
	 * 
	 * 2) NOTE: for debug purposes, the symbols are written from most frequent
	 *          to least frequent... but this is not necessary
	 * 
	 * 3) FORMAT of HEADER is:
	 * 
	 * LENGTH, SYMBOL, FREQUENCY (repeated for all symbols) ZERO (so we know we
	 * are out of symbols - okay because a length of 0 doesn't make sense)
	 * 
	 * 4) EXAMPE (for the following symbols and frequencies: (a,5) (hello,10),
	 *    (EOF,1)
	 * 
	 * 1a5, 5hello10, 3EOF1 (note: there of course are no spaces or commas and
	 * this information is written as bits....)
	 * 
	 * @param huffman_nodes - the collection of symbols and frequencies (i.e., Nodes) in the document
	 * 
	 * @throws IOException - if something goes wrong with the file writing
	 */
	public static byte[] build_file_header(Collection<Node> huffman_nodes) throws IOException {//public for testing
		@SuppressWarnings("unused")//count is used for verbose encoding tree
		int count = 0;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (VERBOSE_ENCODING_TREE) {
			System.out.printf("\n----- encoding table (ordered by frequency) -----\n");
		}
		
		for (Node curr_node : huffman_nodes) {
			byte[] sym_length = Bit_Operations.convert_integer_to_bytes(curr_node.get_symbol().length());
			byte[] symbol_bytes = curr_node.get_symbol().getBytes();
			byte[] freq_bytes = Bit_Operations.convert_integer_to_bytes(curr_node.get_frequency());
						
			for (byte curr_byte : sym_length) {//add 4 bytes to header for symbol length
				out.write(curr_byte);
				count++;
			}
			
			for (Byte symbol : symbol_bytes) {//add byte for each char in the symbol
				out.write(symbol);
				count++;
			}
			
			for (byte curr_byte : freq_bytes) {//add 4 bytes to header for symbol frequency
				out.write(curr_byte);
				count++;
			}
		}
		
		out.write(new byte[]{0,0,0,0});//last 4 bits add to zero for a length of zero
		count++;

		if (VERBOSE_ENCODING_TREE) {
			System.out.println("\n\tEncoding Table Size:  " + count + " bytes");
		}

		return out.toByteArray();
	}

	/**
	 * DECOMPRESSION
	 * 
	 * Writes the decompressed data (Symbols) to the output file.
	 * 
	 * As each symbol is decompressed, write its component characters
	 * 
	 * @param outfile - stream for the output file
	 * @param symbol_list - the symbolss to write
	 * 
	 * @throws IOException
	 */
	public static void write_data(File outfile, List<String> symbol_list) throws IOException {
		try (FileOutputStream fs = new FileOutputStream(outfile)) {
			for (String symbol : symbol_list) {
				for (int i = 0; i < symbol.length(); i++) {
					fs.write(symbol.charAt(i));
				}
			}
		}
	}

	/**
	 * COMPRESSION
	 * 
	 * For each symbol in the input file, encode it using the Huffman tree by
	 * writing the bit code to the output file.
	 * 
	 * This method uses the "determine_bit_pattern_for_symbol" method
	 * 
	 * PSEUDOCODE:
	 * 
	 * for every symbol (in order) find bit pattern put bits from pattern into
	 * bitset return the byte[] from the bitset
	 * 
	 * @param ordered_list_of_symbols - all the letters (words/symbols) from the file in order
	 * @param table - the hashtable from symbol string to node
	 * 
	 * @return the bytes representing the bit stream of the compressed symbols
	 * 
	 * @throws IOException
	 */
	public static byte[] build_compressed_bit_stream(List<String> ordered_list_of_symbols, Hashtable<String, Node> table)
											  throws IOException {
		BitSet bitset = new BitSet();
		int bit_index = 0;
	
		if (VERBOSE_PRINT_SYMBOL_BITS) {
			System.out.println("\n----------------- Compressing  ------------------");
			System.out.println(
					"\n   Building bit representation for " + ordered_list_of_symbols.size() + " symbols");
		}
		
		for (String word : ordered_list_of_symbols) {
			LinkedList<Integer> symbol_pattern = determine_bit_pattern_for_symbol(table.get(word));
			
			for (Integer bit : symbol_pattern) {
				if (bit == 1) {
					/*
					 * turn bits on in BitSet when they need to be
					 * Otherwise leave off (0). BitSet will fill in bits between bits set 
					 * to 1, with 0s
					 */
					bitset.set(bit_index, true);
				}
				bit_index++;
			}
		}

		if (VERBOSE_PRINT_SYMBOL_BITS) {
			System.out.println("\n-------------------- done -----------------------");
		}
				
		return get_bytes(bitset);
	}

	/**
	 * Constructs a Huffman tree to represent bit codes for each character.
	 * 
	 * This is the method is the HEART of the huffman algorithm.
	 * 
	 * Algorithm:
	 * 
	 * o) put all the nodes into a priority queue
	 * 
	 * 1) choose the two least frequent symbols (removing from PQ) 
	 * 2) combine these in a new huffman node 
	 * 3) put this new node back in the PQ 
	 * 4) repeat until no nodes in PQ
	 * 
	 * @return the root of the tree
	 * @param the nodes to be built into a tree
	 */
	public static Node create_tree(Collection<Node> nodes) {
		PriorityQueue<Node> huff_tree = new PriorityQueue<>();
		int node_count = 1;
		
		for (Node curr_node : nodes) {
			huff_tree.offer(curr_node);
		}
		
		while (huff_tree.size() > 1) {//last node will be the Huffman root that contains
			Node left_child = huff_tree.poll();
			Node right_child = huff_tree.poll();
			int combined_freq = left_child.get_frequency() + right_child.get_frequency();
			
			//each parent is given a unique name for dot creation
			Node combined = new Node("N"+ node_count +"F"+ combined_freq, left_child, right_child);
			node_count++;
			
			//set the children's parent to their new parent
			left_child.set_parent(combined);
			right_child.set_parent(combined);
			huff_tree.offer(combined);//add the combined node to the PQ
		}
		
		if (VERBOSE_PRINT_TREE) { 
			System.out.println(huff_tree.peek().createDot());
		}

		return huff_tree.peek();
	}

	/**
	 * Returns the bit code for a symbol.
	 * 
	 * This is computed by traversing the path from the given leaf node up to
	 * the root of the tree.
	 * 
	 * 1) when encountering a left child, a 0 to be pre-appended to the bit
	 * code, and 
	 * 2) when encountering a right child causes a 1 to be
	 * pre-appended.
	 * 
	 * For example: the symbol "A" might return the code "1011101"
	 * 
	 * QUESTION: why do we use a linkedlist as the return type?
	 * ANSWER: make use of the add first link capabilities, avoid reverse and shift calls
	 * 
	 * @param symbol - symbol to be encoded
	 * @param node - the node in the huffman tree containing the symbol
	 * 
	 */
	private static LinkedList<Integer> determine_bit_pattern_for_symbol(Node leaf) {
		LinkedList<Integer> bit_code = new LinkedList<>();
		
		while(leaf.get_parent() != null) {//the root of the tree will have no parents
			if (leaf.parents_left().get_symbol().equals(leaf.get_symbol())) {
				bit_code.addFirst(0);//add first as we going backwards (bottom to top)
			} else {
				bit_code.addFirst(1);
			}
			
			leaf = leaf.get_parent();
		}
		
		return bit_code;
	}
}
