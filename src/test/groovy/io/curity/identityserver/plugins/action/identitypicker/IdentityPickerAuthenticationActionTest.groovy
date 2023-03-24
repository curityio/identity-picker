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

import se.curity.identityserver.sdk.attribute.Attribute
import se.curity.identityserver.sdk.attribute.AttributeValue
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes
import se.curity.identityserver.sdk.attribute.ContextAttributes
import se.curity.identityserver.sdk.attribute.SubjectAttributes
import se.curity.identityserver.sdk.authenticationaction.AuthenticationActionContext
import se.curity.identityserver.sdk.authenticationaction.AuthenticationActionResult
import se.curity.identityserver.sdk.service.SessionManager
import spock.lang.Specification

class IdentityPickerAuthenticationActionTest extends Specification {

    public static final String IDENTITIES_ATTRIBUTE_NAME = "identities"

    def "More than one identity returns a prompt for selection"() {
        given: "An attribute containing some accounts"
        def identityList = [[user_id   : "user1",
                             account_id: "id1",
                             subdomain : "sub1"
                            ],
                            [user_id   : "user2",
                             account_id: "id2",
                             subdomain : "sub2"
                            ],
                            [user_id   : "user3",
                             account_id: "id3",
                             subdomain : "sub3"
                            ]]
        def subjectAttributes = [subject: "teddie", identities: identityList]
        def authnAttributes = AuthenticationAttributes.of(SubjectAttributes.of(subjectAttributes),
                ContextAttributes.empty())

        and: "Mocks setup to return the identities"
        def config = Mock(IdentityPickerAuthenticationActionConfig)
        config.identityListAttribute() >> IDENTITIES_ATTRIBUTE_NAME

        def sessionManager = Mock(SessionManager)
        config.sessionManager() >> sessionManager

        def action = new IdentityPickerAuthenticationAction(config)

        when: "The action is invoked"
        def authenticationResult = action.apply(getActionContext(authnAttributes))

        then:
        authenticationResult instanceof AuthenticationActionResult.PendingCompletionAuthenticationActionResult
    }

    def "One identity skips selection"() {
        given: "An attribute containing some accounts"
        def identityList = [[user_id   : "user1",
                             account_id: "id1",
                             subdomain : "sub1"
                            ]]
        def subjectAttributes = [subject: "teddie", identities: identityList]
        def authnAttributes = AuthenticationAttributes.of(SubjectAttributes.of(subjectAttributes),
                ContextAttributes.empty())

        and: "Mocks setup to return the identities"
        def config = Mock(IdentityPickerAuthenticationActionConfig)
        config.identityListAttribute() >> IDENTITIES_ATTRIBUTE_NAME

        def sessionManager = Mock(SessionManager)
        config.sessionManager() >> sessionManager

        def action = new IdentityPickerAuthenticationAction(config)

        when: "The action is invoked"
        def authenticationResult = action.apply(getActionContext(authnAttributes))

        then:
        authenticationResult instanceof AuthenticationActionResult.SuccessAuthenticationActionResult
        def authnAttrs = (authenticationResult as AuthenticationActionResult.SuccessAuthenticationActionResult).authenticationAttributes

        authnAttrs.subject == subjectAttributes.subject
        authnAttrs.subjectAttributes.user_id.value == identityList[0].user_id
        authnAttrs.subjectAttributes.subdomain.value == identityList[0].subdomain
        authnAttrs.subjectAttributes.account_id.value == identityList[0].account_id
        authnAttrs.subjectAttributes.size() == 4
    }

    def "When an identity was selected, the attributes are replaced"() {
        given: "An attribute containing some accounts"
        def identityList = [[user_id   : "user1",
                             account_id: "id1",
                             subdomain : "sub1"
                            ],
                            [user_id   : "user2",
                             account_id: "id2",
                             subdomain : "sub2"
                            ],
                            [user_id   : "user3",
                             account_id: "id3",
                             subdomain : "sub3"
                            ]]
        def subjectAttributes = [subject: "teddie", identities: identityList]
        def authnAttributes = AuthenticationAttributes.of(SubjectAttributes.of(subjectAttributes),
                ContextAttributes.empty())

        and: "Mocks setup to return the a selected identity, emulating that the user picked one"
        def config = Mock(IdentityPickerAuthenticationActionConfig)
        config.identityListAttribute() >> IDENTITIES_ATTRIBUTE_NAME

        def sessionManager = Mock(SessionManager)
        config.sessionManager() >> sessionManager
        def pickedIndex = 1
        sessionManager.get(_ as String) >> Attribute.of("identity-picker-picked_identity",
                AttributeValue.of(identityList[pickedIndex]))

        def action = new IdentityPickerAuthenticationAction(config)

        when: "The action is invoked"
        def authenticationResult = action.apply(getActionContext(authnAttributes))

        then:
        authenticationResult instanceof AuthenticationActionResult.SuccessAuthenticationActionResult
        def authnAttrs = (authenticationResult as AuthenticationActionResult.SuccessAuthenticationActionResult).authenticationAttributes

        authnAttrs.subject == subjectAttributes.subject
        authnAttrs.subjectAttributes.user_id.value == identityList[pickedIndex].user_id
        authnAttrs.subjectAttributes.subdomain.value == identityList[pickedIndex].subdomain
        authnAttrs.subjectAttributes.account_id.value == identityList[pickedIndex].account_id
        authnAttrs.subjectAttributes.size() == 4
    }

    def "When no identities are found, authentication is failed"() {
        given: "Subject attributes not containing identities"
        def authnAttributes = AuthenticationAttributes.of(SubjectAttributes.of("teddie"),
                ContextAttributes.empty())
        and: "Mocks are setup"
        def config = Mock(IdentityPickerAuthenticationActionConfig)
        config.identityListAttribute() >> IDENTITIES_ATTRIBUTE_NAME

        def sessionManager = Mock(SessionManager)
        config.sessionManager() >> sessionManager

        def action = new IdentityPickerAuthenticationAction(config)

        when: "The action is invoked"
        def authenticationResult = action.apply(getActionContext(authnAttributes))

        then: "A failed result is received"
        authenticationResult instanceof AuthenticationActionResult.FailedAuthenticationActionResult
    }

    AuthenticationActionContext getActionContext(AuthenticationAttributes authnAttributes) {
        def context = Mock(AuthenticationActionContext)
        context.getAuthenticationAttributes() >> authnAttributes

        return context
    }
}
