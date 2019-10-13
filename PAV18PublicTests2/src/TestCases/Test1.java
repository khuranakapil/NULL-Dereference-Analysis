package TestCases;
/**
 * 
 * Purpose: 1) How to process calls and returns
 * 
 */
public class Test1 {
	public void foo() {
		Node n1 = new Node();
		Data d1a = new Data();
		Data r1 = bar(n1, d1a);
		
		Node n2 = new Node();
		Data d1b = new Data();
		Data r2 = bar(n2, d1b);
	}
	
	public Data bar(Node n, Data d1) {
		n.data = d1;
		Data d2 = new Data();
		n.data = d2;
		return d2;
	}
}
