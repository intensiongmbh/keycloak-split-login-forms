<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))?no_esc}
    <#elseif section = "form">
        <#if realm.password>
            <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="${properties.kcFormGroupClass!}">

                    <div class="hidden">
                        <input tabindex="1" id="emailDummy" class="${properties.kcInputClass!}" value="${msg("userEmail", email)}" name="emailDummy" type="text" autocomplete="off" />
                    </div>
                    
                    <div class="${properties.kcLabelWrapperClass!}">
                        <label for="email" class="${properties.kcLabelClass!}">${msg("password")}</label>
                    </div>
                    
                    <div class="${properties.kcInputWrapperClass!}">
                        <input tabindex="1" id="passwordInput" class="${properties.kcInputClass!}" name="passwordInput"  type="password" placeholder="${msg("enterPassword")}" autofocus autocomplete="off" />
                    </div>
                
                </div>
                
                <div class="${properties.kcFormOptionsWrapperClass!}">
                            <#if realm.resetPasswordAllowed>
                                <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                            </#if>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                    <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    </div>

                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <div class="${properties.kcFormButtonsWrapperClass!}">
                            <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="login" type="submit" value="${msg("doLogIn")}"/>
                        </div>
                    </div>
                </div>
            </form>
        </#if>
    </#if>
</@layout.registrationLayout>