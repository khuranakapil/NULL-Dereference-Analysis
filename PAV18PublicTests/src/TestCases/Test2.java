package TestCases;

class Test2 {
	private Test2 next;
	int i, j;
	/*
	 * Public Test Case 2: Create linked list terminated with null 
	 * 
	 */

	public void phiNodeTest() {

		Test2 hd = new Test2();
		Test2 tl;
		tl = hd;
		do {
			
			tl.next = new Test2();
			tl = tl.next;
			tl.next = null;
			i = i + 1;
		} while (i <= 5);

		hd.toString();
		tl.toString();
	}

	public void startTest() {
		phiNodeTest();
	}

}