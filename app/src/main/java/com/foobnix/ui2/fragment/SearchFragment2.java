package com.foobnix.ui2.fragment;

import static com.foobnix.pdf.info.AppsConfig.SEARCH_FRAGMENT_WORKER_NAME;
import static com.foobnix.pdf.info.AppsConfig.WORKER_POLICY;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.ColorUtils;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.foobnix.LibreraApp;
import com.foobnix.android.utils.Apps;
import com.foobnix.android.utils.BaseItemLayoutAdapter;
import com.foobnix.android.utils.Dips;
import com.foobnix.android.utils.Keyboards;
import com.foobnix.android.utils.LOG;
import com.foobnix.android.utils.ResultResponse;
import com.foobnix.android.utils.StringDB;
import com.foobnix.android.utils.TxtUtils;
import com.foobnix.android.utils.Views;
import com.foobnix.dao2.FileMeta;
import com.foobnix.model.AppProfile;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.Android6;
import com.foobnix.pdf.info.AppsConfig;
import com.foobnix.pdf.info.ExtUtils;
import com.foobnix.pdf.info.model.BookCSS;
import com.foobnix.pdf.info.IMG;
import com.foobnix.pdf.info.Prefs;
import android.util.TypedValue;
import com.foobnix.pdf.SlidingTabLayout;
import com.foobnix.pdf.info.R;
import com.foobnix.pdf.info.TintUtil;
import com.foobnix.pdf.info.view.EditTextHelper;
import com.foobnix.pdf.info.view.KeyCodeDialog;
import com.foobnix.pdf.info.view.MyPopupMenu;
import com.foobnix.pdf.info.widget.DialogTranslateFromTo;
import com.foobnix.pdf.info.wrapper.PopupHelper;
import com.foobnix.pdf.search.activity.msg.MessageSyncFinish;
import com.foobnix.pdf.search.activity.msg.NotifyAllFragments;
import com.foobnix.pdf.search.activity.msg.OpenTagMessage;
import com.foobnix.pdf.search.activity.msg.SearchMetaMsg;
import com.foobnix.sys.TempHolder;
import com.foobnix.ui2.AppDB;
import com.foobnix.ui2.AppDB.SEARCH_IN;
import com.foobnix.ui2.MainTabs2;
import com.foobnix.ui2.AppDB.SORT_BY;
import com.foobnix.ui2.BooksService;
import com.foobnix.ui2.adapter.AuthorsAdapter2;
import com.foobnix.ui2.adapter.FileMetaAdapter;
import com.foobnix.work.CheckDeletedBooksWorker;
import com.foobnix.work.SearchAllBooksWorker;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

public class SearchFragment2 extends UIFragment<FileMeta> {

    public static final String EMPTY_ID = "\u00A0";
    public static final Pair<Integer, Integer> PAIR =
            new Pair<Integer, Integer>(R.string.library, R.drawable.glyphicons_589_book_open);
    private static final String CMD_KEYCODE = "@@keycode_config";
    private static final String CMD_EDIT_AUTO_COMPLETE = "@@edit_autocomple";
    private static final String CMD_MARGIN = "@@keycode_margin";

    public static int NONE = -1;
    public static List<FileMeta> cacheItems;
    final Set<String> autocomplitions = new HashSet<String>();
    public int prevLibModeAuthors = NONE;
    public int rememberPos = 0;
    FileMetaAdapter searchAdapter;
    AuthorsAdapter2 authorsAdapter;
    TextView countBooks, layoutErrorOnRestart;
    Handler handler;
    ImageView sortBy, sortOrder, myAutoCompleteImage, cleanFilter, menu2;
    View secondTopPanel, layoutError, emptyLibraryState, layoutOnGrant;
    TextView emptyLibraryAddFolder, emptyLibraryRescan;
    /**
     * Whether the library can be rebuilt right now. The screen used to read this off the
     * refresh button's own state; the button is gone - the panel's Update does the rebuild -
     * so the state is kept here.
     */
    boolean isLibraryReady = true;
    AutoCompleteTextView searchEditText;
    int countTitles = 0;
    private View libraryHeader, continueSection;
    private int libraryFilter;
    private int libraryTotal, libraryReading;
    private FileMeta continueBook;

    private android.graphics.drawable.GradientDrawable libraryBackground(int attr, int radius) {
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(com.foobnix.ui2.ThemeManager.color(requireContext(), attr));
        background.setCornerRadius(Dips.dpToPx(radius));
        if (com.foobnix.ui2.ThemeManager.isEink()
                || (attr != R.attr.appFeaturedContainer && attr != R.attr.appPrimary)) {
            background.setStroke(Dips.dpToPx(1), com.foobnix.ui2.ThemeManager.color(requireContext(), R.attr.appOutline));
        }
        return background;
    }

    private void bindLibraryHeader() {
        ((TextView) libraryHeader.findViewById(R.id.librarySummary)).setText(
                getString(R.string.library_home_summary, libraryTotal, libraryReading));
        boolean browsing = TxtUtils.isEmpty(searchEditText.getText().toString()) && libraryFilter == 0;
        continueSection.setVisibility(browsing && continueBook != null ? View.VISIBLE : View.GONE);
        int[] chips = {R.id.libraryFilterAll, R.id.libraryFilterReading, R.id.libraryFilterFinished};
        for (int i = 0; i < chips.length; i++) {
            TextView chip = libraryHeader.findViewById(chips[i]);
            chip.setSelected(i == libraryFilter);
            chip.setBackground(libraryBackground(i == libraryFilter ? R.attr.appSelectedContainer : R.attr.appSurface, 24));
        }
        if (continueBook == null) return;
        final FileMeta book = continueBook;
        ((TextView) libraryHeader.findViewById(R.id.libraryContinueTitle)).setText(
                TxtUtils.isEmpty(book.getTitle()) ? new java.io.File(book.getPath()).getName() : book.getTitle());
        ((TextView) libraryHeader.findViewById(R.id.libraryContinueAuthor)).setText(book.getAuthor());
        float progress = book.getIsRecentProgress() == null ? 0 : book.getIsRecentProgress();
        ((TextView) libraryHeader.findViewById(R.id.libraryContinueProgress)).setText(
                getString(R.string.library_saved_progress, Math.round(progress * 100)));
        TintUtil.setProgressLine(libraryHeader.findViewById(R.id.libraryContinueProgressBar),
                com.foobnix.ui2.ThemeManager.color(requireContext(), R.attr.appSurfaceVariant),
                com.foobnix.ui2.ThemeManager.color(requireContext(), R.attr.appSuccess), progress);
        IMG.getCoverPageWithEffect(getActivity(), book.getPath(), null)
                .into((ImageView) libraryHeader.findViewById(R.id.libraryContinueCover));
        libraryHeader.findViewById(R.id.libraryContinueCard).setBackground(libraryBackground(R.attr.appFeaturedContainer, 24));
        libraryHeader.findViewById(R.id.libraryContinueButton).setBackground(libraryBackground(R.attr.appPrimary, 16));
        libraryHeader.findViewById(R.id.libraryContinueButton).setOnClickListener(v ->
                com.foobnix.ui2.adapter.DefaultListeners.getOnItemClickListener(getActivity()).onResultRecive(book));
    }

