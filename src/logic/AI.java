package logic;

import java.util.*;

import view.Gomme;


/**
 * class used to represent plan. It will provide for a given set of results an action to perform in each result
 */
class Plans{
	ArrayList<Result> results;
	ArrayList<ArrayList<String>> actions;
	
	/**
	 * construct an empty plan
	 */
	public Plans() {
		this.results = new ArrayList<Result>();
		this.actions = new ArrayList<ArrayList<String>>();
	}
	
	/**
	 * add a new pair of belief-state and corresponding (equivalent) actions 
	 * @param beliefBeliefState the belief state to add
	 * @param action a list of alternative actions to perform. Only one of them is chosen but their results should be similar
	 */
	public void addPlan(Result beliefBeliefState, ArrayList<String> action) {
		this.results.add(beliefBeliefState);
		this.actions.add(action);
	}
	
	/**
	 * return the number of belief-states/actions pairs
	 * @return the number of belief-states/actions pairs
	 */
	public int size() {
		return this.results.size();
	}
	
	/**
	 * return one of the belief-state of the plan
	 * @param index index of the belief-state
	 * @return the belief-state corresponding to the index
	 */
	public Result getResult(int index) {
		return this.results.get(index);
	}
	
	/**
	 * return the list of actions performed for a given belief-state
	 * @param index index of the belief-state
	 * @return the set of actions to perform for the belief-state corresponding to the index
	 */
	public ArrayList<String> getAction(int index){
		return this.actions.get(index);
	}
}

/**
 * class used to represent a transition function i.e., a set of possible belief states the agent may be in after performing an action
 */
class Result{
	private ArrayList<BeliefState> beliefStates;

	/**
	 * construct a new result
	 * @param states the set of states corresponding to the new belief state
	 */
	public Result(ArrayList<BeliefState> states) {
		this.beliefStates = states;
	}

	/**
	 * returns the number of belief states
	 * @return the number of belief states
	 */
	public int size() {
		return this.beliefStates.size();
	}

	/**
	 * return one of the belief state
	 * @param index the index of the belief state to return
	 * @return the belief state to return
	 */
	public BeliefState getBeliefState(int index) {
		return this.beliefStates.get(index);
	}
	
	/**
	 * return the list of belief-states
	 * @return the list of belief-states
	 */
	public ArrayList<BeliefState> getBeliefStates(){
		return this.beliefStates;
	}
}


/**
 * class implement the AI to choose the next move of the Pacman
 */
public class AI{
	private static Set<BeliefState> explored = new HashSet<>();
	
	/**
	 * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
	 * @param beliefState the current belief-state of the agent
	 * @param depth the depth of the search (size of the largest sequence of action checked)
	 * @return a string describing the next action (among PacManLauncher.UP/DOWN/LEFT/RIGHT)
	 */
	public static String findNextMove(BeliefState beliefState) {
		PriorityQueue<BeliefState> frontier = new PriorityQueue<>(Comparator.comparingDouble(b -> b.compareTo(beliefState) + computeHeuristic(b)));
		Plans solution = new Plans();
        frontier.add(beliefState);
        BeliefState current = beliefState;
        int depth = 2;
        
        while(frontier != null && depth>0) {
        	depth --;
        	current = frontier.poll();
        	
        	if (explored.contains(current)) {
                continue;
            }
        	
        	if (current.getNbrOfGommes() == 0) {
        		break;
        	}
        	
        	explored.add(current);
        	Plans succ = current.extendsBeliefState();
        	
        	
        	for(int i =0; i<succ.size(); i++) {
        		Result rslt = succ.getResult(i);
        		for(int j = 0; j<rslt.size(); j++) {
        			BeliefState bfs = rslt.getBeliefState(j);
        			if(!explored.contains(bfs) && !frontier.contains(bfs)) {
        				frontier.add(bfs);
        				solution.addPlan(rslt, succ.getAction(i));
        			}
        		}
        	}
        }
        
        ArrayList<String> sol = new ArrayList<>();
         
        for(int c = 0; c<solution.size(); c++) {
        	Result rslt = solution.getResult(c);
    		for(int cc = 0; cc<rslt.size(); cc++) {
    			BeliefState bfs = rslt.getBeliefState(cc);
    			if(bfs.compareTo(current) == 0) {
    				sol = solution.getAction(c);
    			}
    		}
        }
        
        return sol.getLast();

	}
	
