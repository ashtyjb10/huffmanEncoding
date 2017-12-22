package cs2420;

/**
 * Class holds the data recorded for file as it goes through
 * compression and uncompression
 * 
 * @author Andrew Worley, Ashton Schmidt
 *
 * last update: 4/23/2017 
 */
public class Huffman_Data_Set {
	String file_name;
	int count;
	int compressed_file_size;
	int original_file_size;
	double read_file_time;
	double compute_top_words_time;
	double all_symbols_time;
	double build_tree_time;
	double build_header_time;
	double create_data_stream;
	double read_file_header;
	double read_data_stream;
	double total_compression_time;
	double total_dempression_time;
	
	/**
	 * @param _file_name - the name of the file being compressed
	 * @param _count - the amount of top frequency words used during encoding
	 */
	public Huffman_Data_Set(String _file_name, int _count) {
		file_name = _file_name;
		count = _count;
		compressed_file_size = 0;
		original_file_size = 0;
		read_file_time = 0;
		compute_top_words_time = 0;
		all_symbols_time = 0;
		build_tree_time = 0;
		build_header_time = 0;
		create_data_stream = 0;
		read_file_header = 0;
		read_data_stream = 0;
		total_compression_time = 0;
		total_dempression_time = 0;
	}
	
	/**
	 * @param new_value - sets the read_file_time to this value
	 */
	public void set_read_file_time(double new_value) {
		read_file_time = new_value;
	}
	
	/**
	 * @param new_value - sets the compute_top_words_time to this value
	 */
	public void set_compute_top_words_time(double new_value) {
		compute_top_words_time = new_value;
	}
	
	/**
	 * @param new_value - sets the all_symbols_time to this value
	 */
	public void set_all_symbols_time(double new_value) {
		all_symbols_time = new_value;
	}
	
	/**
	 * @param new_value - sets the build_tree_time to this value
	 */
	public void set_build_tree_time(double new_value) {
		build_tree_time = new_value;
	}
	
	/**
	 * @param new_value - sets the build_header_time to this value
	 */
	public void set_build_header_time(double new_value) {
		build_header_time = new_value;
	}
	
	/**
	 * @param new_value - sets the time to create_data_stream to this value
	 */
	public void set_create_data_stream(double new_value) {
		create_data_stream = new_value;
	}
	
	/**
	 * @param new_value - sets the time to read_file_header to this value
	 */
	public void set_read_file_header(double new_value) {
		read_file_header = new_value;
	}
	
	/**
	 * @param new_value - sets the time to read_data_stream to this value
	 */
	public void set_read_data_stream(double new_value) {
		read_data_stream = new_value;
	}
	
	/**
	 * @param new_value - sets the total_compression_time to this value
	 */
	public void set_total_compression_time(double new_value) {
		total_compression_time = new_value;
	}
	
	/**
	 * @param new_value - sets the total_dempression_time to this value
	 */
	public void set_total_dempression_time(double new_value) {
		total_dempression_time = new_value;
	}
	
	/**
	 * @param new_value - sets the compressed file size to this value
	 */
	public void set_compressed_file_size(int new_value) {
		compressed_file_size = new_value;
	}
	
	/**
	 * @param new_value - sets the original file size to this value
	 */
	public void set_original_file_size(int new_value) {
		original_file_size = new_value;
	}
	
	/**
	 * Display a files information related to its huffman encoding process
	 */
	public String toString() {
		return file_name +"\t"+ 
	           count +"\t"+ 
			   read_file_time +"\t"+ 
	           compute_top_words_time +"\t"+ 
			   all_symbols_time +"\t"+ 
	           build_tree_time +"\t"+ 
			   build_header_time +"\t"+ 
	           create_data_stream +"\t"+ 
			   read_file_header +"\t"+ 
	           read_data_stream +"\t"+ 
			   total_compression_time +"\t"+ 
	           total_dempression_time +"\t"+
			   original_file_size +"\t"+
			   compressed_file_size;
	}
}
