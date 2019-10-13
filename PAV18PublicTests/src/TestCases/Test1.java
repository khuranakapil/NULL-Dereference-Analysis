/*
 * @author Stanly Samuel and Rekha Pai
 */

package TestCases;

import java.util.Random;

public class Test1 {
	/*
	 * Public Test Case 1: variable t1 may be null
	 * 
	 */
	private Test1 next;
	int i, j;

	public void varNullTest(int arg) {
		int i = arg; 

		Test1 t1;
		Test1 t2 = new Test1();

		if (i > 10) {
			t1 = null;
		} else {
			t1 = t2;
		}
		t2 = t1.next; // t1 may be null
		t1.toString();
		t2.toString();

	}

	public void startTest() {
		varNullTest(22);
	}

}