	private static Double findNearestGomme(BeliefState beliefState, int pacmanRow, int pacmanCol, char typeOfGomme) {
	    int numRows = 25;
	    int numCols = 25;

	    Double minDistance = 50.0;

	    for (int i = 0; i < numRows; i++) {
	        for (int j = 0; j < numCols; j++) {
	            char cellContent = beliefState.getMap(i, j);
	            
	            
	            if (cellContent == typeOfGomme) {
	            	int gommeRow = i;
	            	int gommeCol = j;
	                Double distance = calculateDistance(pacmanRow, pacmanCol, gommeRow, gommeCol);
	                if (distance < minDistance) {
	                    minDistance = distance;
	                }
	            }
	        }
	    }

	    return Math.max(1, minDistance);
	}
	
	
	private static double[] calculateGhostDistance(BeliefState bfs) {
		int pacmanRow = bfs.getPacmanPosition().getRow();
	    int pacmanCol = bfs.getPacmanPosition().getColumn();
	    
	    
	    TreeSet<Position> ghost1Positions = new TreeSet<>();
	    TreeSet<Position> ghost2Positions = new TreeSet<>();
	    
	    	ghost1Positions.addAll(bfs.getGhostPositions(0));
	    	ghost2Positions.addAll(bfs.getGhostPositions(1));

	    // Find the nearest ghost
	    double distanceToGhost1 = calculateDistance(pacmanRow, pacmanCol, ghost1Positions.getFirst().getRow(), ghost1Positions.getFirst().getColumn());
	    double distanceToGhost2 = calculateDistance(pacmanRow, pacmanCol, ghost2Positions.getFirst().getRow(), ghost2Positions.getFirst().getColumn());

	    
	    
	    return new double[]{distanceToGhost1, distanceToGhost2};
	}
	
	private static double calculateDistance(int pacmanRow, int pacmanCol, int targetRow, int targetCol) {
	    return Math.abs(pacmanRow - targetRow) + Math.abs(pacmanCol - targetCol);
	}

	private static double computeHeuristic(BeliefState beliefState) {
	    int pacmanRow = beliefState.getPacmanPosition().getRow();
	    int pacmanCol = beliefState.getPacmanPosition().getColumn();

	    // Compute distances to various entities
	    double distanceToNearestGomme = findNearestGomme(beliefState, pacmanRow, pacmanCol, '.');
	    double distanceToNearestSuperGomme = findNearestGomme(beliefState, pacmanRow, pacmanCol, '*');
	    double distanceToGhost1 = calculateGhostDistance(beliefState)[0];
	    double distanceToGhost2 = calculateGhostDistance(beliefState)[1];
	    

	    // Weights for different entities
	    double weightGomme = beliefState.getNbrOfSuperGommes() == 0 ? Math.max(10.0, 60.0 - 5.0 * beliefState.getNbrOfGommes()) : 1.0;
	    double weightSuperGomme = 5.0;
	    double weightGhost1 = beliefState.getCompteurPeur(0) == 0? -60 : 1.5 * beliefState.getCompteurPeur(0);
	    double weightGhost2 = beliefState.getCompteurPeur(1) == 0? -60 : 1.5 * beliefState.getCompteurPeur(1);
	    double weightLife = beliefState.getNbrOfGommes()<= 50 && beliefState.getNbrOfSuperGommes() == 0? 
	    		Math.max(200, 200.0 - 2.0 * beliefState.getNbrOfGommes()) : 300;
	    
	    
	    // Combine distances with weights to form the heuristic value
	    return weightGomme * distanceToNearestGomme +
	           weightSuperGomme * distanceToNearestSuperGomme +
	           (weightGhost1 / (distanceToGhost1 )) + (weightGhost2 / (distanceToGhost2)) + 
	           weightLife * (beliefState.getLife() == 0 ? 1: -1);
	}
}