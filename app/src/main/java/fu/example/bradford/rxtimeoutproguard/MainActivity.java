package fu.example.bradford.rxtimeoutproguard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final BehaviorSubject<Event> mEvents = BehaviorSubject.create();
    private Subscription mSubscriber;
    private TextView mTextView;

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

        findViewById(R.id.timer_activity_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TimerActivity.class);
                startActivity(intent);
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
                // this emits a Timeout exception as expected (I forked the internal source), however our
                //  subscriber's onNext is not invoked with this event when proguarded.
                .timeout(null, new Func1<Event, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Event eventname) {
                        return timer(eventname);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                               @Override
                               public void call(Event event) {
                                    Log.i(TAG, "onNext: "+event);
                                    mTextView.setText(event.toString());
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {
                                    Log.e(TAG, "onError", throwable);
                                    mTextView.setText("I assume we timed out!! "+throwable.getClass().getSimpleName());
                               }
                           });
    }

    public static Observable<Long> timer(Event event) {
        // if the event is CLICKED (i.e. ui button has been pressed), never timeout
        if (event == Event.CLICKED) {
            Log.v(TAG, "Returning never observable");
            return Observable.never();
        }

        // otherwise emit a cached timer observable
        Log.v(TAG, "Returning cached timer observable");
        return Observable.timer(2, TimeUnit.SECONDS).cache();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscriber.unsubscribe();
    }

    private enum Event {
        INITIAL,
        CLICKED
    }
}
