IdentityPicker Authentication Action Plugin
=============================================

A custom, Kotlin-based authentication action plugin for the Curity Identity Server. This plugin expects an attribute in the authentication attributes that contain a list of accounts that the user can select to act as.

The selected identity replaces all the attributes found on the authentication attributes, with the one from the selected account.

Example attributes in subject attributes:

```
identities: [{
        user_id: "user1"
        account_id: "id1",
        subdomain: "sub1",
     },
     {
        user_id: "user2"
        account_id: "id2",
        subdomain: "sub2",
     },
     {
        user_id: "user3"
        account_id: "id3",
        subdomain: "sub3",
}]
```


Customization
~~~~~~~~~~~~~

The default templates list the ``user_id`` field of the identities as a list.

To change what the user sees, copy the [template](https://github.com/curityio/identity-picker/blob/master/src/main/resources/templates/authentication-action/identity-picker/index.vm) into :file:`${IDSVR_HOME}/usr/share/teplates/overrides/authentication-action/identity-picker`.

All attributes in will be available on the ``$_identities`` variable. To list them for dev purposes, add:

```
<pre>
#foreach ($identity in $_identities.values())
    $identity
#end
</pre>
```

Building the Plugin
~~~~~~~~~~~~~~~~~~~

You can build the plugin by issuing the command ``mvn package``. This will produce a JAR file in the ``target`` directory,
which can be installed.

Installing the Plugin
~~~~~~~~~~~~~~~~~~~~~

To install the plugin, copy the compiled JAR (and all of its dependencies) into the :file:`${IDSVR_HOME}/usr/share/plugins/${pluginGroup}`
on each node, including the admin node. For more information about installing plugins, refer to the `curity.io/plugins`_.

Required Dependencies
"""""""""""""""""""""

For a list of the dependencies and their versions, run ``mvn dependency:list``. Ensure that all of these are installed in
the plugin group; otherwise, they will not be accessible to this plug-in and run-time errors will result.

More Information
~~~~~~~~~~~~~~~~

Please visit `curity.io`_ for more information about the Curity Identity Server.

.. _curity.io/plugins: https://support.curity.io/docs/latest/developer-guide/plugins/index.html#plugin-installation
.. _curity.io: https://curity.io/
