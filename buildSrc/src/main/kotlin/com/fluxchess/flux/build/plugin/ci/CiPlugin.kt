/*
 * Copyright 2007-2020 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxchess.flux.build.plugin.ci

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class CiPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val extension = project.extensions.create("ci", CiExtension::class, buildingOnCi())
		if (project == project.rootProject && extension.buildingOnCi) {
			project.logger.lifecycle("Building on CI.")
		}
	}

	private fun buildingOnCi(): Boolean {
		return System.getenv("CI") != null
	}
}
