package cs2420;

import java.io.File;
import java.nio.file.Paths;

/**
 * 
 * @author Ashton Schmidt, Andrew Worley
 * 
 */
public class Main {
	private static HuffmanTreeUsingWords huffman;
	private static String file_name = "";
	private static boolean compress = false;
	
	/**
	 * compress or uncompress a file
	 * 
	 * To compress, use the following command line arguments:
	 * 
	 * huffman - c COUNT file_name
	 * 
	 * To uncompress, use:
	 * 
	 * huffman file_name
	 * 
	 * @param args - see above
	 */
	public static void main(String[] args) {
		/*
		 * Uncomment to make this run on the command line if (args.length == 3)
		 * { huffman = new HuffmanTreeUsingWords(Integer.parseInt(args[1]));
		 * 
		 * if (!args[0].equals("-c")) {
		 * System.out.println("to Compress, use args: '-c number file_name')");
		 * return; } file_name = args[2]; compress = true; } else if
		 * (args.length == 1) { huffman = new HuffmanTreeUsingWords(0);
		 * file_name = args[0]; } else {
		 * System.out.println("to Compress, use args: '-c number file_name')");
		 * System.out.println("to Uncompress, use args: 'file_name')");
		 * System.out.println(
		 * "Note: No extensions are necessary.  compressed files will automatically be extended with .huf"
		 * ); System.out.
		 * println("      Files to uncompress will be assumed to end with .huf"
		 * ); System.out.
		 * println("      Uncompressed files will end with .uncompress to avoid destroying the original."
		 * ); return; }
		 */

		// test code

		huffman = new HuffmanTreeUsingWords(0);
		
//		file_name = "Resources//two_cities";
		file_name = "Resources//constitution";
//		file_name = "Resources//green_eggs_and_ham";
//		file_name = "Resources//alphabetplus";
//		file_name = "Resources//alphabet";
//		file_name = "Resources//simple";
//		file_name = "Resources//a_few_letters";
//		file_name = "Resources//decl_of_ind";
//		file_name = "Resources//road_not_taken";
		
		if (!compress) {
			file_name += "." + huffman.WORD_COUNT;
		}

		if (compress) {
			System.out.println("Compressing file(" + huffman.WORD_COUNT + ") : " + file_name +"\n");
		} else {
			System.out.println("Uncompressing file : " + file_name);
			System.out.println("using " + huffman.WORD_COUNT + " words as symbols.");
		}

		//ignore . extensions
		String[] new_file_name = new String[0];//file_name.split("\\.");

		if (new_file_name.length > 1) {
			file_name = new_file_name[0];
		}

		if (compress) {
			huffman.compress_file(new File(file_name), new File(file_name + "." + huffman.WORD_COUNT + ".huf"));
		} else {
			huffman.decompress_file(Paths.get(file_name + ".huf"), new File(file_name + ".uncompress"));
		}
	}
}
