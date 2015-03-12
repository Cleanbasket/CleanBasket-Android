package com.washappkorea.corp.cleanbasket.util;

import android.content.Context;
import android.util.Log;

import com.washappkorea.corp.cleanbasket.R;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @brief 날짜와 시간 생성 클래스
 * @details 서버에서 받아온 스트링 형식의 날짜, 시간 값을 가공해 String으로 리턴하며 싱글톤으로 구현되어 있습니다.
 */
public class DateTimeFactory {
    private static DateTimeFactory mInstance;

    public static synchronized DateTimeFactory getInstance() {
        if(mInstance == null) {
            mInstance = new DateTimeFactory();
        }

        return mInstance;
    }

    public DateTimeFactory() {

    }

    /**
     * @brief 날짜와 시간 생성 함수
     * @details 서버에서 받아온 스트링 형식의 날짜, 시간 값을 가공해 String으로 리턴합니다.
     */
    public String getPrettyTime(String datetime) {
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;

        try {
            date = transFormat.parse(datetime);
        } catch (ParseException e) {
            Log.d("Error", e.getMessage().toString());
        } catch (NullPointerException e) {
            Log.d("Error", e.getMessage().toString());
        }

        PrettyTime t = new PrettyTime(Locale.KOREA);

        return t.format(date);
    }

    /**
     * @brief 날짜와 시간 생성 함수
     * @details 서버에서 받아온 스트링 형식의 날짜, 시간 값을 가공해 String으로 리턴합니다.
     */
    public String getDate(Context context, String datetime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;

        try {
            date = simpleDateFormat.parse(datetime);
        } catch (ParseException e) {
            Log.d("Error", e.getMessage().toString());
        } catch (NullPointerException e) {
            Log.d("Error", e.getMessage().toString());
        }

        SimpleDateFormat transFormat = new SimpleDateFormat(context.getString(R.string.datetime_parse));

        return transFormat.format(date);
    }

    /**
     * @brief 날짜와 시간 생성 함수
     * @details 서버에서 받아온 스트링 형식의 날짜, 시간 값을 가공해 String으로 리턴합니다.
     */
    public String getTime(Context context, String datetime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;

        try {
            date = simpleDateFormat.parse(datetime);
        } catch (ParseException e) {
            Log.d("Error", e.getMessage().toString());
        } catch (NullPointerException e) {
            Log.d("Error", e.getMessage().toString());
        }

        SimpleDateFormat transFormat = new SimpleDateFormat(context.getString(R.string.time_parse));

        return transFormat.format(date);
    }

    /**
     * @brief 날짜와 시간 생성 함수
     * @details 서버에서 받아온 스트링 형식의 날짜, 시간 값을 가공해 String으로 리턴합니다.
     */
    public String getPrettyTime(Date date) {
        PrettyTime t = new PrettyTime(Locale.KOREA);

        return t.format(date);
    }

    public String getStringDate(Context context, Date date) {
        SimpleDateFormat transFormat = new SimpleDateFormat(context.getString(R.string.date_parse));

        return transFormat.format(date);
    }

    public String getStringDateTime(Date date) {
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        return transFormat.format(date);
    }

    public String getStringTime(Context context, Date date) {
        SimpleDateFormat transFormat = new SimpleDateFormat(context.getString(R.string.time_parse));

        return transFormat.format(date);
    }

    public String getNewLine() {
        return System.getProperty("line.separator");
    }
}
