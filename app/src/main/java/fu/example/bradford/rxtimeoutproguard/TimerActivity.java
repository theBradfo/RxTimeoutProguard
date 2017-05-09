package fu.example.bradford.rxtimeoutproguard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TimerActivity extends AppCompatActivity {
    private static final String TAG = TimerActivity.class.getSimpleName();

    private Subscription mSubscriber;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_timer);

        mTextView = (TextView) findViewById(R.id.hello_world);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTextView.setText("Nothing yet....");
        Log.i(TAG, "creating subscriber for timer");
        mSubscriber =
                Observable.timer(2, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError", e);
                                mTextView.setText("Something bad happened: "+e.getMessage());
                            }

                            @Override
                            public void onNext(Long event) {
                                Log.i(TAG, "We got a timer event!");
                                mTextView.setText("We received a timer event");
                            }
                        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscriber.unsubscribe();
    }
}
