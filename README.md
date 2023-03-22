# IdentityPicker Authentication Action Plugin

[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)


A custom, Kotlin-based authentication action plugin for the Curity Identity Server. This plugin expects an attribute in the authentication attributes that contain a list of accounts that the user can select to act as.

The selected identity replaces all the attributes found on the authentication attributes, with the one from the selected account.

Example attributes in subject attributes:

```javascript
identities: [{
        user_id: "user1",
        account_id: "id1",
        subdomain: "sub1"
     },
     {
        user_id: "user2",
        account_id: "id2",
        subdomain: "sub2"
     },
     {
        user_id: "user3",
        account_id: "id3",
        subdomain: "sub3"
}]
```

If a single identity is found in the list, it will be used automatically without asking the user.

## Configuration

The plugin has two optional configuration settings
* `display-name-attribute` - The attribute to display for the user as the identifier of the account. I.e. configuring `user_id` would display three buttons for the user with the text `user1`, `user2` and `user3`. If a combination of attributes is wanted, this can be accomplished by combining them in the template, or by the HAAPI client.
* `identity-list-attribute` - The attribute where the list of identities are found in the subject attributes. 

## Testing

The repository has a [test config](src/test/resources/config/test-config.xml), that sets up a Username authenticator with actions setting up the example list of identities to easily deploy for testing.   


## HAAPI

The HAAPI representation of the selection screen will give the full identity object as form properties, so that the client may choose to alter the way the choices are visualized, same way the templates allow for.

```JSON
{
  "metadata": {
    "viewName": "authentication-action/identity-picker/index"
  },
  "type": "authentication-step",
  "actions": [
    {
      "template": "selector",
      "kind": "authenticator-selector",
      "title": "Your username is associated with the following accounts. To continue login, select one of the accounts listed below.",
      "model": {
        "options": [
          {
            "template": "form",
            "kind": "select-account",
            "title": "user1",
            "properties": {
              "user_id": "user1",
              "account_id": "id1",
              "subdomain": "sub1"
            },
            "model": {
              "href": "/dev/authn/authenticate/_action/pick-identity",
              "method": "POST",
              "fields": [
                {
                  "name": "identity-index",
                  "type": "hidden",
                  "value": "0"
                }
              ]
            }
          },
          {
            "template": "form",
            "kind": "select-account",
            "title": "user2",
            "properties": {
              "user_id": "user2",
              "account_id": "id2",
              "subdomain": "sub2"
            },
            "model": {
              "href": "/dev/authn/authenticate/_action/pick-identity",
              "method": "POST",
              "fields": [
                {
                  "name": "identity-index",
                  "type": "hidden",
                  "value": "1"
                }
              ]
            }
          },
          {
            "template": "form",
            "kind": "select-account",
            "title": "user3",
            "properties": {
              "user_id": "user3",
              "account_id": "id3",
              "subdomain": "sub3"
            },
            "model": {
              "href": "/dev/authn/authenticate/_action/pick-identity",
              "method": "POST",
              "fields": [
                {
                  "name": "identity-index",
                  "type": "hidden",
                  "value": "2"
                }
              ]
            }
          }
        ]
      }
    }
  ]
}
```
## Template customization

The default templates list the `user_id` field of the identities as a list.

To change what the user sees, copy the [template](https://github.com/curityio/identity-picker/blob/master/src/main/resources/templates/authentication-action/identity-picker/index.vm) into `${IDSVR_HOME}/usr/share/teplates/overrides/authentication-action/identity-picker`.

All attributes in will be available on the `$_identities` variable. To list them for dev purposes, add:

```html
<pre>
#foreach ($identity in $_identities.values())
    $identity
#end
</pre>
```

## Building the Plugin

You can build the plugin by issuing the command `mvn package`. This will produce a JAR file in the `target` directory,
which can be installed.

## Installing the Plugin

To install the plugin, copy the compiled JAR (and all of its dependencies) into the `${IDSVR_HOME}/usr/share/plugins/${pluginGroup}`
on each node, including the admin node. For more information about installing plugins, refer to the [curity.io/plugins][https://support.curity.io/docs/latest/developer-guide/plugins/index.html#plugin-installation].

## Required Dependencies

For a list of the dependencies and their versions, run `mvn dependency:list`. Ensure that all of these are installed in
the plugin group; otherwise, they will not be accessible to this plug-in and run-time errors will result.

## More Information

Please visit [curity.io] for more information about the Curity Identity Server.
