package fu.example.bradford.rxtimeoutproguard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private enum Event {
        INITIAL,
        CLICKED
    }

    private Subscription mSubscriber;
    private TextView mTextView;

    private BehaviorSubject<Event> mEvents = BehaviorSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.hello_world);
        Button button = (Button) findViewById(R.id.timeout_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Timeout button clicked");
                mEvents.onNext(Event.CLICKED);
            }
        });
        mEvents.onNext(Event.INITIAL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTextView.setText("Nothing yet....");
        Log.i(TAG, "creating subscriber for timer");
        mSubscriber =
                mEvents
                .timeout(null, new Func1<Event, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Event eventname) {
                        return timer(eventname);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError", e);
                        mTextView.setText("I assume we timed out!! "+e.getClass().getSimpleName());
                    }

                    @Override
                    public void onNext(Event event) {
                        Log.i(TAG, "onNext: "+event);
                        mTextView.setText(event.toString());
                    }
                });
    }

    public static Observable<Long> timer(Event event) {
        if (event == Event.CLICKED) {
            Log.v(TAG, "Returning never observable");
            return Observable.never();
        }
        Log.v(TAG, "Returning cached timer observable");
        return Observable.timer(2, TimeUnit.SECONDS).cache();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscriber.unsubscribe();
    }
}
