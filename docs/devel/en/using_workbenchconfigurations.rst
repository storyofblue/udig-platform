Using Workbenchconfigurations
#############################

This is the second attempt at "reusing the existing uDig workbench but let me change some stuff
story". In general the use of these hacks is discouraged please make your own Application following
RCP conventions.

The extentions point here is **org.locationtech.udig.ui.workbenchConfigurations**:

::

    <extension
              point="org.locationtech.udig.ui.workbenchConfigurations">
           <workbenchConfiguration
                 class="org.locationtech.udig.internal.ui.UDIGWorkbenchConfiguration"
                 id="org.locationtech.udig.internal.ui.UDIGWorkbenchConfiguration"/>
        </extension>

By implementing this extention in your own application you may specify an instance of
``WorkbenchConfiguration``. If you do not supply a configuration the default
UDIGWorkbenchConfiguration will be used.

The UDIGWorkbenchWindowAdvisor delegates to this code as part of **preWindowOpen** it is used to
perform the following:

-  showCoolBar = true
-  showStatusLine = true
-  showFastviewBars = true

