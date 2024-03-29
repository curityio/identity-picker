/*
 *  Copyright 2021 Curity AB
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

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import se.curity.identityserver.sdk.attribute.Attribute
import se.curity.identityserver.sdk.attribute.ListAttributeValue
import se.curity.identityserver.sdk.attribute.MapAttributeValue
import se.curity.identityserver.sdk.authenticationaction.completions.ActionCompletionRequestHandler
import se.curity.identityserver.sdk.authenticationaction.completions.ActionCompletionResult
import se.curity.identityserver.sdk.errors.ErrorCode.INVALID_INPUT
import se.curity.identityserver.sdk.service.SessionManager
import se.curity.identityserver.sdk.web.Request
import se.curity.identityserver.sdk.web.Response
import se.curity.identityserver.sdk.web.Response.ResponseModelScope.ANY
import se.curity.identityserver.sdk.web.ResponseModel.templateResponseModel
import java.util.Optional

const val IDENTITIES_TEMPLATE_KEY = "_identities"
const val SELECTED_IDENTITY_INDEX_KEY = "identity-index"
private const val DISPLAY_NAME_ATTRIBUTE_NAME = "_displayNameAttribute"

class IdentityPickerRequestHandler(private val config: IdentityPickerAuthenticationActionConfig) :
    ActionCompletionRequestHandler<IdentityPickerRequestModel> {

    private val sessionManager = config.sessionManager()
    private val exceptionFactory = config.exceptionFactory()

    override fun preProcess(request: Request, response: Response): IdentityPickerRequestModel =
        IdentityPickerRequestModel(request, sessionManager)

    override fun get(request: IdentityPickerRequestModel, response: Response): Optional<ActionCompletionResult>
    {
        response.setResponseModel(templateResponseModel(emptyMap(), "index"), ANY)

        val identities = convertAvailableIdentities(request.getIdentities())
        response.putViewData(IDENTITIES_TEMPLATE_KEY, identities, ANY)
        response.putViewData(DISPLAY_NAME_ATTRIBUTE_NAME, config.displayNameAttribute(), ANY)
        return Optional.empty()
    }

    override fun post(request: IdentityPickerRequestModel, response: Response): Optional<ActionCompletionResult> {
        val selectedIdentityIndex = request.getSelectedIdentityIndex()
        val modelFromSession = getAndRemoveModelFromSession()
        val selectedIdentity = modelFromSession[selectedIdentityIndex] as? Map<*, *>
                ?: throw exceptionFactory.badRequestException(INVALID_INPUT, "No matching identity selected")

        sessionManager.put(Attribute.of(PICKED_IDENTITY_SESSION_KEY, MapAttributeValue.of(selectedIdentity)))
        return Optional.of(ActionCompletionResult.complete())
    }

    private fun getAndRemoveModelFromSession(): Map<String, Any> {
        val model = sessionManager.remove("identity-model")?.attributeValue as? MapAttributeValue
            ?: throw IllegalStateException("Did not find model in session")
        return model.value
    }

    private fun convertAvailableIdentities(attribute: Attribute): Map<String, Map<String, Any>> {
        val listOfIdentities = attribute.attributeValue as? ListAttributeValue
            ?: throw IllegalArgumentException("Identities was not in expected format")

        val identityModel = mutableMapOf<String, Map<String, Any>>()
        listOfIdentities.forEachIndexed { index, identityAttributeValue ->
            val identityAsMap = identityAttributeValue as? MapAttributeValue
                ?: throw IllegalArgumentException("Identity was not a map")
            identityModel[index.toString()] = identityAsMap.value
        }

        sessionManager.put(Attribute.of("identity-model", MapAttributeValue.of(identityModel)))

        return identityModel
    }
}

class IdentityPickerRequestModel(request: Request, sessionManager: SessionManager) {

    @Valid
    val getRequestModel = if (request.isGetRequest) GetRequestModel(sessionManager) else null

    @Valid
    private val postRequestModel = if (request.isPostRequest) PostRequestModel(request) else null


    fun getIdentities() = getRequestModel?.identities
        ?: throw IllegalStateException("Request Model was missing for GET request")

    fun getSelectedIdentityIndex() = postRequestModel?.identityIndex
        ?: throw IllegalStateException("Request Model was missing for POST request")

    class PostRequestModel(request: Request) {
        @NotBlank(message = "error.no.selected.identity")
        val identityIndex: String? = request.getFormParameterValues(SELECTED_IDENTITY_INDEX_KEY).firstOrNull()
    }

    class GetRequestModel(sessionManager: SessionManager) {
        @NotNull(message = "error.no.identities.in.session")
        val identities: Attribute = sessionManager[IDENTITY_LIST_SESSION_KEY]
    }
}

