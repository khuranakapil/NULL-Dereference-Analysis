package TestCases;
/**
 * 
 * Purpose: 1) How to process arrays
 * 
 */
public class Test2 {
	
	public void array() {
		PublicTests[] arr_test = new PublicTests[3];
		
		PublicTests test1, test2, test3;
		
		test1 = new PublicTests();
		test2 = null;
		test3 = test1;
		
		arr_test[0] = test1;
		arr_test[1] = test2;
		arr_test[2] = test3;
		test2 = arr_test[0];
	}
}

