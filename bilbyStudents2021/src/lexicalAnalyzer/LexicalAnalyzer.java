package lexicalAnalyzer;


import logging.BilbyLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.Locator;
import inputHandler.PushbackCharStream;
import tokens.CharacterConstantToken;
import tokens.FloatingConstantToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.StringConstantToken;
import tokens.IntegerConstantToken;
import tokens.Token;

import static lexicalAnalyzer.PunctuatorScanningAids.*;

public class LexicalAnalyzer extends ScannerImp implements Scanner {
	private static final char DECIMAL_POINT = '.';
	private static final char  EXPONENTIAL= 'E';
	private static final char  PLUS= '+';
	private static final char  MINUS= '-';
	private static final char NEW_LINE = '\n';
	private static final char HASH = '#';
	private static final char STRING_DELIMITER = '\"';
	private static final char EQUALS = '=';


	public static LexicalAnalyzer make(String filename) {
		InputHandler handler = InputHandler.fromFilename(filename);
		PushbackCharStream charStream = PushbackCharStream.make(handler);
		return new LexicalAnalyzer(charStream);
	}

	public LexicalAnalyzer(PushbackCharStream input) {
		super(input);
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// Token-finding main dispatch	

	@Override
	protected Token findNextToken() {
		LocatedChar ch = nextNonWhitespaceChar();
		if(ch.isDigit()) {
			return scanNumber(ch);
		}
		else if(ch.startsIdentifier()) {
			return scanIdentifier(ch);
		}
		else if(isPunctuatorStart(ch)) {
			return PunctuatorScanner.scan(ch, input);
		}
		else if(isEndOfInput(ch)) {
			return NullToken.make(ch);
		}
		else if(ch.isComment()) {
			scanComment(ch);
			return findNextToken();
		}
		else if(isChar(ch)) {
			return scanChar(ch); 
		}
		else if(isDoubleQuote(ch)) {
			return scanString(ch); 
		}
		else {
			lexicalError(ch);
			return findNextToken();
		}
	}


	private LocatedChar nextNonWhitespaceChar() {
		LocatedChar ch = input.next();
		while(ch.isWhitespace()) {
			ch = input.next();
		}
		return ch;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	//  Comment lexical analysis	
	private void scanComment(LocatedChar firstChar) {
		firstChar.getCharacter();
		LocatedChar c = input.next();
		while(!c.isComment()  ) {
			if(input.peek().getCharacter() == NEW_LINE) {
				break;
			}
			c = input.next();
		}
		c = input.next();
			
	}
	
	//////////////////////////////////////////////////////////////////////////////
	//  Char lexical analysis	
	private Token scanChar(LocatedChar firstChar) {
		firstChar.getCharacter(); // throw away #
		StringBuffer buffer = new StringBuffer();
		
		
		
		if(input.peek().getCharacter() == '#') {
			input.next(); // throw away #
			if(input.peek().getCharacter() != '#' && !input.peek().isDigit() ) {
				lexicalError(firstChar,"malformed character");
				return findNextToken();
			}
			firstChar = input.next();
			buffer.append(firstChar.getCharacter());
			return CharacterConstantToken.make(firstChar, buffer.toString());
		}
		else if(input.peek().isDigit()) {
			appendSubsequentDigits(buffer);
			String octaString = buffer.toString();
			try {
				int decimalChar=Integer.parseInt(octaString,8); 
				char c=(char)decimalChar;
				String stringWithChar =String.valueOf(c);  
				return CharacterConstantToken.make(firstChar, stringWithChar);
			}
			catch(NumberFormatException e){
				lexicalError(firstChar,"malformed character, not octal");
				return findNextToken();
			}
			
		}
		else {
			firstChar = input.next();
			buffer.append(firstChar.getCharacter());

			return CharacterConstantToken.make(firstChar, buffer.toString());
		}
		//return findNextToken();
		
		

	}
	
	//////////////////////////////////////////////////////////////////////////////
	//  String lexical analysis	
	private Token scanString(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		firstChar.getCharacter(); // throw away "
		buffer.append(input.next().getCharacter());
		
		while(isStringContinue(input.peek())) {
			LocatedChar c = input.next();
			buffer.append(c.getCharacter());
			
			
		}
		return appendClosingStringDelimiter(firstChar, buffer);
		

	}
	
	private Token appendClosingStringDelimiter(LocatedChar firstChar, StringBuffer buffer) {
		LocatedChar c = input.next();
		//buffer.append(c.getCharacter());

		if(c.getCharacter() == STRING_DELIMITER) {
			return StringConstantToken.make(firstChar, buffer.toString());
		}
		else { // c.getCharacter() == NEWLINE
			lexicalError(c, "string literal terminated by newline");
			return findNextToken();
		}
	}

	private boolean isStringContinue(LocatedChar c) {
		return !(c.getCharacter() == STRING_DELIMITER || c.getCharacter() == NEW_LINE);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Integer and Floating lexical analysis	

	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentDigits(buffer);
		if(input.peek().getCharacter() == DECIMAL_POINT) {
			LocatedChar decimal_point = input.next();
			// if is range then just return that int instead of mistaking as float
			if(input.peek().getCharacter() == DECIMAL_POINT) {
				input.pushback(decimal_point);
				return IntegerConstantToken.make(firstChar, buffer.toString());
			}
			buffer.append(decimal_point.getCharacter());
			if(input.peek().isDigit()) {
				appendSubsequentDigits(buffer);
				if(input.peek().getCharacter() == EXPONENTIAL) {
					LocatedChar exponential = input.next();
					buffer.append(exponential.getCharacter());
					if(input.peek().getCharacter() == PLUS || input.peek().getCharacter() == MINUS ) {
						LocatedChar sign = input.next();
						buffer.append(sign.getCharacter());
					}
					if(input.peek().isDigit()) {
						appendSubsequentDigits(buffer);
						
					}
					else {
						lexicalError(exponential,"malformed exponential notation");
						return findNextToken();
					}
					
				}
				
				return FloatingConstantToken.make(firstChar, buffer.toString());
			}
			else {
				lexicalError(firstChar,"malformed floating literal");
				return findNextToken();
			}
		}
		else {
			return IntegerConstantToken.make(firstChar, buffer.toString());
		}
		
	}
	

	private void appendSubsequentDigits(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isDigit()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Identifier and keyword lexical analysis	

	private Token scanIdentifier(LocatedChar firstChar) {
		// old: identifier → [ a..z_ ] +
		// new: identifier → [ a..zA..Z_@ ][ a..zA..Z_@0..9 ] *
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentIdentifier(buffer);

		String lexeme = buffer.toString();
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar, lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			return IdentifierToken.make(firstChar, lexeme);
		}
	}
	private void appendSubsequentIdentifier(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isIdentifierChar()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	


	//////////////////////////////////////////////////////////////////////////////
	// Character-classification routines specific to bilby scanning.	

	private boolean isPunctuatorStart(LocatedChar lc) {
		char c = lc.getCharacter();
		return isPunctuatorStartingCharacter(c);
	}
	

	private boolean isEndOfInput(LocatedChar lc) {
		return lc == LocatedCharStream.FLAG_END_OF_INPUT;
	}
	
	private boolean isChar(LocatedChar lc) {
		
		return lc.getCharacter() == HASH;
	}
	private boolean isDoubleQuote(LocatedChar lc) {

		return lc.getCharacter() == STRING_DELIMITER;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	
	public static void lexicalError(Locator firstChar, String message) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error:" +message+ " at "+ firstChar.getLocation());
		
	}
	
	public static void lexicalError(LocatedChar ch) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}

	
}
