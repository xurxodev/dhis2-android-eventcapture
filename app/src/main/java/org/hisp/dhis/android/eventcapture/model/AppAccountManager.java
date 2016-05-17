package org.hisp.dhis.android.eventcapture.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.ui.AppPreferences;

/**
 * A singleton class to abstract/wrap and simplify interactions with Account in relation to synchronizing.
 */
public class AppAccountManager {
    private static final String TAG = AppAccountManager.class.getSimpleName();

    //private static AppAccountManager instance;
    public static final String AUTHORITY = "org.hisp.dhis.android.eventcapture.model.provider";
    public static final String ACCOUNT_TYPE = "org.hisp.dhis.android.eventcapture";
    public static String accountName = "default dhis2 account";

    private Account mAccount;
    private Context appContext;
    private AppPreferences appPreferences;

    public AppAccountManager(Context context, AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        this.appContext = context;
        accountName = D2.me().userCredentials().toBlocking().first().getUsername();
        initialize(context);
    }

    public void initialize(Context context) {
        createAccount(context);
    }

    public void createAccount(Context context) {
        appContext = context;
        mAccount = createAccount();
        initSyncAccount();
    }

    /*
    * Account removal stub functionality.
    * Requires api 22.
    * */
    public void removeAccount() {
        if (mAccount != null && appContext != null) {
            AccountManager accountManager =
                    (AccountManager) appContext.getSystemService(Context.ACCOUNT_SERVICE);
            accountManager.removeAccountExplicitly(mAccount);
        }
    }

    public Account createAccount() {
        // Create the account type and default account
        Account newAccount = new Account(accountName, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) appContext.getSystemService(Context.ACCOUNT_SERVICE);

        Boolean doesntExist = accountManager.addAccountExplicitly(newAccount, null, null);
        if (doesntExist) {
            mAccount = newAccount;
            return newAccount;
        } else {
            /* The account exists or some other error occurred. Find the account: */
            Account all[] = accountManager.getAccountsByType(ACCOUNT_TYPE);
            for (Account found : all) {
                if (found.equals(newAccount)) {
                    mAccount = newAccount;
                    return found;
                }
            }
        }
        return null; //Error
    }

    public void initSyncAccount() {
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);

        if (appPreferences.getBackgroundSyncState()) {
            long minutes = (long) appPreferences.getBackgroundSyncFrequency();
            long seconds = minutes * 60;
            ContentResolver.addPeriodicSync(
                    mAccount,
                    AUTHORITY,
                    Bundle.EMPTY,
                    seconds);
        }
    }

    public void removePeriodicSync() {
        ContentResolver.removePeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY);
    }

    public void setPeriodicSync(int minutes) {
        Long seconds = ((long) minutes) * 60;
        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                seconds);
    }

    public void syncNow() {
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        /*
         * Request the syncMetaData for the default account, authority, and
         * manual syncMetaData settings
         */
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
    }
}
