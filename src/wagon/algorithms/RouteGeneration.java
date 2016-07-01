package wagon.algorithms;

import java.util.List;

/**
* This interface is used for various route generation algorithms. 
* 
* @author Nemanja Milovanovic
*
*/

public interface RouteGeneration {

	/**
	 * This method returns a collection of routes using a route generation algorithm. 
	 * To actually select a route, use a class of type <code>RouteSelection</code>.
	 * 
	 * @return	list of paths
	 */
	public List<Path> generateRoutes();
}
