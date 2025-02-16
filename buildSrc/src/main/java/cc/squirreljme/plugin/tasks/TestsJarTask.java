// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.plugin.tasks;

import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

/**
 * Contains the task for creating a test JAR.
 *
 * @since 2020/03/04
 */
public class TestsJarTask
	extends Jar
{
	/**
	 * Initializes the task.
	 *
	 * @since 2020/03/04
	 */
	@Inject
	public TestsJarTask(Task __testClasses, Task __testResources)
	{
		// Depend on these two tasks because they have to be done first
		this.dependsOn(__testClasses, __testResources);
		this.mustRunAfter(__testClasses, __testResources);
		
		// Set details of this task
		this.setGroup("squirreljme");
		this.setDescription("Builds the test JAR which is needed to run " +
			"tests from within the VM.");
		
		// Setup the task
		this.getArchiveClassifier().set("tests");
		this.from((Callable<FileCollection>)this::__from);
	}
	
	/**
	 * Returns what makes up the test JAR.
	 *
	 * @return The files that make up the test JAR.
	 * @since 2020/03/04
	 */
	private FileCollection __from()
	{
		SourceSet set = this.getProject().getConvention()
			.getPlugin(JavaPluginConvention.class).getSourceSets()
			.getByName(SourceSet.TEST_SOURCE_SET_NAME);
		return this.getProject().files(set.getOutput().getClassesDirs(),
			set.getOutput().getResourcesDir());
	}
}
