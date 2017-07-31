package com.theonepiano.blemidi.bluetooth.base;

/**
 * Created by jim on 2017/6/11.
 */

public class BasePresenter<V extends BaseView, M extends BaseModel> implements Presenter<V, M> {
    protected V mView;

    protected M mModel;

    @Override
    public void attachView(V view) {
        mView = view;
    }

    @Override
    public void attachModel(M model) {
        mModel = model;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void detachModel() {
        mModel = null;
    }

    public M getModel() {
        return mModel;
    }

    public V getView() {
        return mView;
    }

    public boolean isViewBind() {
        return mView != null;
    }
}

