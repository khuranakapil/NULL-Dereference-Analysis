/*
 * @author Stanly Samuel and Dr. Rekha Pai
 */

package TestCases;

public class SampleTests {
	
	int temp;
	char ch;
	static SampleTests a;

	public static void main(String args[])
	{
		null_error(2);
		foo_if(1);
		foo_while(1);
		foo_for(1);
		foo_do_while(1);
		foo_break(1);
		foo_continue(1);
		foo_switch(1);
		SampleTests t = null;
		foo_object(1);
		
		
	}
	public static void null_error(int i)
	{
		SampleTests b1 = new SampleTests();
		SampleTests b2 ;
		SampleTests t1 = null;
		SampleTests t2 = new SampleTests();
		SampleTests t3 = null;
		
		b1 = new SampleTests();
		t2 = new SampleTests();
		
		if(a != t3) //intersection zero
		{
			b2 = t2;
			t1 = new SampleTests();
		}
		else
		{
			b2 = b1;
			t1 = new SampleTests();
		}
		
		/*if(t1==t3) //both null
		{
			b2 = t2;
			t1 = new SampleTests();
		}
		else
		{
			b2 = b1;
			t1 = new SampleTests();
		}
		
		if(i<2)  //
		{
			b2 = t3;
		}
		else
		{
			b2 = b1;
		}
		if(b2==t3) //null and null+non-null
		{
			b2 = t2;
		}
		else
		{
			b2 = b1;
		}
		
		b1 = new SampleTests();
		t2 = new SampleTests();
		
		if(b1 == t2) //intersection zero
		{
			b2 = t2;
		}
		else
		{
			b2 = b1;
		}
		*/	
		System.out.println(b2);
		System.out.println(t1);
	}
	public static void foo_if(int i) {
		SampleTests t1 = null;
		SampleTests t2 = new SampleTests();
		SampleTests t3 = null;
		if(i>10)
		{
			t3 = t1;
		} 
		else
		{
			t3 = t2;
		}
			t3.toString();
		}
	
	public static void foo_while(int i) {
		SampleTests t1 = null;
		SampleTests t3 = new SampleTests();
		//SampleTests t3 = null;
		while(i>10)
		{
			t3 = t1;
			i = i-1;
		} 
		
			t3.toString();
	}
	public static void foo_break(int i) {
		SampleTests t1 = null;
		SampleTests t3 = new SampleTests();
		//SampleTests t3 = null;
		while(i>10)
		{
			t3 = t1;
			i = i-1;
			if(i==5)
				break;
		} 
		
			t3.toString();
	}
	
	public static void foo_continue(int i) {
		SampleTests t1 = null;
		SampleTests t3 = new SampleTests();
		//SampleTests t3 = null;
		while(i>10)
		{
			int j = 2;
			if(j==5)
				continue;
			t3 = t1;
			i = i-1;
			
		} 
		
			t3.toString();
	}
	
	public static void foo_do_while(int i) {
		SampleTests t1 = null;
		SampleTests t3 = new SampleTests();
		//SampleTests t3 = null;
		do
		{
			t3 = t1;
			i = i-1;
		}while(i>10);
		
			t3.toString();
	}
	
	public static void foo_for(int i) {
		SampleTests t1 = null;
		SampleTests t3 = new SampleTests();
		//SampleTests t3 = null;
		for(i = 0; i< 5;i++) {
			t3 = t1;
			
		}
		
			t3.toString();
	}
	
	public static void foo_switch(int i) {
		
		SampleTests t3 = new SampleTests();
		
		i = 2;
		switch (i) {
			case 2:
				t3 = new SampleTests(); 
				
	        case 1: 
	        	t3 = new SampleTests();
	           
	        default : 
	        	t3 = new SampleTests();
   
		}
			
			
		
			t3.toString();
	}
	
	public static int foo_object(int i) {
		
		int temp =  10;
		char ch = 'h';
		return temp;
		
	}

}