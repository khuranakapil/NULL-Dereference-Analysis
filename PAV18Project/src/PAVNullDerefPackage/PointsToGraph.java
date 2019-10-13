package PAVNullDerefPackage;

import java.util.*;

//Class whose objects will be used to store pointsTo graph at each cfg edge
public class PointsToGraph {
	Vector<Pair> vector;
	Hashtable<String,Integer> varpos ;
	boolean bot_ind;
	//constructor which creates an empty vector initially
	public PointsToGraph() {
		this.vector = new Vector<Pair>(5,3);
		this.varpos = new Hashtable<String,Integer>();
		this.bot_ind = true;
	}
	public PointsToGraph(PointsToGraph p) //Copy constructor
	{
		this.vector = new Vector<Pair>(); //passing the vector here actually copyng the addresses of pairs which is not desirable
		this.varpos = new Hashtable<String,Integer>();
		//copying content of vector
		for(int i=0;i<p.vector.size();i++)
		{
			Pair temp = p.vector.elementAt(i);
			HashSet<String> set = new HashSet<String>();
			set.addAll(temp.getSet());
			this.vector.add(new Pair(temp.getV(),set));
		}
		
		Set<String> keys = p.varpos.keySet();
		Iterator<String> itr = keys.iterator();
		 while (itr.hasNext()) 
		 {
			 String v = new String(itr.next());
			 int i = p.varpos.get(v).intValue();
			 this.varpos.put(v, new Integer(i));
		 }
		 this.bot_ind = p.bot_ind;
	}
	//add a new pair / update existing
	public void addPair(Pair p, char ind)    //'w' for weak update  's' for strong update
	{
		if(varpos.get(p.getV())==null)     //true if entry for the variable doesn't exist
		{
			int i = this.vector.size();
			this.vector.add(p);
			this.varpos.put(p.getV(), new Integer(i));
		}
		else
		{
			int i = this.varpos.get(p.getV()).intValue();
			if(ind == 'w')
			{
				this.vector.elementAt(i).weakUpdate(p.getSet());
			}
			else if (ind == 's')
			{
				this.vector.elementAt(i).strongUpdate(p.getSet());
			}
		}
	}
	//get position of pair with variable (string) v in vector
	public int getVarPos(String v)
	{
		if(this.varpos.get(v) == null)
			return -1;
		else
			return this.varpos.get(v).intValue();
	}
	//emptythe Graph
	public void emptyGraph()
	{
		this.vector.removeAllElements();
		this.varpos.clear();
		this.bot_ind = true;
	}
	//print pointsToGraph
	public String printGraph()
	{
		String res = "{";
		//System.out.println("START: PointsToGraph");
		if(this.bot_ind == false)
		{
			boolean first_iter_outer = true;
			for(int i=0;i<this.vector.size();i++)
			{
				//remove [ or ] while printing
				if(first_iter_outer == false)
					res = new String(res + ", ");
				first_iter_outer = false;
				
				String toprint = new String(this.vector.elementAt(i).getV().replace("[",""));
				toprint = toprint.replace("]", "");
				res = new String(res + toprint + " -> {");
				HashSet<String> temp = this.vector.elementAt(i).getSet();
				Iterator<String> it = temp.iterator();
				boolean first_iter = true;
				while(it.hasNext())
				{
					if(first_iter == false)
					{
						res = new String(res + ", ");
					}
					String toprint1 = new String(it.next().replace("[",""));
					toprint1 = toprint1.replace("]", "");
					//null instead of NULL in this new format in phase 2
					if(toprint1.equals("NULL"))
						toprint1 = "null";
					res = new String(res + toprint1);
					first_iter = false;
				}
				res = new String(res + "}");
			}
		}
		//System.out.println("END: PointsToGraph");
		res = new String(res + "}");
		return res;
	}
}
