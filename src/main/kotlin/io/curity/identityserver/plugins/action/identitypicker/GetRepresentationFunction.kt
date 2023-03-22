/*
 *  Copyright 2023 Curity AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.curity.identityserver.plugins.action.identitypicker

import se.curity.identityserver.sdk.haapi.*
import se.curity.identityserver.sdk.http.HttpMethod
import se.curity.identityserver.sdk.web.Representation
import java.net.URI

class GetRepresentationFunction(private val config: IdentityPickerAuthenticationActionConfig) : RepresentationFunction {

    companion object {
        val MSG_TITLE: Message = Message.ofKey("info.message")
    }

    override fun apply(model: RepresentationModel, factory: RepresentationFactory): Representation =
        factory.newAuthenticationStep { step ->
            val url = URI.create(model.getString("_actionUrl"))

            val identities = identitiesFromModel(model)

            step.addSelectorAction(HaapiContract.Actions.Kinds.AUTHENTICATOR_SELECTOR, MSG_TITLE) { options ->
                identities.forEach { (key, value) ->
                    val title = displayName(value)
                    options.addFormAction(
                        ActionKind.of("select-account"),
                        url,
                        HttpMethod.POST,
                        null,
                        Message.ofKey(title)
                    ) { form ->
                        form.addHiddenField(SELECTED_IDENTITY_INDEX_KEY, key)

                        // Set each attribute in identity representation as property
                        value.forEach { (attributeName, attributeValue) ->
                            when (attributeValue) {
                                is String -> form.setProperty(attributeName, attributeValue)
                                // Return a JSON string, client will have to handle it
                                else -> form.setProperty(attributeName, config.json().toJson(attributeValue))
                            }
                        }
                    }
                }
            }
        }

    private fun displayName(value: Map<String, Any?>): String? =
        when (val displayNameAttribute = value[config.displayNameAttribute()]) {
            is String -> displayNameAttribute
            is Long, is Int -> displayNameAttribute.toString()
            is Map<*, *> -> config.json().toJson(displayNameAttribute)
            else -> config.json().toJson(value)
        }

    private fun identitiesFromModel(model: RepresentationModel) =
        model.getAs(IDENTITIES_TEMPLATE_KEY, Map::class.java)
            .mapValues { identity ->
                (identity.value as Map<*, *>)
                    .mapKeys { it.key.toString() }
            }
            .mapKeys { entry -> entry.key as String }
}
