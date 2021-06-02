package lexicalAnalyzer;


import logging.BilbyLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import tokens.CharacterConstantToken;
import tokens.FloatingConstantToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
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
		else if(ch.isLowerCase()) {
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
	// Integer and Floating lexical analysis	

	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentDigits(buffer);
		if(input.peek().getCharacter() == DECIMAL_POINT) {
			LocatedChar decimal_point = input.next();
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
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentLowercase(buffer);

		String lexeme = buffer.toString();
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar, lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			return IdentifierToken.make(firstChar, lexeme);
		}
	}
	private void appendSubsequentLowercase(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isLowerCase()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Punctuator lexical analysis	
	// old method left in to show a simple scanning method.
	// current method is the algorithm object PunctuatorScanner.java

	@SuppressWarnings("unused")
	private Token oldScanPunctuator(LocatedChar ch) {
		
		switch(ch.getCharacter()) {
		case '*':
			return LextantToken.make(ch, "*", Punctuator.MULTIPLY);
		case '+':
			return LextantToken.make(ch, "+", Punctuator.ADD);
		case '>':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(ch, ">=", Punctuator.GREATER_THAN_EQUAL);
			}
			else {
				
				return LextantToken.make(ch, ">", Punctuator.GREATER);
			}
		case '<':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(ch, "<=", Punctuator.LESS_THAN_EQUAL);
			}
			else {
				
				return LextantToken.make(ch, "<", Punctuator.LESS);
			}
		case ':':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(ch, ":=", Punctuator.ASSIGN);
			}
			else {
				lexicalError(ch);
				return(NullToken.make(ch));
			}
		case ',':
			return LextantToken.make(ch, ",", Punctuator.PRINT_SEPARATOR);
		case ';':
			return LextantToken.make(ch, ";", Punctuator.TERMINATOR);
		default:
			lexicalError(ch);
			return(NullToken.make(ch));
		}
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
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	
	private void lexicalError(LocatedChar firstChar, String message) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error:" +message+ " at "+ firstChar.getLocation());
		
	}
	
	private void lexicalError(LocatedChar ch) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}

	
}
