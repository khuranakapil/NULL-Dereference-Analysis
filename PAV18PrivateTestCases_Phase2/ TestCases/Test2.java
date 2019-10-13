//Author: Stanly Samuel and Rekha Pai
package TestCases;

/**
 * 
 * Purpose: Mix n Match
 * 
 */
public class Test2 {
	Node head = new Node();
	Node prev = head;
	int i = 0;

	public void foo2(int n)
	{
		while(i<n)
		{
			Node curr = new Node();
			Data data = new Data();
			curr =bar2(curr,n, data);
			prev.next = curr;
			prev = curr;
			i++;
		}
	}
	
	public Node bar2(Node curr, int n, Data data)
	{
		curr = new Node();
		data = new Data();
		curr.data = data;
		return curr;
	}
}