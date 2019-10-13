package PAVNullDerefPackage;

import java.util.*;
public class Pair{
	private String v;
    private HashSet<String> s;
    public Pair(String v, HashSet<String> s){
        this.v = new String(v);
        this.s = new HashSet<String>();
        this.s.addAll(s);
    }
    public String getV(){ return this.v; }
    public HashSet<String> getSet(){ return this.s; }
    public void weakUpdate(HashSet<String> s)
    {
    	this.s.addAll(s);
    }
    public void strongUpdate(HashSet<String> s)
    {
    		if(!(this.s.isEmpty()))
    		{
    			this.s.clear();
    		}
    		this.s.addAll(s);
    }
}