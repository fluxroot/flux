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
package com.fluxchess.flux.build.plugin.versioning

import com.fluxchess.flux.build.plugin.ci.CiExtension
import com.fluxchess.flux.build.plugin.ci.CiPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType

class VersioningPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val versioning = createExtension(project)

		project.plugins.withType<CiPlugin> {
			val identifier = getIdentifier(project, versioning)
			project.version = "${versioning.version}${identifier}"

			if (project == project.rootProject) {
				project.logger.lifecycle("Building ${project.name} ${project.version}")
			}
		}
	}

	private fun createExtension(project: Project): VersioningExtension {
		val buildNo = System.getenv("BUILD_NUMBER") ?: ""
		val commitId = System.getenv("COMMIT_ID") ?: ""
		val abbreviatedCommitId = if (commitId.isNotEmpty()) commitId.substring(0, 7) else ""
		project.logger.info("buildNo: '${buildNo}', commitId: '${abbreviatedCommitId}'")
		return project.extensions.create(
				"versioning", VersioningExtension::class, project.version, buildNo, commitId, abbreviatedCommitId)
	}

	private fun getIdentifier(project: Project, versioning: VersioningExtension): String {
		val ci: CiExtension by project.extensions
		if (!ci.buildingOnCi
				|| versioning.buildNo.isEmpty()
				|| versioning.abbreviatedId.isEmpty()) {
			return "-SNAPSHOT"
		}
		return "-${versioning.buildNo}.${versioning.abbreviatedId}"
	}
}
