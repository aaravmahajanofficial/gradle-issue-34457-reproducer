/*
 * Copyright 2025 Aarav Mahajan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.github.aaravmahajanofficial

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

public open class PluginExtension
@Inject
constructor(
    private val objects: ObjectFactory,
    libs: VersionCatalog,
) {

    public val quality: QualityExtension by lazy { objects.newInstance<QualityExtension>(libs) }

    public fun quality(action: QualityExtension.() -> Unit) {
        quality.apply(action)
    }

}
