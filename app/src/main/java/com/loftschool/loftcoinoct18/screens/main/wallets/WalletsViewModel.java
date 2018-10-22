package com.loftschool.loftcoinoct18.screens.main.wallets;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.loftschool.loftcoinoct18.data.db.model.CoinEntity;
import com.loftschool.loftcoinoct18.data.db.model.TransactionModel;
import com.loftschool.loftcoinoct18.data.db.model.WalletModel;

import java.util.List;

public abstract class WalletsViewModel extends AndroidViewModel {

    public WalletsViewModel(@NonNull Application application) {
        super(application);
    }


    public abstract void getWallets();

    public abstract void onNewWalletClick();

    public abstract void onCurrencySelected(CoinEntity coin);

    public abstract void onWalletChanged(int position);


    public abstract LiveData<List<WalletModel>> wallets();

    public abstract LiveData<List<TransactionModel>> transactions();


    public abstract LiveData<Boolean> walletsVisible();

    public abstract LiveData<Boolean> newWalletVisible();

    public abstract LiveData<Object> selectCurrency();


}
