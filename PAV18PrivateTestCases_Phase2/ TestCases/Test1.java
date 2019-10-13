//Author: Stanly Samuel and Rekha Pai
package TestCases;
/**
 * 
 * Purpose: Surpise!
 * 
 */
public class Test1 {
	public void foo1() {
		Node n1 = new Node();
		Data d1a = new Data();
		Data r1 = bar1(n1, d1a);
	}
	
	public Data bar1(Node n, Data d1) {
		n.data = d1;
		Data d3 = new Data();
		Data d2 = new Data();
		int a = (int) Math.random();
		int b = (int) Math.random();
		if(a+b==5)
			n.data = d2;
		else
			n.data = null;
		if(n.data == null)
			return d2;
		else
			return d3;
	}
}