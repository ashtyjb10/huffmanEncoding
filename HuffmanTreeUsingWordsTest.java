package cs2420;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * Testing suite for the huffman tree, and timing included if desired
 * 
 * @author Ashton Schmidt, Andrew Worley
 * 
 */
public class HuffmanTreeUsingWordsTest {
	private static File file_name = new File("Resources//test");
	ArrayList<Character> buffer = HuffmanTreeUsingWords.read_file(file_name);
	
	/*
	 * Control variables for timing and testing which will
	 * compress and decompress all files from the Resources folder
	 */
	private static boolean gather_timing_data = true;//turn to false to avoid longer JUnit suite runtime
	private static int count = 0;//amount of words to detect top frequency
	private static int ITERATIONS = 1;//amount of compressions/decompression per run
	private static boolean display_timing_data = true;//print timing data to console
	private static int multiply_factor = 2;//count will be multiplied by this number for each test
	private static int TESTS = 1;//amount of time to run the compression/decompression to track count sizes
	

	@Test
	public void test_read_file()
	{
		//ArrayList<Character> buffer = new ArrayList<>(20);
		String text = "Hello cheese name is Ashton like to name eat cheese!";
		char[] texts = text.toCharArray();

		String containsAll = "true";
		
		for(int i = 0; i < texts.length; i++)
		{
			if(buffer.contains(texts[i]));
			else{
				containsAll = "false";
			}
		}
		
		assertEquals(containsAll, "true");
		assertEquals(texts.length, buffer.size());
		
	}

	@Test
	public void test_top_and_remaining_words() {
		Hashtable<String, Node> top_words = HuffmanTreeUsingWords.compute_most_common_word_symbols(buffer, 2);
		ArrayList<String> al = new ArrayList<>(20);
		
		String words_in_file = "{name=name, Freq:2, A=A, Freq:1, o=o, Freq:3, n=n, Freq:1,"
				+ " l=l, Freq:3, k=k, Freq:1, i=i, Freq:2, EOF=EOF, Freq:1,"
				+ " h=h, Freq:1, !=!, Freq:1, cheese=cheese, Freq:2, e=e, "
				+ "Freq:3,  = , Freq:9, a=a, Freq:1, H=H, Freq:1, t=t, Freq:3, "
				+ "s=s, Freq:2}";

		Hashtable<String, Node> other_words = HuffmanTreeUsingWords.compute_remaining_single_character_symbols(buffer, top_words, al);

		assertEquals(top_words.toString(), "{cheese=cheese, Freq:2, name=name, Freq:2}");
		assertEquals(other_words.toString(), words_in_file);
	}
	
