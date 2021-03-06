package com.softdesign.devintensive.utils;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import com.softdesign.devintensive.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс проверки введенных пользователем данных
 */
public class CheckInputInformation implements TextWatcher {

    private final String TAG = "CheckInputInformation";

    //Регулярные выражения для сравнения.
    private final String phoneNumberPattern = "^(\\+7|8)[\\s(]*\\d{3}[\\s)]*\\d{3}\\s*-\\s*\\d{2}\\s*-\\s*\\d{2}$";
    private final String mailAdressPattern = "^[\\w.-]{3,}\\@[\\w-]{2,}\\.[a-z]{2,3}$";
    private final String vkAdressPattern = "^vk\\.com/[\\w-]{3,}$";
    private final String gitAdressPattern = "^github\\.com/[\\w-]{3,}$";

    //Переменные для работы с UI
    private Context mContext;
    private TextInputLayout mTextInputLayout;
    private EditText mUserInfo;
    private int mResId;
    private String mError;
    private ImageView mToActionBtn;

    //Переменные для сравнения регулярных выражений.
    private Pattern mPattern;
    private Matcher mMatcher;

    public CheckInputInformation(Context context, EditText userInfo, ImageView button, TextInputLayout textInputLayout) {
        this.mContext = context;
        this.mTextInputLayout = textInputLayout;
        this.mUserInfo = userInfo;
        this.mResId = mUserInfo.getId();
        this.mToActionBtn = button;
    }

    /**
     * Проверка введенных данных<br>
     * При несовпадении значений выключается кнопка открытия. Кнопка открытия окрашивается в красный цвет.
     * Иначе все ограничения снимаются. Кнопка окрашивается в зеленый цвет.
     * @param pattern регулярное выражение
     * @param value введенные пользовательские данные
     */
    private void check(String pattern, String value) {
        mPattern = Pattern.compile(pattern);
        mMatcher = mPattern.matcher(value);

        if (!mMatcher.matches()) {
            mToActionBtn.setEnabled(false);
            mToActionBtn.setColorFilter(mContext.getResources().getColor(R.color.background_wrong_info));
            mTextInputLayout.setErrorEnabled(true);
            mTextInputLayout.setError(mError); //Вывод ошибки о неправильном вводе

        } else {
            mToActionBtn.setEnabled(true);
            mToActionBtn.setColorFilter(mContext.getResources().getColor(R.color.background_accept_info));
            mTextInputLayout.setErrorEnabled(false);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //Отсекаем лишние значения.
        if (charSequence.toString().toLowerCase().contains("https://")) {
            charSequence = charSequence.toString().replaceAll("https://", "");
            mUserInfo.setText(charSequence);
        }
        if (charSequence.toString().toLowerCase().contains("http://")) {
            charSequence = charSequence.toString().replaceAll("http://", "");
            mUserInfo.setText(charSequence);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        switch (mResId) {
            case R.id.user_phone:
                mError = mContext.getString(R.string.error_user_phone_message);
                check(phoneNumberPattern, editable.toString());
                break;
            case R.id.user_mail:
                mError = mContext.getString(R.string.error_user_mail_message);
                check(mailAdressPattern, editable.toString().toLowerCase());
                break;
            case R.id.user_vk:
                mError = mContext.getString(R.string.error_user_vk_message);
                check(vkAdressPattern, editable.toString().toLowerCase());
                break;
            case R.id.user_github:
                mError = mContext.getString(R.string.error_user_github_message);
                check(gitAdressPattern, editable.toString().toLowerCase());
                break;
        }
    }
}
