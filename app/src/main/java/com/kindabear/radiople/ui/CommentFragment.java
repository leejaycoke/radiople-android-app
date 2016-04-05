package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.CommentAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.Comment;
import com.kindabear.radiople.response.CommentList;
import com.kindabear.radiople.response.EmptyJson;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentFragment extends BaseFragment {

    private final static String TAG = "CommentFragment";

    private View mView = null;

    private RequestQueue mRequestQueue = null;

    private Broadcast mBroadcast = null;
    private PagingListView mListView = null;
    private CommentAdapter mAdapter = null;

    private com.melnykov.fab.FloatingActionButton mWriteButton = null;

//    private EditText mEditTextContent = null;

    private ArrayList<Comment> mList = null;

    public static CommentFragment newInstance(Broadcast broadcast) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putSerializable("broadcast", broadcast);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.comment_fragment, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        mBroadcast = (Broadcast) getArguments().getSerializable("broadcast");

        mListView = (PagingListView) mView.findViewById(R.id.listview);
        mListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                requestComment(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {

            }

            @Override
            public void onErrorClick(String cursor) {

            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showItemDialog(position);
                return false;
            }
        });

        mList = new ArrayList<Comment>();
        mAdapter = new CommentAdapter(getActivity(), R.layout.comment_list_item, mList);
        mListView.setAdapter(mAdapter);

        mWriteButton = (com.melnykov.fab.FloatingActionButton) mView.findViewById(R.id.button_write);
        if (getUserService().exists()) {
            mWriteButton.attachToListView(mListView);
            mWriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommentWriteDialog();
                }
            });
        } else {
            mWriteButton.setVisibility(View.GONE);
        }

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestComment();
            }
        });

        showLoadingView();

        requestComment();
    }

    private void showItemDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String[] items;

        if (mList.get(position).isDeletable) {
            items = new String[]{getString(R.string.report), getString(R.string.delete)};
        } else {
            items = new String[]{getString(R.string.report)};
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        report(mList.get(position).id);
                        break;
                    default:
                        delete(position);
                        break;
                }
            }
        }).show();
    }

    private void report(int commentId) {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "comment", String.valueOf(commentId), "report").toString();

        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(getActivity(), Request.Method.PUT, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        hideLoadingDialog();
                        showToast(R.string.successfully_reported);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        hideLoadingDialog();
                        showToast(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoadingDialog();
                        showToast(R.string.network_error_message);
                    }
                });

        mRequestQueue.add(request);
    }

    private void delete(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.deletion_warning);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showLoadingDialog();

                int commentId = mList.get(position).id;

                String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "comment", String.valueOf(commentId)).toString();

                GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(getActivity(), Request.Method.DELETE, url, EmptyJson.class,
                        new Response.Listener<EmptyJson>() {
                            @Override
                            public void onResponse(EmptyJson response) {
                                mList.remove(position);
                                mAdapter.notifyDataSetChanged();

                                hideLoadingDialog();

                                if (mList.size() == 0) {
                                    showEmptyView(R.string.not_exists_comment);
                                }
                            }
                        },
                        new CommonResponseListener() {
                            @Override
                            public void onCommonResponse(int statusCode, CommonResponse common) {
                                hideLoadingDialog();
                                showToast(common.displayMessage);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                hideLoadingDialog();
                                showToast(R.string.network_error_message);
                            }
                        });

                mRequestQueue.add(request);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void showCommentWriteDialog() {
        showCommentWriteDialog(null);
    }

    private void showCommentWriteDialog(String prevContent) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.comment_write_view, null);

        final EditText contentEditText = (EditText) view.findViewById(R.id.edittext_content);
        if (prevContent != null) {
            contentEditText.setText(prevContent);
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Dialog)
                .setTitle(R.string.writing_comment)
                .setView(view)
                .setMessage("최대 500자까지 입력할 수 있습니다.")
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = contentEditText.getText().toString();
                        if (!content.trim().isEmpty()) {
                            write(contentEditText.getText().toString());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();
    }

    private void write(final String content) {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "comment").toString();

        GsonRequest request = new GsonRequest<Comment>(getActivity(), Request.Method.POST, url, Comment.class,
                new Response.Listener<Comment>() {
                    @Override
                    public void onResponse(Comment comment) {
                        mList.add(0, comment);
                        mListView.setSelection(0);
                        mAdapter.notifyDataSetChanged();

                        if (mList.size() == 1) {
                            hideHelperViews();
                        }

                        hideLoadingDialog();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showToast(common.displayMessage);
                        hideLoadingDialog();
                        showCommentWriteDialog(content);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast(getString(R.string.network_error_message));
                        hideLoadingDialog();
                        showCommentWriteDialog(content);
                    }
                });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("content", content);
        request.setParams(params);

        mRequestQueue.add(request);
    }


    private void requestComment() {
        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "comment").toString();
        GsonRequest<CommentList> request = new GsonRequest<CommentList>(getActivity(), Request.Method.GET, url, CommentList.class,
                new Response.Listener<CommentList>() {
                    @Override
                    public void onResponse(CommentList response) {
                        if (response.item.size() == 0) {
                            showEmptyView(R.string.not_exists_comment);
                        } else {
                            mList.addAll(response.item);
                            mAdapter.notifyDataSetChanged();
                            mListView.notifyLoadingFinished(response.paging.next);
                            hideHelperViews();
                        }
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showNetworkErrorView(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkErrorView();
                    }
                });

        mRequestQueue.add(request);
    }

    private void requestComment(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "comment").addParam("cursor", cursor).toString();
        GsonRequest<CommentList> request = new GsonRequest<CommentList>(getActivity(), Request.Method.GET, url, CommentList.class,
                new Response.Listener<CommentList>() {
                    @Override
                    public void onResponse(CommentList response) {
                        mList.addAll(response.item);
                        mAdapter.notifyDataSetChanged();
                        mListView.notifyLoadingFinished(response.paging.next);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showNetworkErrorView(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkErrorView();
                    }
                });

        mRequestQueue.add(request);
    }
}
