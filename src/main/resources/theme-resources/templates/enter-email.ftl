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
                    <div class="${properties.kcLabelWrapperClass!}">
                        <label for="email" class="${properties.kcLabelClass!}">${msg("identificator")}</label>
                    </div>

                    <div class="${properties.kcInputWrapperClass!}">
                        <input tabindex="1" id="identificatorInput" class="${properties.kcInputClass!}" name="identificatorInput"  type="text" placeholder="${msg("enterIdentificator")}" autofocus autocomplete="on" />
                    </div>
                    
                    <div class="hidden">
                        <input tabindex="1" id="passwordDummy" class="${properties.kcInputClass!}" name="passwordDummy" type="password" autofocus autocomplete="off" />
                    </div>

                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <div class="${properties.kcFormButtonsWrapperClass!}">
                            <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="next" id="next" type="submit" value="${msg("next")}"}"/>
                        </div>
                     </div>
                </div>
                <#if realm.registrationAllowed??>
                     <div class="card-footer font-weight-light font-size-smaller">
                         <span>${msg("noAccount")} <a href="${url.registrationUrl}" class="font-weight-bold">${msg("doRegister")}</a></span>
                     </div>
                </#if>
            </form>
        </#if>
    </#if>
</@layout.registrationLayout>