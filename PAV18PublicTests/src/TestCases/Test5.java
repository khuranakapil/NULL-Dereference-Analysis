package TestCases;

public class Test5 {
	

	public void conditionalPointerTest(int j) {
		Test5 t1, t2 = new Test5();
		Test5 dummy = new Test5();
		Test5 t3;
		int i = j; // some input
		if (i > 0) {
			t1 = dummy;
		} else {
			t1 = t2;
		}

		if (t1 == dummy) {
			t3 = dummy;
		} else {
			t3 = t2;
		}
	}
	
	public void startTest() {
		
		conditionalPointerTest(10);
	}
}