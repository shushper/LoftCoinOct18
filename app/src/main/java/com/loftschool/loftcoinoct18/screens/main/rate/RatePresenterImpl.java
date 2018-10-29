package com.loftschool.loftcoinoct18.screens.main.rate;

import android.support.annotation.Nullable;
import android.util.Log;

import com.loftschool.loftcoinoct18.data.api.Api;
import com.loftschool.loftcoinoct18.data.db.Database;
import com.loftschool.loftcoinoct18.data.db.model.CoinEntityMapper;
import com.loftschool.loftcoinoct18.data.model.Fiat;
import com.loftschool.loftcoinoct18.data.prefs.Prefs;
import com.loftschool.loftcoinoct18.job.JobHelper;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RatePresenterImpl implements RatePresenter {

    private static final String TAG = "RatePresenterImpl";

    private Api api;
    private Prefs prefs;
    private Database mainDatabase;
    private Database workerDatabase;
    private CoinEntityMapper mapper;
    private JobHelper jobHelper;

    private CompositeDisposable disposables = new CompositeDisposable();


    @Nullable
    private RateView view;

    public RatePresenterImpl(Api api,
                             Prefs prefs,
                             Database mainDatabase,
                             Database workerDatabase,
                             CoinEntityMapper mapper,
                             JobHelper jobHelper) {
        this.api = api;
        this.prefs = prefs;
        this.mainDatabase = mainDatabase;
        this.workerDatabase = workerDatabase;
        this.mapper = mapper;
        this.jobHelper = jobHelper;
    }


    @Override
    public void attachView(RateView view) {
        this.view = view;
        mainDatabase.open();
    }

    @Override
    public void detachView() {
        mainDatabase.close();
        disposables.dispose();
        this.view = null;
    }

    @Override
    public void getRate() {
        Disposable disposable = mainDatabase.getCoins()
                .subscribe(
                        coinEntities -> {
                            if (view != null) {
                                view.setCoins(coinEntities);
                            }
                        },

                        throwable -> {

                        }
                );

        disposables.add(disposable);
    }

    private void loadRate(Boolean fromRefresh) {

        if (!fromRefresh) {
            if (view != null) {
                view.showProgress();
            }
        }

        Disposable disposable = api.ticker("array", prefs.getFiatCurrency().name())
                .subscribeOn(Schedulers.io())
                .map(rateResponse -> mapper.mapCoins(rateResponse.data))
                .map(coinEntities -> {
                    workerDatabase.open();
                    workerDatabase.saveCoins(coinEntities);
                    workerDatabase.close();
                    return new Object();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object -> {
                            if (view != null) {
                                if (fromRefresh) {
                                    view.setRefreshing(false);
                                } else {
                                    view.hideProgress();
                                }

                            }
                        },

                        throwable -> {
                            Log.e(TAG, "Failed to load rate", throwable);
                            if (fromRefresh) {
                                view.setRefreshing(false);
                            } else {
                                view.hideProgress();
                            }
                        }
                );

        disposables.add(disposable);
    }


    @Override
    public void onRefresh() {
        loadRate(true);
    }

    @Override
    public void onMenuItemCurrencyClick() {
        view.showCurrencyDialog();
    }

    @Override
    public void onFiatCurrencySelected(Fiat currency) {
        prefs.setFiatCurrency(currency);
        loadRate(false);
    }

    @Override
    public void onRateLongClick(String symbol) {
        jobHelper.startSyncRateJob(symbol);
    }
}
