package TestCases;

class Test4 {
	private Test4 next;
	int j;

	void nullDerefTest(int arg) {
		Test4 t1, t2=new Test4();
		Test4 dummy = new Test4();
		Test4 t3;
		int i = arg; // some random number
		
		
		if (dummy.j == 1) {
			if (i == 0)
				t1 = null;
			else
				t1 = new Test4();
		} else {
			if (i == 0)
				t1 = null;
			else
				t1 = new Test4();
		}
		// Here t1 is null
		t2 = t1.next;
	}

	public void startTest() {
		nullDerefTest(20);
	}
}