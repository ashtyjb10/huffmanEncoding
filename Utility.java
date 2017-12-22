/**
 * 
 */
package cs2420;

import java.util.Hashtable;

/**
 * @author H. James de St. Germain, modified by Andrew Worley and Ashton Schmidt
 * 
 * Helper methods 
 *
 */
public final class Utility
{

	/**
	 *  This is a "static" class.  All the methods are static. Do not create a Utility object. 
	 */
	private Utility() {}
	
	/**
	 * Helper method for updating "histogram" using hash table.
	 * 
	 * if the symbol is not in table, add it to the table with a count of 1
	 * if the symbo IS in the table,  increment the currently stored value by 1
	 * 
	 * @param symbol - we are looking for
	 * @param table  - hash table containing symbol
	 */
	public static void increment(String symbol, Hashtable<String, Node> table) {
		if (table.containsKey(symbol)) {
			table.get(symbol).increment_frequency();
		} else {
			table.put(symbol, new Node(symbol, 1));
		}
	}
	
	/**
	 * Some characters (like new line) print "funny". Other characters (like *) can benefit
	 * from a more human readable descriptor.
	 * 
	 * Words (e.g.,"Hello") are returned as the same word (i.e., "Hello")
	 * Normal characters (e.g., "A") are returned as the same character (i.e., "A")
	 * 
	 * This method returns a "print friendly" symbol based on the given symbol.
	 *
	 * @param symbol - the symbol the make readable
	 */
	public static String printable_symbol( String symbol ) {
		if (symbol.length() > 1) {
			return symbol;
		} else {
			if (Character.isLetterOrDigit(symbol.charAt(0))) return symbol;

			switch (symbol.charAt(0)) {
				case ' ':					return "SPACE";
				case '\n':					return "NEWLINE";
				case '\r':					return "RNEWLINE";
				case ',':					return "COMMA";
				case '-':					return "HYPHEN";
				case '?':					return "QUESTION_MARK";
				case '!':					return "EXCLAMATION_MARK";
				case '.':					return "PERIOD";
				case '`':					return "APOSTROPHE";
				case '_':					return "UNDERSCORE";
				case ']':					return "RIGHT_SQUARE_BRACKET";
				case '[':					return "LEFT_SQUARE_BRACKET";
				case ')':					return "RIGHT_PAREN";
				case '(':					return "LEFT_PAREN";
				case ';':					return "SEMICOLON";
				case ':':					return "COLON";
				case '/':					return "BACK_SLASH";
				case '\\':					return "FORWARD_SLASH";
				case '@':					return "AMPERSAND";
				case '*':					return "STAR";
				case '\'':					return "TICK";
				case '%':					return "PERCENT";
				case '$':					return "DOLLAR";
				case '#':					return "OCTOTHORPE";
				case '"':					return "QUOTE";
				case '	':					return "TAB";
				case '=':					return "EQUALS";
				case '{':					return "C_BRACKET_OPEN";
				case '}':					return "C_BRACKET_CLOSED";
				case '<':					return "LESS_THAN";
				case '>':					return "GREATER_THAN";
				case '^':					return "CARET";
				default:
				{
					System.out.println("If this is a real symbol, update the rename_special_characters method to include it: "
										+ symbol);
					throw new RuntimeException("we cannot handle this character... help me spock!");
				}
			}
		}
	}
	
	/**
	 * Convert symbols from their dot graph friendly form to their original
	 * form. Passing in symbols not covered returns the symbol/String passed in.
	 *
	 * @param symbol - in friendly form (for dot graphs)
	 */
	public static String convert_printable_symbol(String symbol) {
		switch (symbol) {
			case "SPACE":					return " ";
			case "NEWLINE":					return "\n";
			case "RNEWLINE":				return "\r";
			case "COMMA":					return ",";
			case "HYPHEN":					return "-";
			case "QUESTION_MARK":			return "?";
			case "EXCLAMATION_MARK":		return "!";
			case "PERIOD":					return ".";
			case "APOSTROPHE":				return "`";
			case "UNDERSCORE":				return "_";
			case "RIGHT_SQUARE_BRACKET":	return "]";
			case "LEFT_SQUARE_BRACKET":		return "[";
			case "RIGHT_PAREN":				return ")";
			case "LEFT_PAREN":				return "(";
			case "SEMICOLON":				return ";";
			case "COLON":					return ";";
			case "BACK_SLASH":				return "\\";
			case "FORWARD_SLASH":			return "/";
			case "AMPERSAND":				return "&";
			case "STAR":					return "*";
			case "TICK":					return "\'";
			case "PERCENT":					return "%";
			case "DOLLAR":					return "$";
			case "OCTOTHORPE":				return "#";
			case "QUOTE":					return "\"";
			case "TAB":						return "	";
			case "EQUALS":					return "=";
			case "C_BRACKET_OPEN":			return "{";
			case "C_BRACKET_CLOSE":			return "}";
			case "LESS_THAN":				return "<";
			case "GREATER_THAN":			return ">";
			case "CARET":					return "^";
			
		}
		return symbol;
	}
}
