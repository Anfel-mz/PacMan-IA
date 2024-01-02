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
	/**
	 * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
	 * @param beliefState the current belief-state of the agent
	 * @param depth the depth of the search (size of the largest sequence of action checked)
	 * @return a string describing the next action (among PacManLauncher.UP/DOWN/LEFT/RIGHT)
	 */
	public static String findNextMove(BeliefState beliefState) {
		PriorityQueue<BeliefState> frontier = new PriorityQueue<>(Comparator.comparingDouble(b -> b.compareTo(beliefState) + computeHeuristic(b)));
		Set<BeliefState> explored = new HashSet<>();
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
	            
	            // Assuming 'Gomme.GOMME_CHAR' represents a gomme
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

	    return minDistance;
	}
	
	private static double[] calculateNearestGhostDistance(BeliefState bfs) {
		int pacmanRow = bfs.getPacmanPosition().getRow();
	    int pacmanCol = bfs.getPacmanPosition().getColumn();
	    int idGhost = -1;
	    int nbrOfGhosts = bfs.getNbrOfGhost();
	    TreeSet<Position> ghostPositions = new TreeSet<>();
	    TreeSet<Position> visibleGhosts = new TreeSet<>();
	    for(int i = 0; i<nbrOfGhosts; i++) {
	    	ghostPositions.addAll(bfs.getGhostPositions(i));
	    }
	    for(Position pos : ghostPositions) {
	    	if(pacmanRow == pos.getRow() || pacmanCol == pos.getColumn()) {
	    		visibleGhosts.add(pos);
	    	}
	    }

	    // If there are no ghosts, return a large value to de-prioritize avoiding ghosts
	    if (ghostPositions.isEmpty()) {
	        return new double[] {0.0, (double)idGhost};
	    }

	    // Find the nearest ghost
	    Position nearestGhost = visibleGhosts.isEmpty()? ghostPositions.first(): visibleGhosts.first();
	    double distanceToNearestGhost = calculateDistance(pacmanRow, pacmanCol, nearestGhost.getRow(), nearestGhost.getColumn());
	    
	    for (int i = 0; i < nbrOfGhosts; i++) {
	        TreeSet<Position> nearestghostPositions = bfs.getGhostPositions(i);
	        if (nearestghostPositions.contains(nearestGhost)) {
	            idGhost = i;
	        }
	    }
	    System.out.println(distanceToNearestGhost);
	    return new double[]{distanceToNearestGhost, (double) idGhost};
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
	    double distanceToNearestGhost = calculateNearestGhostDistance(beliefState)[0];

	    // Weights for different entities
	    double weightGomme = 1.0;
	    double weightSuperGomme = 10.0;
	    double weightGhost = beliefState.getCompteurPeur((int)calculateNearestGhostDistance(beliefState)[1]) == 0? -60 : beliefState.getCompteurPeur((int)calculateNearestGhostDistance(beliefState)[1]);
	    
	    // Combine distances with weights to form the heuristic value
	    return weightGomme * distanceToNearestGomme +
	           weightSuperGomme * distanceToNearestSuperGomme +
	           weightGhost * distanceToNearestGhost + 10000 * (beliefState.getLife() == 0 ? 1: -1);
	}

}