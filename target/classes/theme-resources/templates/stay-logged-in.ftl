<style type="text/css">
    b  { font-size: 30px; }
    h2 { font-size: 50px; }
    .button { display: inline-block; color: #fff; background: #06c; border: solid #06c; border-width: 3px 23px; font-weight: bold; font-size: 24px; border-radius: 0px; }
    .button:hover { background: #004080; border: solid #004080; border-width: 3px 23px; }
</style>
<#import "template.ftl" as layout>

<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))?no_esc}
    <#elseif section = "form">
        <#if realm.password>
            <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = false; return true;" action="${url.loginAction}" method="post">
                
                <b>${msg("stayLoggedInHeader")}</b>
                
                <h2>${msg("stayLoggedInDescription")}</h2>
                
                <div class="${properties.kcFormGroupClass!}">
                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <div class="${properties.kcFormButtonsWrapperClass!}">
                            <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="rememberMeNo" id="rememberMeNo" type="submit" value="${msg("no")}"/>
                            <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="rememberMeYes" id="rememberMeYes" type="submit" value="${msg("yes")}"/>
                        </div>
                    </div>
                </div>
            </form>
        </#if>
    </#if>
</@layout.registrationLayout>