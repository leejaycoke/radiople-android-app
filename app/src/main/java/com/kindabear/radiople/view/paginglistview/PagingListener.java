package com.kindabear.radiople.view.paginglistview;

public interface PagingListener {

    public void onNextPage(String cursor);

    public void onMoreClick(String cursor);

    public void onErrorClick(String cursor);
}
