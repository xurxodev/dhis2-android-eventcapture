/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture;

import android.content.Context;

import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenterImpl;
import org.hisp.dhis.client.sdk.core.D2;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractorImpl;
import org.hisp.dhis.client.sdk.core.option.OptionSetInteractor;
import org.hisp.dhis.client.sdk.core.option.OptionSetInteractorImpl;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractorImpl;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityDataValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityDataValueInteractorImpl;
import org.hisp.dhis.client.sdk.core.user.UserInteractor;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultAppAccountManager;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultAppAccountManagerImpl;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultNotificationHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultNotificationHandlerImpl;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultUserModule;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LauncherPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LauncherPresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.ProfilePresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.ProfilePresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.SettingsPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.SettingsPresenterImpl;
import org.hisp.dhis.client.sdk.utils.Logger;

import dagger.Module;
import dagger.Provides;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

@Module
public class UserModule implements DefaultUserModule {
    private final String authority;
    private final String accountType;

    public UserModule(String authority, String accountType) {
        this(null, authority, accountType);
    }

    public UserModule(String serverUrl, String authority, String accountType) {
        this.authority = authority;
        this.accountType = accountType;

        if (!isEmpty(serverUrl)) {
            // it can throw exception in case if configuration has failed
            D2.configure(serverUrl);
        }
    }

    @Provides
    @PerUser
    @Override
    public UserInteractor providesCurrentUserInteractor() {
        return D2.me();
    }

    @Provides
    @PerUser
    @Override
    public LauncherPresenter providesLauncherPresenter(UserInteractor currentUserInteractor) {
        return new LauncherPresenterImpl(currentUserInteractor);
    }

    @Provides
    @PerUser
    @Override
    public LoginPresenter providesLoginPresenter(
            UserInteractor userInteractor, ApiExceptionHandler apiExceptionHandler, Logger logger) {
        return new LoginPresenterImpl(userInteractor, apiExceptionHandler, logger);
    }

    @Provides
    @PerUser
    @Override
    public HomePresenter providesHomePresenter(
            UserInteractor currentUserInteractor, SyncDateWrapper syncDateWrapper, Logger logger) {
        return new HomePresenterImpl(currentUserInteractor, syncDateWrapper, logger);
    }

    @Provides
    @PerUser
    @Override
    public ProfilePresenter providesProfilePresenter(UserInteractor userInteractor,
                                                     SyncDateWrapper syncDateWrapper,
                                                     DefaultAppAccountManager appAccountManager,
                                                     DefaultNotificationHandler defaultNotificationHandler,
                                                     Logger logger) {
        return new ProfilePresenterImpl(userInteractor, syncDateWrapper,
                appAccountManager, defaultNotificationHandler, logger);
    }

    @Provides
    @PerUser
    @Override
    public SettingsPresenter providesSettingsPresenter(AppPreferences appPreferences, DefaultAppAccountManager appAccountManager) {
        return new SettingsPresenterImpl(appPreferences, appAccountManager);
    }

    @Provides
    @PerUser
    @Override
    public DefaultAppAccountManager providesAppAccountManager(Context context, AppPreferences appPreferences, UserInteractor currentUserInteractor, Logger logger) {
        return new DefaultAppAccountManagerImpl(context, appPreferences, currentUserInteractor, authority, accountType, logger);
    }

    @Provides
    @PerUser
    @Override
    public DefaultNotificationHandler providesNotificationHandler(Context context) {
        return new DefaultNotificationHandlerImpl(context);
    }

    @Provides
    @PerUser
    public SelectorPresenter providesSelectorPresenter(Context context) {
        return new SelectorPresenterImpl(null, null, null, null, null, null, null, null);
    }

    @Provides
    @PerUser
    public ProgramInteractor providesProgramInteractor() {
        return new ProgramInteractorImpl(null, null, null);
    }

    @Provides
    @PerUser
    public EventInteractor providesEventInteractor() {
        return new EventInteractorImpl(null, null);
    }

    @Provides
    @PerUser
    public OptionSetInteractor providesOptionSetInteractor() {
        return new OptionSetInteractorImpl(null, null);
    }

    @Provides
    @PerUser
    public TrackedEntityDataValueInteractor providesTrackedEntityDataValueInteractor() {
        return new TrackedEntityDataValueInteractorImpl(null);
    }
}