	@Test
	public void test_huffman_compresion_all_files_in_Resources() {
		boolean assertion = true;
		File[] test_files = new File("Resources/Test_files/").listFiles();
		File[] decompressed_files = new File("Resources/Decompressed_files").listFiles();
		
		if (gather_timing_data) {
			Hashtable<String, Huffman_Data_Set> file_data_per_test = new Hashtable<>();
			List<String> original_files = new LinkedList<>();
			List<String> decompressed_file_names = new LinkedList<>();
			Hashtable<String, ArrayList<Character>> original_data = new Hashtable<>();
			String last_step = "none";
			long start_time = 0;
			long read_file_time = 0;
			long compute_top_words_time = 0;
			long all_symbols_time = 0;
			long build_tree_time = 0;
			long build_header_time = 0;
			long create_data_stream = 0;
			long total_compression_time = 0;
			int original_file_size = 0;
			int compressed_file_size = 0;
			long read_file_header = 0;
			long read_data_stream = 0;
			long total_dempression_time = 0;

			for (int test = 0; test < TESTS; test++) {
				for (File current_file : test_files) {// create the compressed files
					read_file_time = 0;
					compute_top_words_time = 0;
					all_symbols_time = 0;
					build_tree_time = 0;
					build_header_time = 0;
					create_data_stream = 0;
					total_compression_time = 0;
					compressed_file_size = 0;
					original_file_size = 0;
					
					if (current_file.isDirectory()) {
						continue;
					} else {
						original_files.add(current_file.getName());
					}
					
					for (int iter = 0; iter < ITERATIONS; iter++) {
						try {	
								List<String> ordered_list_of_symbols = new ArrayList<>();
								Hashtable<String, Node> top_words;
								Hashtable<String, Node> all_symbols;
								
								start_time = System.nanoTime();
								ArrayList<Character> buffer = HuffmanTreeUsingWords.read_file(current_file);
								read_file_time += System.nanoTime() - start_time;
								original_data.put(current_file.getName(), buffer);
								original_file_size = buffer.size();
								
								last_step = "buffer from file - complete";

								start_time = System.nanoTime();
								top_words = HuffmanTreeUsingWords.compute_most_common_word_symbols(buffer, count);
								compute_top_words_time += System.nanoTime() - start_time;

								last_step = "top words from file - complete";

								start_time = System.nanoTime();
								all_symbols = HuffmanTreeUsingWords.compute_remaining_single_character_symbols(buffer,
										top_words, ordered_list_of_symbols);
								all_symbols_time += System.nanoTime() - start_time;

								last_step = "all symbols from file - complete";

								start_time = System.nanoTime();
								HuffmanTreeUsingWords.create_tree(all_symbols.values());
								build_tree_time += System.nanoTime() - start_time;

								last_step = "build tree from symbols - complete";

								// create header and data stream then write to file
								try (DataOutputStream out = new DataOutputStream(new FileOutputStream("Resources/Compressed_files/" + 
																									   current_file.getName() + "." + count + ".huf"))) {
									start_time = System.nanoTime();
									byte[] file_header = HuffmanTreeUsingWords.build_file_header(all_symbols.values());
									build_header_time += System.nanoTime() - start_time;

									last_step = "create file header - complete";

									start_time = System.nanoTime();
									byte[] symbol_bit_stream = HuffmanTreeUsingWords
											.build_compressed_bit_stream(ordered_list_of_symbols, all_symbols);
									create_data_stream += System.nanoTime() - start_time;

									last_step = "create data stream - complete";

									out.write(file_header);
									out.write(symbol_bit_stream);
									
									compressed_file_size = file_header.length + symbol_bit_stream.length;
									
									//gather total time for compression
									HuffmanTreeUsingWords tester = new HuffmanTreeUsingWords(count);

									start_time = System.nanoTime();
									tester.compress_file(current_file, new File("Resources/Compressed_files/" + current_file.getName() + "." + count + ".huf"));
									total_compression_time += System.nanoTime() - start_time;
								}

						} catch (Exception e) {// catch all exceptions to detect where we went wrong testing
							e.printStackTrace();
							System.err.println(current_file.getName() + " : last successful step was, " + last_step);
						}
					}
					
					double read_file_time_avg = read_file_time / ITERATIONS;
					double compute_top_words_time_avg = compute_top_words_time / ITERATIONS;
					double all_symbols_time_avg = all_symbols_time / ITERATIONS;
					double build_tree_time_avg = build_tree_time / ITERATIONS;
					double build_header_time_avg = build_header_time / ITERATIONS;
					double create_data_stream_avg = create_data_stream / ITERATIONS;
					double total_compression_time_avg = total_compression_time / ITERATIONS;
					Huffman_Data_Set current_file_data = new Huffman_Data_Set(current_file.getName(), count);
					
					current_file_data.set_read_file_time(read_file_time_avg);
					current_file_data.set_compute_top_words_time(compute_top_words_time_avg);
					current_file_data.set_all_symbols_time(all_symbols_time_avg);
					current_file_data.set_build_tree_time(build_tree_time_avg);
					current_file_data.set_build_header_time(build_header_time_avg);
					current_file_data.set_create_data_stream(create_data_stream_avg);
					current_file_data.set_total_compression_time(total_compression_time_avg);
					current_file_data.set_compressed_file_size(compressed_file_size);
					current_file_data.set_original_file_size(original_file_size);
					
					file_data_per_test.put(current_file.getName(), current_file_data);
				}

				File[] test_decompression = new File("Resources/Compressed_files").listFiles();

				for (File current_file : test_decompression) {
					read_file_header = 0;
					read_data_stream = 0;
					total_dempression_time = 0;
					
					if (current_file.isDirectory()) {
						continue;
					}
					
					for (int iter = 0; iter < ITERATIONS; iter++) {
						try {
								byte[] bytes = Files.readAllBytes(current_file.toPath());
								ByteBuffer byte_buffer = ByteBuffer.wrap(bytes);
								Hashtable<String, Node> symbols = new Hashtable<>();

								if (byte_buffer.remaining() == 0) {
									System.err.println("File contains no data");
									throw new IOException();
								}

								start_time = System.nanoTime();
								symbols = HuffmanTreeUsingWords.read_file_header_with_symbol_frequencies(byte_buffer);
								read_file_header += System.nanoTime() - start_time;

								last_step = "Read file header - complete";

								Node root = HuffmanTreeUsingWords.create_tree(symbols.values());
								
								last_step = "Create tree from file header - complete";

								start_time = System.nanoTime();
								HuffmanTreeUsingWords.decompress_data(root, byte_buffer, new File("Resources/Decompressed_files/" + current_file.getName() + ".uncompress"));
								read_data_stream += System.nanoTime() - start_time;

								last_step = "Create file from data stream - complete";
								
								//gather total time for decompression
								HuffmanTreeUsingWords tester = new HuffmanTreeUsingWords(count);

								start_time = System.nanoTime();
								File decompressed_file = new File("Resources/Decompressed_files/" + current_file.getName() + ".uncompress");
								tester.decompress_file(current_file.toPath(), decompressed_file);
								total_dempression_time += System.nanoTime() - start_time;
								
								decompressed_file_names.add(decompressed_file.getName());

						} catch (Exception e) {// catch all exceptions to detect where we went wrong testing
							e.printStackTrace();
							System.err.println(current_file.getName() + " : last successful step was, " + last_step);
						}
					}
					
					double read_file_header_avg = read_file_header / ITERATIONS;
					double read_data_stream_avg = read_data_stream / ITERATIONS;
					double total_dempression_time_avg = total_dempression_time / ITERATIONS;
					String[] current_file_name = current_file.getName().split("\\.");
					
					Huffman_Data_Set current_set = file_data_per_test.get(current_file_name[0]);
					current_set.set_read_file_header(read_file_header_avg);
					current_set.set_read_data_stream(read_data_stream_avg);
					current_set.set_total_dempression_time(total_dempression_time_avg);
				}
				
				if (display_timing_data) {
					System.out.println(
							   "File\t" + "Count\t"
						     + "Read File Time\t"
							 + "Compute Top Words Time\t" 
						     + "All Symbols Time\t"
							 + "Build Tree Time\t" 
						     + "Build File Header Time\t" 
							 + "Create Data Stream Time\t"
							 + "Read File Header Time\t" 
							 + "Read Data Stream Time\t" 
							 + "Total Compression time\t"
							 + "Total Decompression Time\t"
							 + "Original File Size\t"
							 + "Compressed File Size");
					
					ArrayList<Huffman_Data_Set> test_files_list = new ArrayList<>(file_data_per_test.values());
					
					for (Huffman_Data_Set current_set : test_files_list) {
						System.out.println(current_set.toString());
					}
					
					file_data_per_test.clear();
					
					System.out.println();
				}
				
				if (count == 0) {
					count = 1;
				}
				
				count *= multiply_factor;
			}
			
			//check all original files with their encoded/decoded result
			decompressed_files = new File("Resources/Decompressed_files").listFiles();
			
			for (String original_file : original_files) {
				for (String decompressed_file : decompressed_file_names) {
					if (decompressed_file.split("\\.")[0].equals(original_file)) {
						ArrayList<Character> orginial_buffer = original_data.get(original_file);
						ArrayList<Character> decompressed_buffer = HuffmanTreeUsingWords.read_file(new File("Resources/Decompressed_files/"+ decompressed_file));
						
						//System.out.println("Comparing "+ original_file +" to "+ decompressed_file);
						
						if(!orginial_buffer.equals(decompressed_buffer)) {
							assertion = false;
							System.err.println(original_file +" original content is not the same as uncompressed "+ decompressed_file +" content.");
						}
					}
				}
			}
			
		} else {			
			//check all original files with their encoded/decoded result
			for (File original_file : test_files) {
				if (!original_file.isDirectory()) {
					for (File decompressed_file : decompressed_files) {
						if (decompressed_file.getName().split("\\.")[0].equals(original_file.getName())) {
							ArrayList<Character> orginial_buffer = HuffmanTreeUsingWords.read_file(original_file);
							ArrayList<Character> decompressed_buffer = HuffmanTreeUsingWords.read_file(decompressed_file);
							
							//System.out.println("Comparing "+ original_file.getName() +" to "+ decompressed_file.getName());
							
							if(!orginial_buffer.equals(decompressed_buffer)) {
								assertion = false;
								System.err.println(original_file +" original content is not the same as uncompressed "+ decompressed_file +" content.");
							}
						}
					}
					
					if (!assertion) {
						break;
					}
				}
			}
		}
		
		if(assertion && decompressed_files.length > 0) {
			System.out.println("All files post compression show no alteration of data from original");
		} else if (decompressed_files.length == 0){
			System.err.println("No files contained in Resources/Decompressed_files/");
			assertion = false;
		}
		
		assertTrue(assertion);
	}
}
