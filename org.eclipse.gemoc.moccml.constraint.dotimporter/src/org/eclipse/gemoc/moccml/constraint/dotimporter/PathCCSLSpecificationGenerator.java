//package org.eclipse.gemoc.moccml.constraint.dotimporter;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashSet;
//
//import org.eclipse.core.resources.IFile;
//
//import com.alexmerz.graphviz.Parser;
//import com.alexmerz.graphviz.objects.Graph;
//
//import it.unimi.dsi.fastutil.ints.IntSet;
//import toools.io.file.RegularFile;
//
//public class PathCCSLSpecificationGenerator {
//	protected IFile ccslFile;
//	protected IFile dotFile;
//	protected RegularFile maxPathCCSLFile = null;
//	protected RegularFile maxPathConstraintFile = null;
//	protected HashSet<String> allClockNames;
//			
//	public PathCCSLSpecificationGenerator(IFile ccslF, IFile dotF) {
//		ccslFile = ccslF;
//		dotFile = dotF;
//		
//		String resultCCSLPath = ccslFile.getParent().getFullPath().toString()+"/SpecOfTheMaxPath.extendedCCSL";
//		maxPathCCSLFile = new RegularFile(ccslFile.getWorkspace().getRoot().getLocation().toOSString()+resultCCSLPath);
//		try {
//			maxPathCCSLFile.setContentAsASCII("");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//      try {
//    	  InputStream is = dotFile.getContents();
//          Parser p = new Parser();
//          Boolean parseOK = p.parse(new StringBuffer(is.toString()));
//          ArrayList<Graph> al =p.getGraphs();
//          for(int i=0; i<al.size();i++) {
//          	System.out.println(al.get(i).toString());
//          }
//      } catch (Exception e) {
//          e.printStackTrace();
//          System.exit(1);
//      }        
//		
//		String maxPathConstraintPath = ccslFile.getParent().getFullPath().toString()+"/maxPathConstraint.moccml";
//		maxPathConstraintFile = new RegularFile(ccslFile.getWorkspace().getRoot().getLocation().toOSString()+maxPathConstraintPath);
//		try {
//			maxPathConstraintFile.setContentAsASCII("");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		allClockNames = computeUsedClock();
//	}
//
//	
//	private HashSet<String> computeUsedClock() {
//		HashSet<String> res = new HashSet<String>();
//		
//		int lastV = ResultsHolder.maxPath.getSource();
//		int currentV = -1;
//		for(int i = 1; i <= ResultsHolder.maxPath.getLength(); i++){
//			currentV = ResultsHolder.maxPath.getVertexAt(i);
//			IntSet allConnectingEdges = null;
//			allConnectingEdges = stateSpace.getGrph().getEdgesConnecting(lastV, currentV);
//			String oneEdge = stateSpace.i2e(allConnectingEdges.toIntArray()[0]).toString();
//			if (oneEdge.startsWith("LS !:")){
//				oneEdge = oneEdge.substring(5);
//			}
//			if (oneEdge.endsWith("]")){
//				oneEdge = oneEdge.substring(0, oneEdge.length()-1);
//			}
//			for(String clockName : oneEdge.split(":")){
//				res.add(clockName.replaceAll(" *", ""));
//			}
//			lastV = currentV;
//		}
//		
//	
//		return res;
//	}
//	
//	
//	
//	public void createCCSLTraceRelation() throws IOException{
//		
//		
//		maxPathCCSLFile.append(("\t Relation theMaxPathPlayerRelation[maxPathSpecificConstraint](  maxPathSpecificConstraint_phyClock->"+ResultsHolder.physicalClockName+"\n").getBytes());
//		for(String maxPathClockName : allClockNames ){
//			maxPathCCSLFile.append(("\t\t ,maxPathSpecificConstraint_"+maxPathClockName+"-> "+maxPathClockName+" \n").getBytes());
//		}
//		maxPathCCSLFile.append("\t)\n".getBytes());
//		maxPathCCSLFile.append("\t}\n".getBytes());
//		maxPathCCSLFile.append("}\n".getBytes());
//	}
//	
//	
//	public void createCCSLHeader() throws IOException {
//		maxPathCCSLFile.append("/*\n".getBytes());
//		maxPathCCSLFile.append("* CCSL specification\n".getBytes());
//		maxPathCCSLFile.append(" * @author:  the trace player generator written bu Julien Deantoni\n".getBytes());
//		maxPathCCSLFile.append(" * date :  Fri September 22th 2016  10:51:42 CEST \n".getBytes());
//		maxPathCCSLFile.append(" */\n".getBytes());
//		maxPathCCSLFile.append("ClockConstraintSystem maxPathSpecification {\n".getBytes());
//		maxPathCCSLFile.append("    imports {\n".getBytes());
//		maxPathCCSLFile.append("        // import statements\n".getBytes());
//		maxPathCCSLFile.append("		import \"platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib\" as lib0;\n".getBytes()); 
//		maxPathCCSLFile.append("		import \"platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/CCSL.ccslLib\" as lib1; \n".getBytes());
//		maxPathCCSLFile.append(("		import \"./"+this.maxPathConstraintFile.getName()+"\" as MPC;\n").getBytes());
//		maxPathCCSLFile.append(("		import \""+ResultsHolder.originalCCSLFilePath+"\" as theSpec;\n").getBytes());
//		maxPathCCSLFile.append("    }\n".getBytes());
//		maxPathCCSLFile.append("    entryBlock main\n".getBytes());
//		maxPathCCSLFile.append("     \n".getBytes());
//		maxPathCCSLFile.append("        Block main {\n".getBytes());
//	}
//	public void createConstraintSpecificMoCCMLDefinition () throws IOException{
//		
//	
//		createConstraintDefHeader();
//		createConstraintDefVariables();
//		createConstraintDefStates();
//		createConstraintDefFooter();
//	
//
//	}
//
//	private void createConstraintDefFooter() throws IOException{
//		maxPathConstraintFile.append("\n".getBytes());
//		maxPathConstraintFile.append("RelationDeclaration	maxPathSpecificConstraint(maxPathSpecificConstraint_phyClock:clock".getBytes());
//		for(String aClock : allClockNames){
//			maxPathConstraintFile.append((",maxPathSpecificConstraint_"+aClock+":clock").getBytes());
//		}
//		maxPathConstraintFile.append(")\n".getBytes());
//		maxPathConstraintFile.append("	           }\n".getBytes());
//		maxPathConstraintFile.append("}\n".getBytes());
//	}
//	
//	private void createConstraintDefStates() throws IOException{
//		maxPathConstraintFile.append("	           init:  initialState\n".getBytes());
//		maxPathConstraintFile.append("	           State initialState(out: s0s1)\n".getBytes());
//		maxPathConstraintFile.append("	             from initialState to s1 : s0s1 -> ()\n".getBytes());
//		int lastV = ResultsHolder.maxPath.getSource();
//		int currentV = -1;
//		int i;
//		for(i = 1; i <= ResultsHolder.maxPath.getLength(); i++){
//			currentV = ResultsHolder.maxPath.getVertexAt(i);
//			IntSet allConnectingEdges = null;
//			allConnectingEdges = stateSpace.getGrph().getEdgesConnecting(lastV, currentV);
//			UniqueString oneEdge = stateSpace.i2e(allConnectingEdges.toIntArray()[0]);
//			
//			HashSet<String> allUniqueClocksToTrigger = new HashSet<String>();
//			StringBuilder allClocksToTrigger = new StringBuilder();
//			for(String s : oneEdge.split(",")){
//				s = s.replaceAll(" *", "");
//				s = s.replaceAll("\\[", "");
//				s = s.replaceAll("\\]", "");
//				if (allUniqueClocksToTrigger.add(s)){ //ensure no doublon
//					allClocksToTrigger.append("maxPathSpecificConstraint_"+s+", ");
//				}
//				
//			}
//			allClocksToTrigger.deleteCharAt(allClocksToTrigger.length()-1);
//			allClocksToTrigger.deleteCharAt(allClocksToTrigger.length()-1);
//			
//			
//			maxPathConstraintFile.append(("	           State s"+i+"(\n").getBytes());
//			maxPathConstraintFile.append(("	           		in: s"+(i-1)+"s"+i).getBytes());
//			maxPathConstraintFile.append(("\n").getBytes());
//			maxPathConstraintFile.append(("					out : s"+i+"s"+(i+1)+"\n").getBytes());
//			maxPathConstraintFile.append(("			   )\n").getBytes());
//			maxPathConstraintFile.append(("				from s"+i+" to s"+(i+1)+" : s"+i+"s"+(i+1)+" -> (when "+allClocksToTrigger+")\n").getBytes());
//			lastV=currentV;
//		}
//
//	             maxPathConstraintFile.append(("	           State s"+(i)+"(\n                            in: s"+(i-1)+"s"+i).getBytes());
//	     		maxPathConstraintFile.append(("			   )\n}\n").getBytes());
//		
//	}
//	private void createConstraintDefVariables() throws IOException {
//		maxPathConstraintFile.append("			variables { \n".getBytes());
//		maxPathConstraintFile.append(" 			}\n".getBytes());
//	}
//
//	private void createConstraintDefHeader() throws IOException {
//		maxPathConstraintFile.append("AutomataConstraintLibrary maxPathConstraints{\n".getBytes());
//		maxPathConstraintFile.append("   import 'platform:/plugin/fr.inria.aoste.timesquare.ccslkernel.model/ccsllibrary/kernel.ccslLib' as kernel;\n".getBytes());
//		maxPathConstraintFile.append("\n".getBytes());
//		maxPathConstraintFile.append("   RelationLibrary temporalRelations{\n".getBytes());
//		maxPathConstraintFile.append("\n".getBytes());
//		maxPathConstraintFile.append("      AutomataRelationDefinition maxPathSpecificConstraintDef[maxPathSpecificConstraint]{\n".getBytes());
//		maxPathConstraintFile.append("\n".getBytes());
//	}
//
//	
//}