    @Override public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("libraryFilter", libraryFilter);
    }
    Runnable hideKeyboard = new Runnable() {

        @Override public void run() {
            Keyboards.close(searchEditText);
            Keyboards.hideNavigation(getActivity());
        }
    };
    Runnable saveAutoComplete = new Runnable() {

        @Override public void run() {
            String txt = searchEditText.getText()
                                       .toString()
                                       .trim();
            if (TxtUtils.isNotEmpty(txt) && !txt.startsWith("@@") && !StringDB.contains(AppState.get().myAutoCompleteDb,
                    txt)) {
                if (!searchAdapter.getItemsList()
                                  .isEmpty()) {
                    StringDB.add(AppState.get().myAutoCompleteDb, txt, (db) -> AppState.get().myAutoCompleteDb = db);
                    autocomplitions.add(txt);
                    updateFilterListAdapter();
                    myAutoCompleteImage.setVisibility(View.VISIBLE);
                }

            }

        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override public void onReceive(Context context, Intent intent) {

            if (BooksService.RESULT_SEARCH_FINISH.equals(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                searchAndOrderAsync();
                setSearchHint(R.string.library);
                isLibraryReady = true;
                updateEmptyLibraryState();

                if (AppsConfig.IS_LOG) {
                    setSearchHint(Apps.getApplicationName(getContext()));
                }

            } else if (BooksService.RESULT_SEARCH_COUNT.equals(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                int count = intent.getIntExtra("android.intent.extra.INDEX", 0);
                if (count > 0) {
                    countBooks.setText("" + count);
                }
                setSearchHint(R.string.searching_please_wait_);
                isLibraryReady = false;
                updateEmptyLibraryState();
            } else if (BooksService.RESULT_BUILD_LIBRARY.equals(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                isLibraryReady = false;
                setSearchHint(R.string.extracting_information_from_books);
                updateEmptyLibraryState();
            } else if (BooksService.RESULT_SEARCH_MESSAGE_TXT.equals(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                setSearchHint(intent.getStringExtra("TEXT"));
            } else if (BooksService.RESULT_NOTIFY_ALL.equals(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                TempHolder.listHash++;
                EventBus.getDefault()
                        .post(new NotifyAllFragments());
            }
        }

    };
    Runnable sortAndSeach = new Runnable() {

        @Override public void run() {
            recyclerView.scrollToPosition(0);
            searchAndOrderAsync();
        }
    };

    @Override
    public boolean hasFloatingHeader() {
        return false;
    }
    private final TextWatcher filterTextWatcher = new TextWatcher() {

        @Override public void afterTextChanged(final Editable s) {
            AppState.get().searchQuery = s.toString();
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }

        @Override public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            if (//
                    AppState.get().libraryMode == AppState.MODE_GRID || //
                            AppState.get().libraryMode == AppState.MODE_LIST || //
                            AppState.get().libraryMode == AppState.MODE_LIST_COMPACT || //
                            AppState.get().libraryMode == AppState.MODE_COVERS//
            ) {
                handler.removeCallbacks(sortAndSeach);
                handler.removeCallbacks(hideKeyboard);
                if (s.toString()
                     .trim()
                     .length() == 0) {
                    handler.postDelayed(sortAndSeach, 250);
                    handler.postDelayed(hideKeyboard, 2000);
                } else {
                    handler.postDelayed(sortAndSeach, 1000);
                }
            }
            myAutoCompleteImage.setVisibility(View.GONE);
            handler.removeCallbacks(saveAutoComplete);

            if (StringDB.contains(AppState.get().myAutoCompleteDb, s.toString()
                                                                    .trim())) {
                myAutoCompleteImage.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(saveAutoComplete, 10000);
            }

        }

    };
    boolean isOnTop = false;
    private String NO_SERIES = ":no-series";
    private String NO_DATE = "---";
    private Stack<String> prevText = new Stack<String>();
    private ImageView onGridlList;
    ResultResponse<String> onAuthorSeriesClick = new ResultResponse<String>() {

        @Override public boolean onResultRecive(String result) {
            rememberPos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
            onMetaInfoClick(SEARCH_IN.getByMode(AppState.get().libraryMode), result);
            return false;
        }
    };
    ResultResponse<String> onAuthorClick = new ResultResponse<String>() {

        @Override public boolean onResultRecive(String result) {
            onMetaInfoClick(SEARCH_IN.AUTHOR, result);
            return false;
        }
    };
    ResultResponse<String> onSeriesClick = new ResultResponse<String>() {

        @Override public boolean onResultRecive(String result) {
            if (result.contains(NO_SERIES)) {
                onMetaInfoClick(SEARCH_IN.getByPrefix(searchEditText.getText()
                                                                    .toString()), result);
            } else {
                onMetaInfoClick(SEARCH_IN.SERIES, result);
            }
            return false;
        }
    };

    @Override public Pair<Integer, Integer> getNameAndIconRes() {
        return PAIR;
    }

    @Override public void onTintChanged() {
        styleFilterLineOnTint();

    }

    /**
     * The field's prompt is set from a dozen places - what is being searched, what is still
     * loading - and is drawn in capitals from every one of them, the way the shelf titles
     * above it are.
     */
    private void setSearchHint(int resId) {
        setSearchHint(getString(resId));
    }

    private void setSearchHint(CharSequence hint) {
        searchEditText.setHint(hint == null ? null : hint.toString().toUpperCase(Locale.getDefault()));
    }

    /** Keep library controls paired with the semantic background. */
    private void styleFilterLineOnTint() {
        ((View) menu2.getParent()).setBackgroundColor(com.foobnix.ui2.ThemeManager.color(requireContext(), R.attr.appBackground));
        int foreground = com.foobnix.ui2.ThemeManager.color(requireContext(), R.attr.appOnSurface);
        TintUtil.setTintImageNoAlpha(menu2, foreground);
        secondTopPanel.setBackgroundColor(Color.TRANSPARENT);
        searchEditText.setBackgroundColor(Color.TRANSPARENT);
        searchEditText.setPadding(Dips.DP_15,
                                  searchEditText.getPaddingTop(),
                                  searchEditText.getPaddingRight(),
                                  searchEditText.getPaddingBottom());
        TintUtil.setUITextColor(searchEditText, foreground);
        countBooks.setTextColor(foreground);
        TintUtil.setTintImageNoAlpha(cleanFilter, foreground);
        TintUtil.setTintImageNoAlpha(myAutoCompleteImage, foreground);
        TintUtil.setTintImageNoAlpha(sortBy, foreground);
        TintUtil.setTintImageNoAlpha(sortOrder, foreground);
        TintUtil.setTintImageNoAlpha(onGridlList, foreground);
    }

    public void onGridList() {
        onGridList(AppState.get().libraryMode, onGridlList, searchAdapter, authorsAdapter);
    }

    public void initAutocomplition() {
        autocomplitions.clear();
        for (SEARCH_IN search : AppDB.SEARCH_IN.values()) {
            if (search == SEARCH_IN.SERIES) {
                autocomplitions.add(search.getDotPrefix() + " *");
            } else {
                autocomplitions.add(search.getDotPrefix());
            }
        }

        autocomplitions.add(CMD_KEYCODE);
        autocomplitions.add(CMD_MARGIN);
        autocomplitions.add(CMD_EDIT_AUTO_COMPLETE);

        autocomplitions.addAll(StringDB.asList(AppState.get().myAutoCompleteDb));

        updateFilterListAdapter();
    }

    public void updateFilterListAdapter() {
        try {
            ArrayList<String> list = new ArrayList<String>(autocomplitions);
            Collections.sort(list);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
            searchEditText.setAdapter(adapter);
            searchEditText.setThreshold(1);
        } catch (Exception e) {
            LOG.e(e);
        }

    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search2, container, false);

//        String greet = (String) (Object) new Greeting().greet();
//        Toast.makeText(getActivity(), greet,Toast.LENGTH_LONG).show();

        LOG.d("Context-SF-getContext", getContext());
        LOG.d("Context-SF-getApplicationContext", getActivity().getApplicationContext());
        LOG.d("Context-SF-getBaseContext", getActivity().getBaseContext());
        LOG.d("Context-SF-LibreraApp.context", LibreraApp.context);

        LOG.d("SearchFragment2 onCreateView");

        NO_SERIES = " (" + getString(R.string.without_series) + ")";
        NO_DATE = getString(R.string.no_date);

        handler = new Handler(Looper.getMainLooper());

        secondTopPanel = view.findViewById(R.id.secondTopPanel);
        countBooks = (TextView) view.findViewById(R.id.countBooks);
        isLibraryReady = true;
        cleanFilter = (ImageView) view.findViewById(R.id.cleanFilter);
        sortBy = (ImageView) view.findViewById(R.id.sortBy);
        sortOrder = (ImageView) view.findViewById(R.id.sortOrder);
        menu2 = (ImageView) view.findViewById(R.id.menu2);
        myAutoCompleteImage = (ImageView) view.findViewById(R.id.myAutoCompleteImage);
        searchEditText = (AutoCompleteTextView) view.findViewById(R.id.filterLine);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        setButtonTooltip(view.findViewById(R.id.menu2));
        setButtonTooltip(view.findViewById(R.id.myAutoCompleteImage));
        setButtonTooltip(view.findViewById(R.id.cleanFilter));
        setButtonTooltip(view.findViewById(R.id.sortBy));
        setButtonTooltip(view.findViewById(R.id.sortOrder));

        bindOnGrantPanel(view.findViewById(R.id.layoutOnGrant));
        layoutOnGrant = view.findViewById(R.id.layoutOnGrant);
        emptyLibraryState = view.findViewById(R.id.emptyLibraryState);
        emptyLibraryAddFolder = view.findViewById(R.id.emptyLibraryAddFolder);
        emptyLibraryRescan = view.findViewById(R.id.emptyLibraryRescan);
        emptyLibraryAddFolder.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                MainTabs2.startAddLibraryStorage(getActivity());
            }
        });
        emptyLibraryRescan.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                rebuildLibrary();
            }
        });


        layoutError = view.findViewById(R.id.layoutError);
        layoutErrorOnRestart = view.findViewById(R.id.layoutErrorOnRestart);
        layoutError.setVisibility(View.GONE);

        layoutError.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                rebuildLibrary();
            }
        });

        if (AppState.get().appTheme == AppState.THEME_DARK_OLED || (AppState.get().appTheme == AppState.THEME_DARK && TintUtil.color == Color.BLACK)) {
            searchEditText.setBackgroundResource(R.drawable.bg_search_edit_night);
        }

        myAutoCompleteImage.setVisibility(View.GONE);

        searchEditText.addTextChangedListener(filterTextWatcher);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        EditTextHelper.enableKeyboardSearch(searchEditText, new Runnable() {

            @Override public void run() {
                Keyboards.close(searchEditText);
                Keyboards.hideNavigation(getActivity());
            }
        });

        searchAdapter = new FileMetaAdapter();

        libraryFilter = savedInstanceState == null ? 0 : savedInstanceState.getInt("libraryFilter", 0);
        libraryHeader = inflater.inflate(R.layout.redesign_library_header, new FrameLayout(requireContext()), false);
        continueSection = libraryHeader.findViewById(R.id.libraryContinueSection);
        searchAdapter.setLibraryStyle(true);
        searchAdapter.setLibraryHeader(libraryHeader);
        int[] filterIds = {R.id.libraryFilterAll, R.id.libraryFilterReading, R.id.libraryFilterFinished};
        for (int i = 0; i < filterIds.length; i++) {
            final int selected = i;
            libraryHeader.findViewById(filterIds[i]).setOnClickListener(v -> {
                libraryFilter = selected;
                recyclerView.scrollToPosition(0);
                searchAndOrderAsync();
            });
        }
        view.findViewById(R.id.librarySearchToggle).setOnClickListener(v -> {
            boolean opening = view.findViewById(R.id.librarySearchBox).getVisibility() != View.VISIBLE;
            view.findViewById(R.id.librarySearchBox).setVisibility(opening ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.libraryBrand).setVisibility(opening ? View.GONE : View.VISIBLE);
            if (opening) {
                searchEditText.requestFocus();
                ((android.view.inputmethod.InputMethodManager) requireContext().getSystemService(
                        android.content.Context.INPUT_METHOD_SERVICE)).showSoftInput(searchEditText, 0);
            } else {
                searchEditText.setText("");
                Keyboards.close(searchEditText);
            }
        });
        authorsAdapter = new AuthorsAdapter2();

        onGridlList = (ImageView) libraryHeader.findViewById(R.id.onGridList);
        setButtonTooltip(onGridlList);
        onGridlList.setOnClickListener(new OnClickListener() {

            @Override public void onClick(View v) {
                popupMenu(onGridlList);
            }
        });

        cleanFilter.setOnClickListener(new OnClickListener() {

            @Override public void onClick(View v) {
                searchEditText.setText("");
                recyclerView.scrollToPosition(0);
                searchAndOrderAsync();
            }
        });


        sortBy.setOnClickListener(new OnClickListener() {

            @Override public void onClick(View v) {
                sortByPopup(v);
            }
        });

        // The sort button says which way round the list runs; a long press turns it round.
        sortBy.setOnLongClickListener(new OnLongClickListener() {

            @Override public boolean onLongClick(View v) {
                AppState.get().isSortAsc = !AppState.get().isSortAsc;
                recyclerView.scrollToPosition(0);
                searchAndOrderAsync();
                return true;
            }
        });
        View shelfSort = libraryHeader.findViewById(R.id.libraryShelfSort);
        setButtonTooltip(shelfSort);
        shelfSort.setOnClickListener(v -> sortByPopup(v));
        shelfSort.setOnLongClickListener(v -> sortBy.performLongClick());
        sortOrder.setVisibility(View.GONE);

        bindAdapter(searchAdapter);

        searchAdapter.setOnAuthorClickListener(onAuthorClick);
        searchAdapter.setOnSeriesClickListener(onSeriesClick);

        authorsAdapter.setOnItemClickListener(onAuthorSeriesClick);

        if (AppState.get().isRestoreSearchQuery && !TxtUtils.isEmpty(AppState.get().searchQuery)) {
            searchEditText.setText(AppState.get().searchQuery);
            view.findViewById(R.id.librarySearchBox).setVisibility(View.VISIBLE);
            view.findViewById(R.id.libraryBrand).setVisibility(View.GONE);
        }

        onGridList();

        boolean errorExist = Prefs.get()
                                  .isErrorExist(AppProfile.getCurrent(), 0);

        layoutError.setVisibility(errorExist ? View.VISIBLE : View.GONE);
        if (BooksService.isRunning) {
            layoutError.setVisibility(View.GONE);
        }

        if (errorExist) {
            searchAndOrderAsync();
        } else {
            long count = AppProfile.bookCount;
            LOG.d("worker-starts", "cound-db", count);
            if (count == 0) {
                seachAll();
            } else {
                checkForDeleteBooks();
                searchAndOrderAsync();
            }
        }

        initAutocomplition();
        onTintChanged();
        updateEmptyLibraryState();

        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        myAutoCompleteImage.setOnClickListener(new OnClickListener() {

            @Override public void onClick(View v) {
                showAutoCompleteDialog();
            }
        });

        menu2.setOnClickListener(source -> {
            if (getActivity() instanceof MainTabs2) {
                ((MainTabs2) getActivity()).showSecondaryDestinations(source);
            }
        });

        LocalBroadcastManager.getInstance(getActivity())
                             .registerReceiver(broadcastReceiver, new IntentFilter(BooksService.INTENT_NAME));

        return view;

    }

    private void setButtonTooltip(View button) {
        CharSequence label = button.getContentDescription();
        if (label != null) {
            ViewCompat.setTooltipText(button, label);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) public void showResult(MessageSyncFinish event) {
        layoutError.setVisibility(Prefs.get().isErrorExist(AppProfile.getCurrent(), 0) ? View.VISIBLE : View.GONE);
        updateEmptyLibraryState();
    }

    private void updateEmptyLibraryState() {
        boolean libraryFilterIsEmpty = searchEditText.getText().toString().trim().isEmpty();
        boolean noVisibleBooks = "0".contentEquals(countBooks.getText());
        boolean showEmptyLibrary = libraryFilterIsEmpty && libraryFilter == 0
                && noVisibleBooks && libraryTotal == 0
                && !layoutError.isShown()
                && isLibraryReady;
        layoutOnGrant.setVisibility(View.GONE);
        emptyLibraryState.setVisibility(showEmptyLibrary ? View.VISIBLE : View.GONE);
    }


    public void showAutoCompleteDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.search_history_and_autocomplete);

        final ListView list = new ListView(getActivity());

        final List<String> items = new ArrayList<String>(StringDB.asList(AppState.get().myAutoCompleteDb));
        Collections.sort(items);
        BaseItemLayoutAdapter<String> adapter =
                new BaseItemLayoutAdapter<String>(getActivity(), R.layout.path_item, items) {
                    @Override public void populateView(View layout, int position, final String item) {
                        TextView text = layout.findViewById(R.id.browserPath);
                        ImageView delete = layout.findViewById(R.id.delete);

                        text.setText(item);

                        delete.setOnClickListener(new OnClickListener() {

                            @Override public void onClick(View v) {
                                autocomplitions.remove(item);
                                items.remove(item);

                                StringDB.delete(AppState.get().myAutoCompleteDb, item,
                                        (db) -> AppState.get().myAutoCompleteDb = db);
                                notifyDataSetChanged();
                            }
                        });
                        layout.setOnClickListener(new OnClickListener() {

                            @Override public void onClick(View v) {
                                searchEditText.setText(item);
                                searchAndOrderAsync();
                            }
                        });

                    }
                };

        list.setAdapter(adapter);

        builder.setView(list);

        builder.setPositiveButton(R.string.close, new AlertDialog.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {

            @Override public void onDismiss(DialogInterface dialog) {
                Keyboards.hideNavigation(getActivity());
            }
        });
        create.show();

        Keyboards.close(getActivity());

    }

    /**
     * The library also stands in a dialog over the reader, which is handed an empty bundle to
     * say so. There the notice is left out: the dialog is not where a clear install is set up.
     */
    @Override protected boolean isOnGrantPanelAllowed() {
        return getArguments() != Bundle.EMPTY;
    }
    /**
     * The library permission prompt is an empty-library call to action, not a banner above books.
     */
    @Override protected boolean isAskingPermission(boolean canWrite, boolean safFolders) {
        boolean libraryFilterIsEmpty = searchEditText.getText().toString().trim().isEmpty();
        return libraryFilterIsEmpty && "0".contentEquals(countBooks.getText())
                && super.isAskingPermission(canWrite, safFolders);
    }


    @Override public void onResume() {
        super.onResume();
        menu2.setVisibility(View.VISIBLE);
        if (searchAdapter != null) searchAndOrderAsync();
        updateEmptyLibraryState();
    }

    @Override public void onStop() {
        super.onStop();
    }

    @Subscribe
    public void onMetaClick(SearchMetaMsg msg){
        onMetaInfoClick(msg.mode, msg.text);
    }

    private void onMetaInfoClick(SEARCH_IN mode, String result) {
        if (mode == SEARCH_IN.SERIES && !result.startsWith(EMPTY_ID)) {
            result = StringDB.EXACTMATCHCHAR + result + StringDB.EXACTMATCHCHAR;
        }

        searchEditText.setText(mode.getDotPrefix() + " " + result);
        AppState.get().libraryMode = AppState.get().previousLibraryMode;
        onGridList();
        searchAndOrderAsync();
    }

    private void seachAll() {
        try {
            LOG.d("SeachFragment2", "seachAll");
            isLibraryReady = false;
            updateEmptyLibraryState();
            searchAdapter.clearItems();
            //BooksService.startForeground(getActivity(), BooksService.ACTION_SEARCH_ALL);

            SearchAllBooksWorker.run(getActivity());

        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public void checkForDeleteBooks() {
        try {
            if (!AppState.get().isScanOnLaunch) {
                LOG.d("checkForDeleteBooks", "skip");
                return;
            }
            LOG.d("checkForDeleteBooks", "run");
            //BooksService.startForeground(getActivity(), BooksService.ACTION_REMOVE_DELETED);
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CheckDeletedBooksWorker.class).build();
            WorkManager.getInstance(getContext())
                       .enqueueUniqueWork(SEARCH_FRAGMENT_WORKER_NAME, WORKER_POLICY, workRequest);
            //LOG.d("MessageWorker-Status checkForDeleteBooks");
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    @Override public void onTextRecive(String txt) {
        searchAndOrderExteral(txt);
    }

    public void searchAndOrderExteral(String text) {
        if (searchEditText != null) {
            searchEditText.setText(text);
            searchAndOrderAsync();
        }

    }

    public void searchAndOrderAsync() {
        LOG.d("SeachFragment2", "searchAndOrderAsync");

        if (Apps.isDestroyedActivity(getActivity())) {
            return;
        }
        setSearchHint(R.string.msg_loading);
        sortBy.setImageResource(PopupHelper.sortIcon(AppState.get().isSortAsc));

        String order = getString(AppState.get().isSortAsc ? R.string.ascending : R.string.descending);
        // The mark alone says what it opens, so the field being sorted on is left to it.
        sortBy.setContentDescription(getString(R.string.cd_sort_results) + " " + getString(
                AppDB.SORT_BY.getByID(AppState.get().sortBy).getResName()) + ", " + order);

        populate();

    }

    @Subscribe public void onShowTag(OpenTagMessage msg) {
        if (searchEditText != null) {
            searchEditText.setText("@tags " + msg.getTagName());
        }
        searchAndOrderAsync();

    }

    @Override public List<FileMeta> prepareDataInBackground() {
        LOG.d("SeachFragment2", "prepareDataInBackground");
        String txt = searchEditText.getText()
                                   .toString()
                                   .trim();
        countTitles = 0;
        if (Arrays.asList(AppState.MODE_GRID, AppState.MODE_COVERS, AppState.MODE_LIST, AppState.MODE_LIST_COMPACT)
                  .contains(AppState.get().libraryMode)) {

            if (!prevText.contains(txt)) {
                prevText.push(txt);
            }
            if (TxtUtils.isEmpty(txt)) {
                prevText.clear();
            }

            boolean isSearchOnlyEmpy = txt.contains(NO_SERIES);
            if (isSearchOnlyEmpy) {
                txt = txt.replace(NO_SERIES, "");
            }

            List<FileMeta> searchBy = AppDB.get()
                                           .searchBy(txt, SORT_BY.getByID(AppState.get().sortBy),
                                                   AppState.get().isSortAsc);

            List<FileMeta> allBooks = TxtUtils.isEmpty(txt) ? searchBy :
                    AppDB.get().searchBy("", SORT_BY.getByID(AppState.get().sortBy), AppState.get().isSortAsc);
            ExtUtils.removeNotFound(allBooks);
            libraryTotal = allBooks.size();
            libraryReading = 0;
            continueBook = null;
            for (FileMeta book : allBooks) {
                Float progress = book.getIsRecentProgress();
                if (progress != null && progress > 0 && progress < 1) libraryReading++;
                if (Boolean.TRUE.equals(book.getIsRecent()) && (progress == null || progress < 1)
                        && (continueBook == null || (book.getIsRecentTime() != null
                        && (continueBook.getIsRecentTime() == null || book.getIsRecentTime() > continueBook.getIsRecentTime())))) {
                    continueBook = book;
                }
            }
            if (libraryFilter != 2) ExtUtils.removeReadBooks(searchBy);
            ExtUtils.removeNotFound(searchBy);
            if (libraryFilter != 0) {
                Iterator<FileMeta> filtered = searchBy.iterator();
                while (filtered.hasNext()) {
                    Float progress = filtered.next().getIsRecentProgress();
                    if (libraryFilter == 1 ? progress == null || progress <= 0 || progress >= 1 :
                            progress == null || progress < 1) filtered.remove();
                }
            }

            List<String> result = new ArrayList<String>();
            boolean byGenre = txt.startsWith(SEARCH_IN.GENRE.getDotPrefix());
            boolean byAuthor = txt.startsWith(SEARCH_IN.AUTHOR.getDotPrefix());
            if (!txt.contains("::") && (byGenre || byAuthor)) {
                if (isSearchOnlyEmpy) {
                    Iterator<FileMeta> iterator = searchBy.iterator();
                    while (iterator.hasNext()) {
                        if (TxtUtils.isNotEmpty(iterator.next()
                                                        .getSequence())) {
                            iterator.remove();
                        }
                    }
                    return searchBy;
                }

                boolean hasEmpySeries = false;
                for (FileMeta it : searchBy) {
                    String sequence = it.getSequence();
                    TxtUtils.addFilteredGenreSeries(sequence, result, true);
                    if (!hasEmpySeries && TxtUtils.isEmpty(sequence)) {
                        hasEmpySeries = true;
                    }
                }

                Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
                Collections.reverse(result);

                String genreName = txt.replace(byGenre ? "@genre " : "@author ", "");
                for (String it : result) {
                    FileMeta fm = new FileMeta();
                    fm.setCusType(FileMetaAdapter.DISPALY_TYPE_SERIES);
                    fm.setSequence(it);
                    searchBy.add(0, fm);
                }
                if (hasEmpySeries && !result.isEmpty()) {
                    FileMeta fm = new FileMeta();
                    fm.setCusType(FileMetaAdapter.DISPALY_TYPE_SERIES);
                    fm.setSequence(genreName + NO_SERIES);
                    searchBy.add(result.size(), fm);
                }
            }

            if (//
                    AppState.get().sortBy == SORT_BY.PATH.getIndex() ||//
                            AppState.get().sortBy == SORT_BY.LANGUAGE.getIndex() ||//
                            AppState.get().sortBy == SORT_BY.PUBLICATION_YEAR.getIndex() || AppState.get().sortBy == SORT_BY.SERIES.getIndex() || AppState.get().sortBy == SORT_BY.PUBLISHER.getIndex()) {//

                List<FileMeta> res = new ArrayList<FileMeta>();
                String last = null;

                String extDir = Environment.getExternalStorageDirectory()
                                           .getPath();

                int count = 0;
                FileMeta fm = null;

                ///

                for (FileMeta it : searchBy) {
                    String parentName = "";
                    if (AppState.get().sortBy == SORT_BY.PUBLISHER.getIndex()) {
                        parentName = "" + it.getPublisher();
                    } else if (AppState.get().sortBy == SORT_BY.SERIES.getIndex()) {
                        parentName = "" + TxtUtils.nullToEmpty(it.getSequence());
                        parentName = parentName.replace(",", "");
                        if (parentName.isEmpty()) {
                            parentName = "---";
                        }
                    } else if (AppState.get().sortBy == SORT_BY.PUBLICATION_YEAR.getIndex()) {
                        parentName = it.getYear() == null ? NO_DATE : "" + it.getYear();
                    } else if (AppState.get().sortBy == SORT_BY.PATH.getIndex()) {
                        parentName = it.getParentPath();
                        if (parentName != null) {
                            parentName = parentName.replace(extDir, "");
                        }
                    } else if (AppState.get().sortBy == SORT_BY.LANGUAGE.getIndex()) {
                        String lang = it.getLang();
                        if (TxtUtils.isEmpty(lang)) {
                            parentName = "---";
                        } else {
                            parentName = DialogTranslateFromTo.getLanuageByCode(lang);
                        }
                    }
                    count++;
                    if (parentName != null && !parentName.equals(last)) {
                        if (fm != null) {
                            fm.setTitle(fm.getTitle() + " (" + count + ")");
                        }
                        fm = new FileMeta();
                        fm.setCusType(FileMetaAdapter.DISPALY_TYPE_LAYOUT_TITLE_DIVIDER);
                        fm.setTitle(parentName);
                        count = 0;
                        last = parentName;
                        res.add(fm);
                        countTitles++;
                    }
                    res.add(it);
                }
                if (fm != null) {
                    fm.setTitle(fm.getTitle() + " (" + (count + 1) + ")");
                }
                searchBy = res;
            }

            return searchBy;
        } else {
            return null;
        }
    }

    public void toastState(String command, boolean state) {
        Toast.makeText(getContext(), command + " [" + (state ? "ON" : "OFF") + "]", Toast.LENGTH_LONG)
             .show();
    }

    @Override public void populateDataInUI(List<FileMeta> items) {
        cacheItems = items;
        handler.removeCallbacks(sortAndSeach);

        String txt = searchEditText.getText()
                                   .toString()
                                   .trim();
        setSearchHint(R.string.library);

        //if(AppsConfig.IS_LOG){
        //setSearchHint(Apps.getApplicationName(getContext()));
        //}

        if (CMD_KEYCODE.equals(txt)) {
            new KeyCodeDialog(getActivity(), null);
            searchEditText.setText("");
        }
        if (CMD_MARGIN.equals(txt)) {
            SwipeRefreshLayout layout = getActivity().findViewById(R.id.swipeRefreshLayout);
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout.getLayoutParams();
            layoutParams.rightMargin = Dips.screenWidth() / 4;
            layout.setLayoutParams(layoutParams);
            searchEditText.setText("");
        }

        if (CMD_EDIT_AUTO_COMPLETE.equals(txt)) {
            searchEditText.setText("");
            showAutoCompleteDialog();
        }

        if (TxtUtils.isEmpty(txt)) {
            cleanFilter.setVisibility(View.GONE);
        } else {
            cleanFilter.setVisibility(View.VISIBLE);
        }

        if (Arrays.asList(AppState.MODE_GRID, AppState.MODE_COVERS, AppState.MODE_LIST, AppState.MODE_LIST_COMPACT)
                  .contains(AppState.get().libraryMode)) {
            AppState.get().previousLibraryMode = AppState.get().libraryMode;
            searchEditText.setEnabled(true);
            sortBy.setEnabled(true);
            sortBy.setVisibility(View.VISIBLE);

            searchAdapter.clearItems();
            FileMeta header = new FileMeta();
            header.setCusType(FileMetaAdapter.DISPLAY_TYPE_LIBRARY_HEADER);
            searchAdapter.getItemsList().add(header);
            bindLibraryHeader();
            libraryHeader.findViewById(R.id.libraryNoResults).setVisibility(
                    items.isEmpty() && (libraryFilter != 0 || !TxtUtils.isEmpty(txt)) ? View.VISIBLE : View.GONE);
            searchAdapter.getItemsList()
                         .addAll(items);
            searchAdapter.notifyDataSetChanged();
            handler.postDelayed(new Runnable() {

                @Override public void run() {
                    searchAdapter.notifyDataSetChanged();

                }
            }, 1000);

            // recyclerView.scrollToPosition(0);

        } else {
            prevLibModeAuthors = AppState.get().libraryMode;
            searchEditText.setEnabled(false);
            sortBy.setEnabled(false);
            sortBy.setVisibility(View.INVISIBLE);

            String empty = "";
            if (AppState.get().libraryMode == AppState.MODE_AUTHORS) {
                setSearchHint(R.string.author);
                empty = EMPTY_ID + getString(R.string.no_author);
            } else if (AppState.get().libraryMode == AppState.MODE_SERIES) {
                setSearchHint(R.string.serie);
                empty = EMPTY_ID + getString(R.string.no_serie);
            } else if (AppState.get().libraryMode == AppState.MODE_GENRE) {
                setSearchHint(R.string.genre);
                empty = EMPTY_ID + getString(R.string.no_genre);
            } else if (AppState.get().libraryMode == AppState.MODE_KEYWORDS) {
                setSearchHint(R.string.keywords);
                empty = EMPTY_ID + getString(R.string.no_keywords);
            } else if (AppState.get().libraryMode == AppState.MODE_USER_TAGS) {
                setSearchHint(R.string.my_tags);
                empty = EMPTY_ID + getActivity().getString(R.string.no_tag);
            } else if (AppState.get().libraryMode == AppState.MODE_LANGUAGES) {
                setSearchHint(R.string.language);
                empty = EMPTY_ID + getActivity().getString(R.string.no_language);
            } else if (AppState.get().libraryMode == AppState.MODE_PUBLICATION_DATE) {
                setSearchHint(R.string.publication_date);
                empty = EMPTY_ID + getActivity().getString(R.string.no_date);
            } else if (AppState.get().libraryMode == AppState.MODE_PUBLISHER) {
                setSearchHint(R.string.publisher);
                empty = EMPTY_ID + getActivity().getString(R.string.empy);
            }

            authorsAdapter.clearItems();
            List<String> list = AppDB.get()
                                     .getAll(SEARCH_IN.getByMode(AppState.get().libraryMode));
            if (AppState.get().libraryMode == AppState.MODE_LANGUAGES) {
                List<String> res = new ArrayList<String>();
                String prev = null;
                for (String ln : list) {
                    if (TxtUtils.isEmpty(ln)) {
                        ln = "";
                    } else if (ln.length() > 2) {
                        ln = ln.substring(0, 2);
                    }

                    String full = DialogTranslateFromTo.getLanuageByCode(ln);
                    String lnLow = ln.toLowerCase(Locale.US);

                    if (!lnLow.equals(prev)) {
                        prev = lnLow;
                        res.add(full + " (" + lnLow + ")");
                    }
                }
                list = res;

            }
            if (AppState.get().libraryMode == AppState.MODE_PUBLICATION_DATE) {
                Collections.reverse(list);
            }

            list.add(0, empty);
            authorsAdapter.getItemsList()
                          .addAll(list);
            authorsAdapter.notifyDataSetChanged();

            recyclerView.scrollToPosition(rememberPos);

        }

        updateOnGrantPanel();
        showBookCount();
        updateEmptyLibraryState();
    }

    private void sortByPopup(final View view) {

        MyPopupMenu popup = new MyPopupMenu(getActivity(), view);
        PopupHelper.addSortOrder(popup, AppState.get().isSortAsc, () -> {
            AppState.get().isSortAsc = !AppState.get().isSortAsc;
            recyclerView.scrollToPosition(0);
            searchAndOrderAsync();
        });
        for (final SORT_BY sortBy : SORT_BY.values()) {
            popup.getMenu()
                 .add(sortBy.getResName())
                 .setIconSmall(AppState.get().sortBy == sortBy.getIndex() ? TintUtil.SORT_CURRENT_ICON : 0)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {

                     @Override public boolean onMenuItemClick(MenuItem item) {
                         AppState.get().sortBy = sortBy.getIndex();
                         searchAndOrderAsync();
                         return false;
                     }
                 });
        }
        popup.show();
    }

    public void popupMenuTest() {
        popupMenu(onGridlList);
    }

    private void popupMenu(final ImageView onGridList) {
        MyPopupMenu p = new MyPopupMenu(getActivity(), onGridList);
        List<Integer> names = Arrays.asList(R.string.list, //
                R.string.compact, //
                R.string.grid, //
                R.string.cover, //
                R.string.author, //
                R.string.genre, //
                R.string.serie, //
                R.string.keywords, //
                R.string.language, //
                R.string.my_tags, R.string.publisher, R.string.publication_date//
                                           );

        final List<Integer> icons = Arrays.asList(R.drawable.my_glyphicons_114_paragraph_justify, //
                R.drawable.my_glyphicons_114_justify_compact, //
                R.drawable.glyphicons_157_thumbnails, //
                R.drawable.glyphicons_158_thumbnails_small, //
                R.drawable.glyphicons_4_user, //
                R.drawable.glyphicons_66_tag, //
                R.drawable.glyphicons_115_list, //
                R.drawable.glyphicons_67_tags, //
                R.drawable.glyphicons_417_globe, //
                R.drawable.glyphicons_67_tags, R.drawable.glyphicons_451_hand_like,
                R.drawable.glyphicons_589_book_open);
        final List<Integer> actions = Arrays.asList(AppState.MODE_LIST, AppState.MODE_LIST_COMPACT, //
                AppState.MODE_GRID, //
                AppState.MODE_COVERS, //
                AppState.MODE_AUTHORS, //
                AppState.MODE_GENRE, //
                AppState.MODE_SERIES, //
                AppState.MODE_KEYWORDS, //
                AppState.MODE_LANGUAGES, //
                AppState.MODE_USER_TAGS, AppState.MODE_PUBLISHER, AppState.MODE_PUBLICATION_DATE); //

        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            p.getMenu()
             .add(names.get(i))
             .setIcon(icons.get(i))
             .setOnMenuItemClickListener(new OnMenuItemClickListener() {

                 @Override public boolean onMenuItemClick(MenuItem item) {
                     AppState.get().libraryMode = actions.get(index);
                     onGridList.setImageResource(icons.get(index));
                     onGridList.setContentDescription(
                             getString(R.string.cd_view_menu) + " " + getString(names.get(index)));

                     if (Arrays.asList(AppState.MODE_PUBLICATION_DATE, AppState.MODE_PUBLISHER, AppState.MODE_AUTHORS,
                                       AppState.MODE_SERIES, AppState.MODE_GENRE, AppState.MODE_USER_TAGS, AppState.MODE_KEYWORDS,
                                       AppState.MODE_LANGUAGES)
                               .contains(AppState.get().libraryMode)) {
                         searchEditText.setText("");
                     }

                     onGridList();
                     searchAndOrderAsync();
                     return false;
                 }
             });
        }

        p.show();
        return;

    }

    @Override public void onDoubleClick() {
        if (isOnTop) {
            recyclerView.scrollToPosition(rememberPos);
        } else {
            rememberPos =
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            recyclerView.scrollToPosition(0);
        }
        isOnTop = !isOnTop;
    }

    public void showBookCount() {
        countBooks.setText("" + (recyclerView.getAdapter()
                                             .getItemCount() - countTitles - (recyclerView.getAdapter() == searchAdapter ? 1 : 0)));
    }

    @Override public boolean isBackPressed() {
        if (recyclerView == null) {
            return false;
        }
        if (recyclerView.getAdapter() instanceof FileMetaAdapter) {
            String searchText = searchEditText.getText()
                                              .toString();
            if (TxtUtils.isEmpty(searchText)) {
                return false;
            }

            if (searchText.startsWith("@")) {
                try {
                    prevText.pop();

                    String pop = prevText.pop();
                    LOG.d("pop", pop);
                    if (TxtUtils.isNotEmpty(pop)) {
                        searchEditText.setText(pop);
                        searchAndOrderAsync();
                        return true;
                    }

                } catch (EmptyStackException e) {
                    LOG.e(e);
                }
            }

            searchEditText.setText("");
            if (prevLibModeAuthors == NONE) {
                prevLibModeAuthors = AppState.get().previousLibraryMode;
            }
            AppState.get().libraryMode = prevLibModeAuthors;
            onGridList();
            searchAndOrderAsync();
            return true;

        } else {
            AppState.get().libraryMode = AppState.get().previousLibraryMode;
            onGridList();
            searchAndOrderAsync();
            return true;
        }
    }

    /**
     * Rebuilds the library from the folders listed in the preferences panel, without asking
     * for them again. The library screen has no button of its own for this any more - the
     * panel's Update does it - but the error line still offers it when a scan left nothing
     * to show.
     */
    private void rebuildLibrary() {
        if (!isLibraryReady) {
            Toast.makeText(getActivity(), R.string.extracting_information_from_books, Toast.LENGTH_LONG)
                 .show();
            return;
        }
        Prefs.get()
             .remove(AppProfile.getCurrent(), 0);
        layoutError.setVisibility(View.GONE);
        recyclerView.scrollToPosition(0);
        seachAll();
    }

    @Override public void notifyFragment() {
        LOG.d("SeachFragment2", "notifyFragment");
        if (searchAdapter != null) {
            searchAdapter.notifyDataSetChanged();
        }
        if (!BooksService.isRunning) {
            isLibraryReady = true;
            setSearchHint(R.string.library);
            updateEmptyLibraryState();
//            if(AppsConfig.IS_LOG){
//                setSearchHint(Apps.getApplicationName(getContext()));
//            }

        }
    }

    @Override public void resetFragment() {
        LOG.d("SeachFragment2", "resetFragment");
        if (handler != null) {
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    onGridList();
                    searchAndOrderAsync();
                }
            }, 150);
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        LOG.d("SearchFragment2 onDestroy");
        LocalBroadcastManager.getInstance(getActivity())
                             .unregisterReceiver(broadcastReceiver);
        cacheItems = null;

    }
}
