package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UploadFile;
import com.softdesign.devintensive.utils.CheckInputInformation;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static String TAG = ConstantManager.PREFIX_TAG + " MainActivity: ";

    //Инициализация слоев для бокового меню и user content
    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;

    //User EditText layouts
    @BindViews({R.id.user_information_phone_layout,
            R.id.user_mail_layout,
            R.id.user_vk_layout,
            R.id.user_github_layout}) List<TextInputLayout> mUserInfoLayouts;

    //User EditTexts
    @BindViews({R.id.user_phone,
            R.id.user_mail,
            R.id.user_vk,
            R.id.user_github,
            R.id.user_about}) List<EditText> mUserInfo;

    //Инициализация ярлыков для взаимодействия с user information
    @BindViews({R.id.to_call_btn,
            R.id.to_mail_btn,
            R.id.to_vk_btn,
            R.id.to_github_btn}) List<ImageView> mUserAction;

    //Инициализация TextView для работы с контейнером
    @BindViews({R.id.user_rating,
            R.id.user_code_lines,
            R.id.user_projects}) List<TextView> mUserValues;

    private List<CheckInputInformation> watchers;


    //Боковое меню с аватаром и текстовыми полями.
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    ImageView mUserAvatar;
    TextView mUserFio, mUserEmail;

    //ToolBar
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.user_photo_img)
    ImageView userPhotoImg;
    @BindView(R.id.new_user_avatar)
    ImageView userPhotoNew;
    @BindView(R.id.user_photo_change)
    RelativeLayout userPhotoChange;


    private boolean mCurrentEditMode; //Проверка включения режима редактирования
    private DataManager mDataManager; //Синглентон для работы с sharedPref.
    private AppBarLayout.LayoutParams mLayoutParams = null; //Параметры тулбара.

    private File mPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        for (int i = 0; i < mUserAction.size(); i++) {
            mUserAction.get(i).setOnClickListener(this);
        }
        watchers = new ArrayList<>();

        setupToolbar();
        setupNavigation();
        initUserFields();
        initUserStatistic();

        mFab.setOnClickListener(this);
        userPhotoNew.setOnClickListener(this);

        if (savedInstanceState != null) {
            //Проверяем режим редактирования данных
            fabChangeMode(savedInstanceState.getBoolean(ConstantManager.EDIT_MODE_KEY));
        }
    }

    /**
     * Сохранения данных при перевороте экрана.
     *
     * @param outState - переменная, содержащая статус редактирования.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    /**
     * Открытие активности для получения фото из камеры или галереи.
     *
     * @param intent
     * @param requestCode
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Получение фото от новой активности фото или галереи
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    Uri selectedImage = Uri.fromFile(mPhotoFile);

                    loadPhotoToServer(selectedImage);
                    insertPhotoToProfile(selectedImage);
                }
            case ConstantManager.REQUEST_GALLARY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {

                    String[] proj = { MediaStore.Images.Media.DATA };
                    Cursor cursor = this.getContentResolver().query(data.getData(),  proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    cursor.close();

                    loadPhotoToServer(Uri.parse(path));
                    insertPhotoToProfile(data.getData());
                }
                break;
        }
    }

    /**
     * Обработка нажатия кнопок открытия новых intent
     *
     * @param view - элемент UI
     */
    @Override
    public void onClick(View view) {
        Intent intent = null;
        Uri data;
        String url;

        switch (view.getId()) {
            case R.id.to_call_btn:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    intent = new Intent(Intent.ACTION_DIAL);
                    data = Uri.parse("tel:" + mUserInfo.get(0).getText().toString());
                    intent.setData(data);
                } else {
                    showSnackBar(getResources().getString(R.string.intent_need_permission));
                }
                break;
            case R.id.to_mail_btn:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String[] adress = {mUserInfo.get(1).getText().toString()};
                intent.putExtra(Intent.EXTRA_EMAIL, adress);
                break;
            case R.id.to_vk_btn:
                url = mUserInfo.get(2).getText().toString();
                data = Uri.parse("http://" + url);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(data);
                break;
            case R.id.to_github_btn:
                url = mUserInfo.get(3).getText().toString();
                data = Uri.parse("http://" + url);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(data);
                break;
            case R.id.fab:
                if (!mCurrentEditMode) {
                    fabChangeMode(true);
                } else {
                    fabChangeMode(false);
                    saveUserFields();
                }
                break;
            case R.id.new_user_avatar:
                showDialog(ConstantManager.LOAD_PROFILE_DATA);
                break;
        }

        if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showSnackBar(getResources().getString(R.string.intent_error_open_message));
        }
    }

    /**
     * Проверяет режим редактирования по нажатию кнопки fab
     *
     * @param mode - true - режим включен, false выключен
     */
    private void fabChangeMode(boolean mode) {
        for (EditText userValue : mUserInfo) {
            mCurrentEditMode = mode;
            userValue.setEnabled(mode);
            userValue.setFocusable(mode);
            userValue.setFocusableInTouchMode(mode);
        }
        if (mode) {
            mFab.setImageResource(R.drawable.fab_accept_changed);
            mUserInfo.get(0).requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, 0);

            userPhotoImg.setVisibility(View.GONE);
            userPhotoChange.setVisibility(View.VISIBLE);
            enableUserInfo();
            lockAppBarLayout();
            mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        } else {
            mFab.setImageResource(R.drawable.fab_change_mode);

            userPhotoImg.setVisibility(View.VISIBLE);
            userPhotoChange.setVisibility(View.GONE);
            disableUserInfo();
            unlockAppBarLayout();
            mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.background_white));
        }

    }


    private void initUserFields() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();

        for (int i = 0; i < mUserInfo.size(); i++) {
            mUserInfo.get(i).setText(userData.get(i));
        }

        //Вставляем аватар в header
        Picasso.with(this).invalidate(mDataManager.getPreferencesManager().
                loadUserPhoto());

        Picasso.with(this).
                load(mDataManager.getPreferencesManager().
                        loadUserPhoto()).
                placeholder(R.drawable.nav_header_bg).
                into(userPhotoImg);
    }
    private void saveUserFields() {
        List<String> userData = new ArrayList<>();

        for (EditText userInfo : mUserInfo) {
            userData.add(userInfo.getText().toString());
        }

        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

    private void initUserStatistic(){
        List<String> userData = mDataManager.getPreferencesManager().loadUserStatistic();

        for (int i = 0; i < userData.size(); i++) {
            mUserValues.get(i).setText(userData.get(i));
        }
    }

    /**
     * Открывает выдвижное меню с помощью кнопки в тулбаре
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            mNavigationDrawer.openDrawer(GravityCompat.START);

        return super.onOptionsItemSelected(item);
    }

    /**
     * Инициализирует view элементы пользовательского UI
     */
    private void enableUserInfo() {
        for (int i = 0; i < mUserAction.size(); i++) {
            watchers.add(new CheckInputInformation(getBaseContext(),
                    mUserInfo.get(i),
                    mUserAction.get(i),
                    mUserInfoLayouts.get(i)));

            mUserInfo.get(i).addTextChangedListener(watchers.get(i));
        }
    }
    private void disableUserInfo(){
        if (!watchers.isEmpty()){
            for (int i = 0; i < mUserAction.size(); i++) {
                mUserInfo.get(i).removeTextChangedListener(watchers.get(i));
            }
        }
    }
    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        mLayoutParams = (AppBarLayout.LayoutParams) mCollapsingToolbarLayout.getLayoutParams();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.menu_open);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mDataManager.getPreferencesManager().loadFIO());
        }
    }
    private void setupNavigation() {
        //Боковое меню
        View drawerHeader = mNavigationView.inflateHeaderView(R.layout.drawer_header);
        mUserAvatar = (ImageView) drawerHeader.findViewById(R.id.menu_header_avatar);
        //Вставляем аватар в выдвижное меню
        Picasso.with(this).
                load(mDataManager.getPreferencesManager().
                        loadUserAvatar()).
                placeholder(R.drawable.empty_avatar).
                into(mUserAvatar);

        mUserFio = (TextView) drawerHeader.findViewById(R.id.menu_header_user_name);
        mUserFio.setText(mDataManager.getPreferencesManager().loadFIO());

        mUserEmail = (TextView) drawerHeader.findViewById(R.id.menu_header_user_mail);
        mUserEmail.setText(mDataManager.getPreferencesManager().loadLoginEmail());

        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent intent = null;

                switch (item.getItemId()) {
                    case R.id.menu_user_profile:
                        intent = new Intent(getBaseContext(), MainActivity.class);
                        break;
                    case R.id.menu_contacts:
                        intent = new Intent(getBaseContext(), UserListActivity.class);
                        break;
                    case R.id.menu_exit:
                        intent = new Intent(getBaseContext(), LogInActivity.class);
                        mDataManager.getPreferencesManager().saveAuthToken("");
                        break;
                    default:
                        showSnackBar(item.getTitle().toString());
                        item.setChecked(true);
                        break;
                }

                if (intent != null){
                    startActivity(intent);
                }

                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    //************************************************************
    //Код для работы с загрузкой фотографии из галереи или камеры
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_DATA:
                String selectItem[] = {getString(R.string.avatar_load_from_gallary),
                        getString(R.string.avatar_load_from_camera),
                        getString(R.string.avatar_cancel)};

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(getString(R.string.avatar_load_info));
                builder.setItems(selectItem, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                loadPhotoFromGallary();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialogInterface.cancel();
                                break;
                        }
                    }
                });
                return builder.create();
            default:
                return null;
        }

    }

    private File createImageFile() throws IOException {
        String timeStemp = new SimpleDateFormat("yyyymmdd").format(new Date());
        String ImageFileName = "IMG_" + timeStemp;

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(ImageFileName, ".jpg", storageDir);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    private void loadPhotoFromGallary() {
        Intent takeGallaryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeGallaryIntent.setType("image/*");

        startActivityForResult(Intent.createChooser(takeGallaryIntent, getString(R.string.avatar_select)), ConstantManager.REQUEST_GALLARY_PICTURE);
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            mPhotoFile = null;
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mPhotoFile != null) {
                Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_REQUEST_PERMISSION_CODE);
        }
    }

    private void loadPhotoToServer(Uri imageUri){
        Call<ResponseBody> call = mDataManager.uploadPhoto(new UploadFile().photo(imageUri));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                showSnackBar(getString(R.string.avatar_synchronised));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showSnackBar(getString(R.string.error_synchro_photo));
            }
        });

    }

    private void insertPhotoToProfile(Uri selectedImage) {
        //Вставляем фото в header
        Picasso.with(this).
                load(selectedImage).
                placeholder(R.drawable.nav_header_bg).
                into(userPhotoImg);

        mDataManager.getPreferencesManager().saveUserPhoto(selectedImage);
    }

    private void lockAppBarLayout() {
        mAppBarLayout.setExpanded(true, true);
        mLayoutParams.setScrollFlags(0);
        mCollapsingToolbarLayout.setLayoutParams(mLayoutParams);

    }

    private void unlockAppBarLayout() {
        mLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbarLayout.setLayoutParams(mLayoutParams);
    }
    //************************************************************

    /**
     * Обработка permission для получения фото с камеры.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean result = false;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            result = true;

        if (result) {
            loadPhotoFromCamera();
        } else {
            Snackbar.make(mCoordinatorLayout, getString(R.string.intent_need_permission), Snackbar.LENGTH_LONG).
                    setAction("Настройки", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openApplicationSetting();
                        }
                    }).show();
        }
    }

    /**
     * Открытие настроек permissions в случае отсутвия прав.
     */
    public void openApplicationSetting() {
        Intent appSettingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));

        startActivityForResult(appSettingIntent, ConstantManager.PERMISSION_REQUEST_KEY);
    }

    /**
     * Закрытие выдвижного меню по нажатию кнопки "Назад"
     */
    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isDrawerOpen(GravityCompat.START))
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

}
