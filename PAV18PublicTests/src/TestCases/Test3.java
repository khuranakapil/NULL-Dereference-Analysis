package TestCases;

class Test3 {
	private Test3 next;
	int i, j;

	public Test3 getObj(int i) {
		return (Test3) new Object();
	}

	/*
	 * Public Test Case 3: Strong, weak updates, call returning reference to an
	 * object
	 */
	public void returnRefTest(int arg) {
		Test3 t1;
		Test3 t2;
		Test3 t3;

		Test3 dummy = new Test3();
		int i = arg; // what are args?

		if (i > 10) {
			t1 = dummy.getObj(i);
		} else {
			t1 = dummy.getObj(i);
		}
		t2 = dummy.getObj(i);
		t1.next = t2;
	}

	public void startTest() {
		returnRefTest(12);
	}
}