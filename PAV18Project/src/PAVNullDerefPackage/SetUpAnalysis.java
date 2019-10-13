/*
 * @author Stanly Samuel and Dr. Rekha Pai
 */
package PAVNullDerefPackage;

// Do NOT import the pointer analysis package
	import java.io.*;
	import java.util.*;

	import com.ibm.wala.classLoader.IClass;
	import com.ibm.wala.classLoader.IField;
	import com.ibm.wala.classLoader.IMethod;
	import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
	import com.ibm.wala.ipa.callgraph.AnalysisCache;
	import com.ibm.wala.ipa.callgraph.AnalysisOptions;
	import com.ibm.wala.ipa.callgraph.AnalysisScope;
	import com.ibm.wala.ipa.callgraph.CGNode;
	import com.ibm.wala.ipa.callgraph.CallGraph;
	import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
	import com.ibm.wala.ipa.callgraph.Entrypoint;
	import com.ibm.wala.ipa.callgraph.impl.Util;
	import com.ibm.wala.ipa.cha.ClassHierarchy;
	import com.ibm.wala.ipa.cha.ClassHierarchyException;
	import com.ibm.wala.ssa.*;
	import com.ibm.wala.types.FieldReference;
	import com.ibm.wala.util.CancelException;
	import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.io.FileProvider;
	import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;

	public class SetUpAnalysis {
		
		private String classpath;
		private String mainClass;
		private String analysisClass;
		private String analysisMethod;
		
		
		//START: NO CHANGE REGION
		private AnalysisScope scope;	// scope defines the set of files to be analyzed
		private ClassHierarchy ch;		// Generate the class hierarchy for the scope
		private Iterable<Entrypoint> entrypoints;	// In the call graph, these are the entrypoint nodes
		private AnalysisOptions options;	// Specify options for the call graph builder
		private CallGraphBuilder builder;	// Builder object for the call graph
		private CallGraph cg;			// Call graph for the program
		
		
		public SetUpAnalysis(String classpath, String mainClass, String analysisClass, String analysisMethod)
		{
			this.classpath = classpath;
			this.mainClass = mainClass;
			this.analysisClass = analysisClass;
			this.analysisMethod = analysisMethod;			
		}
		
		
		/**
		 * Defines the scope of analysis: identifies the classes for analysis
		 * @throws Exception
		 */
		public void buildScope() throws Exception
		{
			FileProvider f = new FileProvider();
			scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, f.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		}
		
		/**
		 * Builds the hierarchy among the classes to be analyzed: if B extends A, then A is a superclass of B, etc
		 * @throws Exception
		 */
		public void buildClassHierarchy() throws Exception
		{
			ch = ClassHierarchy.make(scope);
		}
		
		/**
		 * The nodes of a call graph are methods. This method defines the "entry points" in the call graph.
		 * Note: The entry point may not necessarily be the main method.  
		 */
		public void buildEntryPoints()
		{
			entrypoints = Util.makeMainEntrypoints(scope, ch, mainClass);
		}
		
		/**
		 * Options to build the required call graph.
		 */
		public void setUpCallGraphConstruction()
		{
			options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroCFABuilder(options, new AnalysisCache(), ch, scope);
		}
		
		/**
		 * Build the call graph.
		 * @throws Exception
		 */
		public void generateCallGraph() throws Exception
		{
			cg = builder.makeCallGraph(options, null);
		}
		//END: NO CHANGE REGION
		
		/**
		 * These are methods for testing purposes. You can call to these to check whether Wala is setup properly.
		 * This method prints the nodes of the call graph.
		 */
		public void printNodes()
		{
			//System.out.println("Displaying Application's Call Graph nodes: ");
			Iterator<CGNode> nodes = cg.iterator();
		
			// Printout the nodes in the call-graph
			while(nodes.hasNext()) {
				String nodeInfo = nodes.next().toString();
				//if(nodeInfo.contains("Application"))
					//System.out.println(nodeInfo);
			}
		}
		
		/**
		 * This method prints the IR of the analysisMethod
		 */
		public void printIR() {
			//System.out.println("\n\n");
			Iterator<CGNode> nodes = cg.iterator();
			CGNode target = null;
			while(nodes.hasNext()) {
				CGNode node = nodes.next();
				String nodeInfo = node.toString();
				if(nodeInfo.contains(analysisClass) && nodeInfo.contains(analysisMethod)) {
					target = node;
					break;
				}
			}
			if(target!=null) {
				//System.out.println("The IR of method " + target.getMethod().getSignature() + " is:");
				//System.out.println(target.getIR().toString());
			} 
			else {
				System.out.println("The given method in the given class could not be found");
			}
		}
		
		/**
		 * Here, you need to fill in code to complete the rest of the analysis workflow.
		 * see project presentation and the write-up for details
		 */
		
		public boolean Join(PointsToGraph obj1, PointsToGraph obj2 )
		{
			obj1.bot_ind = false;
			boolean update_ind = false;
			for(int j = 0 ; j < obj2.vector.size(); j++)
			{
				Pair p = obj2.vector.elementAt(j);
				String v = new String(p.getV());
				HashSet<String> set = new HashSet<String>();
				set.addAll(p.getSet());
				//check if the pair was already there
				int position = obj1.getVarPos(p.getV()) ;
				if(position == -1 || obj1.vector.elementAt(position).getSet().equals(p.getSet()) == false)
					update_ind = true;
				obj1.addPair(new Pair(v,set), 'w'); 
			}
			return update_ind;
		}
		
 		public void doAnalysis()
		{
			
			Queue<Edge> marked_list = new LinkedList<Edge>();
			HashSet<Edge> edgeSet = new HashSet<Edge>();
			Hashtable<Edge,PointsToGraph> edgeGraphSet = new Hashtable<Edge,PointsToGraph>();
			
			//System.out.println("\n\n");
			Iterator<CGNode> nodes = cg.iterator();
			CGNode target = null;
			while(nodes.hasNext()) {
				CGNode node = nodes.next();
				String nodeInfo = node.toString();
				if(nodeInfo.contains(analysisClass) && nodeInfo.contains(analysisMethod)) {
					target = node;
					break;
				}
			}
			if(target!=null) 
			{
				//System.out.println("The Analysis Result of method " + target.getMethod().getSignature() + " is:\n");
				SSACFG cfg  = target.getIR().getControlFlowGraph();
				int no_of_bb = cfg.getNumberOfNodes();
				for (int i=0;i<no_of_bb;i++)
				{
					if(!(cfg.getBasicBlock(i).isExitBlock()))
					{
						Collection<ISSABasicBlock> c = cfg.getNormalSuccessors(cfg.getBasicBlock(i));
						Iterator<ISSABasicBlock> it = c.iterator();
						while(it.hasNext())
						{
							int succ_no = cfg.getNumber(it.next());
							Edge edge = new Edge(i,succ_no);
							marked_list.add(edge);
							edgeSet.add(edge);
						}						
					}					
				}
				
				SymbolTable sym = target.getIR().getSymbolTable();
				Iterator<Edge> itq= marked_list.iterator();
				//putting edge and points to graph pair in edgeset
				while(itq.hasNext())
				{
					PointsToGraph new_graph = new PointsToGraph();
					Edge new_edge = itq.next();
					//bot indicator falsr for entering edge
					if(new_edge.getU() == 0)
						new_graph.bot_ind = false;
					edgeGraphSet.put(new_edge, new_graph);
				}
				
				//Kildall's
				while(!(marked_list.isEmpty()))
				{
					Edge curr_elt = marked_list.remove();
					PointsToGraph pred_graph = edgeGraphSet.get(curr_elt);
					PointsToGraph curr_graph = new PointsToGraph(pred_graph); //copy of points to graph on the edge
					//System.out.println(System.identityHashCode(curr_graph));
					int next_node_id = curr_elt.getV();
					if(cfg.getBasicBlock(next_node_id).isExitBlock())
						continue;
					ISSABasicBlock next_bb = cfg.getBasicBlock(next_node_id);
					Iterator<SSAInstruction> it_ins =  next_bb.iterator();
					
					//some data types to handle conditionals
					boolean true_bot = false, false_bot = false;
					boolean check_for_cond = false;
					int target_block_no = 0;
					Pair true_pair1 = new Pair(new String(),new HashSet<String>());
					Pair true_pair2 = new Pair(new String(),new HashSet<String>());
					Pair false_pair1 = new Pair(new String(),new HashSet<String>());
					Pair false_pair2 = new Pair(new String(),new HashSet<String>());
					
					//data types for switch
					boolean check_for_switch = false;
					Hashtable<Integer , Integer> switch_succ = new Hashtable<Integer ,Integer>();
				
					
					while(it_ins.hasNext())
					{
						//Here we need to parse the instruction and identify TF and apply on curr_graph
						SSAInstruction curr_ins = it_ins.next();
						
						
						if(curr_graph.bot_ind == true)
							break;
						
						//START TF This if...else chain determines type of instruction applies TF on curr_graph according to that
						if (curr_ins instanceof SSAConditionalBranchInstruction )
						{
							//System.out.println(curr_ins.toString(sym));
							SSAConditionalBranchInstruction cond_ins = (SSAConditionalBranchInstruction) curr_ins;
							
							if(cond_ins.isObjectComparison())
							{
								//now we need to check befor join
								check_for_cond = true;
								int first_obj_no = cond_ins.getUse(0);
								int second_obj_no = cond_ins.getUse(1);
								HashSet<String> first_obj_set = new HashSet<String>();
								HashSet<String> second_obj_set = new HashSet<String>();
								if(sym.getValueString(first_obj_no).contains("#null"))  //object points to null
								{
									HashSet<String> null_set = new HashSet<String>();
									null_set.add("[NULL]");
									Pair null_pair = new Pair(new String("v" + first_obj_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									first_obj_set.add("[NULL]");
								}
								else   //object points to a set ... we want that set here
								{
									int first_obj_pos = curr_graph.getVarPos(new String("v" + first_obj_no));
									if(first_obj_pos != -1) //true when used variable was pointing to a set
									{
										first_obj_set.addAll(curr_graph.vector.elementAt(first_obj_pos).getSet());
									}
								}
								if(sym.getValueString(second_obj_no).contains("#null")) //object points to null
								{
									HashSet<String> null_set = new HashSet<String>();
									null_set.add("[NULL]");
									Pair null_pair = new Pair(new String("v" + second_obj_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									second_obj_set.add("[NULL]");
								}
								else  //object points to a set ... we want that set here
								{
									int second_obj_pos = curr_graph.getVarPos(new String("v" + second_obj_no));
									if(second_obj_pos != -1) //true when used variable was pointing to a set
									{
										second_obj_set.addAll(curr_graph.vector.elementAt(second_obj_pos).getSet());
									}
								}
								//getting intersection of both sets
								HashSet<String> intersection = new HashSet<String>(first_obj_set);
								intersection.retainAll(second_obj_set);
								String operator = cond_ins.getOperator().toString();
								//Finding block number of true branch
								target_block_no = cfg.getNumber(cfg.getBlockForInstruction(cond_ins.getTarget()));
								//if both points to just null it will be true (tested)
								HashSet<String> null_set = new HashSet<String>();
								null_set.add("[NULL]");
								
								//generic assignment to true and false pairs (will override below based on conditions if needed)
								{
									false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
									false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
									true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
									true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
								}
								
								//both pointing to null
								if((first_obj_set.equals(null_set) && second_obj_set.equals(null_set)))
								{
									if ( operator.equals("eq"))
										false_bot = true;
									else if (operator.equals("ne"))
										true_bot = true;
								}
								//Now if any one of the obj points to empty set in curr_graph (program crash as it will give compilation error)  
								else if(first_obj_set.isEmpty() || second_obj_set.isEmpty())
								{
									true_bot = false_bot = true;
								}
								//if intersection is empty, true branch will not be taken //TO BE DISCUSSED
								else if(intersection.isEmpty())
								{
									if ( operator.equals("eq"))
										true_bot = true;
									else if (operator.equals("ne"))
										false_bot = true;
								}
								//if only one of the branch is only null, then true branch will take both null while false wil take null removed from the other one
								else if((first_obj_set.equals(null_set) || second_obj_set.equals(null_set))) // note both just null will not be the case as it is already handled 
								{
									if ( operator.equals("eq"))
									{
										if(first_obj_set.equals(null_set))
										{
											second_obj_set.removeAll(null_set);
											false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										else
										{
											first_obj_set.removeAll(null_set);
											false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										
										true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(null_set));
										true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(null_set));			
										
									}
									else if (operator.equals("ne"))
									{
										if(first_obj_set.equals(null_set))
										{
											second_obj_set.removeAll(null_set);
											true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										else
										{
											first_obj_set.removeAll(null_set);
											true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(null_set));
										false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(null_set));
									}
								}
								else //general case where true branch will take intersection part and false will take both
								{
									if ( operator.equals("eq"))
									{
										true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(intersection));
										true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(intersection));
										
										false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
										false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
									}
									else if (operator.equals("ne"))
									{
										false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(intersection));
										false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(intersection));
										
										true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
										true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
									}
								}
							}
							
						}
						if (curr_ins instanceof SSANewInstruction )
						{
							if(curr_ins.hasDef()) //always true
							{
								SSANewInstruction new_ins = (SSANewInstruction) curr_ins;
								
								//creating new pair with getdef(variable number) and newi where i is iindex(line number)
								HashSet<String> new_set = new HashSet<String>();
								new_set.add(new String("[" + "new" + new_ins.iindex + "]"));
								Pair new_pair = new Pair(new String("v" + new_ins.getDef()),new_set);
								
								//add this pair to current PTG
								curr_graph.addPair(new_pair, 's');
							}
						}
						if (curr_ins instanceof SSAInvokeInstruction )
						{
							SSAInvokeInstruction invoke_ins = (SSAInvokeInstruction) curr_ins;
							int var_no = invoke_ins.getDef();
							if(var_no !=  -1)
							{
								//creating new pair with getdef(variable number) and newi where i is iindex(line number)
								HashSet<String> new_set = new HashSet<String>();
								new_set.add(new String("[" + "new" + invoke_ins.iindex + "]"));
								Pair new_pair = new Pair(new String("v" + var_no),new_set);
								
								//add this pair to current PTG
								curr_graph.addPair(new_pair, 's');
							}
						}
						if (curr_ins instanceof SSAGetInstruction )
						{
							SSAGetInstruction get_ins = (SSAGetInstruction) curr_ins;
							
							if(get_ins.hasDef()) //always true
							{
								int var_no = get_ins.getDef();
								HashSet<String> set = new HashSet<String>();
								for(int i = 0;i< get_ins.getNumberOfUses();i++)  // noofuses max value 1
								{
									int used_var_no = get_ins.getUse(i);
									String field_name = get_ins.getDeclaredField().getName().toString();
									
									//if used_var_no corresponds to "this" object then ignore
									
									if(target.getIR().getLocalNames(get_ins.iindex, used_var_no)[0].equals("this"))
										break;
									
									//iterate on used variable set and addd their field_name's points to set to current lhs's set
									
									HashSet<String> null_set = new HashSet<String>();
									null_set.add("[NULL]");
									if(sym.getValueString(used_var_no).contains("#null")) //object points to null
									{
										Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
										curr_graph.addPair(null_pair, 's');
									}
									int used_var_pos = curr_graph.getVarPos("v" + used_var_no);
									if(used_var_pos != -1 && (!curr_graph.vector.elementAt(used_var_pos).getSet().equals(null_set)))
									{
										if(!get_ins.getDeclaredFieldType().isPrimitiveType())
										{
											HashSet<String> used_var_set = curr_graph.vector.elementAt(used_var_pos).getSet();
											Iterator<String> it_used_var_set = used_var_set.iterator();
											while(it_used_var_set.hasNext())
											{
												String obj = it_used_var_set.next();
												if(!obj.equals("[NULL]"))
												{
													int obj_field_loc = curr_graph.getVarPos(obj + "." + field_name);
													//if it points to any set ...the lhs will point to that set now
													if(obj_field_loc != -1)
													{
														set.addAll(curr_graph.vector.elementAt(obj_field_loc).getSet());
													}
												}
											}
											//add pair if the set is not null
											if(!set.isEmpty())
											{
												curr_graph.addPair(new Pair(new String("v" + var_no),set), 's');
											}
										}
									}
									else //bot situation
									{
										curr_graph.emptyGraph();
									}
								}
							}
						}
						if (curr_ins instanceof SSAPutInstruction )
						{
							SSAPutInstruction put_ins = (SSAPutInstruction) curr_ins;
							
							if(put_ins.getNumberOfUses()>1) //always 2 for other than putstatic
							{
								HashSet<String> new_set = new HashSet<String>();
								//set for null entries
								HashSet<String> null_set = new HashSet<String>();
								null_set.add("[NULL]");
								int var_no = put_ins.getUse(0);
								
								//if var_no points to this then ignore
								
								if(target.getIR().getLocalNames(put_ins.iindex, var_no)[0].equals("this"))
									break;
								
								String field_name = put_ins.getDeclaredField().getName().toString();
								int used_var_no = put_ins.getUse(1);
								//check if used var has null as constant value then we need to add that variable and also update current set
								if(sym.getValueString(used_var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									new_set.add("[NULL]");
								}
								//if not null, the add to the current set where this used var point to in the current graph
								else
								{
									int ele_index = curr_graph.getVarPos(new String("v" + used_var_no));
									if(ele_index != -1) //true when used variable was pointing to a set
									{
										new_set.addAll(curr_graph.vector.elementAt(ele_index).getSet());
									}
								}
								//check if var(whose field is to be updated ) has null as constant value then we need to add that variable and also update current set
								if(sym.getValueString(var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
								}
								//add the new set to points to set of each object's (that is point to by var) fieldname 
								int var_pos = curr_graph.getVarPos("v" + var_no);
								if(var_pos != -1 && (!curr_graph.vector.elementAt(var_pos).getSet().equals(null_set)))
								{
									if(!new_set.isEmpty() && !put_ins.getDeclaredFieldType().isPrimitiveType())
									{
										HashSet<String> var_set = curr_graph.vector.elementAt(var_pos).getSet();
										Iterator<String> it_var_set = var_set.iterator();
										while(it_var_set.hasNext())
										{
											String points_to_var = it_var_set.next();
											if(!points_to_var.equals("[NULL]"))
											{
												Pair new_pair = new Pair(new String(points_to_var + "." + field_name),new_set);
												curr_graph.addPair(new_pair, 'w');
											}
										}
									}
								}
								else  //bot situation
								{
									curr_graph.emptyGraph();
								}
							}
						}
						if (curr_ins instanceof SSAPhiInstruction )
						{
							SSAPhiInstruction phi_ins = (SSAPhiInstruction) curr_ins;
							
							//creating new pair with getdef(variable number) and newi where i is iindex(line number)
							HashSet<String> new_set = new HashSet<String>();
							//set for null entries
							HashSet<String> null_set = new HashSet<String>();
							null_set.add("[NULL]");
							for(int i=0;i<phi_ins.getNumberOfUses();i++)
							{
								int used_var_no = phi_ins.getUse(i);
								//if variable is a constant that is null
								if(sym.getValueString(used_var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									new_set.add("[NULL]");
								}
								//if variable is not null add all the elements points to by used variable in the lhs variable's set
								else
								{
									int used_var_pos = curr_graph.getVarPos(new String("v" + used_var_no));
									if(used_var_pos != -1) //true when used variable was pointing to a set
									{
										new_set.addAll(curr_graph.vector.elementAt(used_var_pos).getSet());
									}
								}
							}
							if(!new_set.isEmpty())
							{
								Pair new_pair = new Pair(new String("v" + phi_ins.getDef()),new_set);
								curr_graph.addPair(new_pair, 's');
							}
						}
						if (curr_ins instanceof SSAGotoInstruction )
						{
							//nothing to do
						}
						
						// switch works when a> switch exp is an integer variable which has been assigned constant,
						//                   b> case constants in code are in asc order. 
						// we improve precision by excluding branches before the first match is obtained.
						
						if (curr_ins instanceof SSASwitchInstruction )
						{
						   SSASwitchInstruction switch_ins = (SSASwitchInstruction) curr_ins;
						   //no of uses always one, calculate constant value at that use (if available)
						   int used_var_no = switch_ins.getUse(0);
						   if(sym.isIntegerConstant(used_var_no)) {
							   check_for_switch = true;
							   int used_var_constant = sym.getIntValue(used_var_no);
							   int labels[] = switch_ins.getCasesAndLabels();
							   //IntIterator itr_label = switch_ins.iterateLabels();
							   boolean case_matched =  false;
							  
							   for(int i = 0 ; i < labels.length ; i = i+2)
							   {
								   
								   target_block_no = cfg.getNumber(cfg.getBlockForInstruction(labels[i+1]));
								   
								   if(case_matched == true)
								   {
									   switch_succ.put(target_block_no, 1);
								   }
								   
								   else if(used_var_constant == labels[i])
								   {
									   switch_succ.put(target_block_no, 1);
									   case_matched = true;
								   }
								   else
								   {
									   switch_succ.put(target_block_no, 0);
								   }
							   }
							   
							   
						   }
						   
                            
						}
						if (curr_ins instanceof SSAReturnInstruction )
						{
							//nothing to do
						}
						if (curr_ins instanceof SSABinaryOpInstruction )
						{
							//nothing to do
						}
						if (curr_ins instanceof SSAUnaryOpInstruction )
						{
							//nothing to do
						}
						
						//END TF
					}
					//Here we need to join the curr_graph with the existing graph at successor edges(edges going out of successor nodes)
					//Also we need to decide if the successor edge should be there in marked_list or not
					
					//no need to pass the points to graph if it has bot_ind as true
					if(curr_graph.bot_ind == true)
						continue;
					
					int succ_block_no = curr_elt.getV();
					Iterator<Edge> it_succ_edge = edgeSet.iterator();
					while(it_succ_edge.hasNext())
					{
						PointsToGraph new_curr_graph = new PointsToGraph(curr_graph);
						Edge nextedge = it_succ_edge.next();
						if(nextedge.getU() == succ_block_no)
						{
							PointsToGraph succ_graph = edgeGraphSet.get(nextedge);
							//Here we need to modify curr_graph based on the true or false branch for conditions only
							if(check_for_cond == true)
							{
								int next_block_no = nextedge.getV();
								if(next_block_no == target_block_no) // true branch
								{
									if(true_bot == true)
										new_curr_graph.emptyGraph();
									else
									{
										new_curr_graph.addPair(true_pair1, 's');
										new_curr_graph.addPair(true_pair2, 's');
									}
								}
								else //false branch
								{
									if(false_bot == true)
										new_curr_graph.emptyGraph();
									else
									{
										new_curr_graph.addPair(false_pair1, 's');
										new_curr_graph.addPair(false_pair2, 's');
									}
								}
							}
							
							if(check_for_switch == true)
							{
								int next_block_no = nextedge.getV();
								if(switch_succ.get(next_block_no) == null)
								{
									//do nothing
									//new_curr_graph.emptyGraph();
								}
								else if(switch_succ.get(next_block_no) == 0)
								{
									
									new_curr_graph.emptyGraph();
								}
							}
							
							if(new_curr_graph.bot_ind == false && this.Join(succ_graph, new_curr_graph))  //Joins the two graph and returns true if new succ graph dominates old one
							{
								//System.out.println("");
								if(!marked_list.contains(nextedge))
								{
									marked_list.add(nextedge);
								}
							}
						}
					}
				}
				//print the final edgeset graph
				Iterator<Edge> it_final_graph = edgeSet.iterator();
				while(it_final_graph.hasNext())
				{
					Edge edge = it_final_graph.next();
					System.out.println("BB" + edge.getU() + " " + "->" + " " + "BB" + edge.getV() + ":");
					PointsToGraph ptg_print = edgeGraphSet.get(edge);
					ptg_print.printGraph();
					System.out.println("");
				}
			}
			else 
			{
				System.out.println("The given method in the given class could not be found");
			}
			
		}
		
		
		//Data Structure for table
		 private TreeMap<String,Hashtable<Edge,PointsToGraph>> table = new TreeMap<String,Hashtable<Edge,PointsToGraph>>();
		 private String columns =  "Columns:\n";
		 private String final_output = "";
		 private int cindex = 0;
		 
		 private PointsToGraph call_graph = new PointsToGraph();
		 private PointsToGraph return_graph = new PointsToGraph();
		 private HashSet<String> returnedSet = new HashSet<String>();
 		
 		
 		public void doAnalysis2(String methodName,String methodClass)
		{
			
			Queue<Edge> marked_list = new LinkedList<Edge>();
			HashSet<Edge> edgeSet = new HashSet<Edge>();
			Hashtable<Edge,PointsToGraph> edgeGraphSet = new Hashtable<Edge,PointsToGraph>();
			
			if(methodName.isEmpty())
			{
				methodName = analysisMethod;
				String temp = new String(methodName + "c" + new Integer(cindex).toString());
				table.put(temp, edgeGraphSet);
				cindex++;
			}
			else
			{
				//compare with olds, if join not false
				Set<String> keys = table.keySet();
				//System.err.println(table.keySet());
				Iterator<String> it = keys.iterator();
				String func_name = "";
				while(it.hasNext())
				{
					String func_key = it.next();
					//find last c position
					int pos = func_key.length()-1;
					while(func_key.charAt(pos) != 'c')
						pos--;
					String cval = func_key.substring(pos);
					func_name = func_key.substring(0, pos);
					if(func_name.equals(methodName))
					{
						Hashtable<Edge,PointsToGraph> temp_ptg = table.get(func_key);
						Set<Edge> curr_keys = temp_ptg.keySet();
						Iterator<Edge> curr_itp = curr_keys.iterator();
						while(curr_itp.hasNext())
						{
							Edge nxt_edge = curr_itp.next();
							PointsToGraph prev_graph = new PointsToGraph(temp_ptg.get(nxt_edge));
							if(nxt_edge.getU() == 0 && nxt_edge.getV() == 1 && this.Join(prev_graph, call_graph) == false)
							{
								return;
							}
						}
							
					}
				}
				
				String temp = new String(methodName + "c" + new Integer(cindex).toString());
				table.put(temp, edgeGraphSet);
				cindex++;
			}
			
			if(methodClass.isEmpty())
				methodClass = analysisClass;
			//System.out.println("\n\n");
			Iterator<CGNode> nodes = cg.iterator();
			CGNode target = null;
			while(nodes.hasNext()) {
				CGNode node = nodes.next();
				String nodeInfo = node.toString();
				if(nodeInfo.contains(methodClass) && nodeInfo.contains(methodName)) {
					target = node;
					break;
				}
			}
			if(target!=null) 
			{
				//System.out.println("The Analysis Result of method " + target.getMethod().getSignature() + " is:\n");
				//System.out.println(target.getIR().toString());
				
				SSACFG cfg  = target.getIR().getControlFlowGraph();
				int no_of_bb = cfg.getNumberOfNodes();
				for (int i=0;i<no_of_bb;i++)
				{
					if(!(cfg.getBasicBlock(i).isExitBlock()))
					{
						Collection<ISSABasicBlock> c = cfg.getNormalSuccessors(cfg.getBasicBlock(i));
						Iterator<ISSABasicBlock> it = c.iterator();
						while(it.hasNext())
						{
							int succ_no = cfg.getNumber(it.next());
							Edge edge = new Edge(i,succ_no);
							marked_list.add(edge);
							edgeSet.add(edge);
						}						
					}					
				}
				
				SymbolTable sym = target.getIR().getSymbolTable();
				Iterator<Edge> itq= marked_list.iterator();
				//putting edge and points to graph pair in edgeset
				while(itq.hasNext())
				{
					PointsToGraph new_graph = new PointsToGraph();
					Edge new_edge = itq.next();
					//bot indicator falsr for entering edge
					if(new_edge.getU() == 0)
					{	
						//copy call_graph
						new_graph = new PointsToGraph(call_graph);
						new_graph.bot_ind = false;
						call_graph.emptyGraph();
					}
					edgeGraphSet.put(new_edge, new_graph);
				}
				
				//Kildall's
				while(!(marked_list.isEmpty()))
				{
					Edge curr_elt = marked_list.remove();
					PointsToGraph pred_graph = edgeGraphSet.get(curr_elt);
					PointsToGraph curr_graph = new PointsToGraph(pred_graph); //copy of points to graph on the edge
					//System.out.println(System.identityHashCode(curr_graph));
					int next_node_id = curr_elt.getV();
					if(cfg.getBasicBlock(next_node_id).isExitBlock())
						continue;
					ISSABasicBlock next_bb = cfg.getBasicBlock(next_node_id);
					Iterator<SSAInstruction> it_ins =  next_bb.iterator();
					
					//some data types to handle conditionals
					boolean true_bot = false, false_bot = false;
					boolean check_for_cond = false;
					int target_block_no = 0;
					Pair true_pair1 = new Pair(new String(),new HashSet<String>());
					Pair true_pair2 = new Pair(new String(),new HashSet<String>());
					Pair false_pair1 = new Pair(new String(),new HashSet<String>());
					Pair false_pair2 = new Pair(new String(),new HashSet<String>());
					
					//data types for switch
					boolean check_for_switch = false;
					Hashtable<Integer , Integer> switch_succ = new Hashtable<Integer ,Integer>();
				
					
					while(it_ins.hasNext())
					{
						//Here we need to parse the instruction and identify TF and apply on curr_graph
						SSAInstruction curr_ins = it_ins.next();
						
						
						if(curr_graph.bot_ind == true)
							break;
						
						//START TF This if...else chain determines type of instruction applies TF on curr_graph according to that
						if(curr_ins instanceof SSAArrayLoadInstruction)
						{
							SSAArrayLoadInstruction arrld_ins = (SSAArrayLoadInstruction) curr_ins;
							if(arrld_ins.hasDef())
							{
								HashSet<String> set = new HashSet<String>();
								
								HashSet<String> null_set = new HashSet<String>();
								null_set.add("[NULL]");
								
								String var = new String("v" + arrld_ins.getDef());
								int used_var_pos = curr_graph.getVarPos("v" + arrld_ins.getUse(0));
								if(used_var_pos != -1 && (!curr_graph.vector.elementAt(used_var_pos).getSet().equals(null_set)))
								{
										HashSet<String> used_var_set = curr_graph.vector.elementAt(used_var_pos).getSet();
										Iterator<String> it_used_var_set = used_var_set.iterator();
										while(it_used_var_set.hasNext())
										{
											String obj = it_used_var_set.next();
											
											if(!obj.equals("[NULL]"))
											{
												int obj_field_loc = curr_graph.getVarPos(obj);
												//if it points to any set ...the lhs will point to that set now
												if(obj_field_loc != -1)
												{
													set.addAll(curr_graph.vector.elementAt(obj_field_loc).getSet());
												}
											}
										}
										//add pair if the set is not null
										if(!set.isEmpty())
										{
											curr_graph.addPair(new Pair(new String(var),set), 's');
										}
								}
							}
						}
						/*if(curr_ins instanceof SSAArrayReferenceInstruction)
						{
							System.out.println("3\n");
							System.out.println(curr_ins.toString(sym));
						}*/
						if(curr_ins instanceof SSAArrayStoreInstruction)
						{
							//System.out.println(curr_ins.toString(sym));
							SSAArrayStoreInstruction arrst_ins = (SSAArrayStoreInstruction) curr_ins;
							if(arrst_ins.getNumberOfUses()>1) //always 3 
							{
								HashSet<String> new_set = new HashSet<String>();
								//set for null entries
								HashSet<String> null_set = new HashSet<String>();
								null_set.add("[NULL]");
								int var_no = arrst_ins.getUse(0);
								
								//if var_no points to this then ignore
								
								if(target.getIR().getLocalNames(arrst_ins.iindex, var_no)[0].equals("this"))
									break;
								
								int used_var_no = arrst_ins.getUse(2);
								//check if used var has null as constant value then we need to add that variable and also update current set
								if(sym.getValueString(used_var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									new_set.add("[NULL]");
								}
								//if not null, the add to the current set where this used var point to in the current graph
								else
								{
									int ele_index = curr_graph.getVarPos(new String("v" + used_var_no));
									if(ele_index != -1) //true when used variable was pointing to a set
									{
										new_set.addAll(curr_graph.vector.elementAt(ele_index).getSet());
									}
								}
								//check if var(whose field is to be updated ) has null as constant value then we need to add that variable and also update current set
								if(sym.getValueString(var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
								}
								//add the new set to points to set of each object's (that is point to by var) fieldname 
								int var_pos = curr_graph.getVarPos("v" + var_no);
								if(var_pos != -1 && (!curr_graph.vector.elementAt(var_pos).getSet().equals(null_set)))
								{
									if(!new_set.isEmpty() )
									{
										HashSet<String> var_set = curr_graph.vector.elementAt(var_pos).getSet();
										Iterator<String> it_var_set = var_set.iterator();
										while(it_var_set.hasNext())
										{
											String points_to_var = it_var_set.next();
											if(!points_to_var.equals("[NULL]"))
											{
												Pair new_pair = new Pair(new String(points_to_var),new_set);
												curr_graph.addPair(new_pair, 'w');
											}
										}
									}
								}
								else  //bot situation
								{
									curr_graph.emptyGraph();
								}
							}
						}
						
						if (curr_ins instanceof SSAConditionalBranchInstruction )
						{
							//System.out.println(curr_ins.toString(sym));
							SSAConditionalBranchInstruction cond_ins = (SSAConditionalBranchInstruction) curr_ins;
							
							if(cond_ins.isObjectComparison())
							{
								//System.err.println("here");
								//now we need to check befor join
								check_for_cond = true;
								int first_obj_no = cond_ins.getUse(0);
								int second_obj_no = cond_ins.getUse(1);
								HashSet<String> first_obj_set = new HashSet<String>();
								HashSet<String> second_obj_set = new HashSet<String>();
								if(sym.getValueString(first_obj_no).contains("#null"))  //object points to null
								{
									HashSet<String> null_set = new HashSet<String>();
									null_set.add("[NULL]");
									Pair null_pair = new Pair(new String("v" + first_obj_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									first_obj_set.add("[NULL]");
								}
								else   //object points to a set ... we want that set here
								{
									int first_obj_pos = curr_graph.getVarPos(new String("v" + first_obj_no));
									if(first_obj_pos != -1) //true when used variable was pointing to a set
									{
										first_obj_set.addAll(curr_graph.vector.elementAt(first_obj_pos).getSet());
									}
								}
								if(sym.getValueString(second_obj_no).contains("#null")) //object points to null
								{
									HashSet<String> null_set = new HashSet<String>();
									null_set.add("[NULL]");
									Pair null_pair = new Pair(new String("v" + second_obj_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									second_obj_set.add("[NULL]");
								}
								else  //object points to a set ... we want that set here
								{
									int second_obj_pos = curr_graph.getVarPos(new String("v" + second_obj_no));
									if(second_obj_pos != -1) //true when used variable was pointing to a set
									{
										second_obj_set.addAll(curr_graph.vector.elementAt(second_obj_pos).getSet());
									}
								}
								//getting intersection of both sets
								HashSet<String> intersection = new HashSet<String>(first_obj_set);
								intersection.retainAll(second_obj_set);
								String operator = cond_ins.getOperator().toString();
								//Finding block number of true branch
								target_block_no = cfg.getNumber(cfg.getBlockForInstruction(cond_ins.getTarget()));
								//if both points to just null it will be true (tested)
								HashSet<String> null_set = new HashSet<String>();
								null_set.add("[NULL]");
								
								//generic assignment to true and false pairs (will override below based on conditions if needed)
								{
									false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
									false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
									true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
									true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
								}
								
								//both pointing to null
								if((first_obj_set.equals(null_set) && second_obj_set.equals(null_set)))
								{
									if ( operator.equals("eq"))
										false_bot = true;
									else if (operator.equals("ne"))
										true_bot = true;
								}
								//Now if any one of the obj points to empty set in curr_graph (program crash as it will give compilation error)  
								else if(first_obj_set.isEmpty() || second_obj_set.isEmpty())
								{
									true_bot = false_bot = true;
								}
								//if intersection is empty, true branch will not be taken //TO BE DISCUSSED
								else if(intersection.isEmpty())
								{
									if ( operator.equals("eq"))
										true_bot = true;
									else if (operator.equals("ne"))
										false_bot = true;
								}
								//if only one of the branch is only null, then true branch will take both null while false wil take null removed from the other one
								else if((first_obj_set.equals(null_set) || second_obj_set.equals(null_set))) // note both just null will not be the case as it is already handled 
								{
									if ( operator.equals("eq"))
									{
										if(first_obj_set.equals(null_set))
										{
											second_obj_set.removeAll(null_set);
											false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										else
										{
											first_obj_set.removeAll(null_set);
											false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										
										true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(null_set));
										true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(null_set));			
										
									}
									else if (operator.equals("ne"))
									{
										if(first_obj_set.equals(null_set))
										{
											second_obj_set.removeAll(null_set);
											true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										else
										{
											first_obj_set.removeAll(null_set);
											true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
											true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
										}
										false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(null_set));
										false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(null_set));
									}
								}
								else //general case where true branch will take intersection part and false will take both
								{
									if ( operator.equals("eq"))
									{
										true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(intersection));
										true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(intersection));
										
										false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
										false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
									}
									else if (operator.equals("ne"))
									{
										false_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(intersection));
										false_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(intersection));
										
										true_pair1 = new Pair(new String("v" + first_obj_no),new HashSet<String>(first_obj_set));
										true_pair2 = new Pair(new String("v" + second_obj_no),new HashSet<String>(second_obj_set));
									}
								}
							}
							
						}
						if (curr_ins instanceof SSANewInstruction )
						{
							if(curr_ins.hasDef()) //always true
							{
								SSANewInstruction new_ins = (SSANewInstruction) curr_ins;
									//creating new pair with getdef(variable number) and newi where i is iindex(line number)
									HashSet<String> new_set = new HashSet<String>();
									new_set.add(new String("[" + methodName + ".new" + new_ins.iindex + "]"));
									Pair new_pair = new Pair(new String("v" + new_ins.getDef()),new_set);
									
									//add this pair to current PTG
									curr_graph.addPair(new_pair, 's');
							}
						}
						if (curr_ins instanceof SSAInvokeInstruction )
						{
							SSAInvokeInstruction invoke_ins = (SSAInvokeInstruction) curr_ins;
							int var_no = invoke_ins.getDef();
							//System.out.println(invoke_ins.getDeclaredTarget().getDeclaringClass().getName());
							//System.out.println(invoke_ins.getCallSite().getDeclaredTarget().getName().toString());
							if(invoke_ins.isSpecial() == false)
							{
								if(invoke_ins.toString(sym).contains("Application") == false && var_no != -1)
								{
									//creating new pair with getdef(variable number) and newi where i is iindex(line number)
									HashSet<String> new_set = new HashSet<String>();
									new_set.add(new String("[" + "new" + invoke_ins.iindex + "]"));
									Pair new_pair = new Pair(new String("v" + var_no),new_set);
									
									//add this pair to current PTG
									curr_graph.addPair(new_pair, 's');
								}
								else
								{
									String call_method = invoke_ins.getCallSite().getDeclaredTarget().getName().toString();
									String call_class =  invoke_ins.getDeclaredTarget().getDeclaringClass().getName().toString();
									
									//System.err.println(invoke_ins.getNumberOfParameters());
									//System.err.println(invoke_ins.getNumberOfUses());
									//include parameters calling
									for(int i=1;i<invoke_ins.getNumberOfUses();i++)
									{
										String var = new String("v" + invoke_ins.getUse(i));
										int pos = curr_graph.getVarPos(var);
										HashSet<String> obj_set = new HashSet<String>();
										if(pos != -1)
										{
											obj_set.addAll(curr_graph.vector.elementAt(pos).getSet());
											call_graph.addPair(new Pair(new String("v" + new Integer(i+1).toString()),obj_set), 's');
										}
										
									}
									//include pseudovars
									for(int i = 0;i< curr_graph.vector.size();i++)
									{
										if(curr_graph.vector.elementAt(i).getV().contains(methodName))
										{
											String var = new String(curr_graph.vector.elementAt(i).getV());
											HashSet<String> obj_set = new HashSet<String>();
											obj_set.addAll(curr_graph.vector.elementAt(i).getSet());
											call_graph.addPair(new Pair(new String(var),obj_set), 's');
											
										}
									}
									doAnalysis2(call_method,call_class);
									if(var_no != -1)
									{
										HashSet<String> new_set = new HashSet<String>();
										new_set.addAll(returnedSet);
										Pair new_pair = new Pair(new String("v" + var_no),new_set);
										
										//add this pair to current PTG
										curr_graph.addPair(new_pair, 's');
									}
									this.Join(curr_graph, return_graph);
									return_graph.emptyGraph();
								}
							}
						}
						if (curr_ins instanceof SSAGetInstruction )
						{
							SSAGetInstruction get_ins = (SSAGetInstruction) curr_ins;
							
							if(get_ins.hasDef()) //always true
							{
								int var_no = get_ins.getDef();
								HashSet<String> set = new HashSet<String>();
								for(int i = 0;i< get_ins.getNumberOfUses();i++)  // noofuses max value 1
								{
									int used_var_no = get_ins.getUse(i);
									String field_name = get_ins.getDeclaredField().getName().toString();
									
									//if used_var_no corresponds to "this" object then ignore
									
									if(target.getIR().getLocalNames(get_ins.iindex, used_var_no)[0].equals("this"))
										break;
									
									//iterate on used variable set and addd their field_name's points to set to current lhs's set
									
									HashSet<String> null_set = new HashSet<String>();
									null_set.add("[NULL]");
									if(sym.getValueString(used_var_no).contains("#null")) //object points to null
									{
										Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
										curr_graph.addPair(null_pair, 's');
									}
									int used_var_pos = curr_graph.getVarPos("v" + used_var_no);
									if((used_var_no != 1))
									{
										if(used_var_pos != -1 && (!curr_graph.vector.elementAt(used_var_pos).getSet().equals(null_set)))
										{
											if(!get_ins.getDeclaredFieldType().isPrimitiveType())
											{
												HashSet<String> used_var_set = curr_graph.vector.elementAt(used_var_pos).getSet();
												Iterator<String> it_used_var_set = used_var_set.iterator();
												while(it_used_var_set.hasNext())
												{
													String obj = it_used_var_set.next();
													if(!obj.equals("[NULL]"))
													{
														int obj_field_loc = curr_graph.getVarPos(obj + "." + field_name);
														//if it points to any set ...the lhs will point to that set now
														if(obj_field_loc != -1)
														{
															set.addAll(curr_graph.vector.elementAt(obj_field_loc).getSet());
														}
													}
												}
												//add pair if the set is not null
												if(!set.isEmpty())
												{
													curr_graph.addPair(new Pair(new String("v" + var_no),set), 's');
												}
											}
										}
										else //bot situation
										{
											curr_graph.emptyGraph();
										}
									}
									
								}
							}
						}
						if (curr_ins instanceof SSAPutInstruction )
						{
							SSAPutInstruction put_ins = (SSAPutInstruction) curr_ins;
							
							if(put_ins.getNumberOfUses()>1) //always 2 for other than putstatic
							{
								HashSet<String> new_set = new HashSet<String>();
								//set for null entries
								HashSet<String> null_set = new HashSet<String>();
								null_set.add("[NULL]");
								int var_no = put_ins.getUse(0);
								
								//if var_no points to this then ignore
								
								//if(target.getIR().getLocalNames(put_ins.iindex, var_no)[0].equals("this"))
									//break;
								if(true)
									continue;
								String field_name = put_ins.getDeclaredField().getName().toString();
								int used_var_no = put_ins.getUse(1);
								//check if used var has null as constant value then we need to add that variable and also update current set
								if(sym.getValueString(used_var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									new_set.add("[NULL]");
								}
								//if not null, the add to the current set where this used var point to in the current graph
								else
								{
									int ele_index = curr_graph.getVarPos(new String("v" + used_var_no));
									if(ele_index != -1) //true when used variable was pointing to a set
									{
										new_set.addAll(curr_graph.vector.elementAt(ele_index).getSet());
									}
								}
								//check if var(whose field is to be updated ) has null as constant value then we need to add that variable and also update current set
								if(sym.getValueString(var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
								}
								//add the new set to points to set of each object's (that is point to by var) fieldname 
								int var_pos = curr_graph.getVarPos("v" + var_no);
								if(var_pos != -1 && (!curr_graph.vector.elementAt(var_pos).getSet().equals(null_set)))
								{
									if(!new_set.isEmpty() && !put_ins.getDeclaredFieldType().isPrimitiveType())
									{
										HashSet<String> var_set = curr_graph.vector.elementAt(var_pos).getSet();
										Iterator<String> it_var_set = var_set.iterator();
										while(it_var_set.hasNext())
										{
											String points_to_var = it_var_set.next();
											if(!points_to_var.equals("[NULL]"))
											{
												Pair new_pair = new Pair(new String(points_to_var + "." + field_name),new_set);
												curr_graph.addPair(new_pair, 'w');
											}
										}
									}
								}
								else  //bot situation
								{
									curr_graph.emptyGraph();
								}
							}
						}
						if (curr_ins instanceof SSAPhiInstruction )
						{
							SSAPhiInstruction phi_ins = (SSAPhiInstruction) curr_ins;
							
							//creating new pair with getdef(variable number) and newi where i is iindex(line number)
							HashSet<String> new_set = new HashSet<String>();
							//set for null entries
							HashSet<String> null_set = new HashSet<String>();
							null_set.add("[NULL]");
							for(int i=0;i<phi_ins.getNumberOfUses();i++)
							{
								int used_var_no = phi_ins.getUse(i);
								//if variable is a constant that is null
								if(sym.getValueString(used_var_no).contains("#null"))
								{
									Pair null_pair = new Pair(new String("v" + used_var_no ),null_set);
									curr_graph.addPair(null_pair, 's');
									new_set.add("[NULL]");
								}
								//if variable is not null add all the elements points to by used variable in the lhs variable's set
								else
								{
									int used_var_pos = curr_graph.getVarPos(new String("v" + used_var_no));
									if(used_var_pos != -1) //true when used variable was pointing to a set
									{
										new_set.addAll(curr_graph.vector.elementAt(used_var_pos).getSet());
									}
								}
							}
							if(!new_set.isEmpty())
							{
								Pair new_pair = new Pair(new String("v" + phi_ins.getDef()),new_set);
								curr_graph.addPair(new_pair, 's');
							}
						}
						if (curr_ins instanceof SSAGotoInstruction )
						{
							//nothing to do
						}
						
						// switch works when a> switch exp is an integer variable which has been assigned constant,
						//                   b> case constants in code are in asc order. 
						// we improve precision by excluding branches before the first match is obtained.
						
						if (curr_ins instanceof SSASwitchInstruction )
						{
						   SSASwitchInstruction switch_ins = (SSASwitchInstruction) curr_ins;
						   //no of uses always one, calculate constant value at that use (if available)
						   int used_var_no = switch_ins.getUse(0);
						   if(sym.isIntegerConstant(used_var_no)) {
							   check_for_switch = true;
							   int used_var_constant = sym.getIntValue(used_var_no);
							   int labels[] = switch_ins.getCasesAndLabels();
							   //IntIterator itr_label = switch_ins.iterateLabels();
							   boolean case_matched =  false;
							  
							   for(int i = 0 ; i < labels.length ; i = i+2)
							   {
								   
								   target_block_no = cfg.getNumber(cfg.getBlockForInstruction(labels[i+1]));
								   
								   if(case_matched == true)
								   {
									   switch_succ.put(target_block_no, 1);
								   }
								   
								   else if(used_var_constant == labels[i])
								   {
									   switch_succ.put(target_block_no, 1);
									   case_matched = true;
								   }
								   else
								   {
									   switch_succ.put(target_block_no, 0);
								   }
							   }
							   
							   
						   }
						   
                            
						}
						if (curr_ins instanceof SSAReturnInstruction )
						{
							SSAReturnInstruction return_ins = (SSAReturnInstruction) curr_ins;
							//include pseudovars
							for(int i = 0;i< curr_graph.vector.size();i++)
							{
								//Based on syntax, if var contains [ , it is pseudo variable
								if(curr_graph.vector.elementAt(i).getV().contains("["))
								{
									String var = new String(curr_graph.vector.elementAt(i).getV());
									HashSet<String> obj_set = new HashSet<String>();
									obj_set.addAll(curr_graph.vector.elementAt(i).getSet());
									return_graph.addPair(new Pair(new String(var),obj_set), 's');
									
								}
							}
							if(return_ins.returnsVoid() == true || return_ins.returnsPrimitiveType() == true)
							{
							}
							else
							{
								int ret_var = return_ins.getUse(0);
								String var = new String("v" + new Integer(ret_var).toString());
								int pos = curr_graph.getVarPos(var);
								if(pos!= -1)
								{
									returnedSet.addAll(curr_graph.vector.elementAt(pos).getSet());
								}
							}
							curr_graph.emptyGraph();
							continue;
						}
						if (curr_ins instanceof SSABinaryOpInstruction )
						{
							//nothing to do
						}
						if (curr_ins instanceof SSAUnaryOpInstruction )
						{
							//nothing to do
						}
						
						
						//END TF
					}
					//Here we need to join the curr_graph with the existing graph at successor edges(edges going out of successor nodes)
					//Also we need to decide if the successor edge should be there in marked_list or not
					
					//no need to pass the points to graph if it has bot_ind as true
					if(curr_graph.bot_ind == true)
						continue;
					
					int succ_block_no = curr_elt.getV();
					Iterator<Edge> it_succ_edge = edgeSet.iterator();
					while(it_succ_edge.hasNext())
					{
						PointsToGraph new_curr_graph = new PointsToGraph(curr_graph);
						Edge nextedge = it_succ_edge.next();
						if(nextedge.getU() == succ_block_no)
						{
							PointsToGraph succ_graph = edgeGraphSet.get(nextedge);
							//Here we need to modify curr_graph based on the true or false branch for conditions only
							if(check_for_cond == true)
							{
								int next_block_no = nextedge.getV();
								if(next_block_no == target_block_no) // true branch
								{
									if(true_bot == true)
										new_curr_graph.emptyGraph();
									else
									{
										new_curr_graph.addPair(true_pair1, 's');
										new_curr_graph.addPair(true_pair2, 's');
									}
								}
								else //false branch
								{
									if(false_bot == true)
										new_curr_graph.emptyGraph();
									else
									{
										new_curr_graph.addPair(false_pair1, 's');
										new_curr_graph.addPair(false_pair2, 's');
									}
								}
							}
							
							if(check_for_switch == true)
							{
								int next_block_no = nextedge.getV();
								if(switch_succ.get(next_block_no) == null)
								{
									//do nothing
									//new_curr_graph.emptyGraph();
								}
								else if(switch_succ.get(next_block_no) == 0)
								{
									
									new_curr_graph.emptyGraph();
								}
							}
							
							if(new_curr_graph.bot_ind == false && this.Join(succ_graph, new_curr_graph))  //Joins the two graph and returns true if new succ graph dominates old one
							{
								//System.out.println("");
								if(!marked_list.contains(nextedge))
								{
									marked_list.add(nextedge);
								}
							}
						}
					}
				}
				//print the final edgeset graph
				/*Iterator<Edge> it_final_graph = edgeSet.iterator();
				while(it_final_graph.hasNext())
				{
					Edge edge = it_final_graph.next();
					System.out.println("BB" + edge.getU() + " " + "->" + " " + "BB" + edge.getV() + ":");
					PointsToGraph ptg_print = edgeGraphSet.get(edge);
					ptg_print.printGraph();
					System.out.println("");
				}*/
			}
			else 
			{
				System.out.println("The given method in the given class could not be found");
			}
			
		}
		
		public void printfunc(String f_name,Vector<String> c_vec,Vector<Hashtable<Edge,PointsToGraph>> print_table )
		{
			final_output = new String(final_output + f_name + ":" + "\n\n\n\n\n");
			Hashtable<Edge,PointsToGraph> first_one = print_table.elementAt(0);
			Set<Edge> keys = first_one.keySet();
			Iterator<Edge> itp = keys.iterator();
			
			while(itp.hasNext())
			{
				Edge curr_edge = itp.next();
				if(curr_edge.getU() == 0 && curr_edge.getV() == 1)
					columns = new String(columns +"\n" + c_vec.elementAt(0) + ": " + print_table.elementAt(0).get(curr_edge).printGraph());
				
				final_output = new String(final_output + "BB" + curr_edge.getU() + " -> " +"BB"+ curr_edge.getV() +":" + "\n\n");
				
				final_output = new String(final_output + c_vec.elementAt(0) + ": " + print_table.elementAt(0).get(curr_edge).printGraph() + "\n\n");
				for(int i=1;i<c_vec.size();i++)
				{
					Hashtable<Edge,PointsToGraph> curr_one = print_table.elementAt(i);
					Set<Edge> curr_keys = curr_one.keySet();
					Iterator<Edge> curr_itp = curr_keys.iterator();
					while(curr_itp.hasNext())
					{
						Edge nxt_edge = curr_itp.next();
						if(nxt_edge.getU() == curr_edge.getU() && nxt_edge.getV() == curr_edge.getV())
						{
							//update columns if 0-1 edge
							if(nxt_edge.getU() == 0 && nxt_edge.getV() == 1)
								columns = new String(columns +"\n" + c_vec.elementAt(i) + ": " + print_table.elementAt(i).get(nxt_edge).printGraph());
							
							final_output = new String(final_output + c_vec.elementAt(i) + ": " + print_table.elementAt(i).get(nxt_edge).printGraph() + "\n\n");
							break;
						}
					}
					
				}
				final_output = new String(final_output + "\n");
			}
		}
		
		
		public void printTable()
		{
			int cindex = 0,iter = 0;
			if(table.isEmpty())
			{
				System.out.println("Table Empty");
				return;
			}
			Set<String> keys = table.keySet();
			//System.err.println(table.keySet());
			Iterator<String> it = keys.iterator();
			String prev_func = "";
			String func_name = "";
			Vector<Hashtable<Edge,PointsToGraph>> print_table = new Vector<Hashtable<Edge,PointsToGraph>>();
			Vector<String> c_vec = new Vector<String>();
			while(it.hasNext())
			{
				iter++;
				String func_key = it.next();
				//find last c position
				int pos = func_key.length()-1;
				while(func_key.charAt(pos) != 'c')
					pos--;
				String cval = func_key.substring(pos);
				func_name = func_key.substring(0, pos);
				if(func_name.equals(prev_func) == false && iter > 1)
				{
					
					//System.err.println(func_name);
					//System.err.println(prev_func);
					//System.err.println(iter);
					printfunc(prev_func,c_vec,print_table);
					print_table.clear();
					c_vec.clear();
				}
				prev_func = new String(func_name);
				c_vec.add(cval);
				print_table.add(table.get(func_key));
			}
			printfunc(func_name,c_vec,print_table);
			System.out.println(columns);
			System.out.println("\n\n\n\n");
			System.out.println(final_output);
		}
	
	}

