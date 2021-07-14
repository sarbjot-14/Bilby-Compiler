package asmCodeGenerator;

public class LoopsLabeller {
	private static int labelSequenceNumber = 0;

	private int labelNumber;
	private String prefix;

	public LoopsLabeller() {
		labelNumber = labelSequenceNumber+1;
	};
	public LoopsLabeller(String userPrefix) {
		labelSequenceNumber++;
		labelNumber = labelSequenceNumber;
		this.prefix = makePrefix(userPrefix);
	}
	private String makePrefix(String prefix) {
		return "-" + prefix + "-" + labelNumber + "-";
	}

	public String newLabel(String suffix) {
		return prefix + suffix;
	}
	
	public String newBreakLabel() {
		return "-loop-"+labelNumber + "-endLoop";
		//-loop-4-endLoop  
	}
	
}
