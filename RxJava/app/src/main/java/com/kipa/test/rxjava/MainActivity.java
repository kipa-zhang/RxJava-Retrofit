package com.kipa.test.rxjava;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.SimpleTimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    ImageView img = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = (ImageView)findViewById(R.id.img);
    }

    @Override
    protected void onResume(){
        super.onResume();

//      this.version1();
//      this.version2();
//        this.version3();
        test();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 仅测试 RxJava 的一些 操作符
     */
    private void test(){
//        String[] names = {"a","b","c"};
//        Observable.from(names).subscribe(new Action1<String>() {
//            @Override
//            public void call(String s) {
//                System.out.println(s);
//            }
//        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/")
                //增加返回值为String的支持
                .addConverterFactory(ScalarsConverterFactory.create())
                //增加返回值为Gson的支持(以实体类返回)
                .addConverterFactory(GsonConverterFactory.create())
                //增加返回值为Oservable<T>的支持
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        GetInfo service = retrofit.create(GetInfo.class);

//        Call<String> call = service.getUser();
//        call.enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                System.out.println(response.body());
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//
//            }
//        });
        service.getUser().subscribeOn(Schedulers.newThread())//在新线程中实现该方法
                .map(new Func1<ResponseBody, Bitmap>() {

                    @Override
                    public Bitmap call(ResponseBody arg0) {
                        if(saveFileToDisc(arg0)) {//保存图片成功
                            Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(null) + File.separator + "baidu.png");
                            return bitmap;//返回一个bitmap对象
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//在Android主线程中展示
                .subscribe(new Subscriber<Bitmap>() {

                    ProgressDialog dialog = new ProgressDialog(MainActivity.this);


                    @Override
                    public void onStart() {
                        dialog.show();
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        Log.d(TAG, "onError ===== " + arg0.toString());
                    }

                    @Override
                    public void onNext(Bitmap arg0) {
                        img.setImageBitmap(arg0);

                    }
                });
    }

    /**
     * 原理版 被订阅者（Observable） 订阅（subscribe） 订阅者（Subscriber）
     * 参考文献：http://blog.csdn.net/lzyzsd/article/details/41833541/
     */
    private void version1(){
        System.out.println("activity(Thread):"+Thread.currentThread().getName());

        Observable<String> myObservable = Observable.create(
                new Observable.OnSubscribe<String>(){
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        System.out.println("myObservable(Thread):"+Thread.currentThread().getName());
                        subscriber.onNext("Hello,world! (Version 1)");
                        subscriber.onCompleted();
                    }
                }
        );

        Subscriber<String> mySubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                System.out.println("mySubscriber(Thread):"+Thread.currentThread().getName());
                System.out.println("info from Observable:"+s);
            }
        };

        myObservable.subscribe(mySubscriber);
    }

    /**
     * 简化版
     */
    private void version2(){

        /**
         * 简化 Observable
         */
        Observable<String> myObservable = Observable.just("Hello,world! (Version 2)");

        /**
         * 简化 Subscriber
         *  Action1 只有一个 接口
         */
        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        };

        /**
         *  subscribe方法有一个重载版本，接受三个Action1类型的参数，分别对应OnNext，OnComplete， OnError函数。
         */
//        myObservable.subscribe(onNextAction, onErrorAction, onCompleteAction);
        myObservable.subscribe(onNextAction);

    }

    /**
     * map 使用，更改 Observable（被订阅者）对象数据
     */
    private void version3(){

        /**
         * 简化 Observable
         */
        Observable<String> myObservable = Observable.just("Hello,world! (Version 3)");

        /**
         * map()操作符就是用于变换Observable对象的
         * 返回类型 依旧为 String
         */
        myObservable = myObservable.map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s + " - For Test";
            }
        });

        /**
         *  更改了 Observable 返回的对象类型为 Integer
         */
        Observable<Integer> myNewObservable = myObservable.map(new Func1<String, Integer>() {
            @Override
            public Integer call(String s) {
                return s.hashCode();
            }
        });

        /**
         * 简化 Subscriber
         *  Action1 只有一个 接口
         */
        Action1<Integer> onNextAction = new Action1<Integer>() {
            @Override
            public void call(Integer s) {
                System.out.println(s);
            }
        };

        /**
         *  subscribe方法有一个重载版本，接受三个Action1类型的参数，分别对应OnNext，OnComplete， OnError函数。
         */
//        myObservable.subscribe(onNextAction, onErrorAction, onCompleteAction);
        myNewObservable.subscribe(onNextAction);

    }

    /**
     * 以上代码 在 JAVA8 情况下 可使用 lambda 表达式
     * 强烈建议 使用 JAVA8 ，使用太方便了
     */
    private void version4() {

//        Observable.just("Hello,world! (Version 4)")
//                .map(s -> s + "For Test")
//                .subscribe(s -> System.out.println(s));
    }

    public interface GetInfo{
        @GET("u=2927736268,3993783027&fm=96")
        Observable<ResponseBody> getUser();
    }

    private boolean saveFileToDisc(ResponseBody rb){
        String filepath = getExternalFilesDir(null) + File.separator + "baidu.png";

        File file = new File(filepath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(rb.bytes());

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }   catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}


