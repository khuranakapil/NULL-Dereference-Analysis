//Author: Stanly Samuel and Rekha Pai

package TestCases;
/**
 * 
 * Purpose: Inception
 * 
 */
class Test3 {
	
	public void insert(Node head, int n) 
	{ 
		if (n > 0) {
			Node curr = new Node();
			insert(curr, n-1);
			head.next = curr;
		}
	} 
	
	public void foo3() 
	{ 
		Node list = new Node();
		list.next = null;
		insert(list, 5);
	}  
} 