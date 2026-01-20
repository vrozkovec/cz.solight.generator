package name.berries.app.guice;

import com.google.inject.Module;

/**
 * Base Guice app
 *
 * @author rozkovec
 *
 *
 */
public interface IGuiceApplication
{
	/**
	 * adds module to the application
	 *
	 * @param module
	 */
	void addModule(Module module);

	/**
	 * Method where all Guice modules should be added. Add modules by calling
	 * {@link #addModule(Module)}. <br>
	 * When overriding this method, it is always necessary to call super implementation.
	 */
	void addGuiceModules();

	/**
	 * @return guice modules
	 */
	Module[] getGuiceModules();

}
