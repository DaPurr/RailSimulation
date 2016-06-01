package wagon.algorithms;

import java.util.Collection;

/**
 * This interface should be used in tandem with a <code>RouteGeneration</code> object.
 * Implementing classes are supposed to override the <code>selectPath</code> method, 
 * which selects a <code>DefaultPath</code> according to some route selection rule. 
 * 
 * @author Nemanja Milovanovic
 *
 */
public interface RouteSelection {

	/**
	 * Given a collection of paths, select a path according to
	 * some route selection rule. 
	 * 
	 * @param paths	<code>Collection</code> of <code>DefaultPath</code> objects
	 * @return	<code>DefaultPath</code> object
	 */
	public DefaultPath selectPath(Collection<DefaultPath> paths);
}
