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

import se.curity.identityserver.sdk.config.Configuration
import se.curity.identityserver.sdk.config.annotation.DefaultString
import se.curity.identityserver.sdk.config.annotation.Description
import se.curity.identityserver.sdk.service.ExceptionFactory
import se.curity.identityserver.sdk.service.Json
import se.curity.identityserver.sdk.service.SessionManager

interface IdentityPickerAuthenticationActionConfig: Configuration
{
    @Description("The attribute containing the list of identities. The attribute is expected to contain a List of " +
            "Maps, containing the attributes to set.")
    @DefaultString("identities")
    fun identityListAttribute(): String

    @Description("The attribute in the returned identity to be displayed to the user. " +
            "The full identity is always returned to the template/haapi-client, so a combination of fields " +
            "can always be created by renderer.")
    @DefaultString("user_id")
    fun displayNameAttribute(): String

    fun sessionManager(): SessionManager

    fun json(): Json

    fun exceptionFactory() : ExceptionFactory
}
