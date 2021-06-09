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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.curity.identityserver.sdk.attribute.Attribute
import se.curity.identityserver.sdk.attribute.AttributeValue
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes
import se.curity.identityserver.sdk.attribute.ListAttributeValue
import se.curity.identityserver.sdk.attribute.MapAttributeValue
import se.curity.identityserver.sdk.attribute.SubjectAttributes
import se.curity.identityserver.sdk.authentication.AuthenticatedSessions
import se.curity.identityserver.sdk.authenticationaction.AuthenticationAction
import se.curity.identityserver.sdk.authenticationaction.AuthenticationActionResult
import se.curity.identityserver.sdk.authenticationaction.AuthenticationActionResult.failedResult
import se.curity.identityserver.sdk.authenticationaction.AuthenticationActionResult.successfulResult
import se.curity.identityserver.sdk.authenticationaction.completions.RequiredActionCompletion.PromptUser.prompt
import se.curity.identityserver.sdk.service.authenticationaction.AuthenticatorDescriptor

const val PICKED_IDENTITY_SESSION_KEY = "$PLUGIN_TYPE-identity"
const val IDENTITY_LIST_SESSION_KEY = "$PLUGIN_TYPE-identity_list"

class IdentityPickerAuthenticationAction(private val config: IdentityPickerAuthenticationActionConfig) :
        AuthenticationAction
{
    companion object
    {
        private val logger: Logger = LoggerFactory.getLogger(IdentityPickerAuthenticationAction::class.java)
    }

    override fun apply(authenticationAttributes: AuthenticationAttributes,
                       authenticatedSessions: AuthenticatedSessions,
                       authenticationTransactionId: String,
                       authenticatorDescriptor: AuthenticatorDescriptor): AuthenticationActionResult
    {

        val sessionManager = config.sessionManager()
        val identity: Attribute? = sessionManager.get(PICKED_IDENTITY_SESSION_KEY)

        if (identity != null)
        {
            sessionManager.remove(PICKED_IDENTITY_SESSION_KEY)
            return pickIdentity(authenticationAttributes, identity.attributeValue)
        }

        val identityList = authenticationAttributes.subjectAttributes[config.identityListAttribute()]?.attributeValue
                as? ListAttributeValue

        logger.debug("Received list of identities: $identityList")

        if (identityList == null)
        {
            logger.info("User with with subject '${authenticationAttributes.subject} did not have a list of " +
                    "identities to chose from")
            return failedResult("No account available")
        }


        if (identityList.size() == 1)
        {
            logger.debug("Choosing only available identity")
            return pickIdentity(authenticationAttributes, identityList.first())
        }

        sessionManager.put(Attribute.of(IDENTITY_LIST_SESSION_KEY, identityList))

        return AuthenticationActionResult.pendingResult(prompt())
    }

    private fun pickIdentity(authenticationAttributes: AuthenticationAttributes,
                             identity: AttributeValue): AuthenticationActionResult
    {
        identity as? MapAttributeValue ?: throw IllegalArgumentException("Identity could not be parsed as a Map")

        val replacedAttributes = AuthenticationAttributes.of(
                SubjectAttributes.of(authenticationAttributes.subject, identity),
                authenticationAttributes.contextAttributes)

        return successfulResult(replacedAttributes)
    }
}
