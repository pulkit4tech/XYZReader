package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.PaletteTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    private boolean mIsCard = false;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.article_body)
    TextView tvArticleBody;

    @Bind(R.id.article_byline)
    TextView tvArticleBy;

    @Bind(R.id.ivPhoto)
    ImageView ivPhoto;

    private Typeface myTypeface;
    private String shareText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "Rosario-Regular.ttf");

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(shareText!=null?shareText:"Checkout the article")
                        .getIntent(), getString(R.string.action_share)));
            }
        });



        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        tvArticleBody.setTypeface(myTypeface);
        tvArticleBy.setTypeface(myTypeface);

    }
    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null) {
            mRootView.setVisibility(View.VISIBLE);
            tvArticleBy.setMovementMethod(new LinkMovementMethod());
            collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            tvArticleBy.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#00796B'><b>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</b></font>"));
            tvArticleBody.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            //forming the share text
            shareText = "Check out the article on \""+ mCursor.getString(ArticleLoader.Query.TITLE)+"\" by "+ mCursor.getString(ArticleLoader.Query.AUTHOR);

            Picasso.with(getActivity())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .placeholder(R.drawable.article_icon)
                    .transform(new PaletteTransformation())
                    .into(ivPhoto, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            // TODO I can haz Palette?
                            if(ivPhoto!=null){
                                Bitmap bitmap = ((BitmapDrawable) ivPhoto.getDrawable()).getBitmap(); // Ew!
                                Palette palette = PaletteTransformation.getPalette(bitmap);
                                if (palette != null) {
                                    applyPalleteToWindow(palette);
                                }

                            }

                        }
                    });

        } else {
            mRootView.setVisibility(View.GONE);
            collapsingToolbarLayout.setTitle("N/A");
            tvArticleBy.setText("N/A" );
            tvArticleBody.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        if(isAdded())
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        /*if(isAdded())
        bindViews();*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.unbind(this);
    }

    //Apply toolbar status and navigation color from palatte
    private void applyPalleteToWindow(Palette palette) {


        //Default colors for window
        int colorPrimary = getResources().getColor(R.color.colorPrimary);
        int colorPrimaryDark = getResources().getColor(R.color.colorPrimaryDark);


        if (palette.getDarkMutedSwatch() != null) {

            colorPrimaryDark = palette.getDarkMutedSwatch().getRgb();
            colorPrimary = colorPrimaryDark;

            float[] hsv = new float[3];
            Color.colorToHSV(colorPrimaryDark, hsv);
            hsv[2] *= 1.5f;
            colorPrimary = Color.HSVToColor(hsv);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(colorPrimaryDark);
            window.setNavigationBarColor(colorPrimaryDark);

            collapsingToolbarLayout.setContentScrimColor(colorPrimary);


        }

    }

}
