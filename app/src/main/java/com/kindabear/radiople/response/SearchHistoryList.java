package com.kindabear.radiople.response;

import java.util.ArrayList;

public class SearchHistoryList extends PagingResponse {

    public ArrayList<SearchHistory> item = new ArrayList<SearchHistory>();

    public String keyword = null;
}
